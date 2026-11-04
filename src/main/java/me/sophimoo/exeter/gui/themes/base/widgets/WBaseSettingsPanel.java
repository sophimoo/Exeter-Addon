package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

public class WBaseSettingsPanel extends WContainer implements BaseWidget {
    protected WVerticalList content;
    protected double outlinePadding;
    protected boolean insetBottom = true;
    private Cell<WVerticalList> contentCell;

    @Override
    public void init() {
        contentCell = super.add(theme.verticalList()).expandX().top();
        content = contentCell.widget();
    }

    @Override
    public <T extends WWidget> Cell<T> add(T widget) {
        Cell<T> cell = content.add(widget);
        // These are panel-level descriptions/actions, not nested settings rows.
        if (widget instanceof WLabel || widget instanceof WMultiLabel
            || widget instanceof WButton || widget instanceof WHorizontalList) {
            cell.padVertical(theme().globalPadding.get());
        }
        // Nested containers own their insets; standalone controls and action rows inherit ours.
        if (!(widget instanceof WContainer) || widget instanceof WHorizontalList) {
            cell.padHorizontal(BaseGuiTheme.ITEM_PADDING_X);
        }
        return cell;
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    protected void onCalculateSize() {
        // One inset around the whole panel, independent of its child lists.
        // Round the outline and item padding separately, then undo scaling for Cell.
        double scale = theme.scale(1);
        double outlineInset = Math.round(theme.scale(outlinePadding)) / scale;
        double paddingY = content.cells.isEmpty() ? 0 : Math.round(theme.scale(theme().itemPaddingY.get())) / scale;
        contentCell.padHorizontal(outlineInset)
            .padTop(outlineInset + paddingY)
            .padBottom(outlineInset + (insetBottom ? paddingY : 0));
        super.onCalculateSize();
    }
}
