package com.demo3.util;

import java.util.List;

/**
 * String formatting helpers for UI labels.
 * Generic version — works with any Comparable type, not just Integer.
 */
public final class ListFormatUtil {

    private ListFormatUtil() { /* utility class */ }

    /**
     * Formats values as {@code a → b → c} for traversal/visit-order display.
     *
     * @param values list of values to format
     * @param <T>    value type
     * @return formatted string, or "(empty)" if the list is null/empty
     */
    public static <T> String joinArrowSeparated(List<T> values) {
        if (values == null || values.isEmpty()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(values.get(i));
        }
        return sb.toString();
    }
}
