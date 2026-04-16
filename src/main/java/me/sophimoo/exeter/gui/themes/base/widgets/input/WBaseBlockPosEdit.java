package me.sophimoo.exeter.gui.themes.base.widgets.input;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.marker.Marker;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import java.util.function.Consumer;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WBaseBlockPosEdit extends WVerticalList implements BaseWidget {
    public Runnable action;
    public Runnable actionOnRelease;

    private WTextBox textBoxX;
    private WTextBox textBoxY;
    private WTextBox textBoxZ;

    private Screen previousScreen;

    private BlockPos value;
    private BlockPos lastValue;

    private boolean clicking;

    public WBaseBlockPosEdit(BlockPos value) {
        this.value = value;
    }

    @Override
    public void init() {
        spacing = theme().scale(2);
        
        WHorizontalList coords = add(theme().horizontalList()).expandX().widget();
        addCoordTextBoxes(coords);

        if (Utils.canUpdate()) {
            WHorizontalList actions = add(theme().horizontalList()).expandX().widget();

            WButton click = actions.add(theme().button("Click")).expandX().widget();
            click.action = () -> {
                Modules.get().get(Marker.class).info("Click!\nRight click to pick a new position.\nLeft click to cancel.");

                clicking = true;
                MeteorClient.EVENT_BUS.subscribe(this);
                previousScreen = mc.currentScreen;
                mc.setScreen(null);
            };

            WButton here = actions.add(theme().button("Set Here")).expandX().widget();
            here.action = () -> {
                if (mc.player == null) return;

                lastValue = value;
                set(mc.player.getBlockPos());
                newValueCheck();
            };
        }
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (!clicking) return;

        clicking = false;
        event.cancel();
        MeteorClient.EVENT_BUS.unsubscribe(this);
        mc.setScreen(previousScreen);
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (!clicking) return;
        if (event.result.getType() == HitResult.Type.MISS) return;

        lastValue = value;
        set(event.result.getBlockPos());
        newValueCheck();

        clicking = false;
        event.cancel();
        MeteorClient.EVENT_BUS.unsubscribe(this);
        mc.setScreen(previousScreen);
    }

    private boolean filter(String text, char c) {
        boolean validChar;
        boolean validateRange = true;

        if (c == '-' && text.isEmpty()) {
            validChar = true;
            validateRange = false;
        } else {
            validChar = Character.isDigit(c);
        }

        if (validChar && validateRange) {
            try {
                Integer.parseInt(text + c);
            } catch (NumberFormatException ignored) {
                validChar = false;
            }
        }

        return validChar;
    }

    private WTextBox createCompactCoordBox(String initialValue, Consumer<WTextBox> commit) {
        WTextBox textBox = theme().textBox(initialValue, this::filter);
        textBox.minWidth = theme().scale(40);
        textBox.actionOnUnfocused = () -> commit.accept(textBox);
        return textBox;
    }

    public BlockPos get() {
        return value;
    }

    public void set(BlockPos value) {
        this.value = value;
        syncTextBoxes();
    }

    private void addCoordTextBoxes(WHorizontalList row) {
        textBoxX = row.add(createCompactCoordBox(Integer.toString(value.getX()), box -> updateFromTextBox(box, Axis.X))).expandX().widget();
        textBoxY = row.add(createCompactCoordBox(Integer.toString(value.getY()), box -> updateFromTextBox(box, Axis.Y))).expandX().widget();
        textBoxZ = row.add(createCompactCoordBox(Integer.toString(value.getZ()), box -> updateFromTextBox(box, Axis.Z))).expandX().widget();
    }

    private void updateFromTextBox(WTextBox textBox, Axis axis) {
        lastValue = value;

        if (textBox.get().isEmpty()) {
            set(new BlockPos(0, 0, 0));
            newValueCheck();
            return;
        }

        try {
            int parsed = Integer.parseInt(textBox.get());
            switch (axis) {
                case X -> set(new BlockPos(parsed, value.getY(), value.getZ()));
                case Y -> set(new BlockPos(value.getX(), parsed, value.getZ()));
                case Z -> set(new BlockPos(value.getX(), value.getY(), parsed));
            }
        } catch (NumberFormatException ignored) {
            syncTextBoxes();
        }

        newValueCheck();
    }

    private void syncTextBoxes() {
        if (textBoxX == null || textBoxY == null || textBoxZ == null) return;
        textBoxX.set(Integer.toString(value.getX()));
        textBoxY.set(Integer.toString(value.getY()));
        textBoxZ.set(Integer.toString(value.getZ()));
    }

    private void newValueCheck() {
        if (lastValue == null || !value.equals(lastValue)) {
            if (action != null) action.run();
            if (actionOnRelease != null) actionOnRelease.run();
        }
    }

    private enum Axis {
        X,
        Y,
        Z
    }
}
