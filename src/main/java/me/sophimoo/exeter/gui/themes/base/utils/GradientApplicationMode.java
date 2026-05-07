package me.sophimoo.exeter.gui.themes.base.utils;

public enum GradientApplicationMode {
    INACTIVE,
    ACTIVE,
    BOTH;

    @Override
    public String toString() {
        return switch (this) {
            case INACTIVE -> "Inactive";
            case ACTIVE -> "Active";
            case BOTH -> "Both";
        };
    }

    public boolean shouldApply(boolean active) {
        return this == BOTH || (active ? this == ACTIVE : this == INACTIVE);
    }
}
