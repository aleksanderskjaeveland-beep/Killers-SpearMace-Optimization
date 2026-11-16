package dev.killers.spearmace;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SpearMaceScreen extends Screen {
    public SpearMaceScreen() { super(Component.literal("Killer's SpearMace Optimization")); }

    @Override protected void init() {
        int x = width / 2 - 115, y = height / 2 - 76;
        addRenderableWidget(Button.builder(Component.literal("Block ESP: " + state(SpearMaceClient.blockEspEnabled)), b -> {
            SpearMaceClient.blockEspEnabled = !SpearMaceClient.blockEspEnabled;
            rebuildWidgets();
        }).bounds(x, y, 175, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Choose Blocks..."), b ->
            minecraft.setScreen(new SpearMaceSelector(this, true)))
            .bounds(x + 178, y, 100, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Tracers: " + state(SpearMaceClient.tracerEnabled)), b -> {
            SpearMaceClient.tracerEnabled = !SpearMaceClient.tracerEnabled;
            rebuildWidgets();
        }).bounds(x, y + 29, 175, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Choose Entities..."), b ->
            minecraft.setScreen(new SpearMaceSelector(this, false)))
            .bounds(x + 178, y + 29, 100, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Flight: " + state(SpearMaceClient.flightEnabled)), b -> {
            SpearMaceClient.flightEnabled = !SpearMaceClient.flightEnabled;
            rebuildWidgets();
        }).bounds(x, y + 58, 175, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Flight speed..."), b ->
            minecraft.setScreen(new SpearMaceFlightScreen(this)))
            .bounds(x + 178, y + 58, 100, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
            .bounds(x, y + 100, 278, 20).build());
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        int x = width / 2 - 115, y = height / 2 - 76;
        if (event.button() == 1 && event.x() >= x && event.x() <= x + 278) {
            if (event.y() >= y && event.y() <= y + 20) {
                minecraft.setScreen(new SpearMaceSelector(this, true));
                return true;
            }
            if (event.y() >= y + 29 && event.y() <= y + 49) {
                minecraft.setScreen(new SpearMaceSelector(this, false));
                return true;
            }
            if (event.y() >= y + 58 && event.y() <= y + 78) {
                minecraft.setScreen(new SpearMaceFlightScreen(this));
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }

    private static String state(boolean enabled) { return enabled ? "ON" : "OFF"; }
}
