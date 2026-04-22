package com.demo3.model.binary;

import com.demo3.model.core.INode;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp này đại diện cho một Node trong cây nhị phân.
 * Nó chứa giá trị, con trái, con phải và con trỏ đến cha.
 *
 * Tất cả field đều PRIVATE → tuân thủ nguyên tắc Encapsulation.
 * Truy cập thông qua getter/setter.
 */
public class BinaryNode<T extends Comparable<T>> implements INode<T> {
    private T value;
    private BinaryNode<T> left;
    private BinaryNode<T> right;
    private BinaryNode<T> parent;

    public BinaryNode(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    public BinaryNode<T> getLeft() {
        return left;
    }

    public void setLeft(BinaryNode<T> left) {
        this.left = left;
    }

    public BinaryNode<T> getRight() {
        return right;
    }

    public void setRight(BinaryNode<T> right) {
        this.right = right;
    }

    public BinaryNode<T> getParent() {
        return parent;
    }

    public void setParent(BinaryNode<T> parent) {
        this.parent = parent;
    }

    @Override
    public List<INode<T>> getChildren() {
        List<INode<T>> children = new ArrayList<>();
        if (left != null) children.add(left);
        if (right != null) children.add(right);
        return children;
    }
}