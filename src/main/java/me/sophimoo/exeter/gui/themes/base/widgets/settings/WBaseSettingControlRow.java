package me.sophimoo.exeter.gui.themes.base.widgets.settings;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.utils.WidgetSizeDebug;
import me.sophimoo.exeter.gui.themes.base.widgets.pressable.WBaseColorButton;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseSettingControlRow extends WContainer implements BaseWidget {
    private static final double CONTROL_DOMINANCE_VERTICAL_THRESHOLD = 0.5;

    private final String title;
    private final WWidget control;
    private final boolean alwaysHorizontalLayout;
    private final boolean forceVerticalLayout;

    private double titleWidth;
    private boolean verticalLayout;

    private final MarqueeState marquee = new MarqueeState();
    private double animationProgress;

    public WBaseSettingControlRow(String title, String tooltip, WWidget control) {
        this(title, tooltip, control, false);
    }

    public WBaseSettingControlRow(String title, String tooltip, WWidget control, boolean forceVerticalLayout) {
        this.title = title;
        this.tooltip = tooltip;
        this.control = control;
        this.alwaysHorizontalLayout = control instanceof WBaseColorButton;
        this.forceVerticalLayout = forceVerticalLayout;
    }

    @Override
    public void init() {
        if (forceVerticalLayout) add(control).expandX();
        else add(control);
    }

    @Override
    protected void onCalculateSize() {
        if (titleWidth == 0) titleWidth = theme().textWidth(title);

        double padX = pad();
        double padY = theme().rowPadY();
        double controlWidth = control.width;
        double horizontalWidth = padX + titleWidth + padX + controlWidth + padX;

        verticalLayout = shouldUseVerticalLayout(horizontalWidth, controlWidth);

        if (verticalLayout) {
            width = Math.max(titleWidth, control.width) + padX * 2;
            height = padY + theme().textHeight() + padY + control.height + padY;
        } else {
            width = horizontalWidth;
            double textHeight = theme().textHeight();
            height = resolveItemRowHeight(Math.max(control.height, textHeight + padY * 2));
        }
    }

    @Override
    protected void onCalculateWidgetPositions() {
        if (cells.isEmpty()) return;

        double padX = pad();
        double padY = theme().rowPadY();
        Cell<?> cell = cells.get(0);
        WWidget widget = cell.widget();

        if (verticalLayout) {
            cell.x = x + padX;
            cell.y = y + padY + theme().textHeight() + padY;
            cell.width = Math.max(0, width - padX * 2);
        } else {
            double controlX = x + width - padX - widget.width;
            double controlY = y + (height - widget.height) / 2;
            cell.x = controlX;
            cell.y = controlY;
            cell.width = widget.width;
        }
        cell.height = widget.height;
        cell.alignWidget();
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        WidgetSizeDebug.log(
            theme(),
            this,
            verticalLayout ? "SettingControlRow(vertical)" : "SettingControlRow(horizontal)",
            width,
            height,
            String.format(
                java.util.Locale.US,
                "padX=%.2f padY=%.2f globalPad=%.2f rowPadX=%.2f rowPadY=%.2f controlW=%.2f controlH=%.2f",
                pad(),
                theme().rowPadY(),
                theme().pad(),
                theme().rowPadX(),
                theme().rowPadY(),
                control.width,
                control.height
            )
        );

        RowAnimationState animationState = animateRow(delta, mouseOver, mouseOver, false, animationProgress, 0);
        animationProgress = animationState.primaryProgress();

        RowSurfaceStyle surfaceStyle = itemRowSurfaceStyle(false, false, mouseOver);
        renderRowSurface(
            renderer,
            x,
            y,
            width,
            height,
            animationState.effectiveAnimationMode(),
            0,
            localHoverSurfaceProgress(animationProgress),
            surfaceStyle
        );
        renderInterpolationHover(renderer, x, y, width, height, mouseOver, delta, surfaceStyle);

        double padX = pad();
        double padY = theme().rowPadY();
        Color textColor = resolveSettingsTextColor(animationProgress);
        double textHeight = theme().textHeight();

        if (verticalLayout) {
            double titleAreaX = x + padX;
            double titleAreaW = width - padX * 2;
            double controlHeight = control.height;
            double titleAreaHeight = height - padY - controlHeight - padY;
            double textY = y + padY + (titleAreaHeight - textHeight) / 2;
            renderTitle(renderer, delta, textColor, textY, titleAreaX, titleAreaW);
        } else {
            double controlX = x + width - padX - control.width;
            double titleAreaX = x + padX;
            double titleAreaW = Math.max(0, controlX - padX - titleAreaX);
            double textY = y + (height - textHeight) / 2;
            renderTitle(renderer, delta, textColor, textY, titleAreaX, titleAreaW);
        }
    }

    private boolean shouldUseVerticalLayout(double horizontalWidth, double controlWidth) {
        if (forceVerticalLayout) return true;
        if (alwaysHorizontalLayout) return false;

        double safeReferenceWidth = Math.max(1, layoutReferenceWidth(horizontalWidth));
        double controlRatio = controlWidth / safeReferenceWidth;
        return controlRatio > CONTROL_DOMINANCE_VERTICAL_THRESHOLD;
    }

    private double layoutReferenceWidth(double horizontalWidth) {
        if (!theme().fixedCategorySize.get()) return horizontalWidth;

        for (WWidget widget = this; widget != null; widget = widget.parent) {
            if (widget instanceof WWindow window) {
                return theme().shouldUseFixedCategoryWidth(window.id) ? theme().fixedCategoryWidth.get() : horizontalWidth;
            }
        }

        return horizontalWidth;
    }

    private void renderTitle(GuiRenderer renderer, double delta, Color textColor, double textY, double titleAreaX, double titleAreaW) {
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

}
