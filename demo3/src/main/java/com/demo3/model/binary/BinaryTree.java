package com.demo3.model.binary;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Cài đặt Cây Nhị Phân thuần túy (không phải cây tìm kiếm).
 * Các node được lấp đầy theo thứ tự cấp độ (Level-order).
 */
public class BinaryTree<T extends Comparable<T>> extends AbstractBinaryTree<T> {

    /**
     * Thêm node mới vào vị trí trống đầu tiên trong cây (duyệt theo chiều rộng - BFS).
     */
    public void insert(T value) {
        BinaryNode<T> newNode = new BinaryNode<>(value);
        if (root == null) {
            root = newNode;
            return;
        }

        Queue<BinaryNode<T>> queue = new LinkedList<>();
        queue.add(getRoot());

        while (!queue.isEmpty()) {
            BinaryNode<T> temp = queue.poll();

            // Kiểm tra con bên trái
            if (temp.left == null) {
                temp.left = newNode;
                newNode.parent = temp;
                return;
            } else {
                queue.add(temp.left);
            }

            // Kiểm tra con bên phải
            if (temp.right == null) {
                temp.right = newNode;
                newNode.parent = temp;
                return;
            } else {
                queue.add(temp.right);
            }
        }
    }

    /**
     * Xóa một giá trị khỏi cây bằng cách thay thế bằng node sâu nhất.
     */
    public void delete(T value) {
        if (root == null) return;

        // 1. Tìm node cần xóa (target) và node sâu nhất (deepest)
        BinaryNode<T> targetNode = null;
        BinaryNode<T> deepestNode = null;
        Queue<BinaryNode<T>> queue = new LinkedList<>();
        queue.add(getRoot());

        while (!queue.isEmpty()) {
            deepestNode = queue.poll();
            
            if (deepestNode.value.compareTo(value) == 0) {
                targetNode = deepestNode;
            }
            
            if (deepestNode.left != null) queue.add(deepestNode.left);
            if (deepestNode.right != null) queue.add(deepestNode.right);
        }

        // 2. Thực hiện xóa
        if (targetNode != null) {
            // Thay thế giá trị của target bằng giá trị của node sâu nhất
            targetNode.value = deepestNode.value;
            
            // Cắt bỏ node sâu nhất khỏi cha của nó
            BinaryNode<T> parent = deepestNode.parent;
            if (parent != null) {
                if (parent.left == deepestNode) parent.left = null;
                else parent.right = null;
            } else {
                root = null; // Trường hợp cây chỉ có 1 node duy nhất
            }
        }
    }

    /**
     * Tìm kiếm giá trị (sử dụng hàm find của AbstractBinaryTree)
     */
    public boolean search(T value) {
        return find(value) != null;
    }

    /**
     * Cập nhật giá trị: Tìm node cũ và đổi thành giá trị mới.
     */
    public void update(T oldValue, T newValue) {
        BinaryNode<T> node = find(oldValue);
        if (node != null) {
            node.value = newValue;
        }
    }
}