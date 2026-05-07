package me.sophimoo.exeter.gui.themes.base.utils.enums;

public enum ModuleGradientDirection {
    NONE("None"),
    HORIZONTAL_LEFT_TO_RIGHT("Horizontal (Left to Right)"),
    HORIZONTAL_RIGHT_TO_LEFT("Horizontal (Right to Left)"),
    VERTICAL_TOP_TO_BOTTOM("Vertical (Top to Bottom)"),
    VERTICAL_BOTTOM_TO_TOP("Vertical (Bottom to Top)");

    private final String title;

    ModuleGradientDirection(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
