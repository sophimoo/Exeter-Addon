package me.sophimoo.exeter.gui.themes.base;

import me.sophimoo.exeter.BaseAddon;
import me.sophimoo.exeter.gui.renderer.BlurRendererAccess;
import me.sophimoo.exeter.gui.renderer.GradientRenderer;
import me.sophimoo.exeter.gui.renderer.WorldFramebufferCapture;
import me.sophimoo.exeter.gui.themes.base.utils.AnimatedOverlayRenderer;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.utils.InterpolationState;
import me.sophimoo.exeter.gui.themes.base.utils.enums.SelectionRenderingMode;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleGradientDirection;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleIndicatorPosition;
import me.sophimoo.exeter.gui.themes.base.utils.enums.TextHoverDisplacementDirection;
import com.mojang.blaze3d.textures.GpuTextureView;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.AlignmentY;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import me.sophimoo.exeter.gui.renderer.GuiTextRendererAccess;

import java.util.function.UnaryOperator;

public interface BaseWidget extends meteordevelopment.meteorclient.gui.utils.BaseWidget {
    record ConfirmColors(Color fg, Color bg) {}
    record SurfaceLayer(Color color, Color gradient, boolean renderGradient) {}
    record RowSurfaceStyle(SurfaceLayer baseLayer, SurfaceLayer overlayLayer, SurfaceLayer hoveredOverlayLayer,
                           ModuleGradientDirection gradientDirection, double outlineThickness, Color outlineColor) {}
    record RowIndicatorStyle(ModuleIndicatorPosition position, double thickness, Color color) {}
    record RowAnimationState(SelectionRenderingMode effectiveAnimationMode, double primaryProgress, double hoverProgress) {}
    record RowTextLayout(double areaX, double areaY, double areaWidth, double areaHeight, double textY, double staticTextX) {}

    double EXETER_ICON_SCALE = 1.4;
    double EXETER_ICON_ROTATION_SPEED = 120;
    double METEOR_ICON_ROTATION_SPEED = 14;

    default BaseGuiTheme theme() {
        return (BaseGuiTheme) getTheme();
    }

    default double resolveModuleRowHeight(double defaultHeight) {
        double customHeight = theme().moduleHeight.get();
        if (customHeight > 0) return theme().scale(customHeight);
        return defaultHeight;
    }

    default double resolveItemRowHeight(double defaultHeight) {
        double customHeight = theme().itemHeight.get();
        if (customHeight > 0) return theme().scale(customHeight);
        return defaultHeight;
    }

    default double resolveSeparatorRowHeight(double defaultHeight) {
        double customHeight = theme().separatorHeight.get();
        if (customHeight > 0) return theme().scale(customHeight);
        return defaultHeight;
    }

    default double resolveCategoryTitleRowHeight(double defaultHeight) {
        double customHeight = theme().categoryTitleHeight.get();
        if (customHeight > 0) return theme().scale(customHeight);
        return defaultHeight;
    }

    default double moduleRowBaseHeight(double extraPx) {
        return resolveModuleRowHeight(theme().rowPadY() + theme().textHeight() + theme().rowPadY() + theme().scaledPx(extraPx));
    }

    default double itemRowBaseHeight(double extraPx) {
        return resolveItemRowHeight(theme().rowPadY() + theme().textHeight() + theme().rowPadY() + theme().scaledPx(extraPx));
    }

    default RowAnimationState animateRow(double delta, boolean mouseOver,
                                         boolean primaryVisible, boolean hoverVisible,
                                         double primaryProgress, double hoverProgress) {
        SelectionRenderingMode effectiveAnimationMode = isInterpolationMode() ? SelectionRenderingMode.FADE : theme().selectionRenderingMode.get();

        double fadeInSpeed = theme().selectionSelectSpeed.get();
        double fadeOutSpeed = theme().selectionDeselectSpeed.get();
        double hoverFadeInSpeed = fadeInSpeed;
        double hoverFadeOutSpeed = fadeOutSpeed;

        return new RowAnimationState(
            effectiveAnimationMode,
            clampProgress(stepAnimationProgress(primaryProgress, primaryVisible, delta, fadeInSpeed, fadeOutSpeed)),
            clampProgress(stepAnimationProgress(hoverProgress, hoverVisible, delta, hoverFadeInSpeed, hoverFadeOutSpeed))
        );
    }

