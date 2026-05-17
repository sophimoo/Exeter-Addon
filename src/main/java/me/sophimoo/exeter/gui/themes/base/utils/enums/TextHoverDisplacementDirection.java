package me.sophimoo.exeter.gui.themes.base.utils.enums;

public enum TextHoverDisplacementDirection {
    LEFT("Left"),
    RIGHT("Right"),
    UP("Up"),
    DOWN("Down");

    private final String title;

    TextHoverDisplacementDirection(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
