package com.demo3.model.redblack;

import com.demo3.model.binary.AbstractBinaryTree;
import com.demo3.model.binary.BinaryNode;

/**
 * Cài đặt Cây Đỏ Đen (Red-Black Tree) - Một dạng tự cân bằng của Cây Nhị Phân Tìm Kiếm.
 */
public class RBTree<T extends Comparable<T>> extends AbstractBinaryTree<T> {

    // --- CÁC HÀM TIỆN ÍCH ĐỂ XỬ LÝ MÀU SẮC ---
    // Trong RB Tree, Node null (NIL) luôn được coi là màu Đen (false).
    private boolean isRed(BinaryNode<T> node) {
        if (node == null) return false;
        return ((RBNode<T>) node).isRed;
    }

    private void setRed(BinaryNode<T> node, boolean isRed) {
        if (node != null) {
            ((RBNode<T>) node).isRed = isRed;
        }
    }

    // ========================================================================
    // ĐỈNH CAO OOP: GHI ĐÈ (OVERRIDE) CÁC HÀM CỦA LỚP CHA ĐỂ TỐI ƯU TỐC ĐỘ
    // ========================================================================

    @Override
    public BinaryNode<T> find(T value) {
        // Tận dụng tính chất BST: Nhỏ rẽ trái, Lớn rẽ phải
        BinaryNode<T> current = getRoot();
        while (current != null) {
            int cmp = value.compareTo(current.getValue());
            if (cmp == 0) return current; // Tìm thấy
            if (cmp < 0) current = current.left;
            else current = current.right;
        }
        return null; // Không tìm thấy
    }

    @Override
    public BinaryNode<T> minimum(BinaryNode<T> start) {
        // Tận dụng tính chất BST: Nút nhỏ nhất nằm ở tận cùng bên trái
        BinaryNode<T> current = start;
        while (current != null && current.left != null) {
            current = current.left;
        }
        return current;
    }

    public boolean search(T value) {
        return find(value) != null;
    }

    // ========================================================================
    // THUẬT TOÁN THÊM NODE (INSERT) VÀ TỰ CÂN BẰNG
    // ========================================================================

    public void insert(T value) {
        RBNode<T> newNode = new RBNode<>(value); // Mặc định node mới là màu Đỏ
        
        BinaryNode<T> parent = null;
        BinaryNode<T> current = getRoot();

        // 1. Tìm vị trí đúng chuẩn BST (Nhỏ trái, Lớn phải)
        while (current != null) {
            parent = current;
            if (newNode.getValue().compareTo(current.getValue()) < 0) {
                current = current.left;
            } else if (newNode.getValue().compareTo(current.getValue()) > 0) {
                current = current.right;
            } else {
                return; // Nếu giá trị đã tồn tại thì không làm gì cả
            }
        }

        newNode.parent = parent;

        // 2. Nối node mới vào cây
        if (parent == null) {
            setRoot(newNode);
        } else if (newNode.getValue().compareTo(parent.getValue()) < 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        // 3. Sửa lỗi màu sắc (Fixup) để cây đạt chuẩn Đỏ - Đen
        insertFixup(newNode);
    }

    private void insertFixup(BinaryNode<T> k) {
        BinaryNode<T> u;
        while (k.parent != null && isRed(k.parent)) {
            if (k.parent == k.parent.parent.left) {
                u = k.parent.parent.right; // Chú u (Uncle)
                if (isRed(u)) {
                    // TH1: Chú màu đỏ -> Đổi màu cha, chú và ông nội
                    setRed(k.parent, false);
                    setRed(u, false);
                    setRed(k.parent.parent, true);
                    k = k.parent.parent;
                } else {
                    if (k == k.parent.right) {
                        // TH2: Chú màu đen, k là con phải -> Xoay trái ở cha
                        k = k.parent;
                        leftRotate(k);
                    }
                    // TH3: Chú màu đen, k là con trái -> Đổi màu và xoay phải ở ông nội
                    setRed(k.parent, false);
                    setRed(k.parent.parent, true);
                    rightRotate(k.parent.parent);
                }
            } else {
                // Tương tự nhưng đối xứng sang nhánh phải
                u = k.parent.parent.left;
                if (isRed(u)) {
                    setRed(k.parent, false);
                    setRed(u, false);
                    setRed(k.parent.parent, true);
                    k = k.parent.parent;
                } else {
                    if (k == k.parent.left) {
                        k = k.parent;
                        rightRotate(k);
                    }
                    setRed(k.parent, false);
                    setRed(k.parent.parent, true);
                    leftRotate(k.parent.parent);
                }
            }
        }
        setRed(getRoot(), false); // Nút gốc luôn luôn phải là màu Đen
    }

    // ========================================================================
    // THUẬT TOÁN XÓA NODE (DELETE) VÀ TỰ CÂN BẰNG
    // ========================================================================

    public void delete(T value) {
        BinaryNode<T> z = find(value);
        if (z == null) return; // Không tìm thấy node để xóa

        BinaryNode<T> x, y = z;
        boolean yOriginalIsRed = isRed(y);

        if (z.left == null) {
            x = z.right;
            transplant(z, z.right);
        } else if (z.right == null) {
            x = z.left;
            transplant(z, z.left);
        } else {
            y = minimum(z.right); // Tìm node kế nhiệm thế chỗ
            yOriginalIsRed = isRed(y);
            x = y.right;

            if (y.parent == z) {
                if (x != null) x.parent = y;
            } else {
                transplant(y, y.right);
                y.right = z.right;
                y.right.parent = y;
            }

            transplant(z, y);
            y.left = z.left;
            y.left.parent = y;
            setRed(y, isRed(z));
        }

        // Sửa lỗi nếu node bị mất là màu đen (Làm hỏng tính chất cân bằng chiều cao đen)
        if (!yOriginalIsRed && x != null) {
            deleteFixup(x);
        }
    }

    private void deleteFixup(BinaryNode<T> x) {
        BinaryNode<T> s;
        while (x != getRoot() && !isRed(x)) {
            if (x == x.parent.left) {
                s = x.parent.right; // Anh em (Sibling)
                if (isRed(s)) {
                    setRed(s, false);
                    setRed(x.parent, true);
                    leftRotate(x.parent);
                    s = x.parent.right;
                }
                if (!isRed(s.left) && !isRed(s.right)) {
                    setRed(s, true);
                    x = x.parent;
                } else {
                    if (!isRed(s.right)) {
                        setRed(s.left, false);
                        setRed(s, true);
                        rightRotate(s);
                        s = x.parent.right;
                    }
                    setRed(s, isRed(x.parent));
                    setRed(x.parent, false);
                    setRed(s.right, false);
                    leftRotate(x.parent);
                    x = getRoot();
                }
            } else {
                // Tương tự nhưng đối xứng
                s = x.parent.left;
                if (isRed(s)) {
                    setRed(s, false);
                    setRed(x.parent, true);
                    rightRotate(x.parent);
                    s = x.parent.left;
                }
                if (!isRed(s.right) && !isRed(s.left)) {
                    setRed(s, true);
                    x = x.parent;
                } else {
                    if (!isRed(s.left)) {
                        setRed(s.right, false);
                        setRed(s, true);
                        leftRotate(s);
                        s = x.parent.left;
                    }
                    setRed(s, isRed(x.parent));
                    setRed(x.parent, false);
                    setRed(s.left, false);
                    rightRotate(x.parent);
                    x = getRoot();
                }
            }
        }
        setRed(x, false);
    }
    
    public void update(T oldValue, T newValue) {
        delete(oldValue);
        insert(newValue);
    }
}