    default boolean isInterpolationMode() {
        return theme().selectionRenderingMode.get() == SelectionRenderingMode.INTERPOLATE;
    }

    default boolean localHoverAnimationVisible(boolean hovered) {
        return hovered && !isInterpolationMode();
    }

    default double localHoverSurfaceProgress(double progress) {
        return isInterpolationMode() ? 0 : progress;
    }

    default RowSurfaceStyle moduleRowSurfaceStyle(boolean active, boolean pressed, boolean mouseOver, UnaryOperator<Color> colorMapper) {
        ModuleGradientDirection gradientDirection = theme().gradientRender.get();
        boolean renderGradient = gradientDirection != ModuleGradientDirection.NONE;

        Color inactiveGradient = mapColor(theme().moduleInactiveGradientColor.get(), colorMapper);
        Color activeGradient = mapColor(theme().moduleActiveGradientColor.get(), colorMapper);
        Color hoveredGradient = mapColor(theme().moduleHoveredGradientColor.get(), colorMapper);

        return createRowSurfaceStyle(
            mapColor(theme().moduleInactiveColor.get(), colorMapper),
            inactiveGradient,
            renderGradient,
            mapColor(active ? theme().moduleActiveColor.get() : theme().moduleHoveredColor.get(), colorMapper),
            active ? activeGradient : hoveredGradient,
            renderGradient,
            mapColor(theme().moduleHoveredColor.get(), colorMapper),
            hoveredGradient,
            renderGradient,
            gradientDirection,
            theme().outlineColor.get(pressed, mouseOver),
            colorMapper
        );
    }

    default RowSurfaceStyle itemRowSurfaceStyle(boolean active, boolean pressed, boolean mouseOver) {
        ModuleGradientDirection gradientDirection = theme().gradientRender.get();
        boolean renderGradient = gradientDirection != ModuleGradientDirection.NONE;

        return createRowSurfaceStyle(
            theme().itemInactiveColor.get(),
            theme().itemInactiveGradientColor.get(),
            renderGradient,
            active ? theme().itemActiveColor.get() : theme().itemHoveredColor.get(),
            active ? theme().itemActiveGradientColor.get() : theme().itemHoveredGradientColor.get(),
            renderGradient,
            theme().itemHoveredColor.get(),
            theme().itemHoveredGradientColor.get(),
            renderGradient,
            gradientDirection,
            theme().outlineColor.get(pressed, mouseOver),
            null
        );
    }

    default RowSurfaceStyle separatorRowSurfaceStyle(boolean active, boolean mouseOver) {
        ModuleGradientDirection gradientDirection = theme().gradientRender.get();
        boolean renderGradient = gradientDirection != ModuleGradientDirection.NONE;

        return createRowSurfaceStyle(
            theme().separatorInactiveColor.get(),
            theme().separatorInactiveGradientColor.get(),
            renderGradient,
            active ? theme().separatorActiveColor.get() : theme().separatorHoveredColor.get(),
            active ? theme().separatorActiveGradientColor.get() : theme().separatorHoveredGradientColor.get(),
            renderGradient,
            theme().separatorHoveredColor.get(),
            theme().separatorHoveredGradientColor.get(),
            renderGradient,
            gradientDirection,
            theme().outlineColor.get(false, mouseOver),
            null
        );
    }

    default RowSurfaceStyle categoryTitleRowSurfaceStyle(boolean active, boolean mouseOver) {
        ModuleGradientDirection gradientDirection = theme().gradientRender.get();
        boolean renderGradient = gradientDirection != ModuleGradientDirection.NONE;

        return createRowSurfaceStyle(
            theme().categoryTitleInactiveColor.get(),
            theme().categoryTitleInactiveGradientColor.get(),
            renderGradient,
            active ? theme().categoryTitleActiveColor.get() : theme().categoryTitleHoveredColor.get(),
            active ? theme().categoryTitleActiveGradientColor.get() : theme().categoryTitleHoveredGradientColor.get(),
            renderGradient,
            theme().categoryTitleHoveredColor.get(),
            theme().categoryTitleHoveredGradientColor.get(),
            renderGradient,
            gradientDirection,
            theme().outlineColor.get(false, mouseOver),
            null
        );
    }

