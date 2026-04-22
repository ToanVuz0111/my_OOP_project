package com.demo3.controller;

import com.demo3.model.TreeKind;
import com.demo3.model.core.AnimationStep;
import com.demo3.service.TreeService;
import com.demo3.util.ListFormatUtil;
import com.demo3.util.PseudocodeLibrary;
import com.demo3.view.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Comparison Mode Controller — visualize two tree types side by side.
 *
 * Users perform the same operation on both trees simultaneously
 * to compare behavior (e.g., Binary Tree vs Red-Black Tree insert).
 */
public class ComparisonController {

    private final Stage stage;
    private boolean darkMode;

    // Two independent tree services
    private final TreeService serviceLeft = new TreeService();
    private final TreeService serviceRight = new TreeService();

    // View components
    private HeaderBar headerBar;
    private ComboBox<String> leftTypeChoice;
    private ComboBox<String> rightTypeChoice;
    private OperationForm operationForm;
    private TreePanel treePanelLeft;
    private TreePanel treePanelRight;
    private StatisticsPanel statsLeft;
    private StatisticsPanel statsRight;
    private Label statusLeft;
    private Label statusRight;
    private PlaybackBar playbackBar;

    // Animation state — synchronized playback
    private List<AnimationStep<Integer>> stepsLeft;
    private List<AnimationStep<Integer>> stepsRight;
    private int stepIndex = 0;
    private int maxSteps = 0;
    private Timeline timeline;

    public ComparisonController(Stage stage, boolean darkMode) {
        this.stage = stage;
        this.darkMode = darkMode;
        // Default: Binary Tree (left) vs Red-Black Tree (right)
        serviceLeft.initTree(TreeKind.BINARY_TREE);
        serviceRight.initTree(TreeKind.RED_BLACK_TREE);
    }

    public Scene createScene() {
        // Header
        headerBar = new HeaderBar(this::onBack, this::onThemeToggle);
        headerBar.setTitle("Comparison Mode");

        // Tree type selectors
        leftTypeChoice = createTypeChoice("BINARY_TREE");
        rightTypeChoice = createTypeChoice("RED_BLACK_TREE");
        leftTypeChoice.setOnAction(e -> onChangeTreeType(true));
        rightTypeChoice.setOnAction(e -> onChangeTreeType(false));

        // Operation form
        operationForm = new OperationForm(this::onRun);

        // Tree type selector bar
        Label vsLabel = new Label("VS");
        vsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        HBox typeBar = new HBox(12,
            new Label("Left:"), leftTypeChoice,
            vsLabel,
            new Label("Right:"), rightTypeChoice
        );
        typeBar.setAlignment(Pos.CENTER);
        typeBar.setPadding(new Insets(6, 20, 6, 20));
        typeBar.getStyleClass().add("operation-form");

        // Left tree panel + stats
        treePanelLeft = new TreePanel();
        treePanelLeft.setTreeKind(TreeKind.BINARY_TREE);
        treePanelLeft.setDarkMode(darkMode);
        statusLeft = new Label("Ready.");
        statusLeft.getStyleClass().add("status-label");
        statsLeft = new StatisticsPanel();
        statsLeft.setTreeKind(TreeKind.BINARY_TREE);

        VBox leftColumn = new VBox(8,
            createTreeHeader("Binary Tree"),
            treePanelLeft,
            statusLeft,
            statsLeft
        );
        leftColumn.setPadding(new Insets(4));
        VBox.setVgrow(treePanelLeft, Priority.ALWAYS);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Right tree panel + stats
        treePanelRight = new TreePanel();
        treePanelRight.setTreeKind(TreeKind.RED_BLACK_TREE);
        treePanelRight.setDarkMode(darkMode);
        statusRight = new Label("Ready.");
        statusRight.getStyleClass().add("status-label");
        statsRight = new StatisticsPanel();
        statsRight.setTreeKind(TreeKind.RED_BLACK_TREE);

        VBox rightColumn = new VBox(8,
            createTreeHeader("Red-Black Tree"),
            treePanelRight,
            statusRight,
            statsRight
        );
        rightColumn.setPadding(new Insets(4));
        VBox.setVgrow(treePanelRight, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        // Separator
        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-opacity: 0.3;");

        // Center: two tree panels side by side
        HBox centerBox = new HBox(6, leftColumn, sep, rightColumn);
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        // Playback bar
        playbackBar = new PlaybackBar(
            this::onStepBack, this::onPause, this::onPlay,
            this::onStepForward, this::onUndo, this::onRedo
        );

        // Root layout
        VBox root = new VBox(0, headerBar, typeBar, operationForm, centerBox, playbackBar);
        root.getStyleClass().add("visualization-root");
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 760);
        applyTheme(scene);
        return scene;
    }

