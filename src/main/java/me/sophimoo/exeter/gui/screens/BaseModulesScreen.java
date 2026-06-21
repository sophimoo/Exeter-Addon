package me.sophimoo.exeter.gui.screens;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import me.sophimoo.exeter.gui.themes.base.widgets.WBaseModule;
import me.sophimoo.exeter.gui.themes.base.widgets.WBaseWindow;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.MacWindowUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static meteordevelopment.meteorclient.utils.Utils.getWindowHeight;
import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SUPER;

public class BaseModulesScreen extends TabScreen {
    private final BaseGuiTheme theme;
    private WCategoryController controller;
    private WVerticalList footer;
    private WTextBox searchTextBox;
    private final List<WBaseModule> expandedModules = new ArrayList<>();
    private final Set<Module> searchMatches = new HashSet<>();
    private String searchText = "";
    private boolean expandedModulesDirty;
    private boolean showGrid;

    public BaseModulesScreen(GuiTheme theme) {
        super(theme, Tabs.get().getFirst());
        this.theme = (BaseGuiTheme) theme;
    }

    @Override
    public void initWidgets() {
        showGrid = false;

        controller = add(new WCategoryController()).widget();

        WVerticalList footer = add(theme.verticalList()).pad(4).bottom().widget();
        this.footer = footer;
        WTextBox text = footer.add(theme.textBox(searchText)).minWidth(theme.scale(180)).widget();
        text.action = () -> updateSearch(text.get());
        searchTextBox = text;

        footer.add(theme.label("Left click - Toggle module"));
        footer.add(theme.label(theme.inlineModuleSettings.get()
            ? "Right click - Expand module settings"
            : "Right click - Open module settings"));

        updateSearch(searchText);
    }

