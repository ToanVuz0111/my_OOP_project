package com.demo3.model.binary;

import com.demo3.model.core.AbstractTree;
import com.demo3.model.core.INode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class AbstractBinaryTree<T extends Comparable<T>> extends AbstractTree<T> {

    @Override
    public BinaryNode<T> getRoot() {
        return (BinaryNode<T>) root;
    }

    public void setRoot(BinaryNode<T> newRoot) {
        this.root = newRoot;
    }

    /**
     * SỬA LỖI: Với cây nhị phân thường, ta phải duyệt qua từng node (BFS) để tìm kiếm.
     * Thuật toán: Tìm kiếm theo chiều rộng (Level-order).
     * Sẽ được GHI ĐÈ (Override) ở các lớp con như Red-Black Tree để tối ưu tốc độ.
     */
    public BinaryNode<T> find(T value) {
        if (root == null) return null;
        
        Queue<BinaryNode<T>> queue = new LinkedList<>();
        queue.add(getRoot());
        
        while (!queue.isEmpty()) {
            BinaryNode<T> cur = queue.poll();
            
            // Nếu tìm thấy thì trả về node đó
            if (cur.getValue().compareTo(value) == 0) return cur;
            
            // Nếu không, tiếp tục ném các con vào hàng đợi để tìm
            if (cur.left != null) queue.add(cur.left);
            if (cur.right != null) queue.add(cur.right);
        }
        return null; // Không tìm thấy
    }

    /**
     * SỬA LỖI: Tìm node có giá trị nhỏ nhất bắt đầu từ một node cho trước.
     * Với cây nhị phân thường, không có quy luật lớn/nhỏ, ta bắt buộc phải duyệt 
     * qua TẤT CẢ các node (dùng BFS) để tìm ra giá trị nhỏ nhất thực sự.
     * * Lưu ý: Hàm này sẽ được GHI ĐÈ (Override) ở lớp Red-Black Tree để tối ưu tốc độ 
     * (chỉ cần rẽ trái) đúng với tính đa hình OOP.
     */
    public BinaryNode<T> minimum(BinaryNode<T> start) {
        if (start == null) return null;

        BinaryNode<T> minNode = start;
        Queue<BinaryNode<T>> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            BinaryNode<T> cur = queue.poll();

            // Nếu giá trị của node hiện tại nhỏ hơn minNode đang lưu, thì cập nhật minNode
            if (cur.getValue().compareTo(minNode.getValue()) < 0) {
                minNode = cur;
            }

            // Tiếp tục duyệt các node con
            if (cur.left != null) queue.add(cur.left);
            if (cur.right != null) queue.add(cur.right);
        }
        
        return minNode;
    }

    /**
     * Cấy ghép node (Thay thế node u bằng node v).
     * Hàm này cực kỳ quan trọng trong thuật toán Delete.
     */
    public void transplant(BinaryNode<T> u, BinaryNode<T> v) {
        if (u.parent == null) {
            setRoot(v);
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        if (v != null) v.parent = u.parent;
    }

    /**
     * Xoay trái - Phục vụ việc cân bằng Cây Đỏ Đen
     */
    public void leftRotate(BinaryNode<T> x) {
        BinaryNode<T> y = x.right;
        x.right = y.left;
        if (y.left != null) y.left.parent = x;

        y.parent = x.parent;
        if (x.parent == null) {
            setRoot(y);
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }

        y.left = x;
        x.parent = y;
    }

    /**
     * Xoay phải - Phục vụ việc cân bằng Cây Đỏ Đen
     */
    public void rightRotate(BinaryNode<T> y) {
        BinaryNode<T> x = y.left;
        y.left = x.right;
        if (x.right != null) x.right.parent = y;

        x.parent = y.parent;
        if (y.parent == null) {
            setRoot(x);
        } else if (y == y.parent.right) {
            y.parent.right = x;
        } else {
            y.parent.left = x;
        }

        x.right = y;
        y.parent = x;
    }

    public List<INode<T>> traverseBFS() {
        List<INode<T>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<BinaryNode<T>> queue = new LinkedList<>();
        queue.add(getRoot());

        while (!queue.isEmpty()) {
            BinaryNode<T> current = queue.poll();
            result.add(current);

            if (current.left != null) queue.add(current.left);
            if (current.right != null) queue.add(current.right);
        }
        return result;
    }

    /**
     * DUYỆT DFS (Duyệt theo chiều sâu - Pre-order: Gốc -> Trái -> Phải).
     * GUI sẽ dùng list này để highlight quá trình chạy sâu xuống nhánh.
     */
    public List<INode<T>> traverseDFS() {
        List<INode<T>> result = new ArrayList<>();
        dfsHelper(getRoot(), result);
        return result;
    }

    // Hàm đệ quy hỗ trợ cho DFS
    private void dfsHelper(BinaryNode<T> node, List<INode<T>> result) {
        if (node == null) return;
        
        result.add(node); // Thăm Gốc
        dfsHelper(node.left, result); // Sang nhánh Trái
        dfsHelper(node.right, result); // Sang nhánh Phải
    }
}