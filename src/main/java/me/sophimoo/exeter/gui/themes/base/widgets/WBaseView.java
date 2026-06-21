package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import net.minecraft.client.gui.Click;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class WBaseView extends WView implements BaseWidget {
    private boolean scrollbarPressed;

    @Override
    public boolean onMouseClicked(Click click, boolean doubled) {
        if (handleMouseOver && click.button() == GLFW_MOUSE_BUTTON_LEFT && !doubled) {
            scrollbarPressed = true;
            return super.onMouseClicked(click, doubled);
        }

        return false;
    }

    @Override
    public boolean onMouseReleased(Click click) {
        scrollbarPressed = false;
        return super.onMouseReleased(click);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (canScroll && hasScrollBar) {
            renderer.quad(handleX(), handleY(), handleWidth(), handleHeight(), theme().scrollbarColor.get(scrollbarPressed, handleMouseOver));
        }
    }

    @Override
    public boolean isWidgetInView(WWidget widget) {
        double tolerance = theme().inlineModuleSettings.get() ? Math.max(1, theme().scale(theme().windowOutlineThickness.get())) : 0;
        return widget.y < y + height + tolerance && widget.y + widget.height > y - tolerance;
    }
}