    // ========================================================================
    // TREE TYPE CHANGE
    // ========================================================================

    private void onChangeTreeType(boolean isLeft) {
        String selected = isLeft ? leftTypeChoice.getValue() : rightTypeChoice.getValue();
        TreeKind kind = parseTreeKind(selected);

        if (isLeft) {
            serviceLeft.initTree(kind);
            treePanelLeft.setTreeKind(kind);
            statsLeft.setTreeKind(kind);
            renderTree(treePanelLeft, serviceLeft, statusLeft, statsLeft);
        } else {
            serviceRight.initTree(kind);
            treePanelRight.setTreeKind(kind);
            statsRight.setTreeKind(kind);
            renderTree(treePanelRight, serviceRight, statusRight, statsRight);
        }
    }

    // ========================================================================
    // RUN — apply same operation to both trees
    // ========================================================================

    private void onRun() {
        stopPlayback();

        String op = operationForm.operationChoice.getValue();
        if (op == null || op.isEmpty()) return;

        try {
            stepsLeft = executeOp(serviceLeft, op);
            stepsRight = executeOp(serviceRight, op);

            stepIndex = 0;
            maxSteps = Math.max(
                stepsLeft != null ? stepsLeft.size() : 0,
                stepsRight != null ? stepsRight.size() : 0
            );

            if (maxSteps == 0) {
                renderBoth();
                return;
            }

            // Don't auto-play — let user control playback
            statusLeft.setText("Ready — click ▶ Play");
            statusRight.setText("Ready — click ▶ Play");
            setStep(0);

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private List<AnimationStep<Integer>> executeOp(TreeService service, String op) {
        switch (op) {
            case "Insert" -> {
                Integer parent = parseNullableInt(operationForm.parentValueField.getText());
                int val = parseIntOrThrow(operationForm.valueField.getText(), "Value required.");
                return service.performInsert(parent, val);
            }
            case "Delete" -> {
                int val = parseIntOrThrow(operationForm.valueField.getText(), "Value required.");
                return service.performDelete(val);
            }
            case "Update" -> {
                int oldV = parseIntOrThrow(operationForm.valueField.getText(), "Old value required.");
                int newV = parseIntOrThrow(operationForm.newValueField.getText(), "New value required.");
                return service.performUpdate(oldV, newV);
            }
            case "Traverse" -> {
                boolean bfs = "BFS".equalsIgnoreCase(operationForm.traverseChoice.getValue());
                return service.performTraverse(bfs);
            }
            case "Search" -> {
                int val = parseIntOrThrow(operationForm.valueField.getText(), "Value required.");
                return service.performSearch(val);
            }
            default -> { return List.of(); }
        }
    }

    // ========================================================================
    // PLAYBACK
    // ========================================================================

    private void onPlay() {
        if (maxSteps == 0) return;

        // If at end, replay from beginning
        if (stepIndex >= maxSteps - 1) {
            stepIndex = 0;
            setStep(0);
        }

        stopPlayback();
        long speedMs = (long) playbackBar.speedSlider.getValue();
        timeline = new Timeline();
        for (int i = stepIndex + 1; i < maxSteps; i++) {
            final int idx = i;
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(speedMs * (i - stepIndex)), e -> setStep(idx))
            );
        }
        timeline.setOnFinished(e -> renderBoth());
        timeline.play();
    }

    private void onPause() {
        if (timeline != null) timeline.pause();
    }

    private void onStepForward() {
        if (maxSteps == 0) return;
        stopPlayback();
        if (stepIndex < maxSteps - 1) setStep(stepIndex + 1);
    }

    private void onStepBack() {
        if (maxSteps == 0) return;
        stopPlayback();
        if (stepIndex > 0) setStep(stepIndex - 1);
    }

    private void onUndo() {
        stopPlayback();
        stepsLeft = null;
        stepsRight = null;
        serviceLeft.undo();
        serviceRight.undo();
        renderBoth();
    }

