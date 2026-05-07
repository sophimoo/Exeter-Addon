package me.sophimoo.exeter.gui.themes.base.utils.enums;

public enum ModuleIndicatorPosition {
    NONE("None"),
    LEFT("Left"),
    RIGHT("Right"),
    TOP("Top"),
    BOTTOM("Bottom");

    private final String title;

    ModuleIndicatorPosition(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
