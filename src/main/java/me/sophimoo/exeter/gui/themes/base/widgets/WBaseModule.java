package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WBaseModule extends WPressable implements BaseWidget {
    private final Module module;
    private final String title;

    private double titleWidth;

    private double animationProgress1;

    private double animationProgress2;

    public WBaseModule(Module module, String title) {
        this.module = module;
        this.title = title;
        this.tooltip = module.description;

        if (module.isActive()) {
            animationProgress1 = 1;
            animationProgress2 = 1;
        } else {
            animationProgress1 = 0;
            animationProgress2 = 0;
        }
    }

    @Override
    public double pad() {
        return theme().scale(4);
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();

        if (titleWidth == 0) titleWidth = theme().textWidth(title);

        width = pad + titleWidth + pad;
        height = pad + theme().textHeight() + pad;
    }

    @Override
    protected void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) module.toggle();
        else if (button == GLFW_MOUSE_BUTTON_RIGHT) mc.setScreen(theme().moduleScreen(module));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();

        boolean isActive = module.isActive();
        boolean shouldFadeIn = isActive || mouseOver;
        boolean useOverlay = theme().moduleHoverOverlay.get();

        double fadeInSpeed = theme().moduleFadeInSpeed.get();
        double fadeOutSpeed = theme().moduleFadeOutSpeed.get();

        animationProgress1 += delta * (shouldFadeIn ? fadeInSpeed : fadeOutSpeed) * (shouldFadeIn ? 1 : -1);
        animationProgress1 = MathHelper.clamp(animationProgress1, 0, 1);

        animationProgress2 += delta * (isActive ? 1 : -1);
        animationProgress2 = MathHelper.clamp(animationProgress2, 0, 1);

        Color bgColor = isActive ? theme().moduleActiveBackground.get() : theme().moduleHoveredBackground.get();

        if (useOverlay) {
            // Overlay effect - draws inactive bg then overlays with faded color
            renderer.quad(x, y, width, height, theme().moduleInactiveBackground.get());

            if (animationProgress1 > 0) {
                Color fadedColor = new Color(bgColor.r, bgColor.g, bgColor.b, (int) (bgColor.a * animationProgress1));
                renderer.quad(x, y, width, height, fadedColor);
            }
        } else {
            // Slide effect - animates width
            if (animationProgress1 > 0) {
                renderer.quad(x, y, width * animationProgress1, height, bgColor);
            }
        }

        if (animationProgress2 > 0) {
            renderer.quad(x, y + height * (1 - animationProgress2), theme().scale(2), height * animationProgress2, theme().accentColor.get());
        }

        double thickness = theme().scale(theme().moduleOutlineThickness.get());
        if (thickness > 0) {
            Color outlineColor = theme().outlineColor.get(pressed, mouseOver);
            renderer.quad(this.x, this.y, width, thickness, outlineColor);
            renderer.quad(this.x, this.y + height - thickness, width, thickness, outlineColor);
            renderer.quad(this.x, this.y + thickness, thickness, height - 2 * thickness, outlineColor);
            renderer.quad(this.x + width - thickness, this.y + thickness, thickness, height - 2 * thickness, outlineColor);
        }

        double x = this.x + pad;
        double w = width - pad * 2;

        if (theme().moduleAlignment.get() == AlignmentX.Center) {
            x += w / 2 - titleWidth / 2;
        }
        else if (theme().moduleAlignment.get() == AlignmentX.Right) {
            x += w - titleWidth;
        }

        renderer.text(title, x, y + pad, theme().textColor.get(), false);
    }
}
