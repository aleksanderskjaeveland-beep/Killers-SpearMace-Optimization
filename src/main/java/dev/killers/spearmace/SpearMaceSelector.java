package dev.killers.spearmace;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import java.util.*;

public final class SpearMaceSelector extends Screen {
    private final Screen parent;
    private final boolean blocks;
    private final List<Object> all = new ArrayList<>();
    private final List<Object> visible = new ArrayList<>();
    private EditBox search;
    private int offset = 0;
    private static final int COLS = 12, ROWS = 7, CELL = 22;

    public SpearMaceSelector(Screen parent, boolean blocks) {
        super(Component.literal(blocks ? "Select Blocks for ESP" : "Select Tracer Entities"));
        this.parent = parent;
        this.blocks = blocks;
        if (blocks) for (Block b : BuiltInRegistries.BLOCK) if (b.asItem() != Items.AIR) all.add(b);
        else {} // keep the branch explicit
        if (!blocks) for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE)
            if (type != EntityType.MARKER) all.add(type);
        filter("");
    }
    private String nameOf(Object obj) {
        return blocks ? BuiltInRegistries.BLOCK.getKey((Block)obj).toString()
                      : BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>)obj).toString();
    }
    private void filter(String term) {
        visible.clear();
        String q = term.trim().toLowerCase(java.util.Locale.ROOT);
        for (Object o : all) if (nameOf(o).toLowerCase(java.util.Locale.ROOT).contains(q)) visible.add(o);
        offset = 0;
    }
    @Override protected void init() {
        search = new EditBox(font, width / 2 - 132, 36, 264, 20, Component.literal("Search"));
        search.setHint(Component.literal("Search blocks or entities..."));
        search.setResponder(this::filter);
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
            .bounds(width / 2 - 50, height - 28, 100, 20).build());
    }
    private int left() { return width / 2 - (COLS * CELL) / 2; }
    private int top() { return 69; }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        g.drawCenteredString(font, "Click icons to select • mouse wheel to scroll • " +
            (blocks ? SpearMaceClient.BLOCKS.size() : SpearMaceClient.ENTITIES.size()) + " selected",
            width / 2, 24, 0xAAAAAA);
        int count = Math.min(COLS * ROWS, visible.size() - Math.min(offset, visible.size()));
        for (int i = 0; i < count; i++) {
            Object o = visible.get(offset + i);
            int x = left() + (i % COLS) * CELL, y = top() + (i / COLS) * CELL;
            boolean selected = blocks ? SpearMaceClient.BLOCKS.contains((Block)o)
                : SpearMaceClient.ENTITIES.contains((EntityType<?>)o);
            g.fill(x, y, x + 20, y + 20, selected ? 0xAA5F39BC : 0x88333333);
            g.renderItem(icon(o), x + 2, y + 2);
            if (mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20)
                g.drawString(font, nameOf(o), 8, height - 42, 0xFFFFFF);
        }
    }
    private ItemStack icon(Object o) {
        if (blocks) return new ItemStack((Block)o);
        EntityType<?> type = (EntityType<?>)o;
        if (type == EntityType.PLAYER) return new ItemStack(Items.PLAYER_HEAD);
        SpawnEggItem egg = SpawnEggItem.byId(type);
        return egg == null ? new ItemStack(Items.NAME_TAG) : new ItemStack(egg);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mx = event.x(), my = event.y();
        if (event.button() == 0) {
            int x = (int)mx - left(), y = (int)my - top();
            if (x >= 0 && y >= 0 && x < COLS * CELL && y < ROWS * CELL) {
                int col = x / CELL, row = y / CELL, index = offset + row * COLS + col;
                if (index < visible.size()) {
                    Object o = visible.get(index);
                    if (blocks) {
                        Block b = (Block)o;
                        if (!SpearMaceClient.BLOCKS.add(b)) SpearMaceClient.BLOCKS.remove(b);
                    } else {
                        EntityType<?> type = (EntityType<?>)o;
                        if (!SpearMaceClient.ENTITIES.add(type)) SpearMaceClient.ENTITIES.remove(type);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubled);
    }
    @Override public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        int max = Math.max(0, ((visible.size() - 1) / COLS - ROWS + 1) * COLS);
        offset = Math.max(0, Math.min(max, offset - (int)Math.signum(scrollY) * COLS * 2));
        return true;
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}
