package com.demo3.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Top header bar with Back button, title, and theme toggle.
 */
public class HeaderBar extends HBox {

    public final Button btnBack = new Button("← Main Menu");
    public final Label titleLabel = new Label("Visualization");
    public final Button btnThemeToggle = new Button("🌙 Dark");

    public HeaderBar(Runnable onBack, Runnable onThemeToggle) {
        getStyleClass().add("header-bar");
        setSpacing(16);
        setAlignment(Pos.CENTER_LEFT);

        btnBack.getStyleClass().add("button-back");
        btnBack.setOnAction(e -> onBack.run());

        titleLabel.getStyleClass().add("header-title");

        btnThemeToggle.getStyleClass().add("button-theme");
        btnThemeToggle.setOnAction(e -> onThemeToggle.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(btnBack, titleLabel, spacer, btnThemeToggle);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setThemeButtonText(String text) {
        btnThemeToggle.setText(text);
    }
}
