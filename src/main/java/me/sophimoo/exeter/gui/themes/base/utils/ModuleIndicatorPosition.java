package me.sophimoo.exeter.gui.themes.base.utils;

public enum ModuleIndicatorPosition {
    None("None"),
    Left("Left"),
    Right("Right"),
    Top("Top"),
    Bottom("Bottom");

    private final String title;

    ModuleIndicatorPosition(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
