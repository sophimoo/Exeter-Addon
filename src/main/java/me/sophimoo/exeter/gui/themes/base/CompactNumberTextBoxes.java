package me.sophimoo.exeter.gui.themes.base;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public final class CompactNumberTextBoxes {
    private CompactNumberTextBoxes() {
    }

    public static WTextBox create(GuiTheme theme, String value, CharFilter filter, Consumer<WTextBox> commit) {
        WTextBox textBox = theme.textBox(value, filter);
        textBox.action = () -> updateWidth(theme, textBox);
        textBox.actionOnUnfocused = () -> {
            commit.accept(textBox);
            updateWidth(theme, textBox);
        };
        updateWidth(theme, textBox);
        return textBox;
    }

    public static boolean isIntChar(String text, char c) {
        return Character.isDigit(c) || (c == '-' && text.isEmpty());
    }

    public static boolean isDoubleChar(String text, char c) {
        if (Character.isDigit(c)) return true;
        if (c == '-' && text.isEmpty()) return true;
        return c == '.' && !text.contains(".");
    }

    private static void updateWidth(GuiTheme theme, WTextBox textBox) {
        String value = textBox.get();
        if (value == null || value.isEmpty()) value = "0";

        double contentWidth = theme.textWidth(value);
        double desiredWidth = contentWidth + textBox.pad() * 2 + theme.scale(6);
        double minWidth = theme.scale(40);
        double maxWidth = maxWidth(theme);

        double newMinWidth = MathHelper.clamp(desiredWidth, minWidth, maxWidth);
        if (Math.abs(textBox.minWidth - newMinWidth) > 0.001) {
            textBox.minWidth = newMinWidth;
            textBox.invalidate();
        }
    }

    private static double maxWidth(GuiTheme theme) {
        double settingsWidth = Utils.getWindowWidth() * 0.75;

        if (theme instanceof BaseGuiTheme baseTheme && baseTheme.fixedCategorySize.get()) {
            settingsWidth = baseTheme.scale(baseTheme.fixedCategoryWidth.get());
        }

        return Math.max(theme.scale(40), settingsWidth / 2);
    }
}