    default RowIndicatorStyle moduleIndicatorStyle(Color color) {
        return new RowIndicatorStyle(
            theme().activeIndicatorPosition.get(),
            theme().scale(theme().activeIndicatorThickness.get()),
            color
        );
    }

    default RowTextLayout resolveRowTextLayout(double areaX, double areaY, double areaWidth, double areaHeight,
                                               double textWidth, AlignmentX horizontalAlignment, AlignmentY verticalAlignment) {
        double safeAreaWidth = Math.max(0, areaWidth);
        double safeAreaHeight = Math.max(0, areaHeight);
        double textHeight = theme().textHeight();
        double textY = areaY;

        if (verticalAlignment == AlignmentY.Center) textY += (safeAreaHeight - textHeight) / 2;
        else if (verticalAlignment == AlignmentY.Bottom) textY += safeAreaHeight - textHeight;

        double staticTextX = areaX;
        if (horizontalAlignment == AlignmentX.Center) staticTextX += safeAreaWidth / 2 - textWidth / 2;
        else if (horizontalAlignment == AlignmentX.Right) staticTextX += safeAreaWidth - textWidth;

        return new RowTextLayout(areaX, areaY, safeAreaWidth, safeAreaHeight, textY, staticTextX);
    }

    default void renderRowSurface(GuiRenderer renderer, double x, double y, double width, double height,
                                    SelectionRenderingMode overlayAnimationMode, double overlayProgress,
                                   double hoveredOverlayProgress, RowSurfaceStyle style) {
        double baseProgress = 1 - overlayProgress;
        if (baseProgress > 0 && style.baseLayer() != null) {
            renderSurfaceLayer(renderer, x, y, width, height, SelectionRenderingMode.FADE, baseProgress, style.baseLayer(), style.gradientDirection());
        }

        if (overlayProgress > 0 && style.overlayLayer() != null) {
            renderSurfaceLayer(renderer, x, y, width, height, overlayAnimationMode, overlayProgress, style.overlayLayer(), style.gradientDirection());
        }

        if (hoveredOverlayProgress > 0 && style.hoveredOverlayLayer() != null) {
            renderSurfaceLayer(renderer, x, y, width, height, overlayAnimationMode, hoveredOverlayProgress, style.hoveredOverlayLayer(), style.gradientDirection());
        }

        if (style.outlineThickness() > 0 && style.outlineColor() != null) {
            renderOutline(renderer, x, y, width, height, style.outlineThickness(), style.outlineColor());
        }
    }

    default Object getInterpolationKey() {
        WWidget widget = (WWidget) this;
        while (widget.parent != null) widget = widget.parent;
        return widget;
    }

    default void renderInterpolationHover(GuiRenderer renderer, double x, double y, double width, double height,
                                          boolean hovered, double delta, RowSurfaceStyle style) {
        if (!isInterpolationMode()) return;

        InterpolationState interpolation = theme().getInterpolation(getInterpolationKey());
        interpolation.notifyHover(x, y, width, height, hovered);
        interpolation.update(delta, theme().selectionSelectSpeed.get(), theme().selectionSelectSpeed.get(), theme().selectionDeselectSpeed.get());

        double[] isect = interpolation.getIntersection(x, y, width, height);
        if (isect == null) return;

        double fadeProgress = interpolation.getFadeProgress();
        if (fadeProgress <= 0) return;

        SurfaceLayer layer = style.hoveredOverlayLayer() != null
            ? style.hoveredOverlayLayer()
            : (style.overlayLayer() != null ? style.overlayLayer() : null);

        if (layer == null || layer.color() == null) return;

        Color layerBaseColor = layer.color();
        Color layerGradientColor = layer.gradient();

        Color color = new Color(layerBaseColor.r, layerBaseColor.g, layerBaseColor.b, (int) Math.round(layerBaseColor.a * fadeProgress));
        Color gradient = layerGradientColor != null
            ? new Color(layerGradientColor.r, layerGradientColor.g, layerGradientColor.b, (int) Math.round(layerGradientColor.a * fadeProgress))
            : null;

        if (layer.renderGradient() && style.gradientDirection() != ModuleGradientDirection.NONE && gradient != null) {
            me.sophimoo.exeter.gui.renderer.GradientRenderer.render(
                renderer, isect[0], isect[1], isect[2], isect[3],
                gradient, color, style.gradientDirection()
            );
        } else {
            renderer.quad(isect[0], isect[1], isect[2], isect[3], color);
        }
    }

