package com.demo3.view;

import com.demo3.model.TreeKind;
import com.demo3.model.binary.BinaryNode;
import com.demo3.model.core.INode;
import com.demo3.model.core.ITreeOperations;
import com.demo3.model.generic.GenericNode;
import com.demo3.model.redblack.RBNode;
import com.demo3.util.TreeLayoutConstants;

import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;

import static com.demo3.util.TreeLayoutConstants.*;

/**
 * Tree visualization panel — renders any tree type using INode polymorphism.
 *
 * Improvement over OOP_20252:
 * - Uses getChildren() polymorphism for generic traversal
 * - Distinguishes binary vs generic layout by TreeKind
 * - Supports highlight (cyan) and visited (green) coloring
 * - Smooth transitions when tree structure changes
 */
public class TreePanel extends ScrollPane {

    private final Pane canvas = new Pane();
    private final Group edgeGroup = new Group();
    private final Group nodeGroup = new Group();

    // Track existing GUI elements for smooth transitions
    private final Map<String, Group> nodeGuis = new LinkedHashMap<>();
    private final Map<String, Line> edgeGuis = new LinkedHashMap<>();

    private TreeKind treeKind = TreeKind.BINARY_TREE;
    private boolean darkMode = false;

    public TreePanel() {
        getStyleClass().add("tree-panel");
        canvas.getChildren().addAll(edgeGroup, nodeGroup);
        canvas.setMinSize(800, 500);
        setContent(canvas);
        setFitToWidth(true);
        setFitToHeight(true);
        setPannable(true);
    }

