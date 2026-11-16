package dev.killers.spearmace;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SpearMaceFlightScreen extends Screen {
    private final Screen parent;
    public SpearMaceFlightScreen(Screen parent) {
        super(Component.literal("Flight Settings"));
        this.parent = parent;
    }
    @Override protected void init() {
        int x = width / 2 - 110, y = height / 2 - 42;
        addRenderableWidget(Button.builder(Component.literal("Slower"), b -> {
            SpearMaceClient.flySpeed = Math.max(0.2, Math.round((SpearMaceClient.flySpeed - 0.2) * 10) / 10.0);
            rebuildWidgets();
        }).bounds(x, y, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Faster"), b -> {
            SpearMaceClient.flySpeed = Math.min(10.0, Math.round((SpearMaceClient.flySpeed + 0.2) * 10) / 10.0);
            rebuildWidgets();
        }).bounds(x + 120, y, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Vertical -"), b -> {
            SpearMaceClient.verticalSpeed = Math.max(0.1, Math.round((SpearMaceClient.verticalSpeed - 0.1) * 10) / 10.0);
            rebuildWidgets();
        }).bounds(x, y + 42, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Vertical +"), b -> {
            SpearMaceClient.verticalSpeed = Math.min(5.0, Math.round((SpearMaceClient.verticalSpeed + 0.1) * 10) / 10.0);
            rebuildWidgets();
        }).bounds(x + 120, y + 42, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
            .bounds(x, y + 85, 220, 20).build());
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public void render(net.minecraft.client.gui.GuiGraphics graphics, int mx, int my, float dt) {
        super.render(graphics, mx, my, dt);
        graphics.drawCenteredString(font, "Horizontal speed: " + String.format(java.util.Locale.ROOT, "%.1f", SpearMaceClient.flySpeed),
            width / 2, height / 2 - 67, 0xFFFFFF);
        graphics.drawCenteredString(font, "Vertical speed: " + String.format(java.util.Locale.ROOT, "%.1f", SpearMaceClient.verticalSpeed),
            width / 2, height / 2 - 24, 0xFFFFFF);
    }
}
