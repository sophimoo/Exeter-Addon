package me.sophimoo.exeter.gui.themes.base.utils.enums;

public enum ModuleSettingsIndicator {
    NONE("None"),
    DROPDOWN("Dropdown"),
    EXETER("Exeter"),
    METEOR("Meteor");

    private final String title;

    ModuleSettingsIndicator(String title) { this.title = title; }

    @Override
    public String toString() {
        return title;
    }
}
