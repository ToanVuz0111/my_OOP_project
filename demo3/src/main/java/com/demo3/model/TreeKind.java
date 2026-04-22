package com.demo3.model;

/**
 * Enum định nghĩa 3 loại cây chuẩn xác theo yêu cầu đề bài.
 * Giúp Controller nhận diện được người dùng đang tương tác với cây nào.
 */
public enum TreeKind {
    
    GENERIC_TREE("Generic Tree"),
    BINARY_TREE("Binary Tree"),
    RED_BLACK_TREE("Red-Black Tree");

    private final String displayName;

    // Constructor của Enum
    TreeKind(String displayName) {
        this.displayName = displayName;
    }

    // Hàm để giao diện lấy tên đẹp in ra màn hình
    public String getDisplayName() {
        return displayName;
    }
}