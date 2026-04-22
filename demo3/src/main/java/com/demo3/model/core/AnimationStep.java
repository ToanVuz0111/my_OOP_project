package com.demo3.model.core;

import java.util.Collections;
import java.util.List;

/**
 * Đại diện cho MỘT BƯỚC trong quá trình animation của một thao tác trên cây.
 *
 * Mỗi AnimationStep chứa:
 *   - description:       mô tả ngắn gọn hành động đang diễn ra
 *   - pseudocodeLine:    số thứ tự dòng pseudocode cần highlight trên UI
 *   - highlightedValues: danh sách giá trị các node cần highlight trên giao diện
 *   - treeSnapshot:      bản sao sâu (deep copy) của cây tại thời điểm này
 *                         (chỉ có ở các bước thay đổi cấu trúc, null ở bước nhẹ)
 *
 * Service layer thu thập danh sách các bước này rồi truyền cho View
 * để phát từng bước (play, pause, step forward/backward).
 *
 * Tham khảo thiết kế StepFrame trong OOP_20252 — mỗi bước chứa full snapshot
 * giúp UI render lại toàn bộ cây ở bất kỳ bước nào khi người dùng
 * step forward/backward hoặc kéo progress bar.
 */
public class AnimationStep<T extends Comparable<T>> {

    private final String description;
    private final int pseudocodeLine;
    private final List<T> highlightedValues;
    private final ITreeOperations<T> treeSnapshot; // null nếu bước nhẹ (không thay đổi cấu trúc)

    /**
     * Constructor ĐẦY ĐỦ — có snapshot.
     * Dùng tại các bước thay đổi cấu trúc cây (insert, delete, rotate, ...).
     */
    public AnimationStep(String description, int pseudocodeLine,
                         List<T> highlightedValues, ITreeOperations<T> treeSnapshot) {
        this.description = description;
        this.pseudocodeLine = pseudocodeLine;
        this.highlightedValues = highlightedValues != null
                ? Collections.unmodifiableList(highlightedValues)
                : Collections.emptyList();
        this.treeSnapshot = treeSnapshot;
    }

    /**
     * Constructor NHẸ — không snapshot.
     * Dùng tại các bước chỉ highlight / mô tả, không thay đổi cấu trúc.
     */
    public AnimationStep(String description, int pseudocodeLine, List<T> highlightedValues) {
        this(description, pseudocodeLine, highlightedValues, null);
    }

    public String getDescription() {
        return description;
    }

    public int getPseudocodeLine() {
        return pseudocodeLine;
    }

    public List<T> getHighlightedValues() {
        return highlightedValues;
    }

    /**
     * Trả về bản sao sâu của cây tại bước này.
     * Có thể null nếu bước không thay đổi cấu trúc cây.
     * UI nên dùng snapshot gần nhất (không null) để render.
     */
    public ITreeOperations<T> getTreeSnapshot() {
        return treeSnapshot;
    }

    /**
     * Kiểm tra xem bước này có chứa snapshot hay không.
     */
    public boolean hasSnapshot() {
        return treeSnapshot != null;
    }

    @Override
    public String toString() {
        return "[Line " + pseudocodeLine + "] " + description
                + " | highlight=" + highlightedValues
                + (treeSnapshot != null ? " | HAS_SNAPSHOT" : "");
    }
}
