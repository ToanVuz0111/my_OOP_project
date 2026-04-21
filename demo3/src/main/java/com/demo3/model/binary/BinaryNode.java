package com.demo3.model.binary;

import com.demo3.model.core.INode;
import java.util.ArrayList;
import java.util.List;
/**
 * Lớp này đại diện cho một Node trong cây nhị phân.
 * Nó chứa giá trị, con trái, con phải và con trỏ đến cha.
 */

public class BinaryNode<T extends Comparable<T>> implements INode<T> {
    public T value;
    public BinaryNode<T> left;
    public BinaryNode<T> right;
    public BinaryNode<T> parent;

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

    @Override
    public List<INode<T>> getChildren() {
        List<INode<T>> children = new ArrayList<>();
        if (left != null) children.add(left);
        if (right != null) children.add(right);
        return children;
    }
}