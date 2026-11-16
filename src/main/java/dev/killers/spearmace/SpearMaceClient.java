package dev.killers.spearmace;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class SpearMaceClient implements ClientModInitializer {
    public static boolean espEnabled = false;
    public static boolean flightEnabled = false;
    private static final Set<Entity> OUTLINED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("killers_spearmace", "main"));

    @Override
    public void onInitializeClient() {
        KeyMapping menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.killers_spearmace.open_menu", InputConstants.Type.KEYSYM,
            InputConstants.KEY_R, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.consumeClick()) {
                if (client.player != null && client.screen == null) client.setScreen(new SpearMaceScreen());
            }
            updateEsp(client);
            updateFlight(client);
        });
    }

    public static void setEsp(boolean enabled) {
        espEnabled = enabled;
        if (!enabled) clearOutlines();
    }

    public static void setFlight(boolean enabled) {
        flightEnabled = enabled;
    }

    private static void updateEsp(Minecraft client) {
        if (!espEnabled || client.level == null || client.player == null) {
            clearOutlines();
            return;
        }
        Set<Entity> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        // Only highlight entities that already exist on this client's loaded world.
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity) || entity == client.player || entity.isRemoved()) continue;
            seen.add(entity);
            if (!OUTLINED.contains(entity)) {
                entity.setGlowingTag(true);
                OUTLINED.add(entity);
            }
        }
        for (Entity previous : Set.copyOf(OUTLINED)) {
            if (!seen.contains(previous)) {
                previous.setGlowingTag(false);
                OUTLINED.remove(previous);
            }
        }
    }

    private static void clearOutlines() {
        for (Entity entity : OUTLINED) entity.setGlowingTag(false);
        OUTLINED.clear();
    }

    private static void updateFlight(Minecraft client) {
        LocalPlayer player = client.player;
        if (!flightEnabled || player == null || client.level == null || client.screen != null) return;
        // Client-only movement: servers may reject this; no packet/anti-cheat bypass.
        Vec3 movement = player.getDeltaMovement();
        double y = client.options.keyJump.isDown() ? 0.35 :
                   client.options.keyShift.isDown() ? -0.35 : 0.0;
        player.setDeltaMovement(movement.x, y, movement.z);
        player.resetFallDistance();
    }
}
