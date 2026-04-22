package com.demo3.service;

import com.demo3.model.TreeKind;
import com.demo3.model.binary.BinaryTree;
import com.demo3.model.core.AnimationStep;
import com.demo3.model.core.INode;
import com.demo3.model.core.ITreeOperations;
import com.demo3.model.generic.GenericTree;
import com.demo3.model.redblack.RBTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service layer — trung gian giữa Controller và Model.
 *
 * Thiết kế tận dụng ITreeOperations polymorphism:
 * mọi thao tác chỉ cần gọi qua interface, KHÔNG switch-case theo loại cây.
 *
 * So với OOP_20252 (~200 dòng switch-case), class này chỉ ~80 dòng.
 */
public class TreeService {

    private ITreeOperations<Integer> currentTree;
    private TreeKind currentKind;

    // Undo/Redo history — lưu deepCopy snapshot sau mỗi thao tác thay đổi cấu trúc
    private final List<ITreeOperations<Integer>> history = new ArrayList<>();
    private int historyIndex = -1;

    // ========================================================================
    // FACTORY — điểm DUY NHẤT biết loại cây cụ thể
    // ========================================================================

    /**
     * Tạo cây mới theo loại đã chọn và khởi tạo lịch sử.
     */
    public void initTree(TreeKind kind) {
        this.currentKind = kind;
        this.currentTree = createTreeInstance(kind);
        history.clear();
        historyIndex = -1;
        saveToHistory();
    }

    private ITreeOperations<Integer> createTreeInstance(TreeKind kind) {
        return switch (kind) {
            case GENERIC_TREE   -> new GenericTree<>();
            case BINARY_TREE    -> new BinaryTree<>();
            case RED_BLACK_TREE -> new RBTree<>();
        };
    }

    // ========================================================================
    // 6 THAO TÁC — polymorphism: 1 dòng gọi cho MỌI loại cây
    // ========================================================================

    public List<AnimationStep<Integer>> performCreate() {
        currentTree.startRecording();
        currentTree.create();
        saveToHistory();
        return currentTree.stopAndGetSteps();
    }

    public List<AnimationStep<Integer>> performInsert(Integer parentValue, int newValue) {
        currentTree.startRecording();
        currentTree.insert(parentValue, newValue);
        saveToHistory();
        return currentTree.stopAndGetSteps();
    }

    public List<AnimationStep<Integer>> performDelete(int value) {
        currentTree.startRecording();
        currentTree.delete(value);
        saveToHistory();
        return currentTree.stopAndGetSteps();
    }

    public List<AnimationStep<Integer>> performUpdate(int oldValue, int newValue) {
        currentTree.startRecording();
        currentTree.update(oldValue, newValue);
        saveToHistory();
        return currentTree.stopAndGetSteps();
    }

    public List<AnimationStep<Integer>> performTraverse(boolean useBFS) {
        currentTree.startRecording();
        currentTree.traverse(useBFS);
        return currentTree.stopAndGetSteps();
        // Note: traverse không thay đổi cấu trúc → không saveToHistory
    }

    public List<AnimationStep<Integer>> performSearch(int value) {
        currentTree.startRecording();
        currentTree.searchNode(value);
        return currentTree.stopAndGetSteps();
        // Note: search không thay đổi cấu trúc → không saveToHistory
    }

    // ========================================================================
    // UNDO / REDO
    // ========================================================================

    public boolean canUndo() {
        return historyIndex > 0;
    }

    public boolean canRedo() {
        return historyIndex < history.size() - 1;
    }

    public void undo() {
        if (!canUndo()) return;
        historyIndex--;
        currentTree = history.get(historyIndex).deepCopy();
    }

    public void redo() {
        if (!canRedo()) return;
        historyIndex++;
        currentTree = history.get(historyIndex).deepCopy();
    }

    // ========================================================================
    // GETTERS
    // ========================================================================

    public ITreeOperations<Integer> getCurrentTree() {
        return currentTree;
    }

    public TreeKind getCurrentKind() {
        return currentKind;
    }

    public INode<Integer> getRoot() {
        return currentTree.getRoot();
    }

    // ========================================================================
    // HISTORY HELPER
    // ========================================================================

    private void saveToHistory() {
        // Discard any redo states after current position
        while (history.size() - 1 > historyIndex) {
            history.remove(history.size() - 1);
        }
        history.add(currentTree.deepCopy());
        historyIndex = history.size() - 1;
    }
}