    default void renderRowIndicator(GuiRenderer renderer, double x, double y, double width, double height,
                                    double progress, RowIndicatorStyle style) {
        if (style == null || progress <= 0 || style.position() == ModuleIndicatorPosition.NONE || style.thickness() <= 0 || style.color() == null) return;

        double size = style.thickness() * progress;
        double ix = x, iy = y, iw = width, ih = height;

        switch (style.position()) {
            case LEFT -> iw = size;
            case RIGHT -> {
                ix = x + width - size;
                iw = size;
            }
            case TOP -> ih = size;
            case BOTTOM -> {
                iy = y + height - size;
                ih = size;
            }
        }

        renderer.quad(ix, iy, iw, ih, style.color());
    }

    default void renderRowTitle(GuiRenderer renderer, MarqueeState marqueeState, String text, double textWidth,
                                double delta, boolean animate, boolean marqueeEnabled, Color color,
                                double displacementProgress, RowTextLayout layout) {
        renderTextWithMarquee(
            renderer,
            marqueeState,
            text,
            layout.areaX(),
            layout.areaY(),
            layout.areaWidth(),
            layout.areaHeight(),
            layout.textY(),
            textWidth,
            animate,
            delta,
            marqueeEnabled,
            layout.staticTextX(),
            color,
            displacementProgress
        );
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, Color outlineColor, Color backgroundColor) {
        BaseGuiTheme theme = theme();
        double s = theme.scale(2);

        double innerX = widget.x + s;
        double innerY = widget.y + s;
        double innerWidth = widget.width - s * 2;
        double innerHeight = widget.height - s * 2;

        renderQuadWithOptionalBlur(renderer, innerX, innerY, innerWidth, innerHeight, backgroundColor);

        if (outlineColor != null) {
            renderOutline(renderer, widget.x, widget.y, widget.width, widget.height, s, outlineColor);
        }
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        BaseGuiTheme theme = theme();
        renderBackground(renderer, widget, theme.outlineColor.get(pressed, mouseOver), theme.backgroundColor.get(pressed, mouseOver));
    }

    default void renderQuadWithOptionalBlur(GuiRenderer renderer, double x, double y, double width, double height, Color color) {
        if (theme().widgetBlurStrength.get() > 0) {
            WorldFramebufferCapture capture = WorldFramebufferCapture.getInstance();
            GpuTextureView blurTexture = capture != null ? capture.getBlurredTexture() : null;

            if (blurTexture != null) {
                ((BlurRendererAccess) renderer).blurredQuad(x, y, width, height, blurTexture, color);
                return;
            }
        }

        renderer.quad(x, y, width, height, color);
    }

    default void renderOutline(GuiRenderer renderer, double x, double y, double width, double height, double thickness, Color color) {
        renderer.quad(x, y, width, thickness, color);
        renderer.quad(x, y + height - thickness, width, thickness, color);
        renderer.quad(x, y + thickness, thickness, height - 2 * thickness, color);
        renderer.quad(x + width - thickness, y + thickness, thickness, height - 2 * thickness, color);
    }

