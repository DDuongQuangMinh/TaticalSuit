package com.k1ngtle.taticalsuit.client.camera;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT)
public class HelmetCameraManager {
    
    // T Key to cycle between active teammates/entities
    public static final KeyMapping CAMERA_CYCLE_KEY = new KeyMapping(
            "key.taticalsuit.helmet_cam_cycle", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, "category.taticalsuit.keys");

    // P Key to turn off the camera entirely
    public static final KeyMapping CAMERA_OFF_KEY = new KeyMapping(
            "key.taticalsuit.helmet_cam_off", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.taticalsuit.keys");

    private static boolean isCameraActive = false;
    private static LivingEntity targetEntity = null; // Changed to LivingEntity for testing with Armor Stands!
    private static int currentTargetIndex = 0;

    // Framebuffer for PiP Rendering
    private static RenderTarget pipTarget;
    private static boolean isRenderingPip = false;

    // Helper method to verify if an entity is wearing a tactical helmet
    private static boolean hasTacticalHelmet(LivingEntity entity) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        return head.getItem() instanceof HelmetItem || 
               head.getItem() instanceof HelmetPVS31Item || 
               head.getItem() instanceof HelmetGPNVG18Item;
    }

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

        // 3. Monitor active camera state (50 Block Range & Helmet Check)
        if (isCameraActive) {
            if (targetEntity == null || !targetEntity.isAlive() || targetEntity.isRemoved() || 
                mc.player.distanceTo(targetEntity) > 50.0f || !hasTacticalHelmet(targetEntity)) {
                turnOffCamera(mc);
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cSIGNAL LOST - Connection to target severed."), true);
            }
        }
    }

    private static void cycleCamera(Minecraft mc) {
        // Find all other living entities (Players, Armor Stands, Zombies) within a 50-block signal range WEARING A HELMET
        List<LivingEntity> teammates = mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(50.0)).stream()
                .filter(e -> e != mc.player && e.isAlive() && mc.player.distanceTo(e) <= 50.0f && hasTacticalHelmet(e))
                .collect(Collectors.toList());

        if (teammates.isEmpty()) {
            turnOffCamera(mc);
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNO SIGNAL - No helmet feeds found in range."), true);
            return;
        }

        if (!isCameraActive) {
            isCameraActive = true;
            currentTargetIndex = 0;
        } else {
            currentTargetIndex++;
        }

        if (currentTargetIndex >= teammates.size()) {
            currentTargetIndex = 0;
        }

        targetEntity = teammates.get(currentTargetIndex);
        String targetName = targetEntity.getDisplayName().getString().toUpperCase();
        mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aLINK ESTABLISHED - Viewing " + targetName), true);
        
        // Initialize the HD Framebuffer
        if (pipTarget == null) {
            pipTarget = new TextureTarget(552, 392, true, Minecraft.ON_OSX);
            pipTarget.setClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    private static void turnOffCamera(Minecraft mc) {
        isCameraActive = false;
        targetEntity = null;
        if (pipTarget != null) {
            pipTarget.destroyBuffers();
            pipTarget = null;
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || !isCameraActive || targetEntity == null || pipTarget == null || isRenderingPip) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        isRenderingPip = true;

        // 1. SAVE ALL ORIGINAL STATES
        RenderTarget mainTarget = mc.getMainRenderTarget();
        int originalWidth = mc.getWindow().getWidth();
        int originalHeight = mc.getWindow().getHeight();

        // CRITICAL: Flush any pending main-pass rendering to prevent entities from floating or duplicating!
        mc.renderBuffers().bufferSource().endBatch();

        // 2. PREPARE PIP FRAMEBUFFER
        pipTarget.bindWrite(true);
        RenderSystem.viewport(0, 0, pipTarget.width, pipTarget.height);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        pipTarget.clear(Minecraft.ON_OSX);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);

        // 3. Construct a separate PiP camera without hijacking the main player camera
        net.minecraft.client.Camera pipCamera = new net.minecraft.client.Camera();
        pipCamera.setup(mc.level, targetEntity, false, false, event.getPartialTick());
        
        PoseStack pipPoseStack = new PoseStack();
        pipPoseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pipCamera.getXRot()));
        pipPoseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(pipCamera.getYRot() + 180.0F));
        pipPoseStack.translate(
                -pipCamera.getPosition().x,
                -pipCamera.getPosition().y,
                -pipCamera.getPosition().z
        );
        
        Matrix4f pipProjection = mc.gameRenderer.getProjectionMatrix((double)mc.options.fov().get());

        // 5. INJECT MATRICES INTO RENDER SYSTEM (Fixes the Giant Polygon Glitch)
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.getModelViewStack().mulPoseMatrix(pipPoseStack.last().pose());
        RenderSystem.applyModelViewMatrix();

        Matrix4f oldProj = RenderSystem.getProjectionMatrix();
        RenderSystem.setProjectionMatrix(pipProjection, com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);

        // Rebuild the Frustum for the NEW camera position so the ground and chunks render correctly!
        mc.levelRenderer.prepareCullFrustum(pipPoseStack, pipCamera.getPosition(), pipProjection);

        // Temporarily disable any active full-screen post effect while rendering the PiP feed.
        ResourceLocation currentEffect = null;
        if (mc.gameRenderer.currentEffect() != null) {
            currentEffect = new ResourceLocation(mc.gameRenderer.currentEffect().getName());
            mc.gameRenderer.shutdownEffect();
        }

        // 6. RENDER LEVEL PASS
        mc.gameRenderer.lightTexture().turnOnLightLayer();
        mc.levelRenderer.renderLevel(pipPoseStack, event.getPartialTick(), event.getRenderTick(), false, pipCamera, mc.gameRenderer, mc.gameRenderer.lightTexture(), pipProjection);
        mc.gameRenderer.lightTexture().turnOffLightLayer();

        if (currentEffect != null) {
            mc.gameRenderer.loadEffect(currentEffect);
        }

        // CRITICAL: Flush PiP geometry so it doesn't bleed into the main screen
        mc.renderBuffers().bufferSource().endBatch();

        // 7. RESTORE INTERNAL RENDER SYSTEM MATRICES
        RenderSystem.setProjectionMatrix(oldProj, com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        mainTarget.bindWrite(true);
        RenderSystem.viewport(0, 0, originalWidth, originalHeight);
        
        // REBUILD THE FRUSTUM FOR THE MAIN CAMERA so your screen doesn't glitch!
        mc.levelRenderer.prepareCullFrustum(event.getPoseStack(), mc.gameRenderer.getMainCamera().getPosition(), event.getProjectionMatrix());

        isRenderingPip = false;
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (isRenderingPip) {
            // Cancel rendering for the target entity (so their helmet doesn't block the camera)
            // AND cancel rendering for the spectator (so they don't show up in the shot incorrectly)
            if (event.getEntity() == targetEntity || event.getEntity() == Minecraft.getInstance().player) {
                event.setCanceled(true);
            }
        }
    }

    // Renders the Tactical UI Picture-in-Picture Overlay in the Top Right
    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.type() || !isCameraActive || targetEntity == null || pipTarget == null) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        
        int boxWidth = 140;
        int boxHeight = 160;
        int x = screenWidth - boxWidth - 10;
        int y = 10;

        // Backgrounds
        guiGraphics.fill(x, y, x + boxWidth, y + boxHeight, 0xFF000000);
        guiGraphics.fill(x, y, x + boxWidth, y + 16, 0xFF000000);
        guiGraphics.fill(x, y + 115, x + boxWidth, y + boxHeight, 0xFF000000);

        // Borders
        guiGraphics.fill(x, y, x + boxWidth, y + 1, 0xFF555555);
        guiGraphics.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + boxHeight, 0xFF555555);
        guiGraphics.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, 0xFF555555);

        // Header Indicator
        boolean isBlinking = (System.currentTimeMillis() % 1000) > 500;
        guiGraphics.fill(x + 5, y + 5, x + 11, y + 11, isBlinking ? 0xFFFF0000 : 0xFF550000);
        String targetName = targetEntity.getDisplayName().getString().toUpperCase();
        guiGraphics.drawString(mc.font, "CAM: " + targetName, x + 16, y + 4, 0xFFFFFF);

        // --- RENDER CAPTURED 3D FEED ---
        RenderSystem.setShaderTexture(0, pipTarget.getColorTextureId());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float drawX = x + 1;
        float drawY = y + 17;
        float drawW = boxWidth - 2;
        float drawH = 98;

        bufferbuilder.vertex(matrix4f, drawX,         drawY + drawH, 0).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, drawX + drawW, drawY + drawH, 0).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, drawX + drawW, drawY,         0).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, drawX,         drawY,         0).uv(0.0F, 1.0F).endVertex();

        Tesselator.getInstance().end();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        // Footer Stats
        float healthPct = targetEntity.getHealth() / targetEntity.getMaxHealth();
        guiGraphics.drawString(mc.font, "HP: " + (int)targetEntity.getHealth(), x + 10, y + 118, 0xFFFFFF, false);
        guiGraphics.fill(x + 10, y + 128, x + boxWidth - 10, y + 132, 0xFF550000); 
        guiGraphics.fill(x + 10, y + 128, x + 10 + (int)((boxWidth - 20) * healthPct), y + 132, 0xFF00FF00); 

        guiGraphics.pose().pushPose();
        float textScale = 0.7f;
        guiGraphics.pose().scale(textScale, textScale, textScale);
        
        ItemStack weapon = targetEntity.getMainHandItem();
        String weaponName = weapon.isEmpty() ? "UNARMED" : weapon.getHoverName().getString().toUpperCase();
        guiGraphics.drawString(mc.font, "WEAPON: " + weaponName, (int)((x + 10) / textScale), (int)((y + 138) / textScale), 0xFFAAAAAA, false);
        
        int dist = (int)mc.player.distanceTo(targetEntity);
        guiGraphics.drawString(mc.font, "DIST: " + dist + "M", (int)((x + 10) / textScale), (int)((y + 148) / textScale), 0xFFAAAAAA, false);
        
        guiGraphics.pose().popPose();
    }

    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(CAMERA_CYCLE_KEY);
            event.register(CAMERA_OFF_KEY);
        }
    }
}