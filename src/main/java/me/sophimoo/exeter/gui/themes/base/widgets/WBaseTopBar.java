package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.screen.Screen;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WBaseTopBar extends WTopBar implements BaseWidget {
    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().backgroundColor.get(pressed, hovered);
    }

    @Override
    protected Color getNameColor() {
        return theme().textColor.get();
    }

    @Override
    public void init() {
        for (Tab tab : Tabs.get()) {
            add(new WBaseTopBarButton(tab));
        }
    }

    protected class WBaseTopBarButton extends WTopBarButton {
        private final Tab myTab;

        public WBaseTopBarButton(Tab tab) {
            super(tab);
            this.myTab = tab;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double pad = pad();
            Screen screen = mc.currentScreen;
            boolean isActiveTab = screen instanceof TabScreen && ((TabScreen) screen).tab == myTab;
            Color color = getButtonColor(pressed || isActiveTab, mouseOver);

            renderQuadWithOptionalBlur(renderer, x, y, width, height, color);
            renderText(renderer, myTab.name, x + pad, y + pad, getNameColor());
        }
    }
}
