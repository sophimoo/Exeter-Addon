package me.sophimoo.exeter.gui.themes.base.utils.enums;

public enum GradientApplicationMode {
    INACTIVE("Inactive"),
    ACTIVE("Active"),
    BOTH("Both");

    private final String title;

    GradientApplicationMode(String title) { this.title = title; }

    @Override
    public String toString() {
        return title;
    }

    public boolean shouldApply(boolean active) {
        return this == BOTH || (active ? this == ACTIVE : this == INACTIVE);
    }
}
