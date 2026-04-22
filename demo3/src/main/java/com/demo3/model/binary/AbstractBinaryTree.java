package com.demo3.model.binary;

import com.demo3.model.core.AbstractTree;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Lớp trừu tượng cho mọi cây nhị phân.
 * Chỉ chứa những gì CHUNG cho cả BinaryTree lẫn RBTree:
 *   - find() bằng BFS (BinaryTree dùng trực tiếp, RBTree override lại)
 *   - minimum() bằng BFS (RBTree override lại bằng "rẽ trái" - đây là behavioral polymorphism)
 *   - setRoot() tiện ích
 *
 * KHÔNG chứa leftRotate/rightRotate/transplant vì chỉ RBTree mới cần.
 * Tránh vi phạm ISP (Interface Segregation Principle).
 */
public abstract class AbstractBinaryTree<T extends Comparable<T>> extends AbstractTree<T> {

    @Override
    public BinaryNode<T> getRoot() {
        return (BinaryNode<T>) root;
    }

    public void setRoot(BinaryNode<T> newRoot) {
        this.root = newRoot;
    }

    /**
     * Tìm kiếm bằng BFS — đúng cho Binary Tree thuần (không có thứ tự).
     * RBTree sẽ OVERRIDE lại bằng cách rẽ trái/phải theo BST property.
     */
    public BinaryNode<T> find(T value) {
        if (root == null) return null;

        Queue<BinaryNode<T>> queue = new LinkedList<>();
        queue.add(getRoot());

        while (!queue.isEmpty()) {
            BinaryNode<T> cur = queue.poll();
            if (cur.getValue().compareTo(value) == 0) return cur;
            if (cur.getLeft() != null) queue.add(cur.getLeft());
            if (cur.getRight() != null) queue.add(cur.getRight());
        }
        return null;
    }

    /**
     * Tìm node nhỏ nhất bằng BFS — đúng cho Binary Tree thuần.
     * RBTree sẽ OVERRIDE lại bằng "rẽ trái liên tục" nhờ BST property.
     * Đây là ví dụ behavioral polymorphism: cùng tên hàm, khác hành vi ở subclass.
     */
    public BinaryNode<T> minimum(BinaryNode<T> start) {
        if (start == null) return null;

        BinaryNode<T> minNode = start;
        Queue<BinaryNode<T>> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            BinaryNode<T> cur = queue.poll();
            if (cur.getValue().compareTo(minNode.getValue()) < 0) {
                minNode = cur;
            }
            if (cur.getLeft() != null) queue.add(cur.getLeft());
            if (cur.getRight() != null) queue.add(cur.getRight());
        }
        return minNode;
    }
}