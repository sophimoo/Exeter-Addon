package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.screens.BaseModulesScreen;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleSettingsIndicator;
import meteordevelopment.meteorclient.gui.utils.AlignmentY;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.function.UnaryOperator;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public abstract class WBaseModuleRow extends WPressable implements BaseWidget {
    protected final Module module;
    protected final String title;

    protected double titleWidth;

    protected double animationProgress;
    protected double hoverOverlayProgress;
    protected double indicatorProgress;
    protected double exeterIconRotation;
    protected double meteorIconRotation = Double.NaN;
    protected final MarqueeState marquee = new MarqueeState();

    protected record ModuleRowLayout(
        String collapsedIndicator,
        String expandedIndicator,
        ModuleSettingsIndicator indicatorStyle,
        double settingsIconWidth,
        double settingsIconGap
    ) {}

    protected WBaseModuleRow(Module module, String title) {
        this.module = module;
        this.title = title;
        this.tooltip = module.description;

        if (module.isActive()) {
            animationProgress = 1;
            indicatorProgress = 1;
        } else {
            animationProgress = 0;
            indicatorProgress = 0;
        }

        hoverOverlayProgress = 0;
    }

    @Override
    public double pad() {
        return theme().pad();
    }

    @Override
    protected final void onCalculateSize() {
        double pad = pad();
        ModuleRowLayout layout = computeRowLayout();

        if (titleWidth == 0) titleWidth = theme().textWidth(title);

        if (theme().fixedCategorySize.get()) width = pad + pad + layout.settingsIconWidth + layout.settingsIconGap;
        else width = pad + titleWidth + layout.settingsIconWidth + layout.settingsIconGap + pad;
        height = resolveModuleRowHeight(pad + theme().textHeight() + pad);
    }

    @Override
    protected final void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();
        ModuleRowLayout layout = computeRowLayout();

        updateAnimationProgresses(mouseX, mouseY, delta);
        boolean isActive = module.isActive();
        boolean dimmedBySearch = isDimmedBySearch();
        UnaryOperator<Color> colorMapper = color -> maybeDim(color, dimmedBySearch);

        RowSurfaceStyle surfaceStyle = moduleRowSurfaceStyle(isActive, pressed, mouseOver, colorMapper);
        renderRowSurface(
            renderer,
            x,
            y,
            width,
            height,
            rowAnimationState.effectiveAnimationMode(),
            animationProgress,
            localHoverSurfaceProgress(hoverOverlayProgress),
            surfaceStyle
        );
        renderInterpolationHover(renderer, x, y, width, height, mouseOver, delta, surfaceStyle);

        if (indicatorProgress > 0) {
            renderRowIndicator(renderer, x, y, width, height, indicatorProgress, moduleIndicatorStyle(colorMapper.apply(theme().accentColor.get())));
        }

        Color textColor = resolveTextColor(dimmedBySearch);
        double textY = resolveTextY(pad);
        renderTitle(renderer, pad, layout, textColor, textY, delta);

        renderSettingsIcon(renderer, pad, layout, textY, delta, textColor);
    }

    protected RowAnimationState rowAnimationState = new RowAnimationState(null, 0, 0);

    protected final void updateAnimationProgresses(double mouseX, double mouseY, double delta) {
        boolean isActive = module.isActive();
        rowAnimationState = animateRow(
            delta,
            mouseOver,
            isActive,
            mouseOver,
            animationProgress,
            hoverOverlayProgress
        );

        animationProgress = rowAnimationState.primaryProgress();
        hoverOverlayProgress = rowAnimationState.hoverProgress();

        indicatorProgress = stepProgress(indicatorProgress, isActive, delta);
    }

    protected final void renderTitle(GuiRenderer renderer, double pad, ModuleRowLayout layout, Color textColor, double textY, double delta) {
        double textAreaX = this.x + pad;
        double textAreaW = Math.max(0, width - pad * 2 - layout.settingsIconWidth - layout.settingsIconGap);
        double overflow = Math.max(0, titleWidth - textAreaW);
        boolean needsMarquee = overflow > 0 && theme().fixedCategorySize.get();

        RowTextLayout layoutInfo = resolveRowTextLayout(textAreaX, this.y, textAreaW, height, titleWidth, theme().moduleAlignment.get(), theme().moduleAlignmentY.get());

        renderRowTitle(
            renderer,
            marquee,
            title,
            titleWidth,
            delta,
            mouseOver,
            needsMarquee,
            textColor,
            hoverOverlayProgress,
            new RowTextLayout(layoutInfo.areaX(), layoutInfo.areaY(), layoutInfo.areaWidth(), layoutInfo.areaHeight(), textY, layoutInfo.staticTextX())
        );
    }

    protected void renderSettingsIcon(GuiRenderer renderer, double pad, ModuleRowLayout layout, double textY, double delta, Color textColor) {
        if (layout.indicatorStyle() == ModuleSettingsIndicator.NONE) return;

        double settingsIconX = this.x + width - pad - layout.settingsIconWidth;

        switch (layout.indicatorStyle()) {
            case EXETER -> {
                exeterIconRotation = stepExeterIndicatorRotation(exeterIconRotation, isSettingsExpanded(), delta);
                renderExeterIndicator(renderer, settingsIconX, this.y, layout.settingsIconWidth, height, pad, exeterIconRotation, textColor);
            }
            case METEOR -> {
                meteorIconRotation = stepMeteorIndicatorRotation(meteorIconRotation, isSettingsExpanded(), delta);
                renderMeteorIndicator(renderer, settingsIconX, this.y, layout.settingsIconWidth, height, pad, meteorIconRotation, textColor);
            }
            default -> {
                String settingsIcon = isSettingsExpanded() ? layout.expandedIndicator : layout.collapsedIndicator;
                renderText(renderer, settingsIcon, settingsIconX, textY, textColor);
            }
        }
    }

    protected final ModuleRowLayout computeRowLayout() {
        String collapsedIndicator = safeIndicator(theme().collapsedIndicator.get());
        String expandedIndicator = safeIndicator(theme().expandedIndicator.get());
        ModuleSettingsIndicator indicatorStyle = theme().inlineModuleSettings.get()
            ? theme().indicatorType.get()
            : ModuleSettingsIndicator.NONE;
        double settingsIconWidth = switch (indicatorStyle) {
            case NONE -> 0;
            case EXETER -> theme().textHeight() * EXETER_ICON_SCALE;
            case METEOR -> theme().textHeight();
            case DROPDOWN -> Math.max(theme().textWidth(collapsedIndicator), theme().textWidth(expandedIndicator));
        };
        double settingsIconGap = settingsIconWidth > 0 ? pad() : 0;
        return new ModuleRowLayout(collapsedIndicator, expandedIndicator, indicatorStyle, settingsIconWidth, settingsIconGap);
    }

    protected final String safeIndicator(String value) {
        return value == null ? "" : value;
    }

    protected final Color resolveTextColor(boolean dimmedBySearch) {
        return maybeDim(resolveModuleTextColor(animationProgress, hoverOverlayProgress), dimmedBySearch);
    }

    protected final double resolveTextY(double pad) {
        double textHeight = theme().textHeight();
        double availableHeight = height - pad * 2;
        AlignmentY vAlign = theme().moduleAlignmentY.get();
        if (vAlign == AlignmentY.Top) return y + pad;
        if (vAlign == AlignmentY.Bottom) return y + pad + availableHeight - textHeight;
        return y + pad + (availableHeight - textHeight) / 2;
    }

    protected abstract boolean isSettingsExpanded();

    private boolean isDimmedBySearch() {
        if (!(mc.currentScreen instanceof BaseModulesScreen screen)) return false;
        return screen.isSearchActive() && !screen.isModuleSearchMatch(module);
    }

    private Color maybeDim(Color color, boolean dimmedBySearch) {
        if (!dimmedBySearch || color == null) return color;

        Color dimmed = new Color(color);
        dimmed.r = Math.max(0, (int) Math.round(dimmed.r * 0.4));
        dimmed.g = Math.max(0, (int) Math.round(dimmed.g * 0.4));
        dimmed.b = Math.max(0, (int) Math.round(dimmed.b * 0.4));
        dimmed.a = Math.max(18, (int) Math.round(dimmed.a * 0.55));
        dimmed.validate();
        return dimmed;
    }

    @Override
    protected final void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            module.toggle();
        } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            onRightClick();
        }
    }

    protected abstract void onRightClick();
}
