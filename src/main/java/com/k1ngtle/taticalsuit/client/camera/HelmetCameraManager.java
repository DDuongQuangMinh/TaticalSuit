package com.k1ngtle.taticalsuit.client.camera;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT)
public class HelmetCameraManager {
    
    public static final KeyMapping CAMERA_CYCLE_KEY = new KeyMapping(
            "key.taticalsuit.helmet_cam_cycle", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, "category.taticalsuit.keys");

    public static final KeyMapping CAMERA_OFF_KEY = new KeyMapping(
            "key.taticalsuit.helmet_cam_off", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.taticalsuit.keys");

    private static boolean isCameraActive = false;
    private static Player targetPlayer = null;
    private static int currentTargetIndex = 0;

    // Framebuffer for PiP Rendering
    private static RenderTarget pipTarget;
    private static boolean isRenderingPip = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 1. Handle Cycle Key (T)
        if (CAMERA_CYCLE_KEY.consumeClick()) {
            cycleCamera(mc);
        }

        // 2. Handle Off Key (P)
        if (CAMERA_OFF_KEY.consumeClick() && isCameraActive) {
            turnOffCamera(mc);
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cHELMET CAM OFF"), true);
        }

        // 3. Monitor active camera state
        if (isCameraActive) {
            // If the teammate disconnected, died, or went out of render distance, shut off
            if (targetPlayer == null || !targetPlayer.isAlive() || !mc.level.players().contains(targetPlayer) || mc.player.distanceTo(targetPlayer) > 30.0f) {
                turnOffCamera(mc);
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cSIGNAL LOST - Connection to teammate severed."), true);
            }
        }
    }

