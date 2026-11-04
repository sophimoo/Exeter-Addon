package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.screens.BaseModulesScreen;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleSettingsIndicator;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;


import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public abstract class WBaseModuleRow extends WPressable implements BaseWidget {
    protected final Module module;
    protected final String title;

    protected double titleWidth;

    protected double animationProgress;
    protected double hoverOverlayProgress;
    protected double exeterIconRotation;
    protected double meteorIconRotation = Double.NaN;
    protected final MarqueeState marquee = new MarqueeState();

    protected record ModuleRowLayout(
        String collapsedIndicator,
        String expandedIndicator,
        ModuleSettingsIndicator indicatorStyle,
        double settingsIconWidth,
        double settingsIconGap,
        String bindText,
        double bindWidth,
        double bindGap
    ) {
        double trailingWidth() {
            return settingsIconWidth + settingsIconGap + bindWidth + bindGap;
        }
    }

    protected WBaseModuleRow(Module module, String title) {
        this.module = module;
        this.title = title;
        this.tooltip = module.description;

        if (module.isActive()) {
            animationProgress = 1;
        } else {
            animationProgress = 0;
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

        if (theme().fixedCategorySize.get()) width = pad + pad + layout.trailingWidth();
        else width = pad + titleWidth + layout.trailingWidth() + pad;
        height = resolveModuleRowHeight(pad + theme().textHeight() + pad);
    }

    @Override
    protected final void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();
        ModuleRowLayout layout = computeRowLayout();

        updateAnimationProgresses(mouseX, mouseY, delta);
        boolean isActive = module.isActive();
        RowSurfaceStyle surfaceStyle = moduleRowSurfaceStyle(isActive, pressed, mouseOver, null);
        renderRowSurface(
            renderer,
            x,
            y,
            width,
            height,
            rowAnimationState.effectiveAnimationMode(),
            animationProgress,
            localHoverSurfaceProgress(hoverOverlayProgress),
            surfaceStyle, 0
        );
        renderInterpolationHover(renderer, x, y, width, height, mouseOver, delta, surfaceStyle);

        if (animationProgress > 0) {
            renderRowIndicator(renderer, x, y, width, height, animationProgress, moduleIndicatorStyle(theme().accentColor.get()));
        }

        Color textColor = resolveTextColor();
        RowTextLayout textLayout = resolveRowTextLayout(x + pad, y + pad, width - pad * 2 - layout.trailingWidth(), height - pad * 2,
            titleWidth, theme().moduleAlignment.get(), theme().moduleAlignmentY.get());
        double textY = textLayout.textY();
        renderTitle(renderer, textLayout, textColor, delta);

        if (!layout.bindText().isEmpty()) {
            double bindX = x + width - pad - layout.settingsIconWidth - layout.settingsIconGap - layout.bindWidth;
            renderText(renderer, layout.bindText(), bindX, textY, textColor);
        }

        renderSettingsIcon(renderer, pad, layout, textY, delta, textColor);
        if (mc.currentScreen instanceof BaseModulesScreen screen) {
            screen.queueDimmingOverlay(renderer, this, resolveSearchOverlayProgress());
        }
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
    }

    protected final void renderTitle(GuiRenderer renderer, RowTextLayout textLayout, Color textColor, double delta) {
        double overflow = Math.max(0, titleWidth - textLayout.areaWidth());
        boolean needsMarquee = overflow > 0 && theme().fixedCategorySize.get();

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
            textLayout
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
        ModuleSettingsIndicator indicatorStyle = theme().inlineModuleSettings.get() && theme().indicatorsOnModules.get()
            ? theme().indicatorType.get()
            : ModuleSettingsIndicator.NONE;
        double settingsIconWidth = indicatorWidth(indicatorStyle);
        double settingsIconGap = settingsIconWidth > 0 ? pad() : 0;
        String bindText = theme().showModuleBinds.get() && module.keybind.isSet() ? "[" + module.keybind + "]" : "";
        double bindWidth = bindText.isEmpty() ? 0 : theme().textWidth(bindText);
        double bindGap = bindWidth > 0 ? pad() : 0;
        return new ModuleRowLayout(collapsedIndicator, expandedIndicator, indicatorStyle, settingsIconWidth, settingsIconGap, bindText, bindWidth, bindGap);
    }

    protected final String safeIndicator(String value) {
        return value == null ? "" : value;
    }

    protected final Color resolveTextColor() {
        return resolveModuleTextColor(animationProgress, hoverOverlayProgress);
    }

    protected abstract boolean isSettingsExpanded();

    private double resolveSearchOverlayProgress() {
        if (!(mc.currentScreen instanceof BaseModulesScreen screen)) return 0;
        if (!screen.isSearchActive() || screen.isModuleSearchMatch(module)) return 0;

        WBaseModule moduleWidget = parent instanceof WBaseModule parentModule ? parentModule : null;
        double moduleProgress = moduleWidget != null ? moduleWidget.effectiveInlineDimmingProgress() : 0;
        double targetAlpha = DIMMING_OVERLAY_MAX_ALPHA * theme().darkAmount.get();
        double moduleAlpha = DIMMING_OVERLAY_MAX_ALPHA * moduleProgress;
        if (moduleAlpha >= targetAlpha) return 0;

        double additionalAlpha = (targetAlpha - moduleAlpha) / (1 - moduleAlpha);
        return additionalAlpha / DIMMING_OVERLAY_MAX_ALPHA;
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
