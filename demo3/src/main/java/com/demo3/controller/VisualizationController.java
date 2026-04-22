package com.demo3.controller;

import com.demo3.model.TreeKind;
import com.demo3.model.core.AnimationStep;
import com.demo3.model.core.ITreeOperations;
import com.demo3.service.TreeService;
import com.demo3.util.ListFormatUtil;
import com.demo3.util.PseudocodeLibrary;
import com.demo3.view.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the tree visualization screen.
 * Orchestrates: TreeService, animation playback, and all View components.
 */
public class VisualizationController {

    private final Stage stage;
    private final TreeKind treeKind;
    private boolean darkMode;

    // Service
    private final TreeService treeService = new TreeService();

    // View components
    private HeaderBar headerBar;
    private OperationForm operationForm;
    private TreePanel treePanel;
    private InformationPanel infoPanel;
    private PlaybackBar playbackBar;

    // Animation playback state
    private List<AnimationStep<Integer>> activeSteps;
    private int stepIndex = 0;
    private Timeline timeline;

    // Visited values accumulator for traversal/search animation
    private final List<Integer> visitedAccumulator = new ArrayList<>();

    public VisualizationController(Stage stage, TreeKind kind, boolean darkMode) {
        this.stage = stage;
        this.treeKind = kind;
        this.darkMode = darkMode;
        treeService.initTree(kind);
    }

    public Scene createScene() {
        // Header
        headerBar = new HeaderBar(this::onBack, this::onThemeToggle);
        headerBar.setTitle(getTreeTitle());

        // Operation form
        operationForm = new OperationForm(this::onRun);

        // Tree panel (center)
        treePanel = new TreePanel();
        treePanel.setTreeKind(treeKind);
        treePanel.setDarkMode(darkMode);
        VBox.setVgrow(treePanel, Priority.ALWAYS);
        HBox.setHgrow(treePanel, Priority.ALWAYS);

        // Information panel (right)
        infoPanel = new InformationPanel();
        infoPanel.statisticsPanel.setTreeKind(treeKind);
        infoPanel.statisticsPanel.refreshStats(treeService.getCurrentTree());

        // Center area = tree + info
        HBox centerBox = new HBox(10, treePanel, infoPanel);
        HBox.setHgrow(treePanel, Priority.ALWAYS);
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        // Playback bar (bottom)
        playbackBar = new PlaybackBar(
            this::onStepBack, this::onPause, this::onPlay,
            this::onStepForward, this::onUndo, this::onRedo
        );

        // Root layout
        VBox root = new VBox(0, headerBar, operationForm, centerBox, playbackBar);
        root.getStyleClass().add("visualization-root");
        root.setPadding(new Insets(0));
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 760);
        applyTheme(scene);
        return scene;
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    private void onRun() {
        // If animation is playing, jump to end first
        if (activeSteps != null && stepIndex < activeSteps.size() - 1) {
            setStep(activeSteps.size() - 1);
        }
        stopPlayback();

        String op = operationForm.operationChoice.getValue();
        if (op == null || op.isEmpty()) {
            showError("Please select an operation.");
            return;
        }

        try {
            List<AnimationStep<Integer>> steps;

            switch (op) {
                case "Insert" -> {
                    Integer parent = parseNullableInt(operationForm.parentValueField.getText());
                    int newVal = parseIntOrThrow(operationForm.valueField.getText(), "Value is required.");
                    steps = treeService.performInsert(parent, newVal);
                }
                case "Delete" -> {
                    int val = parseIntOrThrow(operationForm.valueField.getText(), "Value is required.");
                    steps = treeService.performDelete(val);
                }
                case "Update" -> {
                    int oldVal = parseIntOrThrow(operationForm.valueField.getText(), "Old value is required.");
                    int newVal = parseIntOrThrow(operationForm.newValueField.getText(), "New value is required.");
                    steps = treeService.performUpdate(oldVal, newVal);
                }
                case "Traverse" -> {
                    boolean bfs = "BFS".equalsIgnoreCase(
                        operationForm.traverseChoice.getValue());
                    steps = treeService.performTraverse(bfs);
                }
                case "Search" -> {
                    int val = parseIntOrThrow(operationForm.valueField.getText(), "Value is required.");
                    steps = treeService.performSearch(val);
                }
                default -> {
                    return;
                }
            }

            // Load pseudocode
            String[] code = PseudocodeLibrary.getCode(treeKind, op);
            infoPanel.codePanel.setCodeLines(code);

            startAnimation(steps, op);

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Operation failed: " + ex.getMessage());
        }
    }

    private void startAnimation(List<AnimationStep<Integer>> steps, String operationTitle) {
        activeSteps = steps;
        stepIndex = 0;
        visitedAccumulator.clear();

        if (steps.isEmpty()) {
            infoPanel.statusLabel.setText(operationTitle + " — no steps.");
            renderCurrentTree();
            return;
        }

        // Show step 0 and WAIT for user to click Play/StepForward
        // (Don't auto-play — let user control playback)
        infoPanel.statusLabel.setText(operationTitle + " ready — click ▶ Play to start.");
        setStep(0);
    }

    // ========================================================================
    // PLAYBACK CONTROLS
    // ========================================================================

