package com.demo3.model.redblack;

import com.demo3.model.binary.BinaryNode;

/**
 * Nút của Cây Đỏ - Đen.
 * Kế thừa toàn bộ cấu trúc của BinaryNode và thêm thuộc tính màu sắc.
 *
 * Field màu sắc PRIVATE → tuân thủ Encapsulation.
 */
public class RBNode<T extends Comparable<T>> extends BinaryNode<T> {
    
    // true = Màu Đỏ, false = Màu Đen
    private boolean red;

    public RBNode(T value) {
        super(value); // Gọi hàm khởi tạo của BinaryNode để gán giá trị
        
        // Quy tắc cốt lõi của Red-Black Tree: 
        // Bất kỳ Node MỚI nào khi được chèn vào cây CŨNG PHẢI LÀ MÀU ĐỎ.
        this.red = true;
    }

    public boolean isRed() {
        return red;
    }

    public void setRed(boolean red) {
        this.red = red;
    }
}