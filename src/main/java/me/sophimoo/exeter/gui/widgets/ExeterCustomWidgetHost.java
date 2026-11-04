package me.sophimoo.exeter.gui.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;

public class ExeterCustomWidgetHost extends WContainer {
    private final WWidget content;
    private final boolean expandContentX;

    public ExeterCustomWidgetHost(WWidget content) {
        this.content = content;
        this.expandContentX = content instanceof WContainer;
    }

    @Override
    public void init() {
        var cell = add(content);
        if (theme instanceof BaseGuiTheme) {
            cell.padHorizontal(BaseGuiTheme.ITEM_PADDING_X).padVertical(BaseGuiTheme.ITEM_PADDING_Y);
        }
        if (expandContentX) cell.expandX();
    }

    @Override
    public void calculateSize() {
        // Custom module widgets can rebuild their subtree after init, so re-mark before layout.
        ExeterStackedTable.mark(content);
        constrain(content, 0);
        super.calculateSize();

        double availableWidth = ExeterStackedLayout.availableWidth(this);
        if (availableWidth > 0) {
            Cell<?> cell = cells.get(0);
            constrain(content, Math.max(0, availableWidth - cell.padLeft() - cell.padRight()));
            super.calculateSize();
        }
    }

    private void constrain(WWidget widget, double maxWidth) {
        if (widget instanceof ExeterWidthConstrained constrained) constrained.exeter$setMaxWidth(maxWidth);

        if (!(widget instanceof WContainer container)) return;

        for (Cell<?> cell : container.cells) {
            double childWidth = Math.max(0, maxWidth - cell.padLeft() - cell.padRight());
            constrain(cell.widget(), childWidth);
        }
    }
}
