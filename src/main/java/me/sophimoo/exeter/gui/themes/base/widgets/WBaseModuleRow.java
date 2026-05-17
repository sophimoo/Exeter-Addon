package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.BaseAddon;
import me.sophimoo.exeter.gui.screens.BaseModulesScreen;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.AnimatedOverlayRenderer;
import me.sophimoo.exeter.gui.themes.base.utils.enums.GradientApplicationMode;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleAnimationMode;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleGradientDirection;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleIndicatorPosition;
import me.sophimoo.exeter.gui.themes.base.utils.SmartSlideAnimationState;
import meteordevelopment.meteorclient.gui.utils.AlignmentY;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public abstract class WBaseModuleRow extends WPressable implements BaseWidget {
    protected static final double EXETER_ICON_SCALE = 1.4;
    protected static final double EXETER_ICON_ROTATION_SPEED = 120;
    protected static final Color EXETER_ICON_COLOR = new Color(255, 255, 255);

    protected final Module module;
    protected final String title;

    protected double titleWidth;

    protected double animationProgress;
    protected double hoverOverlayProgress;
    protected double indicatorProgress;
    protected double exeterIconRotation;
    protected final MarqueeState marquee = new MarqueeState();
    protected final SmartSlideAnimationState smartSlide = new SmartSlideAnimationState();

    protected record RenderSettings(
        ModuleGradientDirection gradientDir,
        Color activeGradientColor,
        Color inactiveGradientColor,
        GradientApplicationMode applyMode,
        double thickness,
        Color baseColor,
        boolean renderBaseGradient,
        Color overlayColor,
        Color overlayGradient,
        boolean renderOverlayGradient,
        Color hoveredOverlayColor,
        Color hoveredOverlayGradient,
        boolean renderHoveredOverlayGradient,
        Color outlineColor
    ) {}

    protected record ModuleRowLayout(
        String collapsedIndicator,
        String expandedIndicator,
        boolean useExeterIndicator,
        boolean showIndicator,
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

        ModuleAnimationMode effectiveAnimationMode = updateAnimationProgresses(mouseX, mouseY, delta);
        boolean isActive = module.isActive();
        boolean dimmedBySearch = isDimmedBySearch();

        RenderSettings settings = computeRenderSettings(isActive, dimmedBySearch);

        renderAnimatedOverlay(renderer, ModuleAnimationMode.FADE, 1,
            settings.baseColor(), settings.inactiveGradientColor(),
            settings.renderBaseGradient(), settings.gradientDir());

        if (animationProgress > 0) {
            renderAnimatedOverlay(renderer, effectiveAnimationMode, animationProgress,
                settings.overlayColor(), settings.overlayGradient(),
                settings.renderOverlayGradient(), settings.gradientDir());
        }

        if (isActive && hoverOverlayProgress > 0) {
            renderAnimatedOverlay(renderer, effectiveAnimationMode, hoverOverlayProgress,
                settings.hoveredOverlayColor(), settings.hoveredOverlayGradient(),
                settings.renderHoveredOverlayGradient(), settings.gradientDir());
        }

        if (settings.thickness() > 0 && settings.outlineColor() != null) {
            renderOutline(renderer, x, y, width, height, settings.thickness(), settings.outlineColor());
        }

        if (indicatorProgress > 0) {
            renderIndicator(renderer, indicatorProgress);
        }

        Color textColor = resolveTextColor(dimmedBySearch);
        double textY = resolveTextY(pad);
        renderTitle(renderer, pad, layout, textColor, textY, delta);

        renderSettingsIcon(renderer, pad, layout, textY, delta, textColor);
    }

    protected final ModuleAnimationMode updateAnimationProgresses(double mouseX, double mouseY, double delta) {
        boolean isActive = module.isActive();
        boolean shouldFadeIn = isActive || mouseOver;
        ModuleAnimationMode animationMode = theme().moduleAnimationMode.get();
        ModuleAnimationMode effectiveAnimationMode = smartSlide.resolveMode(
            animationMode,
            mouseOver,
            shouldFadeIn,
            mouseX,
            mouseY,
            x,
            y,
            width,
            height,
            theme(),
            animationProgress
        );

        double fadeInSpeed = theme().moduleSelectSpeed.get();
        double fadeOutSpeed = theme().moduleDeselectSpeed.get();

        animationProgress = smartSlide.stepProgress(animationProgress, shouldFadeIn, delta, fadeInSpeed, fadeOutSpeed);
        hoverOverlayProgress = smartSlide.stepProgress(hoverOverlayProgress, mouseOver, delta, fadeInSpeed, fadeOutSpeed);

        indicatorProgress += delta * (isActive ? fadeInSpeed : fadeOutSpeed) * (isActive ? 1 : -1);
        indicatorProgress = MathHelper.clamp(indicatorProgress, 0, 1);

        return effectiveAnimationMode;
    }

    protected final RenderSettings computeRenderSettings(boolean isActive, boolean dimmedBySearch) {
        ModuleGradientDirection gradientDir = theme().moduleGradientDirection.get();
        Color activeGradientColor = maybeDim(theme().moduleActiveGradientColor.get(), dimmedBySearch);
        Color inactiveGradientColor = maybeDim(theme().moduleInactiveGradientColor.get(), dimmedBySearch);
        GradientApplicationMode applyMode = theme().gradientApplicationMode.get();
        double thickness = theme().scale(theme().moduleOutlineThickness.get());
        Color baseColor = maybeDim(theme().moduleInactiveColor.get(), dimmedBySearch);
        boolean renderBaseGradient = !isActive && applyMode.shouldApply(false) && gradientDir != ModuleGradientDirection.NONE;
        Color overlayColor = maybeDim(isActive ? theme().moduleActiveColor.get() : theme().moduleHoveredColor.get(), dimmedBySearch);
        Color overlayGradient = isActive ? activeGradientColor : maybeDim(theme().moduleHoveredGradientColor.get(), dimmedBySearch);
        boolean renderOverlayGradient = applyMode.shouldApply(isActive);
        Color hoveredOverlayColor = maybeDim(theme().moduleHoveredColor.get(), dimmedBySearch);
        Color hoveredOverlayGradient = maybeDim(theme().moduleHoveredGradientColor.get(), dimmedBySearch);
        boolean renderHoveredOverlayGradient = applyMode.shouldApply(false);
        Color outlineColor = maybeDim(theme().outlineColor.get(pressed, mouseOver), dimmedBySearch);

        return new RenderSettings(
            gradientDir, activeGradientColor, inactiveGradientColor, applyMode, thickness,
            baseColor, renderBaseGradient, overlayColor, overlayGradient, renderOverlayGradient,
            hoveredOverlayColor, hoveredOverlayGradient, renderHoveredOverlayGradient, outlineColor
        );
    }

    protected final void renderAnimatedOverlay(GuiRenderer renderer, ModuleAnimationMode animationMode, double progress,
                                               Color color, Color gradient, boolean renderGradient, ModuleGradientDirection gradientDir) {
        AnimatedOverlayRenderer.render(
            renderer,
            x,
            y,
            width,
            height,
            animationMode,
            progress,
            color,
            gradient,
            renderGradient ? gradientDir : ModuleGradientDirection.NONE
        );
    }

    protected final void renderTitle(GuiRenderer renderer, double pad, ModuleRowLayout layout, Color textColor, double textY, double delta) {
        double textAreaX = this.x + pad;
        double textAreaW = Math.max(0, width - pad * 2 - layout.settingsIconWidth - layout.settingsIconGap);
        double overflow = Math.max(0, titleWidth - textAreaW);
        boolean needsMarquee = overflow > 0 && theme().fixedCategorySize.get();

        double staticTextX = textAreaX;
        if (theme().moduleAlignment.get() == AlignmentX.Center) {
            staticTextX += textAreaW / 2 - titleWidth / 2;
        } else if (theme().moduleAlignment.get() == AlignmentX.Right) {
            staticTextX += textAreaW - titleWidth;
        }

        renderTextWithMarquee(renderer, marquee, title, textAreaX, this.y, textAreaW, height, textY, titleWidth,
            mouseOver, delta, needsMarquee, staticTextX, textColor, hoverOverlayProgress);
    }

    protected void renderSettingsIcon(GuiRenderer renderer, double pad, ModuleRowLayout layout, double textY, double delta, Color textColor) {
        if (!layout.showIndicator) return;

        double settingsIconX = this.x + width - pad - layout.settingsIconWidth;

        if (layout.useExeterIndicator) {
            double baseIconSize = Math.max(1, Math.min(layout.settingsIconWidth, height - pad * 2));
            if (isSettingsExpanded()) {
                exeterIconRotation = (exeterIconRotation + delta * EXETER_ICON_ROTATION_SPEED) % 360;
            }
            double iconSize = baseIconSize;
            double iconY = this.y + (height - iconSize) / 2;
            double iconX = settingsIconX + (layout.settingsIconWidth - iconSize) / 2;

            if (BaseAddon.EXETER_ICON_TEXTURE != null) {
                renderer.rotatedQuad(iconX, iconY, iconSize, iconSize, exeterIconRotation, BaseAddon.EXETER_ICON_TEXTURE, EXETER_ICON_COLOR);
            }
        } else {
            String settingsIcon = isSettingsExpanded() ? layout.expandedIndicator : layout.collapsedIndicator;
            renderText(renderer, settingsIcon, settingsIconX, textY, textColor);
        }
    }

    protected final ModuleRowLayout computeRowLayout() {
        String collapsedIndicator = safeIndicator(theme().moduleCollapsedIndicator.get());
        String expandedIndicator = safeIndicator(theme().moduleExpandedIndicator.get());
        boolean useExeterIndicator = theme().exeterIndicator.get();
        boolean showIndicator = (useExeterIndicator || theme().dropdownIndicator.get()) && theme().inlineModuleSettings.get();
        double settingsIconWidth = showIndicator
            ? (useExeterIndicator
                ? theme().textHeight() * EXETER_ICON_SCALE
                : Math.max(theme().textWidth(collapsedIndicator), theme().textWidth(expandedIndicator)))
            : 0;
        double settingsIconGap = showIndicator ? pad() : 0;
        return new ModuleRowLayout(collapsedIndicator, expandedIndicator, useExeterIndicator, showIndicator, settingsIconWidth, settingsIconGap);
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

    protected final void renderIndicator(GuiRenderer renderer, double progress) {
        ModuleIndicatorPosition position = theme().moduleIndicatorPosition.get();
        if (position == ModuleIndicatorPosition.NONE) return;

        double thickness = theme().scale(theme().moduleIndicatorThickness.get());
        if (thickness <= 0) return;

        Color accentColor = theme().accentColor.get();
        if (isDimmedBySearch()) accentColor = maybeDim(accentColor, true);
        double size = thickness * progress;

        double ix = this.x, iy = this.y, iw = this.width, ih = this.height;

        switch (position) {
            case LEFT -> iw = size;
            case RIGHT -> { ix = this.x + this.width - size; iw = size; }
            case TOP -> ih = size;
            case BOTTOM -> { iy = this.y + this.height - size; ih = size; }
        }

        renderer.quad(ix, iy, iw, ih, accentColor);
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
