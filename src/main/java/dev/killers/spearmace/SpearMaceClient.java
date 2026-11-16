package dev.killers.spearmace;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
    public static double verticalSpeed = 0.65;
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
            if (++scanTick % 30 == 0) scanBlocks(client);
            moveFlight(client);
        });
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(ctx -> renderWorld());
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

    private static void renderWorld() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || (!blockEspEnabled && !tracerEnabled)) return;
        var camera = mc.gameRenderer.getMainCamera();
        Vec3 eye = camera.position();
        var pose = new com.mojang.blaze3d.vertex.PoseStack();
        var buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderTypes.lines());
        pose.pushPose();
        pose.translate(-eye.x, -eye.y, -eye.z);
        if (blockEspEnabled) for (BlockPos p : FOUND)
            ShapeRenderer.renderShape(pose, lines, Shapes.block(), p.getX(), p.getY(), p.getZ(), 0xAA55FFFF, 1.0f);
        if (tracerEnabled) for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || e.isRemoved() || !ENTITIES.contains(e.getType())) continue;
            Vec3 target = e.position().add(0, e.getBbHeight() * 0.5, 0);
            Vec3 from = mc.player.getEyePosition();
            var m = pose.last().pose();
            lines.addVertex(m, (float)from.x, (float)from.y, (float)from.z).setColor(0.35f, 1f, 0.8f, 1f).setNormal(0, 1, 0);
            lines.addVertex(m, (float)target.x, (float)target.y, (float)target.z).setColor(0.35f, 1f, 0.8f, 1f).setNormal(0, 1, 0);
        }
        pose.popPose();
        buffers.endBatch(RenderTypes.lines());
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
        double vy = (mc.options.keyJump.isDown() ? verticalSpeed : 0)
                  - (mc.options.keyShift.isDown() ? verticalSpeed : 0);
        Vec3 old = p.getDeltaMovement();
        double smoothing = 0.23;
        p.setDeltaMovement(
            old.x + (wanted.x - old.x) * smoothing,
            old.y + (vy - old.y) * 0.35,
            old.z + (wanted.z - old.z) * smoothing);
        p.resetFallDistance();
    }
}
