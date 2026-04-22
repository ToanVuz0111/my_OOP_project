package com.demo3.model.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class AbstractTree<T extends Comparable<T>> implements ITree<T> {
    protected INode<T> root;

    // Bộ ghi bước animation — mọi subclass đều dùng chung
    protected final StepRecorder<T> recorder = new StepRecorder<>();

    @Override
    public INode<T> getRoot() {
        return root;
    }
    
    @Override
    public void clear() {
        root = null;
    }
    
    @Override
    public boolean isEmpty() {
        return root == null;
    }

    // ========================================================================
    // ANIMATION RECORDING — delegate sang StepRecorder
    // ========================================================================

    public void startRecording() {
        recorder.startRecording();
    }

    public List<AnimationStep<T>> stopAndGetSteps() {
        return recorder.stopAndGetSteps();
    }

    /**
     * Ghi bước NHẸ — không snapshot (dùng cho so sánh, highlight, mô tả).
     */
    protected void recordStep(String description, int pseudocodeLine, List<T> highlightedValues) {
        recorder.recordStep(description, pseudocodeLine, highlightedValues);
    }

    /**
     * Ghi bước CÓ SNAPSHOT — dùng khi cây thay đổi cấu trúc.
     * Subclass truyền this.deepCopy() vào để UI có thể render lại cây tại bước này.
     */
    protected void recordStepWithSnapshot(String description, int pseudocodeLine,
                                          List<T> highlightedValues, ITreeOperations<T> snapshot) {
        recorder.recordStep(description, pseudocodeLine, highlightedValues, snapshot);
    }

    // ========================================================================
    // GOM CHUNG THUẬT TOÁN DUYỆT CÂY (NGUYÊN TẮC DRY - DON'T REPEAT YOURSELF)
    // Nhờ tính Đa hình (Polymorphism), thuật toán này chạy đúng cho MỌI loại cây!
    // ========================================================================

    public List<INode<T>> traverseBFS() {
        List<INode<T>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<INode<T>> queue = new LinkedList<>();
        queue.add(getRoot());

        while (!queue.isEmpty()) {
            INode<T> current = queue.poll();
            result.add(current);

            // Bất kể là BinaryTree hay GenericTree, cứ gọi getChildren() là lấy được con
            List<? extends INode<T>> children = current.getChildren();
            if (children != null) {
                queue.addAll(children);
            }
        }
        return result;
    }

    public List<INode<T>> traverseDFS() {
        List<INode<T>> result = new ArrayList<>();
        dfsHelper(getRoot(), result);
        return result;
    }

    private void dfsHelper(INode<T> node, List<INode<T>> result) {
        if (node == null) return;

        result.add(node); // Thăm node hiện tại
        
        // Thăm đệ quy các node con
        List<? extends INode<T>> children = node.getChildren();
        if (children != null) {
            for (INode<T> child : children) {
                dfsHelper(child, result);
            }
        }
    }
}