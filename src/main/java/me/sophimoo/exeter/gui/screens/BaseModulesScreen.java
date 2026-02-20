package me.sophimoo.exeter.gui.screens;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static meteordevelopment.meteorclient.utils.Utils.getWindowHeight;
import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

public class BaseModulesScreen extends TabScreen {
    private final BaseGuiTheme theme;
    private WCategoryController controller;

    public BaseModulesScreen(GuiTheme theme) {
        super(theme, Tabs.get().getFirst());
        this.theme = (BaseGuiTheme) theme;
    }

    @Override
    public void initWidgets() {
        controller = add(new WCategoryController()).widget();

        WVerticalList help = add(theme.verticalList()).pad(4).bottom().widget();
        help.add(theme.label("Left click - Toggle module"));
        help.add(theme.label("Right click - Open module settings"));
    }

    protected void addIcon(WContainer container, Object icon) {
        if (icon instanceof net.minecraft.item.ItemStack stack) {
            container.add(theme.item(stack)).pad(2);
        } else {
            container.add(theme.label(icon != null ? icon.toString() : "")).pad(2);
        }
    }

    @Override
    protected void init() {
        super.init();
        controller.refresh();
    }

    private double spacing() {
        return theme.scale(theme.moduleSpacing.get());
    }

    protected void addModulesWithPadding(WContainer container, List<Module> modules) {
        double s = spacing();
        double outline = theme.scale(theme.windowOutlineThickness.get());
        double scaled = s + outline / theme.scale(1);

        for (int i = 0; i < modules.size(); i++) {
            var cell = container.add(theme.module(modules.get(i))).expandX();
            cell.padLeft(scaled).padRight(scaled);
            if (i == 0) cell.padTop(scaled);
            if (i == modules.size() - 1) cell.padBottom(scaled);
        }
    }

    protected WWindow createCategory(WContainer c, Category category, List<Module> modules) {
        WWindow w = theme.window(category.name);
        w.id = category.name;
        w.padding = w.spacing = 0;
        if (theme.categoryIcons()) {
            String iconText = null;
            ItemStack iconStack = null;

            try {
                java.lang.reflect.Field iconTextField = Category.class.getField("iconText");
                iconText = (String) iconTextField.get(category);
            } catch (NoSuchFieldException | IllegalAccessException e) {
            }

            try {
                java.lang.reflect.Field iconField = Category.class.getField("icon");
                Object icon = iconField.get(category);
                if (icon instanceof ItemStack) {
                    iconStack = (ItemStack) icon;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
            }

            if (iconText != null && !iconText.isEmpty()) {
                final String text = iconText;
                w.beforeHeaderInit = wContainer -> wContainer.add(theme.label(text)).pad(2);
            } else if (iconStack != null) {
                final ItemStack stack = iconStack;
                w.beforeHeaderInit = wContainer -> wContainer.add(theme.item(stack)).pad(2);
            }
        }
        c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = spacing();
        addModulesWithPadding(w, modules);
        return w;
    }

    protected <T> void addSearchItemsWithPadding(WContainer container, List<T> items, java.util.function.Function<T, Module> toModule) {
        double s = spacing();
        double outline = theme.scale(theme.windowOutlineThickness.get());
        double scaled = s + outline / theme.scale(1);
        int max = Config.get().moduleSearchCount.get();

        for (int i = 0; i < Math.min(items.size(), max); i++) {
            Module m = toModule.apply(items.get(i));
            String highlight = items.get(i) instanceof Pair ? ((Pair<Module, String>) items.get(i)).getRight() : null;
            var cell = container.add(highlight != null ? theme.module(m, highlight) : theme.module(m)).expandX();
            if (i == 0) cell.padTop(scaled);
            if (i == Math.min(items.size(), max) - 1) cell.padBottom(scaled);
        }
    }

    protected void createSearchW(WContainer w, String text) {
        if (text.isEmpty()) return;

        List<Pair<Module, String>> modules = Modules.get().searchTitles(text);
        if (!modules.isEmpty()) {
            WSection section = w.add(theme.section("Modules")).expandX().widget();
            section.spacing = spacing();
            addSearchItemsWithPadding(section, modules, Pair::getLeft);
        }

        Set<Module> settings = Modules.get().searchSettingTitles(text);
        if (!settings.isEmpty()) {
            WSection section = w.add(theme.section("Settings")).expandX().widget();
            section.spacing = spacing();
            addSearchItemsWithPadding(section, new ArrayList<>(settings), m -> m);
        }
    }

    protected WWindow createSearch(WContainer c) {
        WWindow w = theme.window("Search");
        w.id = "search";

        if (theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> addIcon(wContainer, Items.COMPASS.getDefaultStack());
        }

        c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.maxHeight -= 20;

        WVerticalList l = theme.verticalList();
        WTextBox text = w.add(theme.textBox("")).minWidth(140).expandX().widget();
        text.setFocused(true);
        text.action = () -> {
            l.clear();
            createSearchW(l, text.get());
        };

        w.add(l).expandX();
        createSearchW(l, text.get());

        return w;
    }

    protected Cell<WWindow> createFavorites(WContainer c) {
        List<Module> favorites = Modules.get().getAll().stream()
            .filter(m -> m.favorite)
            .sorted(Comparator.comparing(m -> m.name, String.CASE_INSENSITIVE_ORDER))
            .toList();

        if (favorites.isEmpty()) return null;

        WWindow w = theme.window("Favorites");
        w.id = "favorites";
        w.padding = w.spacing = 0;

        if (theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> addIcon(wContainer, Items.NETHER_STAR.getDefaultStack());
        }

        Cell<WWindow> cell = c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = spacing();

        addModulesWithPadding(w, favorites);
        return cell;
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Modules.get());
    }

    @Override
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Modules.get());
    }

    @Override
    public void reload() {}

    protected class WCategoryController extends WContainer {
        public final List<WWindow> windows = new ArrayList<>();
        private Cell<WWindow> favoritesCell;

        @Override
        public void init() {
            for (Category category : Modules.loopCategories()) {
                List<Module> modules = Modules.get().getGroup(category).stream()
                        .filter(m -> !Config.get().hiddenModules.get().contains(m))
                        .toList();
                if (!modules.isEmpty()) {
                    windows.add(createCategory(this, category, modules));
                }
            }
            windows.add(createSearch(this));
            refresh();
        }

        protected void refresh() {
            if (favoritesCell == null) {
                favoritesCell = createFavorites(this);
                if (favoritesCell != null) windows.add(favoritesCell.widget());
            } else {
                favoritesCell.widget().clear();
                remove(favoritesCell);
                windows.remove(favoritesCell.widget());
                favoritesCell = null;
                Cell<WWindow> newFavoritesCell = createFavorites(this);
                if (newFavoritesCell != null) {
                    favoritesCell = newFavoritesCell;
                    windows.add(favoritesCell.widget());
                }
            }
        }

        @Override
        protected void onCalculateWidgetPositions() {
            double pad = theme.scale(4);
            double h = theme.scale(40);
            double x = this.x + pad, y = this.y;

            for (Cell<?> cell : cells) {
                double ww = getWindowWidth(), wh = getWindowHeight();

                if (x + cell.width > ww) {
                    x = x + pad;
                    y += h;
                }
                if (x > ww) x = Math.max(0, ww / 2.0 - cell.width / 2.0);
                if (y > wh) y = Math.max(0, wh / 2.0 - cell.height / 2.0);

                cell.x = x;
                cell.y = y;
                cell.width = cell.widget().width;
                cell.height = cell.widget().height;
                cell.alignWidget();

                x += cell.width + pad;
            }
        }
    }
}
