package me.sophimoo.exeter.gui.themes.base;

public enum ModuleAnimationMode {
    FADE("Fade"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    SLIDE_UP("Slide Up"),
    SLIDE_DOWN("Slide Down"),
    SMART_SLIDE("Smart Slide");

    private final String name;

    ModuleAnimationMode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
