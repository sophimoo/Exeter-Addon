package me.sophimoo.exeter.gui.themes.base.utils;

public enum SliderStyle {
    BottomBar("Bottom Bar"),
    FullBar("Full Bar");

    private final String title;

    SliderStyle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
