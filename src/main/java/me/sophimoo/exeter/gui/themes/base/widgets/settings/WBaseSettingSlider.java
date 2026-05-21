package me.sophimoo.exeter.gui.themes.base.widgets.settings;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.utils.enums.SliderStyle;
import me.sophimoo.exeter.gui.themes.base.utils.WidgetSizeDebug;
import me.sophimoo.exeter.gui.themes.base.widgets.input.WBaseTextBox;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class WBaseSettingSlider extends WPressable implements BaseWidget {
     private static WBaseSettingSlider currentlyEditingSlider = null;
 
     private final IntSetting intSetting;
     private final DoubleSetting doubleSetting;
     private final String title;

    private final double min;
    private final double max;
    private final int decimalPlaces;
    private final boolean wholeNumbers;

    private double titleWidth;
    private double valueWidth;

    private boolean dragging;
    private boolean editing;
    private WTextBox editingTextBox;

    private final MarqueeState marquee = new MarqueeState();
    private double animationProgress;

    public WBaseSettingSlider(IntSetting setting) {
        this.intSetting = setting;
        this.doubleSetting = null;
        this.title = setting.title;
        this.tooltip = setting.description;

        this.min = setting.sliderMin;
        this.max = setting.sliderMax;
        this.decimalPlaces = 0;
        this.wholeNumbers = true;
    }

    public WBaseSettingSlider(DoubleSetting setting) {
        this.intSetting = null;
        this.doubleSetting = setting;
        this.title = setting.title;
        this.tooltip = setting.description;

        this.min = setting.sliderMin;
        this.max = setting.sliderMax;
        this.decimalPlaces = setting.decimalPlaces;
        this.wholeNumbers = false;
    }

    @Override
    public double pad() {
        return theme().rowPadX();
    }

    @Override
    protected void onCalculateSize() {
        if (titleWidth == 0) titleWidth = theme().textWidth(title);
        valueWidth = Math.max(theme().textWidth(formatMinValue()), theme().textWidth(formatMaxValue()));

        double pad = pad();
        width = pad + titleWidth + pad + valueWidth + pad;
        height = itemRowBaseHeight(0);
    }

    @Override
    public boolean onMouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        if (editing) {
            syncEditingTextBoxLayout();
            if (editingTextBox != null) {
                editingTextBox.mouseMoved(click.x(), click.y(), click.x(), click.y());
                if (editingTextBox.mouseClicked(click, doubled)) return true;
            }

            if (!mouseOver) {
                stopEditing();
                return false;
            }
            return true;
        }

        if (mouseOver && click.button() == GLFW_MOUSE_BUTTON_LEFT) {
            dragging = true;
            setFocused(true);
            setFromMouse(click.x());
            return true;
        }

        if (mouseOver && click.button() == GLFW_MOUSE_BUTTON_RIGHT) {
            startEditing();
            return true;
        }

        return false;
    }

    @Override
    public boolean onMouseReleased(net.minecraft.client.gui.Click click) {
        if (editing) {
            if (editingTextBox != null) return editingTextBox.mouseReleased(click);
            return true;
        }

        if (dragging) {
            dragging = false;
            setFocused(false);
            return true;
        }

        return false;
    }

    @Override
    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        super.onMouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
        if (editing) {
            syncEditingTextBoxLayout();
            if (editingTextBox != null) editingTextBox.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
            return;
        }

        if (dragging) setFromMouse(mouseX);
    }

    @Override
    public boolean onKeyPressed(net.minecraft.client.input.KeyInput input) {
        if (editing) {
            if (input.key() == GLFW_KEY_ESCAPE) {
                cancelEditing();
                return true;
            }

            if (input.key() == GLFW_KEY_ENTER || input.key() == GLFW_KEY_KP_ENTER) {
                stopEditing();
                return true;
            }

            return editingTextBox != null && editingTextBox.keyPressed(input);
        }

        return false;
    }

    @Override
    public boolean onKeyRepeated(net.minecraft.client.input.KeyInput input) {
        if (editing) return editingTextBox != null && editingTextBox.keyRepeated(input);
        return false;
    }

    @Override
    public boolean onCharTyped(net.minecraft.client.input.CharInput input) {
        if (editing) {
            return editingTextBox != null && editingTextBox.charTyped(input);
        }
        return false;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (editing) {
            syncEditingTextBoxLayout();
            if (editingTextBox != null) {
                // Clip textbox rendering to the slider row bounds
                renderer.scissorStart(x, y, width, height);
                editingTextBox.render(renderer, mouseX, mouseY, delta);
                renderer.scissorEnd();
            }
            return;
        }

        WidgetSizeDebug.log(
            theme(),
            this,
            "SettingSlider",
            width,
            height,
            String.format(
                Locale.US,
                "padX=%.2f padY=%.2f globalPad=%.2f rowPadX=%.2f rowPadY=%.2f",
                pad(),
                theme().rowPadY(),
                theme().pad(),
                theme().rowPadX(),
                theme().rowPadY()
            )
        );

        boolean hoveredForAnimation = mouseOver || dragging;
        RowAnimationState animationState = animateRow(delta, hoveredForAnimation, hoveredForAnimation, false, animationProgress, 0);
        animationProgress = animationState.primaryProgress();

        RowSurfaceStyle surfaceStyle = itemRowSurfaceStyle(false, pressed, mouseOver);
        renderRowSurface(
            renderer,
            x,
            y,
            width,
            height,
            animationState.effectiveAnimationMode(),
            localHoverSurfaceProgress(animationProgress),
            0,
            surfaceStyle
        );
        renderInterpolationHover(renderer, x, y, width, height, hoveredForAnimation, delta, surfaceStyle);

        double pad = pad();
        double textHeight = theme().textHeight();
        SliderStyle sliderStyle = theme().sliderStyle.get();
        double barSpace = sliderStyle == SliderStyle.FULL_BAR ? 0 : pad;
        double textY = y + (height - barSpace - textHeight) / 2;
        Color textColor = resolveModuleTextColor(animationProgress);

        double progress = (getValue() - min) / (max - min);
        progress = MathHelper.clamp(progress, 0, 1);

        double barStartX = getBarStartX();
        double barWidth = getBarWidth();
        double barY = getBarY();
        double barHeight = getBarHeight();
        double filled = barWidth * progress;

        renderer.quad(barStartX, barY, barWidth, barHeight, theme().sliderDirection.get("right-"));
        renderer.quad(barStartX, barY, filled, barHeight, theme().sliderDirection.get("left-"));

        String valueText = formatValue();
          double actualValueWidth = theme().textWidth(valueText);
          double valueX = x + width - pad - actualValueWidth;
          renderText(renderer, valueText, valueX, textY, textColor);

        double titleAreaX = x + pad;
        double titleAreaW = Math.max(0, valueX - pad - titleAreaX);
        RowTextLayout layout = resolveRowTextLayout(
            titleAreaX,
            y,
            titleAreaW,
            height,
            titleWidth,
            meteordevelopment.meteorclient.gui.utils.AlignmentX.Left,
            meteordevelopment.meteorclient.gui.utils.AlignmentY.Center
        );

        renderRowTitle(
            renderer,
            marquee,
            title,
            titleWidth,
            delta,
            mouseOver,
            true,
            textColor,
            animationProgress,
            new RowTextLayout(layout.areaX(), layout.areaY(), layout.areaWidth(), layout.areaHeight(), textY, layout.staticTextX())
        );
    }

    private void setFromMouse(double mouseX) {
        double barStartX = getBarStartX();
        double barWidth = getBarWidth();

        double progress = (mouseX - barStartX) / barWidth;
        progress = MathHelper.clamp(progress, 0, 1);
        double raw = min + (max - min) * progress;

        if (wholeNumbers) {
            int rounded = (int) Math.round(raw);
            intSetting.set(MathHelper.clamp(rounded, (int) min, (int) max));
        } else {
            double clamped = MathHelper.clamp(raw, min, max);
            doubleSetting.set(clamped);
        }
    }

    private double getBarStartX() {
        if (theme().sliderStyle.get() == SliderStyle.FULL_BAR) return x + theme().sliderInset();
        return x + pad();
    }

    private double getBarWidth() {
        double endX = getBarEndX();
        return Math.max(theme().sliderMinTrackWidth(), endX - getBarStartX());
    }

    private double getBarEndX() {
        if (theme().sliderStyle.get() == SliderStyle.FULL_BAR) return x + width - theme().sliderInset();
        return x + width - pad();
    }

    private double getBarY() {
        if (theme().sliderStyle.get() == SliderStyle.FULL_BAR) return y + theme().sliderInset();
        return y + height - theme().sliderBottomGap();
    }

    private double getBarHeight() {
        if (theme().sliderStyle.get() == SliderStyle.FULL_BAR) {
            double inset = theme().sliderInset();
            return Math.max(theme().sliderFullBarMinTrackHeight(), height - inset * 2);
        }
        return theme().sliderTrackHeight();
    }

    private double getValue() {
        if (wholeNumbers) return intSetting.get();
        return doubleSetting.get();
    }

    private String formatValue() {
        if (wholeNumbers) return Integer.toString(intSetting.get());
        return String.format(Locale.US, "%." + decimalPlaces + "f", doubleSetting.get());
    }

    private String formatMaxValue() {
        if (wholeNumbers) return Integer.toString((int) max);
        return String.format(Locale.US, "%." + decimalPlaces + "f", max);
    }

    private String formatMinValue() {
        if (wholeNumbers) return Integer.toString((int) min);
        return String.format(Locale.US, "%." + decimalPlaces + "f", min);
    }

    private void startEditing() {
        if (currentlyEditingSlider != null && currentlyEditingSlider != this) {
            currentlyEditingSlider.stopEditing();
        }
        currentlyEditingSlider = this;

        editing = true;
        editingTextBox = theme().textBox(formatValue(), wholeNumbers ? this::isIntChar : this::isDoubleChar);
        if (editingTextBox instanceof WBaseTextBox baseTextBox) baseTextBox.setTextAnchorBottomLeft(true);
        editingTextBox.calculateSize();
        editingTextBox.calculateWidgetPositions();
        syncEditingTextBoxLayout();
        editingTextBox.setCursorMax();
        editingTextBox.setFocused(true);
        setFocused(true);
    }

    private void stopEditing() {
        if (!editing) return;

        if (editingTextBox != null) applyEditingValue(editingTextBox.get());
        editing = false;
        if (currentlyEditingSlider == this) currentlyEditingSlider = null;
        if (editingTextBox != null) editingTextBox.setFocused(false);
        setFocused(false);
        editingTextBox = null;
    }

    private void cancelEditing() {
        if (!editing) return;

        editing = false;
        if (currentlyEditingSlider == this) currentlyEditingSlider = null;
        if (editingTextBox != null) editingTextBox.setFocused(false);
        setFocused(false);
        editingTextBox = null;
    }

    private void applyEditingValue(String value) {
        if (value == null || value.isBlank() || value.equals("-") || value.equals(".")) return;

        try {
            if (wholeNumbers) {
                int parsed = Integer.parseInt(value.trim());
                intSetting.set(MathHelper.clamp(parsed, intSetting.min, intSetting.max));
            } else {
                double parsed = Double.parseDouble(value.trim());
                doubleSetting.set(MathHelper.clamp(parsed, doubleSetting.min, doubleSetting.max));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void syncEditingTextBoxLayout() {
         if (editingTextBox == null) return;

        // calculateSize() is not called cuz it would take control
        editingTextBox.x = x;
        editingTextBox.y = y;
        editingTextBox.width = width;
        editingTextBox.height = height;
    }

    private boolean isIntChar(String text, char c) {
        return Character.isDigit(c) || (c == '-' && text.isEmpty());
    }

    private boolean isDoubleChar(String text, char c) {
        if (Character.isDigit(c)) return true;
        if (c == '-' && text.isEmpty()) return true;
        return c == '.' && !text.contains(".");
    }
}
