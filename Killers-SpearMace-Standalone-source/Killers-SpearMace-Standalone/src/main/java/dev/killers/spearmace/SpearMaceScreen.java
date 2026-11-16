package dev.killers.spearmace;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SpearMaceScreen extends Screen {
    public SpearMaceScreen() {
        super(Component.literal("Killer's SpearMace Optimization"));
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 105;
        int y = this.height / 2 - 45;
        addRenderableWidget(Button.builder(espLabel(), b -> {
            SpearMaceClient.setEsp(!SpearMaceClient.espEnabled);
            b.setMessage(espLabel());
        }).bounds(x, y, 210, 20).build());

        addRenderableWidget(Button.builder(flightLabel(), b -> {
            SpearMaceClient.setFlight(!SpearMaceClient.flightEnabled);
            b.setMessage(flightLabel());
        }).bounds(x, y + 26, 210, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
            .bounds(x, y + 57, 210, 20).build());
    }

    private Component espLabel() { return Component.literal("Entity ESP: " + (SpearMaceClient.espEnabled ? "ON" : "OFF")); }
    private Component flightLabel() { return Component.literal("Flight: " + (SpearMaceClient.flightEnabled ? "ON" : "OFF")); }
}
