package com.demo3.model.generic;

import com.demo3.model.core.AbstractTree;
import com.demo3.model.core.INode;
import com.demo3.model.core.ITreeOperations;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Cây Tổng Quát (Generic Tree).
 * Mỗi node có thể có số lượng con không giới hạn.
 *
 * Implements ITreeOperations → service layer gọi qua interface,
 * không cần biết đây là loại cây gì.
 */
public class GenericTree<T extends Comparable<T>> extends AbstractTree<T>
        implements ITreeOperations<T> {

    @Override
    public GenericNode<T> getRoot() {
        return (GenericNode<T>) root;
    }

    // ========================================================================
    // CREATE
    // ========================================================================

    @Override
    public void create() {
        recordStep("Xóa toàn bộ cây, tạo cây rỗng mới", 1, Collections.emptyList());
        clear();
        recordStepWithSnapshot("Cây đã được tạo mới (rỗng)", 2, Collections.emptyList(), deepCopy());
    }

    // ========================================================================
    // FIND nội bộ — BFS duyệt toàn cây
    // ========================================================================

    public GenericNode<T> find(T value) {
        if (root == null) return null;
        Queue<GenericNode<T>> queue = new LinkedList<>();
        queue.add(getRoot());
        while (!queue.isEmpty()) {
            GenericNode<T> cur = queue.poll();
            if (cur.getValue().compareTo(value) == 0) return cur;
            queue.addAll(cur.getChildrenList());
        }
        return null;
    }

    // ========================================================================
    // INSERT — Từ chối giá trị trùng lặp (thống nhất với RBTree)
    // ========================================================================

    @Override
    public void insert(T parentValue, T newValue) {
        recordStep("Bắt đầu thêm node " + newValue, 1, List.of(newValue));

        // Kiểm tra trùng lặp
        if (find(newValue) != null) {
            recordStep("Giá trị " + newValue + " đã tồn tại — hủy thao tác", 2, List.of(newValue));
            throw new IllegalArgumentException("Giá trị " + newValue + " đã tồn tại trong cây.");
        }

        GenericNode<T> newNode = new GenericNode<>(newValue);

        // Cây rỗng → node mới thành root
        if (root == null) {
            root = newNode;
            recordStepWithSnapshot("Cây rỗng → " + newValue + " trở thành root", 3, List.of(newValue), deepCopy());
            return;
        }

        recordStep("Tìm node cha có giá trị " + parentValue, 4, List.of(parentValue));
        GenericNode<T> parentNode = find(parentValue);
        if (parentNode == null) {
            recordStep("Không tìm thấy node cha " + parentValue, 5, Collections.emptyList());
            throw new IllegalArgumentException(
                "Không tìm thấy node cha có giá trị: " + parentValue);
        }
        recordStep("Tìm thấy node cha " + parentValue, 5, List.of(parentValue));

        parentNode.addChild(newNode);
        recordStepWithSnapshot("Gắn " + newValue + " làm con của " + parentValue, 6,
                List.of(parentValue, newValue), deepCopy());
    }

    // ========================================================================
    // DELETE
    // ========================================================================

    @Override
    public void delete(T value) {
        recordStep("Tìm node cần xóa: " + value, 1, List.of(value));
        GenericNode<T> node = find(value);
        if (node == null) {
            recordStep("Không tìm thấy node " + value, 2, Collections.emptyList());
            return;
        }

        recordStep("Tìm thấy node " + value, 2, List.of(value));
        if (node == root) {
            clear(); // Xóa root → bay cả cây
            recordStepWithSnapshot("Xóa root → xóa toàn bộ cây", 3, Collections.emptyList(), deepCopy());
        } else {
            node.getParent().removeChild(node);
            recordStepWithSnapshot("Xóa node " + value + " khỏi cây", 3, Collections.emptyList(), deepCopy());
        }
    }

    // ========================================================================
    // UPDATE — đổi trực tiếp, không cần delete+insert (không có thứ tự sắp xếp)
    // ========================================================================

    @Override
    public void update(T oldValue, T newValue) {
        recordStep("Tìm node có giá trị " + oldValue, 1, List.of(oldValue));

        // Kiểm tra trùng lặp
        if (find(newValue) != null) {
            recordStep("Giá trị mới " + newValue + " đã tồn tại — hủy", 2, List.of(newValue));
            throw new IllegalArgumentException("Giá trị " + newValue + " đã tồn tại trong cây.");
        }

        GenericNode<T> node = find(oldValue);
        if (node == null) {
            recordStep("Không tìm thấy node " + oldValue, 2, Collections.emptyList());
            throw new IllegalArgumentException(
                "Không tìm thấy node có giá trị: " + oldValue);
        }
        recordStep("Tìm thấy node " + oldValue, 2, List.of(oldValue));

        node.setValue(newValue);
        recordStepWithSnapshot("Đã cập nhật " + oldValue + " → " + newValue, 3, List.of(newValue), deepCopy());
    }

    // ========================================================================
    // TRAVERSE — dùng traverseBFS / traverseDFS từ AbstractTree
    // ========================================================================

    @Override
    public List<INode<T>> traverse(boolean useBFS) {
        recordStep("Bắt đầu duyệt " + (useBFS ? "BFS" : "DFS"), 1, Collections.emptyList());
        List<INode<T>> result = useBFS ? traverseBFS() : traverseDFS();
        for (int i = 0; i < result.size(); i++) {
            recordStep("Thăm node: " + result.get(i).getValue(), 2,
                    List.of(result.get(i).getValue()));
        }
        recordStep("Hoàn tất duyệt cây — " + result.size() + " node", 3, Collections.emptyList());
        return result;
    }

    // ========================================================================
    // SEARCH — trả về node để service layer highlight
    // ========================================================================

    @Override
    public INode<T> searchNode(T value) {
        recordStep("Bắt đầu tìm kiếm " + value, 1, List.of(value));
        GenericNode<T> result = find(value);
        if (result != null) {
            recordStep("Tìm thấy node " + value, 2, List.of(value));
        } else {
            recordStep("Không tìm thấy node " + value, 2, Collections.emptyList());
        }
        return result;
    }

    // ========================================================================
    // DEEP COPY — service layer tạo snapshot tại mỗi bước animation
    // ========================================================================

    @Override
    public GenericTree<T> deepCopy() {
        GenericTree<T> copy = new GenericTree<>();
        copy.root = copyNode(getRoot(), null);
        return copy;
    }

    private GenericNode<T> copyNode(GenericNode<T> node, GenericNode<T> parentCopy) {
        if (node == null) return null;
        GenericNode<T> copy = new GenericNode<>(node.getValue());
        copy.setParent(parentCopy);
        for (GenericNode<T> child : node.getChildrenList()) {
            copy.getChildrenList().add(copyNode(child, copy));
        }
        return copy;
    }
}