package dev.killers.spearmace;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.Locale;

public final class SpearMaceFlightScreen extends Screen {
    private final Screen parent;
    private boolean recording = false;

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
        addRenderableWidget(Button.builder(Component.literal("Flight toggle key: " + (recording ? "Press a key..." : keyName())), b -> {
            recording = true;
            rebuildWidgets();
        }).bounds(x, y + 43, 220, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
            .bounds(x, y + 88, 220, 20).build());
    }
    private static String keyName() {
        int key = SpearMaceClient.flightToggleKey;
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null && !name.isBlank()) return name.toUpperCase(Locale.ROOT);
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25)
            return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        return "Key " + key;
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if (recording) {
            if (event.key() != GLFW.GLFW_KEY_ESCAPE && event.key() != GLFW.GLFW_KEY_UNKNOWN)
                SpearMaceClient.flightToggleKey = event.key();
            recording = false;
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(event);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public void render(GuiGraphics graphics, int mx, int my, float dt) {
        super.render(graphics, mx, my, dt);
        graphics.drawCenteredString(font, "Horizontal speed: " + String.format(Locale.ROOT, "%.1f", SpearMaceClient.flySpeed),
            width / 2, height / 2 - 67, 0xFFFFFF);
        graphics.drawCenteredString(font, "Space to rise / Shift to descend", width / 2, height / 2 + 30, 0xBBBBBB);
    }
}
