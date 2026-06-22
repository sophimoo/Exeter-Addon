package me.sophimoo.exeter.gui.themes.base.widgets.settings;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.utils.WidgetSizeDebug;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class WBaseSettingToggle extends WPressable implements BaseWidget {
    private final String title;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private final boolean showIndicator;

    private double titleWidth;

    private final MarqueeState marquee = new MarqueeState();
    private double animationProgress;
    private double hoverOverlayProgress;

    public WBaseSettingToggle(BoolSetting setting) {
        this(setting.title, setting.description, setting::get, setting::set, true);
    }

    protected WBaseSettingToggle(String title, String tooltip, BooleanSupplier getter, Consumer<Boolean> setter, boolean showIndicator) {
        this.title = title;
        this.tooltip = tooltip;
        this.getter = getter;
        this.setter = setter;
        this.showIndicator = showIndicator;
        this.animationProgress = getter.getAsBoolean() ? 1.0 : 0.0;
        this.hoverOverlayProgress = 0.0;
    }

    @Override
    protected void onCalculateSize() {
        if (titleWidth == 0) titleWidth = theme().textWidth(title);

        double padX = pad();
        double padY = theme().rowPadY();
        width = padX + titleWidth + padX;
        height = resolveItemRowHeight(padY + theme().textHeight() + padY);
    }

    @Override
    protected void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) setter.accept(!getter.getAsBoolean());
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        boolean active = getter.getAsBoolean();
        logDebugInfo();
        RowAnimationState animationState = animateRow(
            delta,
            mouseOver,
            active || localHoverAnimationVisible(mouseOver),
            mouseOver,
            animationProgress,
            hoverOverlayProgress
        );
        animationProgress = animationState.primaryProgress();
        hoverOverlayProgress = animationState.hoverProgress();

        renderBackgroundLayers(renderer, active, animationState, delta);
        renderTitle(renderer, delta);

        if (active && showIndicator) renderIndicator(renderer);
    }

    private void logDebugInfo() {
        WidgetSizeDebug.log(
            theme(),
            this,
            "SettingToggle",
            width,
            height,
            String.format(
                java.util.Locale.US,
                "padX=%.2f padY=%.2f globalPad=%.2f rowPadX=%.2f rowPadY=%.2f",
                pad(),
                theme().rowPadY(),
                theme().pad(),
                theme().rowPadX(),
                theme().rowPadY()
            )
        );
    }

    private void renderBackgroundLayers(GuiRenderer renderer, boolean active, RowAnimationState animationState, double delta) {
        RowSurfaceStyle surfaceStyle = itemRowSurfaceStyle(active, pressed, mouseOver);
        renderRowSurface(
            renderer,
            x,
            y,
            width,
            height,
            animationState.effectiveAnimationMode(),
            animationProgress,
            active ? localHoverSurfaceProgress(hoverOverlayProgress) : 0,
            surfaceStyle
        );
        renderInterpolationHover(renderer, x, y, width, height, mouseOver, delta, surfaceStyle);
    }

    private void renderTitle(GuiRenderer renderer, double delta) {
        double padX = pad();
        Color textColor = resolveSettingsTextColor(animationProgress, hoverOverlayProgress);

        double titleAreaX = x + padX;
        double titleAreaW = Math.max(0, width - padX * 2);
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
            hoverOverlayProgress,
            layout
        );
    }

    private void renderIndicator(GuiRenderer renderer) {
        renderRowIndicator(renderer, x, y, width, height, 1, moduleIndicatorStyle(theme().accentColor.get()));
    }
}
