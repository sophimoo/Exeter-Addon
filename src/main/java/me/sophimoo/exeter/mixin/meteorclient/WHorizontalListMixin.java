package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.widgets.ExeterWrappingList;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WHorizontalList.class, remap = false)
public abstract class WHorizontalListMixin extends WContainer implements ExeterWrappingList {
    @Shadow protected abstract double spacing();

    @Unique private boolean exeter$wrapping;
    @Unique private boolean exeter$stacked;

    @Override
    public void exeter$setWrapping(boolean wrapping) {
        exeter$wrapping = wrapping;
        if (wrapping) for (Cell<?> cell : cells) cell.expandX();
    }

    @Inject(method = "onCalculateSize", at = @At("RETURN"))
    private void exeter$calculateWrappedSize(CallbackInfo info) {
        if (!exeter$wrapping) return;

        double availableWidth = exeter$availableWidth();
        exeter$stacked = availableWidth > 0 && width > availableWidth;
        if (!exeter$stacked) return;

        width = 0;
        height = 0;

        for (int i = 0; i < cells.size(); i++) {
            Cell<?> cell = cells.get(i);
            if (i > 0) height += spacing();
            width = Math.max(width, exeter$cellWidth(cell));
            height += exeter$cellHeight(cell);
        }
    }

    @Inject(method = "onCalculateWidgetPositions", at = @At("HEAD"), cancellable = true)
    private void exeter$calculateWrappedPositions(CallbackInfo info) {
        if (!exeter$stacked) return;

        double y = this.y;
        for (int i = 0; i < cells.size(); i++) {
            Cell<?> cell = cells.get(i);
            if (i > 0) y += spacing();

            cell.x = x + cell.padLeft();
            cell.y = y + cell.padTop();
            cell.width = width - cell.padLeft() - cell.padRight();
            cell.height = cell.widget().height;
            cell.alignWidget();

            y += exeter$cellHeight(cell);
        }

        info.cancel();
    }

    @Unique
    private double exeter$availableWidth() {
        WWidget ancestor = parent;
        while (ancestor != null && ancestor.width <= 0) ancestor = ancestor.parent;
        return ancestor != null ? ancestor.width : 0;
    }

    @Unique
    private double exeter$cellWidth(Cell<?> cell) {
        return cell.padLeft() + cell.widget().width + cell.padRight();
    }

    @Unique
    private double exeter$cellHeight(Cell<?> cell) {
        return cell.padTop() + cell.widget().height + cell.padBottom();
    }
}