    default void renderText(GuiRenderer renderer, String text, double x, double y, Color color) {
        if (theme().textShadow.get() && theme().textRenderer() instanceof VanillaTextRenderer && renderer instanceof GuiTextRendererAccess shadowRenderer) {
            shadowRenderer.exeter$queueVanillaShadowText(text, x, y, color);
            return;
        }

        if (theme().textShadow.get()) {
            // Vanilla-style shadow: same alpha, RGB divided by 4
            Color shadowColor = new Color(color.r / 4, color.g / 4, color.b / 4, color.a);
            double offset = theme().scale(theme().textShadowOffset.get());
            renderer.text(text, x + offset, y + offset, shadowColor, false);
        }

        renderer.text(text, x, y, color, false);
    }

    default void renderText(GuiRenderer renderer, String text, double x, double y, Color color, double displacementProgress) {
        if (theme().textHoverDisplacement.get() && displacementProgress > 0) {
            double clampedProgress = Math.max(0, Math.min(1, displacementProgress));
            double displacement = theme().scale(theme().textHoverDisplacementAmount.get()) * clampedProgress;
            TextHoverDisplacementDirection direction = theme().textHoverDisplacementDirection.get();

            switch (direction) {
                case LEFT -> x -= displacement;
                case RIGHT -> x += displacement;
                case UP -> y -= displacement;
                case DOWN -> y += displacement;
            }
        }

        renderText(renderer, text, x, y, color);
    }

    default void renderTextWithMarquee(GuiRenderer renderer, MarqueeState marqueeState, String text,
                                       double textAreaX, double textAreaY, double textAreaW, double textAreaH,
                                       double textY, double textWidth, boolean animate, double delta,
                                       boolean marqueeEnabled, double staticTextX, Color color) {
        renderTextWithMarquee(renderer, marqueeState, text, textAreaX, textAreaY, textAreaW, textAreaH,
            textY, textWidth, animate, delta, marqueeEnabled, staticTextX, color, 0);
    }

    default void renderTextWithMarquee(GuiRenderer renderer, MarqueeState marqueeState, String text,
                                       double textAreaX, double textAreaY, double textAreaW, double textAreaH,
                                       double textY, double textWidth, boolean animate, double delta,
                                       boolean marqueeEnabled, double staticTextX, Color color, double displacementProgress) {
        double overflow = Math.max(0, textWidth - textAreaW);

        if (marqueeEnabled && overflow > 0 && textAreaW > 0) {
            double marqueeOffset = marqueeState.step(overflow, animate, delta);
            renderer.scissorStart(textAreaX, textAreaY, textAreaW, textAreaH);
            renderText(renderer, text, textAreaX - marqueeOffset, textY, color, displacementProgress);
            renderer.scissorEnd();
            return;
        }

        marqueeState.reset();
        renderText(renderer, text, staticTextX, textY, color, displacementProgress);
    }

    private RowSurfaceStyle createRowSurfaceStyle(Color baseColor, Color baseGradient, boolean renderBaseGradient,
                                                  Color overlayColor, Color overlayGradient, boolean renderOverlayGradient,
                                                  Color hoveredOverlayColor, Color hoveredOverlayGradient, boolean renderHoveredOverlayGradient,
                                                  ModuleGradientDirection gradientDirection, Color outlineColor,
                                                  UnaryOperator<Color> colorMapper) {
        return new RowSurfaceStyle(
            new SurfaceLayer(mapColor(baseColor, colorMapper), mapColor(baseGradient, colorMapper), renderBaseGradient),
            overlayColor != null ? new SurfaceLayer(mapColor(overlayColor, colorMapper), mapColor(overlayGradient, colorMapper), renderOverlayGradient) : null,
            hoveredOverlayColor != null ? new SurfaceLayer(mapColor(hoveredOverlayColor, colorMapper), mapColor(hoveredOverlayGradient, colorMapper), renderHoveredOverlayGradient) : null,
            gradientDirection,
            theme().scale(theme().moduleOutlineThickness.get()),
            mapColor(outlineColor, colorMapper)
        );
    }

    private void renderSurfaceLayer(GuiRenderer renderer, double x, double y, double width, double height,
                                    SelectionRenderingMode animationMode, double progress, SurfaceLayer layer,
                                    ModuleGradientDirection gradientDirection) {
        if (layer == null || layer.color() == null) return;

        AnimatedOverlayRenderer.render(
            renderer,
            x,
            y,
            width,
            height,
            animationMode,
            progress,
            layer.color(),
            layer.gradient(),
            layer.renderGradient() ? gradientDirection : ModuleGradientDirection.NONE
        );
    }

