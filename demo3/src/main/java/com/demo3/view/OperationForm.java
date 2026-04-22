package com.demo3.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Operation toolbar — select operation, enter parameters, execute.
 */
public class OperationForm extends HBox {

    public final ComboBox<String> operationChoice  = new ComboBox<>();
    public final TextField valueField              = new TextField();
    public final TextField parentValueField        = new TextField();
    public final TextField newValueField           = new TextField();
    public final ComboBox<String> traverseChoice   = new ComboBox<>();
    public final Button btnRun                     = new Button("▶ Execute");

    public OperationForm(Runnable onRun) {
        getStyleClass().add("operation-form");
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);

        operationChoice.getItems().addAll("Insert", "Delete", "Update", "Traverse", "Search");
        operationChoice.setPrefWidth(120);
        operationChoice.setPromptText("Operation");
        operationChoice.setOnAction(e -> updateFieldVisibility());

        valueField.setPrefWidth(80);
        valueField.setPromptText("value");

        parentValueField.setPrefWidth(100);
        parentValueField.setPromptText("parent (opt)");

        newValueField.setPrefWidth(80);
        newValueField.setPromptText("new value");

        traverseChoice.getItems().addAll("BFS", "DFS");
        traverseChoice.setPrefWidth(80);
        traverseChoice.setValue("BFS");

        btnRun.getStyleClass().add("button-run");
        btnRun.setOnAction(e -> onRun.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
            labeled("Op:", operationChoice),
            labeled("Value:", valueField),
            labeled("Parent:", parentValueField),
            labeled("New:", newValueField),
            labeled("Algo:", traverseChoice),
            spacer,
            btnRun
        );

        // Initial visibility
        updateFieldVisibility();
    }

    /**
     * Show/hide input fields based on the selected operation.
     */
    private void updateFieldVisibility() {
        String op = operationChoice.getValue();
        if (op == null) op = "";

        // Default: hide all
        valueField.setVisible(false);
        valueField.setManaged(false);
        parentValueField.setVisible(false);
        parentValueField.setManaged(false);
        newValueField.setVisible(false);
        newValueField.setManaged(false);
        traverseChoice.setVisible(false);
        traverseChoice.setManaged(false);

        // Show relevant fields per each parent label too
        setLabelVisible("Value:", false);
        setLabelVisible("Parent:", false);
        setLabelVisible("New:", false);
        setLabelVisible("Algo:", false);

        switch (op) {
            case "Insert" -> {
                showField(valueField, "Value:");
                showField(parentValueField, "Parent:");
            }
            case "Delete", "Search" -> {
                showField(valueField, "Value:");
            }
            case "Update" -> {
                showField(valueField, "Value:");
                showField(newValueField, "New:");
            }
            case "Traverse" -> {
                showField(traverseChoice, "Algo:");
            }
        }
    }

    private void showField(Control field, String labelText) {
        field.setVisible(true);
        field.setManaged(true);
        setLabelVisible(labelText, true);
    }

    private void setLabelVisible(String labelText, boolean visible) {
        for (var child : getChildren()) {
            if (child instanceof HBox hbox) {
                for (var c : hbox.getChildren()) {
                    if (c instanceof Label l && l.getText().equals(labelText)) {
                        hbox.setVisible(visible);
                        hbox.setManaged(visible);
                        return;
                    }
                }
            }
        }
    }

    private HBox labeled(String labelText, Control control) {
        Label l = new Label(labelText);
        l.getStyleClass().add("form-label");
        HBox box = new HBox(4, l, control);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}
