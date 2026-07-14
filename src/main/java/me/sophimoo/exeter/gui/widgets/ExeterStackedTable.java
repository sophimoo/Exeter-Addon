package me.sophimoo.exeter.gui.widgets;

import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;

public interface ExeterStackedTable {
    void exeter$setStacked(boolean stacked);

    static void mark(WWidget widget) {
        if (widget instanceof ExeterStackedTable table) table.exeter$setStacked(true);
        if (widget instanceof ExeterWrappingList list) list.exeter$setWrapping(true);
        if (widget instanceof WContainer container) {
            for (var cell : container.cells) {
                mark(cell.widget());
                if (cell.widget() instanceof ExeterWrappingList) cell.expandX();
            }
        }
    }
}
