package com.demo3.model.redblack;

import com.demo3.model.binary.AbstractBinaryTree;
import com.demo3.model.binary.BinaryNode;
import com.demo3.model.core.INode;
import com.demo3.model.core.ITreeOperations;

import java.util.Collections;
import java.util.List;

/**
 * Cây Đỏ Đen (Red-Black Tree) — tự cân bằng sau mỗi insert/delete.
 *
 * OOP nổi bật:
 * - Override find() và minimum() với hành vi BST thay vì BFS → behavioral polymorphism.
 * - leftRotate/rightRotate/transplant đặt private ở đây, không lên AbstractBinaryTree.
 * - Implements ITreeOperations → service layer gọi qua interface.
 */
public class RBTree<T extends Comparable<T>> extends AbstractBinaryTree<T>
        implements ITreeOperations<T> {

    // ========================================================================
    // TIỆN ÍCH MÀU SẮC — null node luôn là màu Đen
    // ========================================================================

    private boolean isNodeRed(BinaryNode<T> node) {
        if (node == null) return false;
        return ((RBNode<T>) node).isRed();
    }

    private void setNodeColor(BinaryNode<T> node, boolean red) {
        if (node != null) ((RBNode<T>) node).setRed(red);
    }

    // ========================================================================
    // OVERRIDE — behavioral polymorphism so với AbstractBinaryTree
    // ========================================================================

    /** Override: tận dụng BST property → O(log n) thay vì BFS O(n). */
    @Override
    public BinaryNode<T> find(T value) {
        BinaryNode<T> cur = getRoot();
        while (cur != null) {
            int cmp = value.compareTo(cur.getValue());
            if (cmp == 0) return cur;
            cur = (cmp < 0) ? cur.getLeft() : cur.getRight();
        }
        return null;
    }

    /** Override: rẽ trái liên tục → O(log n) thay vì BFS O(n). */
    @Override
    public BinaryNode<T> minimum(BinaryNode<T> start) {
        if (start == null) return null;
        BinaryNode<T> cur = start;
        while (cur.getLeft() != null) cur = cur.getLeft();
        return cur;
    }

    // ========================================================================
    // ROTATION & TRANSPLANT — private, chỉ RBTree dùng
    // ========================================================================

    private void leftRotate(BinaryNode<T> x) {
        BinaryNode<T> y = x.getRight();
        x.setRight(y.getLeft());
        if (y.getLeft() != null) y.getLeft().setParent(x);
        y.setParent(x.getParent());
        if (x.getParent() == null)              setRoot(y);
        else if (x == x.getParent().getLeft())  x.getParent().setLeft(y);
        else                                    x.getParent().setRight(y);
        y.setLeft(x);
        x.setParent(y);
    }

    private void rightRotate(BinaryNode<T> y) {
        BinaryNode<T> x = y.getLeft();
        y.setLeft(x.getRight());
        if (x.getRight() != null) x.getRight().setParent(y);
        x.setParent(y.getParent());
        if (y.getParent() == null)               setRoot(x);
        else if (y == y.getParent().getRight())  y.getParent().setRight(x);
        else                                     y.getParent().setLeft(x);
        x.setRight(y);
        y.setParent(x);
    }

    private void transplant(BinaryNode<T> u, BinaryNode<T> v) {
        if (u.getParent() == null)              setRoot(v);
        else if (u == u.getParent().getLeft())  u.getParent().setLeft(v);
        else                                    u.getParent().setRight(v);
        if (v != null) v.setParent(u.getParent());
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
    // INSERT + FIXUP
    // ========================================================================

    @Override
    public void insert(T parentValue, T newValue) {
        // RBTree tự tìm vị trí theo BST property, bỏ qua parentValue
        insertValue(newValue);
    }

    private void insertValue(T value) {
        recordStep("Bắt đầu thêm node " + value + " (BST insert)", 1, List.of(value));

        RBNode<T> z = new RBNode<>(value);
        BinaryNode<T> parent = null;
        BinaryNode<T> cur = getRoot();

        while (cur != null) {
            parent = cur;
            int cmp = value.compareTo(cur.getValue());
            recordStep("So sánh " + value + " với " + cur.getValue(), 2,
                    List.of(value, cur.getValue()));
            if (cmp < 0) {
                cur = cur.getLeft();
                recordStep(value + " < " + parent.getValue() + " → đi sang trái", 3,
                        List.of(parent.getValue()));
            } else if (cmp > 0) {
                cur = cur.getRight();
                recordStep(value + " > " + parent.getValue() + " → đi sang phải", 3,
                        List.of(parent.getValue()));
            } else {
                recordStep("Giá trị " + value + " đã tồn tại — hủy thao tác", 4,
                        List.of(value));
                throw new IllegalArgumentException(
                    "Giá trị " + value + " đã tồn tại trong cây.");
            }
        }

        z.setParent(parent);
        if (parent == null) {
            setRoot(z);
            recordStepWithSnapshot(value + " trở thành root", 5, List.of(value), deepCopy());
        } else if (value.compareTo(parent.getValue()) < 0) {
            parent.setLeft(z);
            recordStep("Gắn " + value + " vào bên TRÁI của " + parent.getValue(), 5,
                    List.of(parent.getValue(), value));
        } else {
            parent.setRight(z);
            recordStep("Gắn " + value + " vào bên PHẢI của " + parent.getValue(), 5,
                    List.of(parent.getValue(), value));
        }

        recordStep("Bắt đầu insertFixup để cân bằng cây", 6, List.of(value));
        insertFixup(z);
        recordStepWithSnapshot("insertFixup hoàn tất — cây đã cân bằng", 7, List.of(value), deepCopy());
    }

    private void insertFixup(BinaryNode<T> k) {
        while (k.getParent() != null && isNodeRed(k.getParent())) {
            if (k.getParent() == k.getParent().getParent().getLeft()) {
                BinaryNode<T> uncle = k.getParent().getParent().getRight();
                if (isNodeRed(uncle)) {
                    // Case 1: Uncle đỏ → đổi màu
                    recordStep("Case 1: Uncle đỏ → đổi màu parent, uncle, grandparent", 8,
                            List.of(k.getValue()));
                    setNodeColor(k.getParent(), false);
                    setNodeColor(uncle, false);
                    setNodeColor(k.getParent().getParent(), true);
                    k = k.getParent().getParent();
                } else {
                    if (k == k.getParent().getRight()) {
                        // Case 2: k là con phải → left rotate
                        recordStep("Case 2: k là con phải → leftRotate(parent)", 9,
                                List.of(k.getValue()));
                        k = k.getParent();
                        leftRotate(k);
                    }
                    // Case 3: k là con trái → right rotate
                    recordStep("Case 3: đổi màu + rightRotate(grandparent)", 10,
                            List.of(k.getValue()));
                    setNodeColor(k.getParent(), false);
                    setNodeColor(k.getParent().getParent(), true);
                    rightRotate(k.getParent().getParent());
                }
            } else {
                // Mirror: parent là con phải của grandparent
                BinaryNode<T> uncle = k.getParent().getParent().getLeft();
                if (isNodeRed(uncle)) {
                    recordStep("Case 1 (mirror): Uncle đỏ → đổi màu", 8,
                            List.of(k.getValue()));
                    setNodeColor(k.getParent(), false);
                    setNodeColor(uncle, false);
                    setNodeColor(k.getParent().getParent(), true);
                    k = k.getParent().getParent();
                } else {
                    if (k == k.getParent().getLeft()) {
                        recordStep("Case 2 (mirror): k là con trái → rightRotate(parent)", 9,
                                List.of(k.getValue()));
                        k = k.getParent();
                        rightRotate(k);
                    }
                    recordStep("Case 3 (mirror): đổi màu + leftRotate(grandparent)", 10,
                            List.of(k.getValue()));
                    setNodeColor(k.getParent(), false);
                    setNodeColor(k.getParent().getParent(), true);
                    leftRotate(k.getParent().getParent());
                }
            }
        }
        setNodeColor(getRoot(), false);
    }

    // ========================================================================
    // DELETE + FIXUP
    // ========================================================================

    @Override
    public void delete(T value) {
        recordStep("Tìm node cần xóa: " + value, 1, List.of(value));
        BinaryNode<T> z = find(value);
        if (z == null) {
            recordStep("Không tìm thấy node " + value, 2, Collections.emptyList());
            return;
        }
        recordStep("Tìm thấy node " + value, 2, List.of(value));

        BinaryNode<T> x;
        BinaryNode<T> y = z;
        boolean yOriginalRed = isNodeRed(y);

        if (z.getLeft() == null) {
            recordStep("Node không có con trái → thay bằng con phải", 3, List.of(value));
            x = z.getRight();
            transplant(z, z.getRight());
        } else if (z.getRight() == null) {
            recordStep("Node không có con phải → thay bằng con trái", 3, List.of(value));
            x = z.getLeft();
            transplant(z, z.getLeft());
        } else {
            y = minimum(z.getRight());
            recordStep("Node có 2 con → tìm successor: " + y.getValue(), 3,
                    List.of(y.getValue()));
            yOriginalRed = isNodeRed(y);
            x = y.getRight();
            if (y.getParent() == z) {
                if (x != null) x.setParent(y);
            } else {
                transplant(y, y.getRight());
                y.setRight(z.getRight());
                y.getRight().setParent(y);
            }
            transplant(z, y);
            y.setLeft(z.getLeft());
            y.getLeft().setParent(y);
            setNodeColor(y, isNodeRed(z));
            recordStep("Thay thế " + value + " bằng successor " + y.getValue(), 4,
                    List.of(y.getValue()));
        }

        if (!yOriginalRed) {
            recordStep("Bắt đầu deleteFixup để cân bằng cây", 5, Collections.emptyList());
            deleteFixup(x);
            recordStepWithSnapshot("deleteFixup hoàn tất", 6, Collections.emptyList(), deepCopy());
        }
    }

    private void deleteFixup(BinaryNode<T> x) {
        if (x == null) return; // Guard: tránh NPE khi node bị xóa không có con

        while (x != getRoot() && !isNodeRed(x)) {
            if (x == x.getParent().getLeft()) {
                BinaryNode<T> s = x.getParent().getRight();
                if (isNodeRed(s)) {
                    setNodeColor(s, false);
                    setNodeColor(x.getParent(), true);
                    leftRotate(x.getParent());
                    s = x.getParent().getRight();
                }
                if (!isNodeRed(s.getLeft()) && !isNodeRed(s.getRight())) {
                    setNodeColor(s, true);
                    x = x.getParent();
                } else {
                    if (!isNodeRed(s.getRight())) {
                        setNodeColor(s.getLeft(), false);
                        setNodeColor(s, true);
                        rightRotate(s);
                        s = x.getParent().getRight();
                    }
                    setNodeColor(s, isNodeRed(x.getParent()));
                    setNodeColor(x.getParent(), false);
                    setNodeColor(s.getRight(), false);
                    leftRotate(x.getParent());
                    x = getRoot();
                }
            } else {
                BinaryNode<T> s = x.getParent().getLeft();
                if (isNodeRed(s)) {
                    setNodeColor(s, false);
                    setNodeColor(x.getParent(), true);
                    rightRotate(x.getParent());
                    s = x.getParent().getLeft();
                }
                if (!isNodeRed(s.getRight()) && !isNodeRed(s.getLeft())) {
                    setNodeColor(s, true);
                    x = x.getParent();
                } else {
                    if (!isNodeRed(s.getLeft())) {
                        setNodeColor(s.getRight(), false);
                        setNodeColor(s, true);
                        leftRotate(s);
                        s = x.getParent().getLeft();
                    }
                    setNodeColor(s, isNodeRed(x.getParent()));
                    setNodeColor(x.getParent(), false);
                    setNodeColor(s.getLeft(), false);
                    rightRotate(x.getParent());
                    x = getRoot();
                }
            }
        }
        setNodeColor(x, false);
    }

    // ========================================================================
    // UPDATE — delete + insert lại để giữ BST property
    // ========================================================================

    @Override
    public void update(T oldValue, T newValue) {
        recordStep("Update: xóa " + oldValue + " rồi insert " + newValue, 1,
                List.of(oldValue, newValue));

        // Kiểm tra giá trị mới đã tồn tại chưa (trừ trường hợp oldValue == newValue)
        if (oldValue.compareTo(newValue) != 0 && find(newValue) != null) {
            recordStep("Giá trị mới " + newValue + " đã tồn tại — hủy", 2, List.of(newValue));
            throw new IllegalArgumentException("Giá trị " + newValue + " đã tồn tại trong cây.");
        }

        delete(oldValue);
        insertValue(newValue);
        recordStepWithSnapshot("Update hoàn tất: " + oldValue + " → " + newValue, 3, List.of(newValue), deepCopy());
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
        recordStep("Bắt đầu tìm kiếm " + value + " (BST search)", 1, List.of(value));
        BinaryNode<T> cur = getRoot();
        while (cur != null) {
            int cmp = value.compareTo(cur.getValue());
            recordStep("So sánh " + value + " với " + cur.getValue(), 2,
                    List.of(cur.getValue()));
            if (cmp == 0) {
                recordStep("Tìm thấy node " + value, 3, List.of(value));
                return cur;
            }
            cur = (cmp < 0) ? cur.getLeft() : cur.getRight();
        }
        recordStep("Không tìm thấy node " + value, 3, Collections.emptyList());
        return null;
    }

    // ========================================================================
    // DEEP COPY — copy cả isRed, bắt buộc có để tạo snapshot
    // ========================================================================

    @Override
    public RBTree<T> deepCopy() {
        RBTree<T> copy = new RBTree<>();
        copy.root = copyNode(getRoot(), null);
        return copy;
    }

    private BinaryNode<T> copyNode(BinaryNode<T> node, BinaryNode<T> parentCopy) {
        if (node == null) return null;
        RBNode<T> copy = new RBNode<>(node.getValue());
        copy.setRed(((RBNode<T>) node).isRed());
        copy.setParent(parentCopy);
        copy.setLeft(copyNode(node.getLeft(), copy));
        copy.setRight(copyNode(node.getRight(), copy));
        return copy;
    }
}