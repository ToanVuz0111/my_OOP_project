package com.demo3.model.core;

import java.util.List;

/**
 * Bản thiết kế chung cho mọi Node (Nút) trên cây.
 */
public interface INode<T extends Comparable<T>> {
    
    // Lấy giá trị đang lưu trong Node
    T getValue();
    
    // Sửa giá trị của Node
    void setValue(T value);

    // Bắt buộc mọi loại Node phải có khả năng báo cáo danh sách các con của nó.
    // Dấu "?" nghĩa là "bất kỳ loại Node nào kế thừa INode".
    List<? extends INode<T>> getChildren();
}