    private void onRedo() {
        stopPlayback();
        stepsLeft = null;
        stepsRight = null;
        serviceLeft.redo();
        serviceRight.redo();
        renderBoth();
    }

    private void stopPlayback() {
        if (timeline != null) { timeline.stop(); timeline = null; }
    }

    private void setStep(int index) {
        if (index < 0) index = 0;
        if (index >= maxSteps) index = maxSteps - 1;
        stepIndex = index;

        double speed = playbackBar.speedSlider.getValue() * 0.8;

        // Left
        if (stepsLeft != null && stepIndex < stepsLeft.size()) {
            AnimationStep<Integer> s = stepsLeft.get(stepIndex);
            if (s.hasSnapshot()) {
                treePanelLeft.render(s.getTreeSnapshot(), s.getHighlightedValues(), List.of(), speed);
            } else {
                treePanelLeft.render(serviceLeft.getCurrentTree(), s.getHighlightedValues(), List.of(), speed);
            }
            statusLeft.setText("Step " + (stepIndex + 1) + "/" + stepsLeft.size()
                + " — " + s.getDescription());
        }

        // Right
        if (stepsRight != null && stepIndex < stepsRight.size()) {
            AnimationStep<Integer> s = stepsRight.get(stepIndex);
            if (s.hasSnapshot()) {
                treePanelRight.render(s.getTreeSnapshot(), s.getHighlightedValues(), List.of(), speed);
            } else {
                treePanelRight.render(serviceRight.getCurrentTree(), s.getHighlightedValues(), List.of(), speed);
            }
            statusRight.setText("Step " + (stepIndex + 1) + "/" + stepsRight.size()
                + " — " + s.getDescription());
        }

        double progress = maxSteps <= 1 ? 1.0 : (double) stepIndex / (maxSteps - 1);
        playbackBar.progressBar.setProgress(progress);
    }

    // ========================================================================
    // RENDER HELPERS
    // ========================================================================

    private void renderBoth() {
        renderTree(treePanelLeft, serviceLeft, statusLeft, statsLeft);
        renderTree(treePanelRight, serviceRight, statusRight, statsRight);
    }

    private void renderTree(TreePanel panel, TreeService service,
                             Label status, StatisticsPanel stats) {
        panel.render(service.getCurrentTree(), List.of(), List.of(), 200);
        stats.refreshStats(service.getCurrentTree());
        status.setText("Ready.");
    }

    // ========================================================================
    // NAVIGATION & THEME
    // ========================================================================

    private void onBack() {
        stopPlayback();
        MainMenuController menuCtrl = new MainMenuController(stage);
        stage.setScene(menuCtrl.createScene());
    }

    private void onThemeToggle() {
        darkMode = !darkMode;
        treePanelLeft.setDarkMode(darkMode);
        treePanelRight.setDarkMode(darkMode);
        headerBar.setThemeButtonText(darkMode ? "☀ Light" : "🌙 Dark");
        applyTheme(stage.getScene());
        renderBoth();
    }

    private void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        var cssUrl = getClass().getResource("/com/demo3/app.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        if (darkMode) {
            var darkUrl = getClass().getResource("/com/demo3/dark.css");
            if (darkUrl != null) scene.getStylesheets().add(darkUrl.toExternalForm());
        }
    }

    // ========================================================================
    // UI HELPERS
    // ========================================================================

    private ComboBox<String> createTypeChoice(String defaultValue) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("GENERIC_TREE", "BINARY_TREE", "RED_BLACK_TREE");
        cb.setValue(defaultValue);
        cb.setPrefWidth(160);
        return cb;
    }

    private Label createTreeHeader(String title) {
        Label l = new Label(title);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #475569; -fx-padding: 4 8;");
        l.setAlignment(Pos.CENTER);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private TreeKind parseTreeKind(String s) {
        return switch (s) {
            case "GENERIC_TREE"   -> TreeKind.GENERIC_TREE;
            case "RED_BLACK_TREE" -> TreeKind.RED_BLACK_TREE;
            default               -> TreeKind.BINARY_TREE;
        };
    }

    private Integer parseNullableInt(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;
        return Integer.parseInt(t);
    }

    private int parseIntOrThrow(String text, String msg) {
        if (text == null || text.trim().isEmpty()) throw new NumberFormatException(msg);
        return Integer.parseInt(text.trim());
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg == null ? "Invalid input." : msg);
        a.showAndWait();
    }
}