    private double clampProgress(double value) {
        return Math.max(0, Math.min(1, value));
    }

    default double stepProgress(double currentProgress, boolean shouldFadeIn, double delta) {
        return stepAnimationProgress(currentProgress, shouldFadeIn, delta,
            theme().selectionSelectSpeed.get(), theme().selectionDeselectSpeed.get());
    }

    default double stepAnimationProgress(double currentProgress, boolean shouldFadeIn, double delta, double fadeInSpeed, double fadeOutSpeed) {
        if (shouldFadeIn && fadeInSpeed == 0) return 1;
        if (!shouldFadeIn && fadeOutSpeed == 0) return 0;

        double progress = currentProgress + delta * (shouldFadeIn ? fadeInSpeed : fadeOutSpeed) * (shouldFadeIn ? 1 : -1);
        return Math.max(0, Math.min(1, progress));
    }

    default double stepExeterIndicatorRotation(double currentRotation, boolean active, double delta) {
        if (!active) return currentRotation;
        return (currentRotation + delta * EXETER_ICON_ROTATION_SPEED) % 360;
    }

    default double stepMeteorIndicatorRotation(double currentRotation, boolean expanded, double delta) {
        double target = expanded ? 0 : -90;
        if (Double.isNaN(currentRotation)) return target;
        return currentRotation + (target - currentRotation) * Math.min(1, delta * METEOR_ICON_ROTATION_SPEED);
    }

    default void renderExeterIndicator(GuiRenderer renderer, double iconBoxX, double rowY, double iconBoxWidth,
                                       double rowHeight, double pad, double rotation, Color color) {
        if (BaseAddon.EXETER_ICON_TEXTURE == null) return;
        double iconSize = Math.max(1, Math.min(iconBoxWidth, rowHeight - pad * 2));
        double iconY = rowY + (rowHeight - iconSize) / 2;
        double iconX = iconBoxX + (iconBoxWidth - iconSize) / 2;
        renderer.rotatedQuad(iconX, iconY, iconSize, iconSize, rotation, BaseAddon.EXETER_ICON_TEXTURE, color);
    }

    default void renderMeteorIndicator(GuiRenderer renderer, double iconBoxX, double rowY, double iconBoxWidth,
                                       double rowHeight, double pad, double rotation, Color color) {
        double iconSize = Math.max(1, Math.min(iconBoxWidth, rowHeight - pad * 2));
        double iconY = rowY + (rowHeight - iconSize) / 2;
        double iconX = iconBoxX + (iconBoxWidth - iconSize) / 2;
        renderer.rotatedQuad(iconX, iconY, iconSize, iconSize, rotation, GuiRenderer.TRIANGLE, color);
    }

    default double dropdownHeightProgress(double progress, boolean expanding) {
        double clampedProgress = Math.max(0, Math.min(1, progress));
        if (expanding) return 1.0 - Math.pow(1.0 - clampedProgress, 3);
        return Math.pow(clampedProgress, 3);
    }

    private Color mapColor(Color color, UnaryOperator<Color> colorMapper) {
        if (color == null || colorMapper == null) return color;
        return colorMapper.apply(color);
    }

    default Color interpolateColor(Color from, Color to, double progress) {
        double clampedProgress = Math.max(0, Math.min(1, progress));
        int r = (int) Math.round(from.r + (to.r - from.r) * clampedProgress);
        int g = (int) Math.round(from.g + (to.g - from.g) * clampedProgress);
        int b = (int) Math.round(from.b + (to.b - from.b) * clampedProgress);
        int a = (int) Math.round(from.a + (to.a - from.a) * clampedProgress);
        return new Color(r, g, b, a);
    }

    default Color resolveTextStateColor(Color inactiveColor, Color activeColor, Color hoveredColor,
                                        double activeProgress, double hoverProgress) {
        Color baseColor = interpolateColor(inactiveColor, activeColor, activeProgress);
        return interpolateColor(baseColor, hoveredColor, hoverProgress);
    }

