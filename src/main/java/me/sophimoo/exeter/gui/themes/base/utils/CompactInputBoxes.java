package me.sophimoo.exeter.gui.themes.base.utils;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public final class CompactInputBoxes {
    private CompactInputBoxes() {
    }

    public static WTextBox createInt(GuiTheme theme, String value, Consumer<WTextBox> commit) {
        return createNumber(theme, value, CompactInputBoxes::isIntChar, commit);
    }

    public static WTextBox createDecimal(GuiTheme theme, String value, Consumer<WTextBox> commit) {
        return createNumber(theme, value, CompactInputBoxes::isDecimalChar, commit);
    }

    public static WTextBox createString(
        GuiTheme theme,
        String value,
        String placeholder,
        CharFilter filter,
        Class<? extends WTextBox.Renderer> renderer,
        Consumer<WTextBox> update
    ) {
        WTextBox textBox = theme.textBox(value, placeholder, filter, renderer);
        textBox.action = () -> {
            update.accept(textBox);
            updateWidth(theme, textBox);
        };
        updateWidth(theme, textBox);
        return textBox;
    }

    private static WTextBox createNumber(GuiTheme theme, String value, CharFilter filter, Consumer<WTextBox> commit) {
        WTextBox textBox = theme.textBox(value, filter);
        textBox.action = () -> updateWidth(theme, textBox);
        textBox.actionOnUnfocused = () -> {
            commit.accept(textBox);
            updateWidth(theme, textBox);
        };
        updateWidth(theme, textBox);
        return textBox;
    }

    private static boolean isIntChar(String text, char c) {
        return Character.isDigit(c) || (c == '-' && text.isEmpty());
    }

    private static boolean isDecimalChar(String text, char c) {
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
