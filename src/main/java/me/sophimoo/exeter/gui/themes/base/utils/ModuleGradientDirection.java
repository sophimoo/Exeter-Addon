package me.sophimoo.exeter.gui.themes.base.utils;

public enum ModuleGradientDirection {
    None("None"),
    Horizontal_LeftToRight("Horizontal (Left to Right)"),
    Horizontal_RightToLeft("Horizontal (Right to Left)"),
    Vertical_TopToBottom("Vertical (Top to Bottom)"),
    Vertical_BottomToTop("Vertical (Bottom to Top)");

    private final String title;

    ModuleGradientDirection(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
