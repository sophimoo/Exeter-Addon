package me.sophimoo.exeter.gui.themes.base.utils.enums;

public enum ModuleAnimationMode {
    FADE("Fade"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    SLIDE_UP("Slide Up"),
    SLIDE_DOWN("Slide Down"),
    SMART_SLIDE("Smart Slide");

    private final String title;

    ModuleAnimationMode(String title) { this.title = title; }

    @Override
    public String toString() {
        return title;
    }
}
