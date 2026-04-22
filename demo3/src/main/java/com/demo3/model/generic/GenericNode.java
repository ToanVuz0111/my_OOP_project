package com.demo3.model.generic;

import com.demo3.model.core.INode;

import java.util.ArrayList;
import java.util.List;

/**
 * Nút của Cây Tổng Quát (Generic Tree).
 * Một nút có thể có bất kỳ số lượng nút con nào (không giới hạn).
 *
 * Tất cả field đều PRIVATE → tuân thủ nguyên tắc Encapsulation.
 */
public class GenericNode<T extends Comparable<T>> implements INode<T> {
    private T value;
    private GenericNode<T> parent;
    
    // Danh sách lưu trữ vô số các node con
    private final List<GenericNode<T>> children;

    public GenericNode(T value) {
        this.value = value;
        this.children = new ArrayList<>();
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    public GenericNode<T> getParent() {
        return parent;
    }

    public void setParent(GenericNode<T> parent) {
        this.parent = parent;
    }

    /**
     * Trả về danh sách con nội bộ (dùng trong cùng package model).
     */
    public List<GenericNode<T>> getChildrenList() {
        return children;
    }

    @Override
    public List<INode<T>> getChildren() {
        // Trả về danh sách con dưới dạng INode để UI vẽ giao diện
        return new ArrayList<>(children);
    }
    
    // --- Các hàm tiện ích đặc thù của Generic Tree ---
    
    public void addChild(GenericNode<T> child) {
        child.setParent(this);
        this.children.add(child);
    }
    
    public void removeChild(GenericNode<T> child) {
        this.children.remove(child);
        child.setParent(null);
    }
}