    default Color resolveTextStateColor(Color inactiveColor, Color hoveredColor, double hoverProgress) {
        return resolveTextStateColor(inactiveColor, inactiveColor, hoveredColor, 0, hoverProgress);
    }

    default Color resolveModuleTextColor(double activeProgress, double hoverProgress) {
        return resolveTextStateColor(
            theme().moduleTextInactiveColor.get(),
            theme().moduleTextActiveColor.get(),
            theme().moduleTextHoveredColor.get(),
            activeProgress,
            hoverProgress
        );
    }

    default Color resolveModuleTextColor(double hoverProgress) {
        return resolveModuleTextColor(0, hoverProgress);
    }

    default Color resolveSeparatorTextColor(double activeProgress, double hoverProgress) {
        return resolveTextStateColor(
            theme().separatorTextInactiveColor.get(),
            theme().separatorTextActiveColor.get(),
            theme().separatorTextHoveredColor.get(),
            activeProgress,
            hoverProgress
        );
    }

    default Color resolveSeparatorTextColor(double hoverProgress) {
        return resolveSeparatorTextColor(0, hoverProgress);
    }

    default Color resolveCategoryTitleTextColor(double activeProgress, double hoverProgress) {
        return resolveTextStateColor(
            theme().categoryTitleTextInactiveColor.get(),
            theme().categoryTitleTextActiveColor.get(),
            theme().categoryTitleTextHoveredColor.get(),
            activeProgress,
            hoverProgress
        );
    }

    default Color resolveSettingsTextColor(double activeProgress, double hoverProgress) {
        return resolveTextStateColor(
            theme().itemTextInactiveColor.get(),
            theme().itemTextActiveColor.get(),
            theme().itemTextHoveredColor.get(),
            activeProgress,
            hoverProgress
        );
    }

    default Color resolveSettingsTextColor(double hoverProgress) {
        return resolveSettingsTextColor(0, hoverProgress);
    }

    default void renderSliderSegment(GuiRenderer renderer, double x, double y, double width, double height,
                                     String partKey, double hoverProgress) {
        if (width <= 0 || height <= 0) return;

        Color color = resolveTextStateColor(theme().sliderDirection.get(partKey), theme().itemHoveredColor.get(), hoverProgress);
        Color gradient = resolveTextStateColor(theme().itemActiveGradientColor.get(), theme().itemHoveredGradientColor.get(), hoverProgress);

        if (theme().sliderGradient.get()) GradientRenderer.render(renderer, x, y, width, height, gradient, color, theme().gradientRender.get());
        else renderer.quad(x, y, width, height, color);
    }

    default void renderCenteredTextOrTexture(GuiRenderer renderer, String text, double textWidth, GuiTexture texture,
                                             double x, double y, double width, double pad, Color color) {
        if (text != null) {
            renderText(renderer, text, x + width / 2 - textWidth / 2, y + pad, color);
        } else {
            double ts = theme().textHeight();
            renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, texture, color);
        }
    }

    default void renderCenteredSquare(GuiRenderer renderer, double x, double y, double width, double pad, Color color) {
        double ts = theme().textHeight();
        renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, color);
    }

    default void renderHorizontalGlyph(GuiRenderer renderer, double x, double y, double width, double height,
                                       double pad, double thickness, Color color) {
        renderer.quad(x + pad, y + height / 2 - thickness / 2, width - pad * 2, thickness, color);
    }

    default void renderVerticalGlyph(GuiRenderer renderer, double x, double y, double width, double height,
                                     double pad, double thickness, Color color) {
        renderer.quad(x + width / 2 - thickness / 2, y + pad, thickness, height - pad * 2, color);
    }

    default ConfirmColors confirmedColors(Color normalFg, boolean pressed, boolean mouseOver, boolean pressedOnce) {
        Color normalBg = theme().backgroundColor.get(pressed, mouseOver);
        if (pressedOnce) return new ConfirmColors(normalBg, normalFg);
        return new ConfirmColors(normalFg, normalBg);
    }
}
