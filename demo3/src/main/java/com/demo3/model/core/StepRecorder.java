package com.demo3.model.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bộ ghi nhận các bước animation khi thực hiện thao tác trên cây.
 *
 * Cách dùng:
 *   1. Service layer gọi startRecording() TRƯỚC khi gọi operation.
 *   2. Bên trong operation, model gọi recordStep(...) tại mỗi bước quan trọng.
 *      - Bước NHẸ (so sánh, highlight): không truyền snapshot.
 *      - Bước THAY ĐỔI CẤU TRÚC (insert, delete, rotate): truyền deepCopy() snapshot.
 *   3. Sau khi operation hoàn thành, service gọi stopAndGetSteps() để lấy kết quả.
 *
 * Thiết kế này giúp model KHÔNG phụ thuộc vào UI —
 * model chỉ ghi dữ liệu, view tự quyết định cách phát.
 */
public class StepRecorder<T extends Comparable<T>> {

    private final List<AnimationStep<T>> steps = new ArrayList<>();
    private boolean recording = false;

    /**
     * Bắt đầu phiên ghi — xóa mọi bước cũ.
     */
    public void startRecording() {
        steps.clear();
        recording = true;
    }

    /**
     * Dừng ghi và trả về danh sách bước (bất biến).
     */
    public List<AnimationStep<T>> stopAndGetSteps() {
        recording = false;
        return Collections.unmodifiableList(new ArrayList<>(steps));
    }

    /**
     * Ghi bước NHẸ (không snapshot) — dùng cho so sánh, highlight, mô tả.
     */
    public void recordStep(String description, int pseudocodeLine, List<T> highlightedValues) {
        if (recording) {
            steps.add(new AnimationStep<>(description, pseudocodeLine, highlightedValues));
        }
    }

    /**
     * Ghi bước CÓ SNAPSHOT — dùng khi cây thay đổi cấu trúc (insert, delete, rotate).
     * UI có thể render lại toàn bộ cây tại bước này.
     */
    public void recordStep(String description, int pseudocodeLine,
                           List<T> highlightedValues, ITreeOperations<T> treeSnapshot) {
        if (recording) {
            steps.add(new AnimationStep<>(description, pseudocodeLine, highlightedValues, treeSnapshot));
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public void clear() {
        steps.clear();
        recording = false;
    }
}
