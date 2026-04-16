package me.sophimoo.exeter.gui.themes.base.widgets.input;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
 import me.sophimoo.exeter.gui.themes.base.widgets.WBaseLabel;
 import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
 import meteordevelopment.meteorclient.gui.utils.CharFilter;
 import meteordevelopment.meteorclient.gui.widgets.WWidget;
 import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
 import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
 import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
 import meteordevelopment.meteorclient.utils.render.color.Color;
 import net.minecraft.client.gui.Click;
 import net.minecraft.util.math.MathHelper;
 
 import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WBaseTextBox extends WTextBox implements BaseWidget {
    private boolean cursorVisible;
    private double cursorTimer;
    private boolean textAnchorBottomLeft;

    private double animProgress;

    public WBaseTextBox(String text, String placeholder, CharFilter filter, Class<? extends Renderer> renderer) {
        super(text, placeholder, filter, renderer);
    }

    public void setTextAnchorBottomLeft(boolean textAnchorBottomLeft) {
         this.textAnchorBottomLeft = textAnchorBottomLeft;
     }
 
     @Override
     public boolean onMouseClicked(Click click, boolean doubled) {
         if (mouseOver && click.button() == GLFW_MOUSE_BUTTON_RIGHT) {
             // Focus without clearing text (unlike base WTextBox)
             setFocused(true);
             return true;
         }
         return super.onMouseClicked(click, doubled);
     }
 
     @Override
     protected WContainer createCompletionsRootWidget() {
        return new WVerticalList() {
            @Override
            protected void onRender(GuiRenderer renderer1, double mouseX, double mouseY, double delta) {
                double s = theme().scale(2);
                Color c = theme().outlineColor.get();

                Color col = theme().backgroundColor.get();
                int preA = col.a;
                col.a += col.a / 2;
                col.validate();
                renderer1.quad(this, col);
                col.a = preA;

                renderer1.quad(x, y + height - s, width, s, c);
                renderer1.quad(x, y, s, height - s, c);
                renderer1.quad(x + width - s, y, s, height - s, c);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends WWidget & ICompletionItem> T createCompletionsValueWidth(String completion, boolean selected) {
        return (T) new CompletionItem(completion, false, selected);
    }

    private static class CompletionItem extends WBaseLabel implements ICompletionItem {
        private static final Color SELECTED_COLOR = new Color(255, 255, 255, 15);

        private boolean selected;

        public CompletionItem(String text, boolean title, boolean selected) {
            super(text, title);
            this.selected = selected;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            super.onRender(renderer, mouseX, mouseY, delta);

            if (selected) renderer.quad(this, SELECTED_COLOR);
        }

        @Override
        public boolean isSelected() {
            return selected;
        }

        @Override
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public String getCompletion() {
            return text;
        }
    }

    @Override
    protected void onCursorChanged() {
        cursorVisible = true;
        cursorTimer = 0;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (cursorTimer >= 1) {
            cursorVisible = !cursorVisible;
            cursorTimer = 0;
        }
        else {
            cursorTimer += delta * 1.75;
        }

        renderBackground(renderer, this, false, false);

        double pad = pad();
        double textY = textAnchorBottomLeft ? y + height - pad - theme().textHeight() : y + pad;

        renderer.scissorStart(x + pad, y + pad, width - pad * 2, height - pad * 2);

        // Calculate text X position (center if fits, left align if overflows)
        double textX = x + pad - getOverflowWidthForRender();

        // Text content
        if (!text.isEmpty()) {
            this.renderer.render(renderer, textX, textY, text, theme().textColor.get());
        }
        else if (placeholder != null) {
            double placeholderWidth = theme().textWidth(placeholder);
            double placeholderX = placeholderWidth > width - pad * 2 ? x + pad : x + (width - placeholderWidth) / 2;
            this.renderer.render(renderer, placeholderX, textY, placeholder, theme().placeholderColor.get());
        }

        // Text highlighting
        if (focused && (cursor != selectionStart || cursor != selectionEnd)) {
            double selStart = textX + getTextWidth(selectionStart);
            double selEnd = textX + getTextWidth(selectionEnd);

            renderer.quad(selStart, textY, selEnd - selStart, theme().textHeight(), theme().textHighlightColor.get());
        }

        // Cursor
        animProgress += delta * 10 * (focused && cursorVisible ? 1 : -1);
        animProgress = MathHelper.clamp(animProgress, 0, 1);

        if ((focused && cursorVisible) || animProgress > 0) {
            renderer.setAlpha(animProgress);
            renderer.quad(textX + getTextWidth(cursor), textY, theme().scale(1), theme().textHeight(), theme().textColor.get());
            renderer.setAlpha(1);
        }

        renderer.scissorEnd();
    }

    @Override
    protected double getOverflowWidthForRender() {
        double textWidth = getTextWidth(text.length());
        double maxTextWidth = width - pad() * 2;
        if (textWidth > maxTextWidth) return textStart;

        double centeredTextX = x + (width - textWidth) / 2;
        return x + pad() - centeredTextX;
    }
}
