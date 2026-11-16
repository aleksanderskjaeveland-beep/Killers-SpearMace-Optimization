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
    private int selectedOffset = 0;
    private static final int COLS = 12, ROWS = 7, CELL = 22;

    public SpearMaceSelector(Screen parent, boolean blocks) {
        super(Component.literal(blocks ? "Select Blocks for ESP" : "Select Tracer Entities"));
        this.parent = parent;
        this.blocks = blocks;
        if (blocks) for (Block b : BuiltInRegistries.BLOCK) if (b.asItem() != Items.AIR) all.add(b);
        if (!blocks) for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE)
            if (type != EntityType.MARKER) all.add(type);
        filter("");
    }
    private String nameOf(Object obj) {
        String id = blocks ? BuiltInRegistries.BLOCK.getKey((Block)obj).toString()
                           : BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>)obj).toString();
        String[] words = id.substring(id.indexOf(':') + 1).replace('_', ' ').split(" ");
        StringBuilder readable = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (readable.length() > 0) readable.append(' ');
            readable.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return readable.toString();
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
    private int gridRows() { return Math.max(2, Math.min(6, (height - 220) / CELL)); }
    private int pickedTop() { return top() + gridRows() * CELL + 26; }
    private int pickedRows() { return Math.max(1, Math.min(8, (height - pickedTop() - 38) / 18)); }
    private List<Object> selected() {
        List<Object> result = new ArrayList<>();
        if (blocks) result.addAll(SpearMaceClient.BLOCKS);
        else result.addAll(SpearMaceClient.ENTITIES);
        return result;
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        g.drawCenteredString(font, "Click icons to select • mouse wheel to scroll • " +
            (blocks ? SpearMaceClient.BLOCKS.size() : SpearMaceClient.ENTITIES.size()) + " selected",
            width / 2, 24, 0xAAAAAA);
        int count = Math.min(COLS * gridRows(), visible.size() - Math.min(offset, visible.size()));
        for (int i = 0; i < count; i++) {
            Object o = visible.get(offset + i);
            int x = left() + (i % COLS) * CELL, y = top() + (i / COLS) * CELL;
            boolean selected = blocks ? SpearMaceClient.BLOCKS.contains((Block)o)
                : SpearMaceClient.ENTITIES.contains((EntityType<?>)o);
            g.fill(x, y, x + 20, y + 20, selected ? 0xAA5F39BC : 0x88333333);
            g.renderItem(icon(o), x + 2, y + 2);
            if (mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20) {
                String label = nameOf(o);
                int labelX = Math.min(mouseX + 12, width - font.width(label) - 12);
                int labelY = Math.min(mouseY + 14, height - 20);
                g.fill(labelX - 3, labelY - 3, labelX + font.width(label) + 4, labelY + 12, 0xE0101019);
                g.drawString(font, label, labelX, labelY, 0xFFFFFF);
            }
        }
        List<Object> picked = selected();
        int y0 = pickedTop(), maxRows = pickedRows();
        g.drawString(font, "Selected (" + picked.size() + ") - click X to remove", left(), y0 - 15, 0xEEEEFF);
        selectedOffset = Math.max(0, Math.min(selectedOffset, Math.max(0, picked.size() - maxRows)));
        for (int i = selectedOffset; i < Math.min(picked.size(), selectedOffset + maxRows); i++) {
            int y = y0 + (i - selectedOffset) * 18;
            String label = nameOf(picked.get(i));
            g.drawString(font, label, left() + 2, y + 3, 0xFFFFFF);
            int xx = left() + COLS * CELL - 20;
            g.fill(xx, y, xx + 17, y + 16, 0xAA883344);
            g.drawCenteredString(font, "X", xx + 8, y + 4, 0xFFFFFF);
        }
        if (picked.size() > maxRows)
            g.drawString(font, "Scroll over this list to see more", left(), height - 42, 0xAAAAAA);
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
            List<Object> picked = selected();
            int xx = left() + COLS * CELL - 20;
            int listY = (int)my - pickedTop();
            if (mx >= xx && mx < xx + 17 && listY >= 0 && listY < pickedRows() * 18) {
                int index = selectedOffset + listY / 18;
                if (index < picked.size()) {
                    Object chosen = picked.get(index);
                    if (blocks) SpearMaceClient.BLOCKS.remove((Block)chosen);
                    else SpearMaceClient.ENTITIES.remove((EntityType<?>)chosen);
                    selectedOffset = Math.max(0, Math.min(selectedOffset, Math.max(0, selected().size() - pickedRows())));
                    return true;
                }
            }
            int x = (int)mx - left(), y = (int)my - top();
            if (x >= 0 && y >= 0 && x < COLS * CELL && y < gridRows() * CELL) {
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
        if (my >= pickedTop()) {
            selectedOffset = Math.max(0, Math.min(Math.max(0, selected().size() - pickedRows()), selectedOffset - (int)Math.signum(scrollY) * 2));
        } else {
            int max = Math.max(0, ((visible.size() - 1) / COLS - gridRows() + 1) * COLS);
            offset = Math.max(0, Math.min(max, offset - (int)Math.signum(scrollY) * COLS * 2));
        }
        return true;
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}
