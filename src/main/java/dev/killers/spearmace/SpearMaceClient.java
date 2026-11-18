package dev.killers.spearmace;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class SpearMaceClient implements ClientModInitializer {
    public static boolean blockEspEnabled = false;
    public static boolean tracerEnabled = false;
    public static boolean flightEnabled = false;
    public static double flySpeed = 1.0;
    public static final double VERTICAL_SPEED = 0.42;
    public static int flightToggleKey = InputConstants.KEY_G;
    private static boolean flightKeyPreviouslyDown = false;
    public static final Set<Block> BLOCKS = new LinkedHashSet<>();
    public static final Set<EntityType<?>> ENTITIES = new LinkedHashSet<>();
    private static final List<BlockPos> FOUND = new ArrayList<>();
    private static int scanTick = 0;
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("killers_spearmace", "main"));

    @Override public void onInitializeClient() {
        KeyMapping menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.killers_spearmace.open_menu", InputConstants.Type.KEYSYM,
            InputConstants.KEY_R, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.consumeClick()) if (client.player != null && client.screen == null)
                client.setScreen(new SpearMaceScreen());
            if (++scanTick % 12 == 0) scanBlocks(client);
            emitEspGizmos(client);
            boolean keyDown = client.screen == null && client.player != null &&
                InputConstants.isKeyDown(client.getWindow(), flightToggleKey);
            if (keyDown && !flightKeyPreviouslyDown) flightEnabled = !flightEnabled;
            flightKeyPreviouslyDown = keyDown;
            moveFlight(client);
        });
    }

    private static void scanBlocks(Minecraft mc) {
        FOUND.clear();
        if (!blockEspEnabled || mc.level == null || mc.player == null || BLOCKS.isEmpty()) return;
        BlockPos center = mc.player.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // 17 x 13 x 17 area; only blocks already present in the client world.
        for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++)
            for (int y = -6; y <= 6; y++) {
                pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                if (BLOCKS.contains(mc.level.getBlockState(pos).getBlock())) FOUND.add(pos.immutable());
            }
    }

    /**
     * Minecraft 1.21.11 separates render extraction from drawing. Its Gizmos API
     * queues debug shapes safely for the next frame. Never append vertices to
     * the world renderer's buffers from an AFTER_ENTITIES drawing event.
     */
    private static void emitEspGizmos(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        if ((!blockEspEnabled || BLOCKS.isEmpty()) && (!tracerEnabled || ENTITIES.isEmpty())) return;

        try (var scope = mc.collectPerTickGizmos()) {
            if (blockEspEnabled && !BLOCKS.isEmpty()) {
                int count = 0;
                for (BlockPos pos : FOUND) {
                    if (count++ >= 192) break; // keep very common blocks from stalling the game
                    Gizmos.cuboid(new AABB(pos).inflate(0.005), GizmoStyle.stroke(0xFF55AAFF, 1.5f))
                        .setAlwaysOnTop();
                }
            }

            if (tracerEnabled && !ENTITIES.isEmpty()) {
                Vec3 eye = mc.player.getEyePosition();
                int count = 0;
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity == mc.player || entity.isRemoved()
                            || !ENTITIES.contains(entity.getType())
                            || entity.distanceToSqr(mc.player) > 128.0 * 128.0) continue;
                    if (count++ >= 96) break;
                    AABB box = entity.getBoundingBox().inflate(0.04);
                    Gizmos.cuboid(box, GizmoStyle.stroke(0xFF55FF99, 1.5f))
                        .setAlwaysOnTop();
                    Gizmos.line(eye, box.getCenter(), 0xFF55FF99, 1.5f)
                        .setAlwaysOnTop();
                }
            }
        }
    }

    private static void moveFlight(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (!flightEnabled || p == null || mc.level == null || mc.screen != null) return;
        Vec3 forward = Vec3.directionFromRotation(0, p.getYRot());
        Vec3 sideways = new Vec3(-forward.z, 0, forward.x);
        double fwd = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        double side = (mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0);
        Vec3 wanted = forward.scale(fwd).add(sideways.scale(side));
        if (wanted.lengthSqr() > 1) wanted = wanted.normalize();
        wanted = wanted.scale(flySpeed * 0.24);
        double vy = (mc.options.keyJump.isDown() ? VERTICAL_SPEED : 0)
                  - (mc.options.keyShift.isDown() ? VERTICAL_SPEED : 0);
        Vec3 old = p.getDeltaMovement();
        double smoothing = 0.23;
        p.setDeltaMovement(
            old.x + (wanted.x - old.x) * smoothing,
            old.y + (vy - old.y) * 0.35,
            old.z + (wanted.z - old.z) * smoothing);
        p.resetFallDistance();
    }
}
