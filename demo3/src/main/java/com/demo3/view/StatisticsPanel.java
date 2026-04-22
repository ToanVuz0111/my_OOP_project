package com.demo3.view;

import com.demo3.model.TreeKind;
import com.demo3.model.core.ITreeOperations;
import com.demo3.util.TreeStats;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Panel displaying tree statistics: height, node count, leaf count,
 * balance factor (binary), black-height (RB), and max width.
 *
 * Auto-updates when refreshStats() is called.
 */
public class StatisticsPanel extends VBox {

    private final Label lblNodeCount   = createValueLabel("0");
    private final Label lblHeight      = createValueLabel("0");
    private final Label lblLeafCount   = createValueLabel("0");
    private final Label lblMaxWidth    = createValueLabel("0");
    private final Label lblBalance     = createValueLabel("—");
    private final Label lblBlackHeight = createValueLabel("—");

    // Row references for conditional visibility
    private final Label lblBalanceTitle = createTitleLabel("Balance Factor");
    private final Label lblBlackHeightTitle = createTitleLabel("Black Height");

    private TreeKind treeKind = TreeKind.BINARY_TREE;

    public StatisticsPanel() {
        getStyleClass().add("panel-card");
        setSpacing(6);

        Label title = new Label("Tree Statistics");
        title.getStyleClass().add("panel-card-title");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(6);
        grid.setPadding(new Insets(4, 0, 0, 0));

        int row = 0;
        grid.addRow(row++, createTitleLabel("Nodes"), lblNodeCount);
        grid.addRow(row++, createTitleLabel("Height"), lblHeight);
        grid.addRow(row++, createTitleLabel("Leaves"), lblLeafCount);
        grid.addRow(row++, createTitleLabel("Max Width"), lblMaxWidth);
        grid.addRow(row++, lblBalanceTitle, lblBalance);
        grid.addRow(row++, lblBlackHeightTitle, lblBlackHeight);

        getChildren().addAll(title, grid);
    }

    public void setTreeKind(TreeKind kind) {
        this.treeKind = kind;
        // Show/hide relevant rows
        boolean isBinary = (kind == TreeKind.BINARY_TREE || kind == TreeKind.RED_BLACK_TREE);
        boolean isRB = (kind == TreeKind.RED_BLACK_TREE);

        lblBalanceTitle.setVisible(isBinary);
        lblBalanceTitle.setManaged(isBinary);
        lblBalance.setVisible(isBinary);
        lblBalance.setManaged(isBinary);

        lblBlackHeightTitle.setVisible(isRB);
        lblBlackHeightTitle.setManaged(isRB);
        lblBlackHeight.setVisible(isRB);
        lblBlackHeight.setManaged(isRB);
    }

    /**
     * Recompute and display all statistics for the given tree.
     */
    public void refreshStats(ITreeOperations<Integer> tree) {
        if (tree == null || tree.getRoot() == null) {
            lblNodeCount.setText("0");
            lblHeight.setText("0");
            lblLeafCount.setText("0");
            lblMaxWidth.setText("0");
            lblBalance.setText("—");
            lblBlackHeight.setText("—");
            return;
        }

        lblNodeCount.setText(String.valueOf(TreeStats.nodeCount(tree)));
        lblHeight.setText(String.valueOf(TreeStats.height(tree)));
        lblLeafCount.setText(String.valueOf(TreeStats.leafCount(tree)));
        lblMaxWidth.setText(String.valueOf(TreeStats.maxWidth(tree)));

        if (treeKind == TreeKind.BINARY_TREE || treeKind == TreeKind.RED_BLACK_TREE) {
            int bf = TreeStats.balanceFactor(tree);
            lblBalance.setText(bf + (bf == 0 ? " (balanced)" : bf > 0 ? " (left-heavy)" : " (right-heavy)"));
        }

        if (treeKind == TreeKind.RED_BLACK_TREE) {
            int bh = TreeStats.blackHeight(tree);
            lblBlackHeight.setText(bh >= 0 ? String.valueOf(bh) : "—");
        }
    }

    private Label createTitleLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("stats-title");
        l.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        return l;
    }

    private Label createValueLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("stats-value");
        l.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }
}
