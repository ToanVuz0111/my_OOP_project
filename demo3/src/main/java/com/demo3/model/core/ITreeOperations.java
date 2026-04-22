package com.demo3.model.core;

import java.util.List;

/**
 * Interface khai báo ĐẦY ĐỦ 6 thao tác theo đúng đề bài:
 * Create, Insert, Delete, Update, Traverse, Search.
 *
 * Service layer gọi qua interface này — không cần biết loại cây cụ thể.
 * Đây là nền tảng behavioral polymorphism: cùng tên method,
 * mỗi loại cây có hành vi override riêng.
 *
 * Ngoài ra cung cấp API ghi bước animation (startRecording / stopAndGetSteps)
 * để hỗ trợ step-by-step playback trên giao diện.
 */
public interface ITreeOperations<T extends Comparable<T>> extends ITree<T> {

    /**
     * CREATE: Xóa toàn bộ cây, tạo lại cây rỗng.
     * Đề bài: "Create a new, empty tree."
     */
    void create();

    /**
     * INSERT: Thêm node mới.
     * - Generic Tree / Binary Tree: parentValue xác định node cha.
     * - Red-Black Tree: parentValue bỏ qua, tự tìm vị trí theo BST property.
     *
     * @param parentValue giá trị node cha (null nếu cây rỗng hoặc RBTree)
     * @param newValue    giá trị node mới cần thêm
     * @throws IllegalArgumentException nếu không tìm thấy node cha hoặc giá trị đã tồn tại
     */
    void insert(T parentValue, T newValue);

    /**
     * DELETE: Xóa node có giá trị cho trước.
     * Red-Black Tree tự rebalance (deleteFixup) sau khi xóa.
     */
    void delete(T value);

    /**
     * UPDATE: Cập nhật giá trị node.
     * - Generic Tree / Binary Tree: đổi trực tiếp tại chỗ (không có thứ tự).
     * - Red-Black Tree: delete rồi insert lại để đảm bảo BST property.
     */
    void update(T oldValue, T newValue);

    /**
     * TRAVERSE: Duyệt toàn bộ cây, trả về danh sách node theo thứ tự duyệt.
     * Đề bài yêu cầu hỗ trợ 2 thuật toán: DFS và BFS.
     *
     * @param useBFS true  → duyệt theo chiều rộng (BFS / Level-order)
     *               false → duyệt theo chiều sâu  (DFS / Pre-order)
     * @return danh sách các node theo thứ tự đã duyệt
     */
    List<INode<T>> traverse(boolean useBFS);

    /**
     * SEARCH: Tìm kiếm node có giá trị cho trước.
     * Trả về node tìm được để service layer có thể highlight trên UI.
     * Trả về null nếu không tìm thấy.
     *
     * @param value giá trị cần tìm
     * @return node tìm được, hoặc null nếu không có
     */
    INode<T> searchNode(T value);

    /**
     * DEEP COPY: Tạo bản sao sâu hoàn toàn độc lập của cây.
     * Dùng để lưu snapshot cho Undo/Redo.
     */
    ITreeOperations<T> deepCopy();

    // ========================================================================
    // ANIMATION RECORDING — phục vụ step-by-step playback trên giao diện
    // ========================================================================

    /**
     * Bắt đầu ghi nhận các bước animation.
     * Gọi TRƯỚC khi thực hiện operation (insert, delete, ...).
     */
    void startRecording();

    /**
     * Dừng ghi và trả về danh sách các bước animation đã ghi được.
     * Gọi SAU khi operation hoàn thành.
     */
    List<AnimationStep<T>> stopAndGetSteps();
}