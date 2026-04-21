package com.demo3.model.core;

/**
 * Lớp trừu tượng này cung cấp các cài đặt mặc định cho những hành động cơ bản nhất của một cái Cây.
 * Nó giúp các cây cụ thể (như Binary Tree, Nary Tree) không phải viết lại code thừa thãi.
 */
public abstract class AbstractTree<T extends Comparable<T>> implements ITree<T> {
    
    // Nút gốc của cây. Dùng 'protected' để các cây con (kế thừa lớp này) có thể chạm vào nó.
    protected INode<T> root;

    @Override
    public void clear() {
        // Xóa sạch cây đơn giản là cho gốc bay màu. Trình thu gom rác của Java sẽ tự dọn các nút con.
        this.root = null;
    }

    @Override
    public boolean isEmpty() {
        // Nếu gốc là null thì chắc chắn cây rỗng.
        return this.root == null;
    }

    @Override
    public INode<T> getRoot() {
        return this.root;
    }
}