package com.demo3.model.core;

/**
 * Bản thiết kế chung nhất cho mọi loại Cây.
 * Chỉ chứa các phương thức mà mọi cây đều hoạt động y hệt nhau.
 */
public interface ITree<T extends Comparable<T>> {
    
    // Xóa toàn bộ dữ liệu của cây
    void clear();

    // Kiểm tra xem cây có đang rỗng (không có Node nào) hay không
    boolean isEmpty();

    // Lấy ra Node gốc (Rễ cây). Dùng để bắt đầu duyệt hoặc vẽ cây lên màn hình.
    INode<T> getRoot();
}