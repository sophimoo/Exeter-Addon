package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.renderer.BlurRendererAccess;
import me.sophimoo.exeter.gui.renderer.WorldFramebufferCapture;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseWindow extends WWindow implements BaseWidget {
    public WBaseWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WBaseHeader(icon);
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (padding == 0) {
            padding = theme.scale(theme().windowOutlineThickness.get());
        }
        
        if (!visible) return true;

        boolean scissor = (animProgress != 0 && animProgress != 1) || (expanded && animProgress != 1);
        if (scissor) renderer.scissorStart(x, y, width, (height - header.height) * animProgress + header.height);

        if (expanded || animProgress > 0) {
            if (theme().widgetBlurStrength.get() > 0 && WorldFramebufferCapture.getInstance() != null) {
                ((BlurRendererAccess) renderer).blurredQuad(
                    x, y + header.height,
                    width, height - header.height,
                    WorldFramebufferCapture.getInstance().getBlurredTexture(),
                    theme().backgroundColor.get()
                );
            } else {
                renderer.quad(x, y + header.height, width, height - header.height, theme().backgroundColor.get());
            }
        }

        super.render(renderer, mouseX, mouseY, delta);

        if (scissor) renderer.scissorEnd();

        if (expanded || animProgress > 0) {
            double thickness = theme.scale(theme().windowOutlineThickness.get());
            if (thickness > 0) {
                double contentY = y + header.height;
                double contentHeight = (height - header.height) * animProgress;
                Color outlineColor = theme().windowOutlineColor.get();

                renderer.quad(x, contentY, width, thickness, outlineColor);

                if (animProgress > 0) {
                    renderer.quad(x, contentY + contentHeight - thickness, width, thickness, outlineColor);
                    renderer.quad(x, contentY + thickness, thickness, contentHeight - 2 * thickness, outlineColor);
                    renderer.quad(x + width - thickness, contentY + thickness, thickness, contentHeight - 2 * thickness, outlineColor);
                }
            }
        }

        return false;
    }

    private class WBaseHeader extends WHeader {
        public WBaseHeader(WWidget icon) {
            super(icon);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            // Apply blur behind the header if enabled
            if (theme().widgetBlurStrength.get() > 0 && WorldFramebufferCapture.getInstance() != null) {
                ((BlurRendererAccess) renderer).blurredQuad(
                    x, y,
                    width, height,
                    WorldFramebufferCapture.getInstance().getBlurredTexture(),
                    theme().accentColor.get()
                );
            } else {
                renderer.quad(this, theme().accentColor.get());
            }
        }
    }
}