    // https://github.com/X-C-0/catppuccin-addon/blob/d642959fbaa9e5757013ea38f57556eb88c8b822/src/main/java/me/pindour/catppuccin/gui/screens/CatppuccinModulesScreen.java#L80
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);

        int gridSize = theme.snappingGridSize.get();
        if (!showGrid || gridSize <= 0) return;

        Color gridColor = new Color(theme.outlineColor.get());
        gridColor.a = Math.min(gridColor.a, 60);

        int packedColor = Color.fromRGBA(gridColor.r, gridColor.g, gridColor.b, gridColor.a);
        int windowWidth = (int) getWindowWidth();
        int windowHeight = (int) getWindowHeight();

        for (int x = 0; x <= windowWidth; x += gridSize) {
            context.drawVerticalLine(x, 0, windowHeight, packedColor);
        }

        for (int y = 0; y <= windowHeight; y += gridSize) {
            context.drawHorizontalLine(0, windowWidth, y, packedColor);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (expandedModulesDirty) {
            refreshExpandedModules();
            expandedModulesDirty = false;
        }

        if (theme.inlineModuleSettings.get() && !expandedModules.isEmpty()) {
            for (WBaseModule module : expandedModules) {
                module.tickSettings();
            }
        }
    }

    public void requestExpandedModulesRefresh() {
        if (theme.inlineModuleSettings.get()) expandedModulesDirty = true;
    }

    private void refreshExpandedModules() {
        if (controller == null) return;
        expandedModules.clear();
        collectExpandedModules(controller, expandedModules);
    }

    private void collectExpandedModules(WContainer container, List<WBaseModule> out) {
        if (container == null) return;
        for (Cell<?> cell : container.cells) {
            WWidget widget = cell.widget();
            if (widget instanceof WBaseModule module) {
                if (module.isSettingsExpanded()) out.add(module);
            } else if (widget instanceof WContainer nested) {
                collectExpandedModules(nested, out);
            }
        }
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
        refreshExpandedModules();
        expandedModulesDirty = false;
        invalidate();
    }

    private double spacing() {
        return theme.scale(theme.moduleSpacing.get());
    }

    protected void addModulesWithPadding(WContainer container, List<Module> modules) {
        double s = spacing();
        double outline = theme.scale(theme.windowOutlineThickness.get());
        double scaled = s + outline / theme.scale(1);
        double edgePadding = Math.max(scaled, 1);

        for (int i = 0; i < modules.size(); i++) {
            var cell = container.add(theme.module(modules.get(i))).expandX();
            cell.padLeft(scaled).padRight(scaled);
            if (i == 0) cell.padTop(edgePadding);
            if (i == modules.size() - 1) cell.padBottom(edgePadding);
        }
    }

    private WBaseWindow modulesWindow(String title) {
        WBaseWindow window = theme.window(title);
        if (theme.snapModuleCategories.get()) window.initSnapping(this);
        return window;
    }

    protected WWindow createCategory(WContainer c, Category category, List<Module> modules) {
        WBaseWindow w = modulesWindow(category.name);
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

    private void updateSearch(String text) {
        searchText = text;
        searchMatches.clear();

        if (!searchText.isEmpty()) {
            int max = Config.get().moduleSearchCount.get();

            Modules.get().searchTitles(searchText).stream()
                .filter(result -> matchesModuleSearch(result.getLeft(), searchText))
                .limit(max)
                .forEach(result -> searchMatches.add(result.getLeft()));
        }

        if (controller != null) controller.invalidate();
        invalidate();
    }

    public boolean isSearchActive() {
        return !searchText.isEmpty();
    }

    public boolean isModuleSearchMatch(Module module) {
        return !isSearchActive() || searchMatches.contains(module);
    }

    private boolean matchesModuleSearch(Module module, String text) {
        if (Utils.searchTextDefault(module.title, text, false)) return true;
        if (!Config.get().moduleAliases.get()) return false;

        for (String alias : module.aliases) {
            if (Utils.searchTextDefault(alias, text, false)) return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (locked) return false;

        boolean control = MacWindowUtil.IS_MAC ? input.modifiers() == GLFW_MOD_SUPER : input.modifiers() == GLFW_MOD_CONTROL;
        if (control && input.key() == GLFW_KEY_F) {
            if (searchTextBox != null) {
                searchTextBox.setFocused(true);
                searchTextBox.setCursorMax();
            }

            return true;
        }

        return super.keyPressed(input);
    }

    protected Cell<WWindow> createFavorites(WContainer c) {
        List<Module> favorites = Modules.get().getAll().stream()
            .filter(m -> m.favorite)
            .sorted(Comparator.comparing(m -> m.name, String.CASE_INSENSITIVE_ORDER))
            .toList();

        if (favorites.isEmpty()) return null;

        WBaseWindow w = modulesWindow("Favorites");
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

    public void unfocusSearchTextBox() {
        if (searchTextBox != null) {
            searchTextBox.setFocused(false);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !Modules.get().isBinding();
    }

    @Override
    public void reload() {}

    public void showGrid(boolean show) {
        showGrid = show;
    }

    public boolean showGrid() {
        return showGrid;
    }

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

            BaseModulesScreen.this.requestExpandedModulesRefresh();
        }

        @Override
        public void calculateSize() {
            if (footer != null) footer.calculateSize();

            double listPad = theme.scale(8);
            double footerHeight = footer != null ? footer.height : 0;
            double tabBarHeight = theme.pad() * 2 + theme.textHeight();
            double maxViewHeight = getWindowHeight() - footerHeight - tabBarHeight - listPad * 2 - theme.textHeight(true) - theme.scale(8);

            for (WWindow window : windows) window.view.maxHeight = Math.max(theme.scale(20), maxViewHeight);

            super.calculateSize();
        }

        @Override
        protected void onCalculateWidgetPositions() {
            int ww = (int) getWindowWidth(), wh = (int) getWindowHeight();
            double gridPad = theme.scale(4);
            double listPad = theme.scale(8);
            double topY = theme.pad() * 2 + theme.textHeight() + listPad;
            double bottomY = (footer != null ? footer.y : wh) - listPad;
            double rowHeight = 0;

            double x = this.x + gridPad, y = topY;

            for (Cell<?> cell : cells) {
                WWidget widget = cell.widget();

                if (x + widget.width > ww) {
                    x = this.x + gridPad;
                    y += rowHeight + gridPad;
                    rowHeight = 0;
                }
                if (x > ww) x = Math.max(0, ww / 2.0 - widget.width / 2.0);
                if (y + widget.height > bottomY) y = Math.max(topY, bottomY - widget.height);

                cell.x = x;
                cell.y = y;
                cell.width = widget.width;
                cell.height = widget.height;
                cell.alignWidget();

                x += cell.width + gridPad;
                rowHeight = Math.max(rowHeight, widget.height);
            }
        }
    }
}
