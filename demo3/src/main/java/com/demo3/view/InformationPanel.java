package com.demo3.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Right-side information panel containing:
 *  - Operation status label
 *  - Traversal path display
 *  - Algorithm pseudocode (CodePanel)
 */
public class InformationPanel extends VBox {

    public final Label statusLabel = new Label("Ready.");
    public final Label traversalOrderLabel = new Label("(empty)");
    public final CodePanel codePanel = new CodePanel();
    public final StatisticsPanel statisticsPanel = new StatisticsPanel();

    public InformationPanel() {
        getStyleClass().add("info-panel");
        setSpacing(12);
        setPrefWidth(320);
        setMinWidth(280);

        // Status card
        VBox statusBox = createCard("Operation Status", statusLabel);

        // Traversal card
        Label legend = new Label("Cyan = Current · Green = Visited");
        legend.getStyleClass().add("legend-label");
        VBox traverseBox = createCard("Traversal Path", traversalOrderLabel);
        traverseBox.getChildren().add(legend);

        // Statistics (auto-hides irrelevant rows based on tree type)

        // Pseudocode card
        VBox codeBox = createCard("Algorithm Pseudocode", codePanel);
        VBox.setVgrow(codeBox, Priority.ALWAYS);
        VBox.setVgrow(codePanel, Priority.ALWAYS);

        getChildren().addAll(statusBox, traverseBox, statisticsPanel, codeBox);
    }

    private VBox createCard(String title, javafx.scene.Node content) {
        Label t = new Label(title);
        t.getStyleClass().add("panel-card-title");
        VBox box = new VBox(8);
        box.getStyleClass().add("panel-card");
        box.getChildren().addAll(t, content);
        return box;
    }
}
