package com.demo3.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Panel showing algorithm pseudocode with line highlighting.
 * The currently executing line is highlighted with a distinct style.
 */
public class CodePanel extends VBox {

    public CodePanel() {
        getStyleClass().add("code-panel");
        setSpacing(4);
        setPadding(new Insets(10));
    }

    /**
     * Load pseudocode lines. Clears previous content.
     */
    public void setCodeLines(String[] codeLines) {
        getChildren().clear();
        if (codeLines == null) return;

        for (String line : codeLines) {
            Label l = new Label(line == null ? "" : line);
            l.getStyleClass().add("code-line");
            l.setWrapText(true);
            l.setMaxWidth(400);
            getChildren().add(l);
        }
    }

    /**
     * Highlight the line at the given 1-based index.
     * (AnimationStep.pseudocodeLine is 1-based; array index = pseudocodeLine - 1)
     */
    public void highlightLine(int pseudocodeLine) {
        int index = pseudocodeLine - 1; // convert to 0-based
        for (int i = 0; i < getChildren().size(); i++) {
            if (!(getChildren().get(i) instanceof Label l)) continue;
            l.getStyleClass().remove("code-line-highlight");
            if (i == index) {
                l.getStyleClass().add("code-line-highlight");
            }
        }
    }

    /** Clear all highlights. */
    public void clearHighlight() {
        for (var child : getChildren()) {
            if (child instanceof Label l) {
                l.getStyleClass().remove("code-line-highlight");
            }
        }
    }
}
