package com.demo3.util;

import com.demo3.model.binary.BinaryNode;
import com.demo3.model.core.INode;
import com.demo3.model.core.ITreeOperations;
import com.demo3.model.redblack.RBNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Computes statistics from any tree via INode polymorphism.
 * Works for Generic Tree, Binary Tree, and Red-Black Tree.
 */
public final class TreeStats {

    private TreeStats() { /* utility class */ }

    /** Total number of nodes in the tree. */
    public static int nodeCount(ITreeOperations<?> tree) {
        return countNodes(tree.getRoot());
    }

    /** Height of the tree (longest root-to-leaf path). Empty tree = 0, single node = 1. */
    public static int height(ITreeOperations<?> tree) {
        return computeHeight(tree.getRoot());
    }

    /**
     * Balance factor for Binary Trees = height(left) - height(right).
     * Only meaningful for BinaryNode-based trees. Returns 0 for empty/generic trees.
     */
    public static int balanceFactor(ITreeOperations<?> tree) {
        INode<?> root = tree.getRoot();
        if (!(root instanceof BinaryNode<?> bn)) return 0;
        int leftH = computeHeight(bn.getLeft());
        int rightH = computeHeight(bn.getRight());
        return leftH - rightH;
    }

    /**
     * Black-height for Red-Black Trees (number of black nodes from root to any leaf).
     * Returns -1 if not a valid RBTree or tree is empty.
     */
    public static int blackHeight(ITreeOperations<?> tree) {
        INode<?> root = tree.getRoot();
        if (root == null) return 0;
        if (!(root instanceof RBNode<?>)) return -1;
        return computeBlackHeight((RBNode<?>) root);
    }

    /** Number of leaf nodes (nodes with no children). */
    public static int leafCount(ITreeOperations<?> tree) {
        return countLeaves(tree.getRoot());
    }

    /**
     * Width of the tree (maximum number of nodes at any level).
     */
    public static int maxWidth(ITreeOperations<?> tree) {
        INode<?> root = tree.getRoot();
        if (root == null) return 0;

        Queue<INode<?>> queue = new LinkedList<>();
        queue.add(root);
        int maxW = 0;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            maxW = Math.max(maxW, levelSize);
            for (int i = 0; i < levelSize; i++) {
                INode<?> cur = queue.poll();
                List<? extends INode<?>> children = cur.getChildren();
                if (children != null) queue.addAll(children);
            }
        }
        return maxW;
    }

    // ========================================================================
    // PRIVATE HELPERS — recursive, uses getChildren() polymorphism
    // ========================================================================

    private static int countNodes(INode<?> node) {
        if (node == null) return 0;
        int count = 1;
        List<? extends INode<?>> children = node.getChildren();
        if (children != null) {
            for (INode<?> child : children) {
                count += countNodes(child);
            }
        }
        return count;
    }

    private static int computeHeight(INode<?> node) {
        if (node == null) return 0;
        int maxChildH = 0;
        List<? extends INode<?>> children = node.getChildren();
        if (children != null) {
            for (INode<?> child : children) {
                maxChildH = Math.max(maxChildH, computeHeight(child));
            }
        }
        return 1 + maxChildH;
    }

    private static int countLeaves(INode<?> node) {
        if (node == null) return 0;
        List<? extends INode<?>> children = node.getChildren();
        if (children == null || children.isEmpty()) return 1;
        int count = 0;
        for (INode<?> child : children) {
            count += countLeaves(child);
        }
        return count;
    }

    private static int computeBlackHeight(RBNode<?> node) {
        if (node == null) return 1; // NIL nodes are black
        int leftBH = computeBlackHeight((RBNode<?>) node.getLeft());
        return leftBH + (node.isRed() ? 0 : 1);
    }
}
