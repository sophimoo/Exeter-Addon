package me.sophimoo.exeter.gui.themes.base.utils.enums;

public enum SelectionRenderingMode {
    FADE("Fade"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    SLIDE_UP("Slide Up"),
    SLIDE_DOWN("Slide Down"),
    INTERPOLATE("Interpolate");

    private final String title;

    SelectionRenderingMode(String title) { this.title = title; }

    @Override
    public String toString() {
        return title;
    }
}