    private static void cycleCamera(Minecraft mc) {
        // Find all other living players within a 30-block signal range
        List<Player> teammates = mc.level.players().stream()
                .filter(p -> p != mc.player && p.isAlive() && mc.player.distanceTo(p) <= 30.0f)
                .collect(Collectors.toList());

        if (teammates.isEmpty()) {
            turnOffCamera(mc);
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNO SIGNAL - No active teammates found in range."), true);
            return;
        }

        // Initialize or increment target index
        if (!isCameraActive) {
            isCameraActive = true;
            currentTargetIndex = 0;
        } else {
            currentTargetIndex++;
        }

        // Loop back to the first player if we reach the end of the list
        if (currentTargetIndex >= teammates.size()) {
            currentTargetIndex = 0;
        }

        targetPlayer = teammates.get(currentTargetIndex);
        mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aLINK ESTABLISHED - Viewing " + targetPlayer.getScoreboardName()), true);
        
        // Initialize the Framebuffer mapped EXACTLY to the 138x98 UI Box Aspect Ratio (x4 for HD)
        if (pipTarget == null) {
            pipTarget = new TextureTarget(552, 392, true, Minecraft.ON_OSX);
            pipTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

    private static void turnOffCamera(Minecraft mc) {
        isCameraActive = false;
        targetPlayer = null;
        if (pipTarget != null) {
            pipTarget.destroyBuffers();
            pipTarget = null;
        }
    }

    // --- PIP RENDERING LOGIC ---
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // We only want to hijack the render process BEFORE the main world is rendered
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || !isCameraActive || targetPlayer == null || pipTarget == null || isRenderingPip) return;

        Minecraft mc = Minecraft.getInstance();
        Entity originalCamera = mc.getCameraEntity();
        
        // Prevent infinite recursion loops
        isRenderingPip = true;

        // 1. Save original states
        int originalWidth = mc.getWindow().getWidth();
        int originalHeight = mc.getWindow().getHeight();
        RenderTarget mainTarget = mc.getMainRenderTarget();
        CameraType originalCameraType = mc.options.getCameraType();

        // 2. Prepare our HD PiP Framebuffer
        pipTarget.clear(Minecraft.ON_OSX);
        pipTarget.bindWrite(true);

        // 3. Hijack Camera & Force First Person
        mc.setCameraEntity(targetPlayer);
        mc.options.setCameraType(CameraType.FIRST_PERSON); // Prevents rendering their own head into the camera!
        
        // 4. Temporarily resize viewport for the PiP rendering
        RenderSystem.viewport(0, 0, pipTarget.width, pipTarget.height);

        // 5. Construct a true, mathematically accurate FPV Camera
        net.minecraft.client.Camera pipCamera = new net.minecraft.client.Camera();
        pipCamera.setup(mc.level, targetPlayer, false, false, event.getPartialTick());
        
        PoseStack pipPoseStack = new PoseStack();
        pipPoseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pipCamera.getXRot()));
        pipPoseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(pipCamera.getYRot() + 180.0F));
        
        Matrix4f pipProjection = mc.gameRenderer.getProjectionMatrix((double)mc.options.fov().get());

        // 6. Force a Level Render pass from the perfectly calculated perspective
        mc.levelRenderer.renderLevel(pipPoseStack, event.getPartialTick(), event.getRenderTick(), false, pipCamera, mc.gameRenderer, mc.gameRenderer.lightTexture(), pipProjection);

        // 7. Restore original states safely
        mainTarget.bindWrite(true);
        RenderSystem.viewport(0, 0, originalWidth, originalHeight);
        mc.setCameraEntity(originalCamera);
        mc.options.setCameraType(originalCameraType);
        
        isRenderingPip = false;
    }

    // Renders the Tactical UI Picture-in-Picture Overlay in the Top Right
    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        // Ensure this only runs on the base GUI overlay to prevent double-drawing over the hotbar or other elements
        if (event.getOverlay() != net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.type() || !isCameraActive || targetPlayer == null || pipTarget == null) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        
        // Define the dimensions and position of the PiP Window
        int boxWidth = 140;
        int boxHeight = 160;
        int x = screenWidth - boxWidth - 10;
        int y = 10;

        // --- DRAW BACKGROUND ---
        // Main transparent dark box
        guiGraphics.fill(x, y, x + boxWidth, y + boxHeight, 0x99000000);
        // Header Bar
        guiGraphics.fill(x, y, x + boxWidth, y + 16, 0xCC000000);
        // Footer Info Bar
        guiGraphics.fill(x, y + 115, x + boxWidth, y + boxHeight, 0xCC000000);

        // --- DRAW BORDERS ---
        guiGraphics.fill(x, y, x + boxWidth, y + 1, 0xFF555555); // Top
        guiGraphics.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, 0xFF555555); // Bottom
        guiGraphics.fill(x, y, x + 1, y + boxHeight, 0xFF555555); // Left
        guiGraphics.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, 0xFF555555); // Right

        // --- DRAW HEADER ---
        // Blinking Red "REC" Indicator
        boolean isBlinking = (System.currentTimeMillis() % 1000) > 500;
        guiGraphics.fill(x + 5, y + 5, x + 11, y + 11, isBlinking ? 0xFFFF0000 : 0xFF550000);
        
        // Target Name
        guiGraphics.drawString(mc.font, "CAM: " + targetPlayer.getScoreboardName().toUpperCase(), x + 16, y + 4, 0xFFFFFF);

        // --- RENDER ACTUAL TEAMMATE PERSPECTIVE (PiP) ---
        // We bind the Framebuffer's raw OpenGL Texture ID
        RenderSystem.setShaderTexture(0, pipTarget.getColorTextureId());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // Disable depth testing temporarily so the texture renders strictly on top of the UI
        RenderSystem.disableDepthTest();

        // Draw the raw texture manually using the Tesselator instead of guiGraphics.blit
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float drawX = x + 1;
        float drawY = y + 17;
        float drawW = boxWidth - 2;
        float drawH = 98;

        // Note: OpenGL Framebuffers are rendered upside down, so the V coordinates are flipped (0,0 is bottom-left)
        bufferbuilder.vertex(matrix4f, drawX,         drawY + drawH, 0).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, drawX + drawW, drawY + drawH, 0).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, drawX + drawW, drawY,         0).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, drawX,         drawY,         0).uv(0.0F, 1.0F).endVertex();

        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();

        // --- DRAW FOOTER STATS ---
        // Health Bar
        float healthPct = targetPlayer.getHealth() / targetPlayer.getMaxHealth();
        guiGraphics.drawString(mc.font, "HP: " + (int)targetPlayer.getHealth(), x + 10, y + 118, 0xFFFFFF, false);
        guiGraphics.fill(x + 10, y + 128, x + boxWidth - 10, y + 132, 0xFF550000); // Empty Red
        guiGraphics.fill(x + 10, y + 128, x + 10 + (int)((boxWidth - 20) * healthPct), y + 132, 0xFF00FF00); // Full Green

        // Smaller Text Scale for Weapon and Distance
        guiGraphics.pose().pushPose();
        float textScale = 0.7f;
        guiGraphics.pose().scale(textScale, textScale, textScale);
        
        // Weapon
        ItemStack weapon = targetPlayer.getMainHandItem();
        String weaponName = weapon.isEmpty() ? "UNARMED" : weapon.getHoverName().getString().toUpperCase();
        guiGraphics.drawString(mc.font, "WEAPON: " + weaponName, (int)((x + 10) / textScale), (int)((y + 138) / textScale), 0xFFAAAAAA, false);
        
        // Distance
        int dist = (int)mc.player.distanceTo(targetPlayer);
        guiGraphics.drawString(mc.font, "DIST: " + dist + "M", (int)((x + 10) / textScale), (int)((y + 148) / textScale), 0xFFAAAAAA, false);
        
        guiGraphics.pose().popPose();
    }

    // Register the custom 'T' and 'P' keybinds to the Forge Event Bus
    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(CAMERA_CYCLE_KEY);
            event.register(CAMERA_OFF_KEY);
        }
    }
}