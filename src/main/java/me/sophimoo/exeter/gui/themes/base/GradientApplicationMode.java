package me.sophimoo.exeter.gui.themes.base;

/**
 * Controls which module states the gradient fade-in should apply to.
 */
public enum GradientApplicationMode {
    INACTIVE("Inactive"),
    ACTIVE("Active"),
    BOTH("Both");

    private final String name;

    GradientApplicationMode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Check if the gradient should be applied to inactive modules.
     */
    public boolean appliesToInactive() {
        return this == INACTIVE || this == BOTH;
    }

    /**
     * Check if the gradient should be applied to active modules.
     */
    public boolean appliesToActive() {
        return this == ACTIVE || this == BOTH;
    }
}
