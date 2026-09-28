package me.sophimoo.exeter.gui.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

import java.util.List;

import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

public final class ExeterStackedLayout {
    private ExeterStackedLayout() {}

    public static double availableWidth(WWidget widget) {
        double availableWidth = getWindowWidth();

        if (widget.parent instanceof WContainer container) {
            for (Cell<?> cell : container.cells) {
                if (cell.widget() == widget && cell.expandCellX && cell.width > 0) {
                    availableWidth = Math.min(availableWidth, cell.width);
                    break;
                }
            }
        }

        for (WWidget ancestor = widget.parent; ancestor != null; ancestor = ancestor.parent) {
            if (ancestor instanceof WWindow window && widget.theme instanceof BaseGuiTheme theme
                && theme.shouldUseFixedCategoryWidth(window.id)) {
                availableWidth = Math.min(availableWidth, theme.scaledFixedCategoryWidth());
            }

            if (ancestor.width > 0) availableWidth = Math.min(availableWidth, ancestor.width);
        }

        return Math.max(1, availableWidth);
    }

    public static int lineEnd(List<Cell<?>> cells, int start, double maxWidth, double spacing) {
        if (isLabel(cells.get(start))) return start + 1;

        double width = 0;
        int end = start;
        while (end < cells.size() && !isLabel(cells.get(end))) {
            Cell<?> cell = cells.get(end);
            double nextWidth = cellWidth(cell);
            if (cell.widget() instanceof WButton && nextWidth > maxWidth) {
                cell.widget().width = Math.max(0, maxWidth - cell.padLeft() - cell.padRight());
                nextWidth = maxWidth;
            }

            if (end > start) nextWidth += spacing;
            if (end > start && width + nextWidth > maxWidth) return end;
            width += nextWidth;
            end++;
        }

        return end;
    }

    public static double lineWidth(List<Cell<?>> cells, int start, int end, double spacing) {
        double width = 0;
        for (int i = start; i < end; i++) width += cellWidth(cells.get(i)) + (i > start ? spacing : 0);
        return width;
    }

    public static double lineHeight(List<Cell<?>> cells, int start, int end) {
        double height = 0;
        for (int i = start; i < end; i++) height = Math.max(height, cellHeight(cells.get(i)));
        return height;
    }

    public static double cellWidth(Cell<?> cell) {
        return cell.padLeft() + cell.widget().width + cell.padRight();
    }

    public static double cellHeight(Cell<?> cell) {
        return cell.padTop() + cell.widget().height + cell.padBottom();
    }

    private static boolean isLabel(Cell<?> cell) {
        return cell.widget() instanceof WLabel || cell.widget() instanceof WMultiLabel;
    }
}
