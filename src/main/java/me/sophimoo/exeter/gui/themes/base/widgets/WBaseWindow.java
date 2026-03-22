package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
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
    public void calculateSize() {
        super.calculateSize();

        if (theme().shouldUseFixedCategoryWidth(id)) {
            width = theme().scaledFixedCategoryWidth();
        }
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
            renderQuadWithOptionalBlur(renderer, x, y + header.height, width, height - header.height, theme().backgroundColor.get());
        }

        super.render(renderer, mouseX, mouseY, delta);

        if (scissor) renderer.scissorEnd();

        if (expanded || animProgress > 0) {
            double thickness = theme.scale(theme().windowOutlineThickness.get());
            if (thickness > 0) {
                double contentY = y + header.height;
                double contentHeight = (height - header.height) * animProgress;
                Color outlineColor = theme().windowOutlineColor.get();

                if (animProgress > 0) renderOutline(renderer, x, contentY, width, contentHeight, thickness, outlineColor);
            }
        }

        return false;
    }

    private class WBaseHeader extends WHeader {
        public WBaseHeader(WWidget icon) {
            super(icon);
        }

        @Override
        public void init() {
            super.init();

            Cell<?> titleCell = getTitleLabelCell();
            if (titleCell == null) return;

            AlignmentX alignment = theme().categoryTitleAlignment.get();
            if (alignment == AlignmentX.Center) {
                titleCell.center();
            } else if (alignment == AlignmentX.Right) {
                titleCell.right().centerY();
            }
        }

        @Override
        protected void onCalculateWidgetPositions() {
            super.onCalculateWidgetPositions();

            if (theme().categoryTitleAlignment.get() == AlignmentX.Left) {
                Cell<?> titleCell = getTitleLabelCell();
                if (titleCell != null && titleCell.widget() instanceof WLabel label) {
                    label.x = titleCell.x + titleCell.padLeft();
                }
            }
        }

        private Cell<?> getTitleLabelCell() {
            for (Cell<?> cell : cells) {
                if (cell.widget() instanceof WLabel) return cell;
            }

            return null;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderQuadWithOptionalBlur(renderer, x, y, width, height, theme().accentColor.get());
        }
    }
}
