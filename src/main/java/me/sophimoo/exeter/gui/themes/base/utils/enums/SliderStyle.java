package me.sophimoo.exeter.gui.themes.base.utils.enums;

public enum SliderStyle {
    BOTTOM_BAR("Bottom Bar"),
    FULL_BAR("Full Bar");

    private final String title;

    SliderStyle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