    public void setTreeKind(TreeKind kind) {
        this.treeKind = kind;
    }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
    }

    /**
     * Render a tree with optional highlight and visited sets.
     */
    public void render(ITreeOperations<Integer> tree, List<Integer> highlight,
                       List<Integer> visited, double animDurationMs) {
        if (tree == null || tree.getRoot() == null) {
            clearAll();
            return;
        }

        Set<Integer> highlightSet = highlight != null ? new HashSet<>(highlight) : Collections.emptySet();
        Set<Integer> visitedSet = visited != null ? new LinkedHashSet<>(visited) : Collections.emptySet();

        // Compute layout coordinates
        Map<INode<Integer>, Point2D> coords = new LinkedHashMap<>();
        computeLayout(tree.getRoot(), coords);

        // Build target state
        Map<String, Point2D> targetCoords = new LinkedHashMap<>();
        Map<String, String> targetEdges = new LinkedHashMap<>(); // child → parent
        Map<String, NodeStyle> targetStyles = new LinkedHashMap<>();

        extractTargets(tree.getRoot(), null, coords, targetCoords, targetEdges,
                       targetStyles, highlightSet, visitedSet);

        // Animate transition
        Duration dur = Duration.millis(Math.max(50, animDurationMs));
        animateToTarget(targetCoords, targetEdges, targetStyles, dur);
    }

    /** Render from a snapshot stored in an AnimationStep. */
    public void renderSnapshot(ITreeOperations<Integer> snapshot, List<Integer> highlight) {
        render(snapshot, highlight, List.of(), 200);
    }

    public void clearAll() {
        nodeGuis.clear();
        edgeGuis.clear();
        nodeGroup.getChildren().clear();
        edgeGroup.getChildren().clear();
    }

    // ========================================================================
    // LAYOUT COMPUTATION
    // ========================================================================

    private void computeLayout(INode<Integer> root, Map<INode<Integer>, Point2D> coords) {
        if (root instanceof BinaryNode) {
            double[] nextX = {1};
            assignBinaryPositions((BinaryNode<Integer>) root, 0, nextX, coords);
        } else if (root instanceof GenericNode) {
            Map<INode<Integer>, Double> widthCache = new HashMap<>();
            double totalWidth = computeGenericWidths((GenericNode<Integer>) root, widthCache);
            assignGenericPositions((GenericNode<Integer>) root, 0, 0, totalWidth, coords, widthCache);
        }
    }

    // --- Binary tree: in-order position assignment ---
    private void assignBinaryPositions(BinaryNode<Integer> node, int depth,
                                        double[] nextX, Map<INode<Integer>, Point2D> coords) {
        if (node == null) return;
        assignBinaryPositions(node.getLeft(), depth + 1, nextX, coords);
        coords.put(node, new Point2D(nextX[0] * X_SPACING, TOP_PADDING + depth * Y_SPACING));
        nextX[0] += 1;
        assignBinaryPositions(node.getRight(), depth + 1, nextX, coords);
    }

    // --- Generic tree: width-based subtree positioning ---
    private double computeGenericWidths(GenericNode<Integer> node, Map<INode<Integer>, Double> cache) {
        List<GenericNode<Integer>> children = node.getChildrenList();
        if (children.isEmpty()) {
            cache.put(node, NODE_RADIUS * 2 + 40);
            return cache.get(node);
        }
        double sum = 0;
        for (GenericNode<Integer> child : children) {
            sum += computeGenericWidths(child, cache);
        }
        double gap = 40;
        if (children.size() > 1) sum += gap * (children.size() - 1);
        cache.put(node, sum);
        return sum;
    }

    private void assignGenericPositions(GenericNode<Integer> node, int depth, double xStart,
                                         double totalWidth, Map<INode<Integer>, Point2D> coords,
                                         Map<INode<Integer>, Double> widthCache) {
        double xCenter = xStart + totalWidth / 2.0;
        double y = TOP_PADDING + depth * Y_SPACING;
        coords.put(node, new Point2D(xCenter, y));

        double childX = xStart;
        for (GenericNode<Integer> child : node.getChildrenList()) {
            double childWidth = widthCache.get(child);
            assignGenericPositions(child, depth + 1, childX, childWidth, coords, widthCache);
            childX += childWidth + 40;
        }
    }

    // ========================================================================
    // TARGET EXTRACTION — polymorphic via getChildren()
    // ========================================================================

    private void extractTargets(INode<Integer> node, INode<Integer> parent,
                                 Map<INode<Integer>, Point2D> coords,
                                 Map<String, Point2D> targetCoords,
                                 Map<String, String> targetEdges,
                                 Map<String, NodeStyle> targetStyles,
                                 Set<Integer> highlightSet, Set<Integer> visitedSet) {
        if (node == null) return;

        String key = String.valueOf(node.getValue());
        targetCoords.put(key, coords.get(node));

        // Edge to parent
        if (parent != null) {
            targetEdges.put(key, String.valueOf(parent.getValue()));
        }

        // Style
        boolean isHighlight = highlightSet.contains(node.getValue());
        boolean isVisited = visitedSet.contains(node.getValue());
        targetStyles.put(key, computeStyle(node, isHighlight, isVisited));

        // Recurse children — works for both Binary and Generic via polymorphism
        if (node instanceof BinaryNode<Integer> bn) {
            // Explicit binary traversal to maintain left/right order
            extractTargets(bn.getLeft(), node, coords, targetCoords, targetEdges,
                           targetStyles, highlightSet, visitedSet);
            extractTargets(bn.getRight(), node, coords, targetCoords, targetEdges,
                           targetStyles, highlightSet, visitedSet);
        } else {
            for (INode<Integer> child : node.getChildren()) {
                extractTargets(child, node, coords, targetCoords, targetEdges,
                               targetStyles, highlightSet, visitedSet);
            }
        }
    }

    private NodeStyle computeStyle(INode<Integer> node, boolean isHighlight, boolean isVisited) {
        Color fill, stroke;
        double strokeWidth;

        if (node instanceof RBNode<Integer> rb) {
            // Red-Black Tree: color based on node color
            fill = rb.isRed() ? Color.web("#ef4444") : Color.web("#1f2937");
            stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#10b981") : Color.web("#374151"));
            strokeWidth = isHighlight ? 4.0 : (isVisited ? 3.0 : 2.2);
        } else {
            // Generic / Binary Tree
            fill = darkMode ? Color.web("#334155") : Color.web("#e8eaf0");
            stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : (darkMode ? Color.web("#64748b") : Color.web("#5a616a")));
            strokeWidth = isHighlight ? 4.0 : (isVisited ? 3.0 : 2.0);
        }

        Color textColor;
        if (node instanceof RBNode) {
            textColor = Color.WHITE;
        } else {
            textColor = darkMode ? Color.WHITE : Color.web("#1e293b");
        }

        return new NodeStyle(fill, stroke, strokeWidth, textColor);
    }

    // ========================================================================
    // ANIMATION
    // ========================================================================

    private void animateToTarget(Map<String, Point2D> targetCoords,
                                  Map<String, String> targetEdges,
                                  Map<String, NodeStyle> targetStyles,
                                  Duration dur) {
        ParallelTransition pt = new ParallelTransition();
        Set<String> activeKeys = new HashSet<>(targetCoords.keySet());

        // Remove nodes no longer in tree
        for (String key : new ArrayList<>(nodeGuis.keySet())) {
            if (!activeKeys.contains(key)) {
                Group g = nodeGuis.remove(key);
                FadeTransition ft = new FadeTransition(dur, g);
                ft.setToValue(0);
                ft.setOnFinished(e -> nodeGroup.getChildren().remove(g));
                pt.getChildren().add(ft);
            }
        }

        // Remove edges no longer needed
        for (String key : new ArrayList<>(edgeGuis.keySet())) {
            if (!targetEdges.containsKey(key.split("->")[0])) {
                Line line = edgeGuis.remove(key);
                FadeTransition ft = new FadeTransition(dur, line);
                ft.setToValue(0);
                ft.setOnFinished(e -> edgeGroup.getChildren().remove(line));
                pt.getChildren().add(ft);
            }
        }

        // Add / move nodes
        for (var entry : targetCoords.entrySet()) {
            String key = entry.getKey();
            Point2D pos = entry.getValue();
            NodeStyle style = targetStyles.get(key);

            Group g = nodeGuis.get(key);
            if (g == null) {
                // Create new node
                g = createNodeGroup(key, style);
                g.setTranslateX(pos.getX());
                g.setTranslateY(pos.getY());
                g.setOpacity(0);
                nodeGroup.getChildren().add(g);
                nodeGuis.put(key, g);

                FadeTransition ft = new FadeTransition(dur, g);
                ft.setToValue(1);
                pt.getChildren().add(ft);
            } else {
                // Move existing node
                updateNodeStyle(g, style);

                TranslateTransition tt = new TranslateTransition(dur, g);
                tt.setToX(pos.getX());
                tt.setToY(pos.getY());
                pt.getChildren().add(tt);
            }
        }

        // Add / update edges
        for (var entry : targetEdges.entrySet()) {
            String childKey = entry.getKey();
            String parentKey = entry.getValue();
            String edgeKey = childKey + "->" + parentKey;

            Group childG = nodeGuis.get(childKey);
            Group parentG = nodeGuis.get(parentKey);
            if (childG == null || parentG == null) continue;

            Line line = edgeGuis.get(edgeKey);
            if (line == null) {
                line = new Line();
                line.setStrokeWidth(1.8);
                line.setStroke(darkMode ? Color.web("#64748b") : Color.web("#8a8f98"));
                line.setOpacity(0);
                edgeGroup.getChildren().add(line);
                edgeGuis.put(edgeKey, line);

                FadeTransition ft = new FadeTransition(dur, line);
                ft.setToValue(1);
                pt.getChildren().add(ft);
            }

            // Bind line endpoints to node positions
            line.startXProperty().bind(parentG.translateXProperty());
            line.startYProperty().bind(parentG.translateYProperty());
            line.endXProperty().bind(childG.translateXProperty());
            line.endYProperty().bind(childG.translateYProperty());

            // Edge color based on highlight
            NodeStyle childStyle = targetStyles.get(childKey);
            if (childStyle != null && childStyle.stroke.equals(Color.CYAN)) {
                line.setStroke(Color.CYAN);
                line.setStrokeWidth(2.5);
            } else if (childStyle != null && childStyle.stroke.equals(Color.web("#2ecc71"))) {
                line.setStroke(Color.web("#2ecc71"));
                line.setStrokeWidth(2.2);
            } else {
                line.setStroke(darkMode ? Color.web("#64748b") : Color.web("#8a8f98"));
                line.setStrokeWidth(1.8);
            }
        }

        pt.play();
    }

    private Group createNodeGroup(String label, NodeStyle style) {
        Circle circle = new Circle(NODE_RADIUS);
        circle.setFill(style.fill);
        circle.setStroke(style.stroke);
        circle.setStrokeWidth(style.strokeWidth);

        Text text = new Text(label);
        text.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        text.setFill(style.textColor);
        text.setX(-text.getLayoutBounds().getWidth() / 2);
        text.setY(text.getLayoutBounds().getHeight() / 4);

        return new Group(circle, text);
    }

    private void updateNodeStyle(Group g, NodeStyle style) {
        if (g.getChildren().size() >= 2) {
            Circle circle = (Circle) g.getChildren().get(0);
            circle.setFill(style.fill);
            circle.setStroke(style.stroke);
            circle.setStrokeWidth(style.strokeWidth);

            Text text = (Text) g.getChildren().get(1);
            text.setFill(style.textColor);
        }
    }

    // ========================================================================
    // HELPER
    // ========================================================================

    private record NodeStyle(Color fill, Color stroke, double strokeWidth, Color textColor) {}
}
