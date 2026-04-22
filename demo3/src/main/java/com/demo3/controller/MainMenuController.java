package com.demo3.controller;

import com.demo3.model.TreeKind;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main menu screen — choose tree type, view help, or quit.
 */
public class MainMenuController {

    private final Stage stage;
    private boolean darkMode = false;

    public MainMenuController(Stage stage) {
        this.stage = stage;
    }

    public Scene createScene() {
        // Title
        Label title = new Label("Tree Visualization");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 32));
        title.getStyleClass().add("menu-title");

        Label subtitle = new Label("Select a tree type to begin");
        subtitle.getStyleClass().add("menu-subtitle");

        // Tree type buttons
        Button btnGeneric = createTreeButton("🌳 Generic Tree", "Each node can have unlimited children",
                TreeKind.GENERIC_TREE);
        Button btnBinary = createTreeButton("🌲 Binary Tree", "Each node has at most 2 children",
                TreeKind.BINARY_TREE);
        Button btnRB = createTreeButton("🔴 Red-Black Tree", "Self-balancing BST with color properties",
                TreeKind.RED_BLACK_TREE);

        VBox buttonBox = new VBox(14, btnGeneric, btnBinary, btnRB);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(400);

        // Comparison mode button
        Button btnCompare = new Button("⚖ Compare Mode — Side by Side");
        btnCompare.getStyleClass().add("tree-select-button");
        btnCompare.setMaxWidth(400);
        btnCompare.setPrefHeight(50);
        btnCompare.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        btnCompare.setOnAction(e -> goToComparison());

        // Bottom controls
        Button btnHelp = new Button("❓ How to Use");
        btnHelp.getStyleClass().add("button-secondary");
        btnHelp.setOnAction(e -> showHelp());

        Button btnTheme = new Button("🌙 Toggle Theme");
        btnTheme.getStyleClass().add("button-secondary");
        btnTheme.setOnAction(e -> {
            darkMode = !darkMode;
            applyTheme(stage.getScene());
        });

        Button btnQuit = new Button("✕ Quit");
        btnQuit.getStyleClass().add("button-danger");
        btnQuit.setOnAction(e -> confirmQuit());

        HBox bottomBar = new HBox(12, btnHelp, btnTheme, new Region(), btnQuit);
        bottomBar.setAlignment(Pos.CENTER);
        HBox.setHgrow(bottomBar.getChildren().get(2), Priority.ALWAYS);

        // Layout
        VBox root = new VBox(24, title, subtitle, buttonBox, btnCompare, new Region(), bottomBar);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40, 60, 30, 60));
        root.getStyleClass().add("main-menu");
        VBox.setVgrow(root.getChildren().get(4), Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 760);
        applyTheme(scene);
        return scene;
    }

    private Button createTreeButton(String text, String description, TreeKind kind) {
        Label nameLabel = new Label(text);
        nameLabel.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 18));
        nameLabel.getStyleClass().add("tree-btn-name");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("tree-btn-desc");

        VBox content = new VBox(4, nameLabel, descLabel);
        content.setAlignment(Pos.CENTER_LEFT);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.getStyleClass().add("tree-select-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(70);
        btn.setOnAction(e -> goToVisualization(kind));

        return btn;
    }

    private void goToVisualization(TreeKind kind) {
        VisualizationController vizCtrl = new VisualizationController(stage, kind, darkMode);
        Scene scene = vizCtrl.createScene();
        stage.setScene(scene);
    }

    private void goToComparison() {
        ComparisonController cmpCtrl = new ComparisonController(stage, darkMode);
        Scene scene = cmpCtrl.createScene();
        stage.setScene(scene);
    }

    private void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("How to Use");
        alert.setHeaderText(null);
        alert.setContentText(
            "1. Choose a tree type.\n" +
            "2. Select an operation (Create/Insert/Delete/Update/Traverse/Search).\n" +
            "3. Enter required values and click Execute.\n" +
            "4. Use Pause/Play and Step controls to inspect execution.\n" +
            "5. Undo/Redo moves between completed operations.\n" +
            "6. Toggle light/dark theme with the theme button."
        );
        alert.showAndWait();
    }

    private void confirmQuit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quit");
        confirm.setHeaderText("Are you sure you want to quit?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Platform.exit();
        }
    }

    private void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        String css = getClass().getResource("/com/demo3/app.css").toExternalForm();
        scene.getStylesheets().add(css);
        if (darkMode) {
            String darkCss = getClass().getResource("/com/demo3/dark.css").toExternalForm();
            if (darkCss != null) scene.getStylesheets().add(darkCss);
        }
    }
}