    private void onPlay() {
        if (activeSteps == null || activeSteps.isEmpty()) return;

        // If already at end, replay from beginning
        if (stepIndex >= activeSteps.size() - 1) {
            stepIndex = 0;
            visitedAccumulator.clear();
            setStep(0);
        }

        stopPlayback();
        long speedMs = (long) playbackBar.speedSlider.getValue();

        timeline = new Timeline();
        for (int i = stepIndex + 1; i < activeSteps.size(); i++) {
            final int idx = i;
            long delay = speedMs * (i - stepIndex);
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(delay), e -> setStep(idx))
            );
        }
        timeline.setOnFinished(e -> {
            infoPanel.statusLabel.setText("Playback complete. (Step "
                + activeSteps.size() + "/" + activeSteps.size() + ")");
            renderCurrentTree();
        });
        timeline.play();
    }

    private void onPause() {
        if (timeline != null) timeline.pause();
    }

    private void onStepForward() {
        if (activeSteps == null || activeSteps.isEmpty()) return;
        stopPlayback();
        if (stepIndex < activeSteps.size() - 1) {
            setStep(stepIndex + 1);
        } else {
            infoPanel.statusLabel.setText("Already at last step. (Step "
                + activeSteps.size() + "/" + activeSteps.size() + ")");
        }
    }

    private void onStepBack() {
        if (activeSteps == null || activeSteps.isEmpty()) return;
        stopPlayback();
        if (stepIndex > 0) {
            // Rebuild visited accumulator from scratch up to new step
            int targetIndex = stepIndex - 1;
            visitedAccumulator.clear();
            for (int i = 0; i <= targetIndex; i++) {
                for (Integer v : activeSteps.get(i).getHighlightedValues()) {
                    if (!visitedAccumulator.contains(v)) visitedAccumulator.add(v);
                }
            }
            setStep(targetIndex);
        } else {
            infoPanel.statusLabel.setText("Already at first step. (Step 1/"
                + activeSteps.size() + ")");
        }
    }

    private void onUndo() {
        stopPlayback();
        activeSteps = null;
        visitedAccumulator.clear();
        infoPanel.codePanel.setCodeLines(new String[0]);
        infoPanel.codePanel.clearHighlight();

        treeService.undo();
        renderCurrentTree();
        infoPanel.statusLabel.setText(treeService.canUndo() ? "Undo." : "At oldest state.");
        playbackBar.progressBar.setProgress(0);
    }

    private void onRedo() {
        stopPlayback();
        activeSteps = null;
        visitedAccumulator.clear();
        infoPanel.codePanel.setCodeLines(new String[0]);
        infoPanel.codePanel.clearHighlight();

        treeService.redo();
        renderCurrentTree();
        infoPanel.statusLabel.setText(treeService.canRedo() ? "Redo." : "At newest state.");
        playbackBar.progressBar.setProgress(0);
    }

    private void stopPlayback() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    // ========================================================================
    // STEP RENDERING
    // ========================================================================

    private void setStep(int index) {
        if (activeSteps == null || activeSteps.isEmpty()) return;
        if (index < 0) index = 0;
        if (index >= activeSteps.size()) index = activeSteps.size() - 1;
        stepIndex = index;

        AnimationStep<Integer> step = activeSteps.get(stepIndex);

        // Accumulate visited values
        List<Integer> highlights = step.getHighlightedValues();
        for (Integer v : highlights) {
            if (!visitedAccumulator.contains(v)) visitedAccumulator.add(v);
        }

        // Determine which tree to render
        double speedMs = playbackBar.speedSlider.getValue() * 0.8;

        if (step.hasSnapshot()) {
            // Render the snapshot from this step
            treePanel.render(step.getTreeSnapshot(), highlights,
                             new ArrayList<>(visitedAccumulator), speedMs);
        } else {
            // No snapshot — render current tree with highlights
            treePanel.render(treeService.getCurrentTree(), highlights,
                             new ArrayList<>(visitedAccumulator), speedMs);
        }

        // Update info panel
        infoPanel.codePanel.highlightLine(step.getPseudocodeLine());
        infoPanel.statusLabel.setText("Step " + (stepIndex + 1) + "/" + activeSteps.size()
            + " — " + step.getDescription());
        infoPanel.traversalOrderLabel.setText(
            ListFormatUtil.joinArrowSeparated(visitedAccumulator));

        // Update statistics if we have a snapshot (tree structure changed)
        if (step.hasSnapshot()) {
            infoPanel.statisticsPanel.refreshStats(step.getTreeSnapshot());
        }

        // Update progress bar
        double progress = activeSteps.size() <= 1 ? 1.0
            : (double) stepIndex / (activeSteps.size() - 1);
        playbackBar.progressBar.setProgress(progress);
    }

    /** Render the current tree without animation highlights. */
    private void renderCurrentTree() {
        treePanel.render(treeService.getCurrentTree(), List.of(), List.of(), 200);
        infoPanel.traversalOrderLabel.setText("(empty)");
        infoPanel.statisticsPanel.refreshStats(treeService.getCurrentTree());
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
        treePanel.setDarkMode(darkMode);
        headerBar.setThemeButtonText(darkMode ? "☀ Light" : "🌙 Dark");
        applyTheme(stage.getScene());
        renderCurrentTree();
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
    // HELPERS
    // ========================================================================

    private String getTreeTitle() {
        return switch (treeKind) {
            case GENERIC_TREE   -> "Generic Tree Visualization";
            case BINARY_TREE    -> "Binary Tree Visualization";
            case RED_BLACK_TREE -> "Red-Black Tree Visualization";
        };
    }

    private Integer parseNullableInt(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;
        return Integer.parseInt(t);
    }

    private int parseIntOrThrow(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new NumberFormatException(message);
        }
        return Integer.parseInt(text.trim());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Invalid input." : message);
        alert.showAndWait();
    }
}
