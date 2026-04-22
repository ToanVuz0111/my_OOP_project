package com.demo3.util;

/**
 * Shared layout tuning constants for TreePanel rendering.
 * Centralised here so both View and any future exporters use the same values.
 */
public final class TreeLayoutConstants {

    /** Radius of each drawn node circle (pixels). */
    public static final double NODE_RADIUS = 22;

    /** Horizontal gap between adjacent in-order positions. */
    public static final double X_SPACING = 72;

    /** Vertical gap between tree levels. */
    public static final double Y_SPACING = 85;

    /** Top padding before root level. */
    public static final double TOP_PADDING = 60;

    private TreeLayoutConstants() { /* utility class */ }
}
