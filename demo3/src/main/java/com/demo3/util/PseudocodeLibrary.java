package com.demo3.util;

import com.demo3.model.TreeKind;

/**
 * Stores pseudocode text for each operation on each tree type.
 * The CodePanel displays these lines and highlights the one
 * matching {@code AnimationStep.pseudocodeLine}.
 *
 * All pseudocode is in ENGLISH as requested.
 */
public final class PseudocodeLibrary {

    private PseudocodeLibrary() { /* utility class */ }

    // ========================================================================
    // GENERIC TREE
    // ========================================================================

    private static final String[] GENERIC_INSERT = {
        "1. Start inserting newValue",               // line 1
        "2. Check if newValue already exists",       // line 2
        "3. If tree is empty → newValue becomes root", // line 3
        "4. Search for parentValue in tree (BFS)",   // line 4
        "5. Parent found / not found",               // line 5
        "6. Attach newValue as child of parent"      // line 6
    };

    private static final String[] GENERIC_DELETE = {
        "1. Search for node to delete (BFS)",
        "2. Node found / not found",
        "3. Remove node (and its subtree) from parent"
    };

    private static final String[] GENERIC_UPDATE = {
        "1. Search for node with oldValue",
        "2. Node found / not found; check duplicate",
        "3. Update value: oldValue → newValue"
    };

    // ========================================================================
    // BINARY TREE (pure — not BST)
    // ========================================================================

    private static final String[] BINARY_INSERT = {
        "1. Start inserting newValue",
        "2. Check if newValue already exists",
        "3. If tree is empty → newValue becomes root",
        "4. Search for parentValue in tree (BFS)",
        "5. Parent found / not found",
        "6. Attach newValue as left or right child",
        "7. Error: parent already has 2 children"
    };

    private static final String[] BINARY_DELETE = {
        "1. BFS to find target node and deepest node",
        "2. Target node found / not found",
        "3. Identify the deepest (last) node",
        "4. Copy deepest node's value to target position",
        "5. Remove the deepest node from tree"
    };

    private static final String[] BINARY_UPDATE = {
        "1. Search for node with oldValue",
        "2. Node found / not found; check duplicate",
        "3. Update value: oldValue → newValue"
    };

    // ========================================================================
    // RED-BLACK TREE
    // ========================================================================

    private static final String[] RB_INSERT = {
        "1. Start BST insert for newValue",
        "2. Compare newValue with current node",
        "3. Go left / go right",
        "4. Duplicate found — abort",
        "5. Place newValue at found position",
        "6. Begin insertFixup (rebalance)",
        "7. insertFixup complete — tree balanced",
        "8. Case 1: Uncle is RED → recolor",
        "9. Case 2: Triangle → rotate parent",
        "10. Case 3: Line → recolor + rotate grandparent"
    };

    private static final String[] RB_DELETE = {
        "1. Search for node to delete (BST search)",
        "2. Node found / not found",
        "3. Determine replacement strategy",
        "4. Replace with successor",
        "5. Begin deleteFixup (rebalance)",
        "6. deleteFixup complete — tree balanced"
    };

    private static final String[] RB_UPDATE = {
        "1. Update = delete(oldValue) + insert(newValue)",
        "2. Check if newValue already exists",
        "3. Update complete: oldValue → newValue"
    };

    // ========================================================================
    // SHARED (same for all tree types)
    // ========================================================================

    private static final String[] TRAVERSE = {
        "1. Start traversal (BFS or DFS)",
        "2. Visit current node",
        "3. Traversal complete"
    };

    private static final String[] SEARCH = {
        "1. Start searching for value",
        "2. Compare with current node",
        "3. Result: found / not found"
    };

    private static final String[] CREATE = {
        "1. Clear all nodes from tree",
        "2. Tree is now empty"
    };

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    public static String[] getCode(TreeKind kind, String operation) {
        return switch (operation.toUpperCase()) {
            case "CREATE"   -> CREATE;
            case "INSERT"   -> getInsertCode(kind);
            case "DELETE"   -> getDeleteCode(kind);
            case "UPDATE"   -> getUpdateCode(kind);
            case "TRAVERSE" -> TRAVERSE;
            case "SEARCH"   -> kind == TreeKind.RED_BLACK_TREE ? RB_SEARCH() : SEARCH;
            default         -> new String[]{"(no pseudocode)"};
        };
    }

    private static String[] getInsertCode(TreeKind kind) {
        return switch (kind) {
            case GENERIC_TREE   -> GENERIC_INSERT;
            case BINARY_TREE    -> BINARY_INSERT;
            case RED_BLACK_TREE -> RB_INSERT;
        };
    }

    private static String[] getDeleteCode(TreeKind kind) {
        return switch (kind) {
            case GENERIC_TREE   -> GENERIC_DELETE;
            case BINARY_TREE    -> BINARY_DELETE;
            case RED_BLACK_TREE -> RB_DELETE;
        };
    }

    private static String[] getUpdateCode(TreeKind kind) {
        return switch (kind) {
            case GENERIC_TREE   -> GENERIC_UPDATE;
            case BINARY_TREE    -> BINARY_UPDATE;
            case RED_BLACK_TREE -> RB_UPDATE;
        };
    }

    /** RBTree search has step-by-step BST comparison. */
    private static String[] RB_SEARCH() {
        return new String[]{
            "1. Start BST search for value",
            "2. Compare with current node, go left/right",
            "3. Result: found / not found"
        };
    }
}
