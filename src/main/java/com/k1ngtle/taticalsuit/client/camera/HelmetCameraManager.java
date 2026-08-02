package com.k1ngtle.taticalsuit.client.camera;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.client.screen.SquadSelectionScreen;
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
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT)
public class HelmetCameraManager {
    
    // Squad Selection Keybind ([)
    public static final KeyMapping SQUAD_MENU_KEY = new KeyMapping(
            "key.taticalsuit.squad_menu", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, "category.taticalsuit.keys");

    public static final KeyMapping CAMERA_CYCLE_KEY = new KeyMapping(
            "key.taticalsuit.helmet_cam_cycle", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, "category.taticalsuit.keys");

    public static final KeyMapping CAMERA_OFF_KEY = new KeyMapping(
            "key.taticalsuit.helmet_cam_off", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.taticalsuit.keys");

    private static boolean isCameraActive = false;
    private static LivingEntity targetEntity = null;
    private static int currentTargetIndex = 0;

    private static RenderTarget pipTarget;
    private static RenderTarget backupTarget;
    private static boolean isRenderingPip = false;

    // Checks BOTH the Head slot and the Main Hand slot
    private static boolean hasTacticalHelmet(LivingEntity entity) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack hand = entity.getMainHandItem(); 
        
        boolean wearingHelmet = head.getItem() instanceof HelmetItem || 
                                head.getItem() instanceof HelmetPVS31Item || 
                                head.getItem() instanceof HelmetGPNVG18Item;
                                
        boolean holdingHelmet = hand.getItem() instanceof HelmetItem || 
                                hand.getItem() instanceof HelmetPVS31Item || 
                                hand.getItem() instanceof HelmetGPNVG18Item;

        return wearingHelmet || holdingHelmet;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Squad Menu Logic
        if (SQUAD_MENU_KEY.consumeClick()) {
            if (hasTacticalHelmet(mc.player)) {
                mc.setScreen(new SquadSelectionScreen());
            } else {
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cEQUIP OR HOLD A TACTICAL HELMET TO ACCESS SQUAD LINK"), true);
            }
        }

        if (CAMERA_CYCLE_KEY.consumeClick()) cycleCamera(mc);

        if (CAMERA_OFF_KEY.consumeClick() && isCameraActive) {
            turnOffCamera(mc);
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cHELMET CAM OFF"), true);
        }

        if (isCameraActive) {
            if (targetEntity == null || !targetEntity.isAlive() || targetEntity.isRemoved() || 
                mc.player.distanceTo(targetEntity) > 50.0f || !hasTacticalHelmet(targetEntity)) {
                turnOffCamera(mc);
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cSIGNAL LOST - Connection to target severed."), true);
            }
        }
    }

    private static void cycleCamera(Minecraft mc) {
        ItemStack headItem = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack handItem = mc.player.getMainHandItem();
        
        // Find whichever one has the tag
        ItemStack myHelmet = ItemStack.EMPTY;
        if (headItem.hasTag() && headItem.getTag().contains("squad_name")) {
            myHelmet = headItem;
        } else if (handItem.hasTag() && handItem.getTag().contains("squad_name")) {
            myHelmet = handItem;
        }

        if (myHelmet.isEmpty()) {
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNO SQUAD LINKED - Press '[' while holding/wearing to select a frequency"), true);
            return;
        }
        
        String mySquad = myHelmet.getTag().getString("squad_name");

        // Advanced filter: Distance < 50, Alive, Not Self, Has Helmet, Has SAME SQUAD, Max 5 cams!
        List<LivingEntity> teammates = mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(50.0)).stream()
                .filter(e -> e != mc.player && e.isAlive() && mc.player.distanceTo(e) <= 50.0f && hasTacticalHelmet(e))
                .filter(e -> {
                    ItemStack theirHead = e.getItemBySlot(EquipmentSlot.HEAD);
                    ItemStack theirHand = e.getMainHandItem();
                    
                    return (theirHead.hasTag() && theirHead.getTag().getString("squad_name").equals(mySquad)) ||
                           (theirHand.hasTag() && theirHand.getTag().getString("squad_name").equals(mySquad));
                })
                .limit(5) // Max 5 cameras!
                .collect(Collectors.toList());

        if (teammates.isEmpty()) {
            turnOffCamera(mc);
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNO SIGNAL - No " + mySquad + " squad members found in range."), true);
            return;
        }

        if (!isCameraActive) {
            isCameraActive = true;
            currentTargetIndex = 0;
        } else {
            currentTargetIndex++;
        }

        if (currentTargetIndex >= teammates.size()) currentTargetIndex = 0;

        targetEntity = teammates.get(currentTargetIndex);
        mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a" + mySquad + " LINK ESTABLISHED - Viewing " + targetEntity.getDisplayName().getString().toUpperCase()), true);
    }

    private static void turnOffCamera(Minecraft mc) {
        isCameraActive = false;
        targetEntity = null;
        if (pipTarget != null) {
            pipTarget.destroyBuffers();
            pipTarget = null;
        }
        if (backupTarget != null) {
            backupTarget.destroyBuffers();
            backupTarget = null;
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || !isCameraActive || targetEntity == null || isRenderingPip) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        isRenderingPip = true;

        RenderTarget mainTarget = mc.getMainRenderTarget();
        Entity originalCamera = mc.getCameraEntity();
        CameraType originalCameraType = mc.options.getCameraType();

        mc.renderBuffers().bufferSource().endBatch();

        if (backupTarget == null || backupTarget.width != mainTarget.width || backupTarget.height != mainTarget.height) {
            if (backupTarget != null) backupTarget.destroyBuffers();
            backupTarget = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
        }
        
        if (pipTarget == null || pipTarget.width != mainTarget.width || pipTarget.height != mainTarget.height) {
            if (pipTarget != null) pipTarget.destroyBuffers();
            pipTarget = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
        }

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, backupTarget.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, mainTarget.width, mainTarget.height, 0, 0, backupTarget.width, backupTarget.height, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);

        try {
            mainTarget.bindWrite(true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);

            mc.setCameraEntity(targetEntity);
            mc.options.setCameraType(CameraType.FIRST_PERSON); 

            net.minecraft.client.Camera pipCamera = new net.minecraft.client.Camera();
            pipCamera.setup(mc.level, targetEntity, false, false, event.getPartialTick());
            
            PoseStack pipPoseStack = new PoseStack();
            pipPoseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pipCamera.getXRot()));
            pipPoseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(pipCamera.getYRot() + 180.0F));
            
            Matrix4f pipProjection = mc.gameRenderer.getProjectionMatrix((double)mc.options.fov().get());

            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().setIdentity();
            RenderSystem.getModelViewStack().mulPoseMatrix(pipPoseStack.last().pose());
            RenderSystem.applyModelViewMatrix();

            Matrix4f oldProj = RenderSystem.getProjectionMatrix();
            RenderSystem.setProjectionMatrix(pipProjection, com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);

            mc.levelRenderer.prepareCullFrustum(pipPoseStack, pipCamera.getPosition(), pipProjection);

            mc.gameRenderer.lightTexture().turnOnLightLayer();
            mc.levelRenderer.renderLevel(pipPoseStack, event.getPartialTick(), event.getRenderTick(), false, pipCamera, mc.gameRenderer, mc.gameRenderer.lightTexture(), pipProjection);
            mc.gameRenderer.lightTexture().turnOffLightLayer();

            mc.renderBuffers().bufferSource().endBatch();

            RenderSystem.setProjectionMatrix(oldProj, com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();

            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, pipTarget.frameBufferId);
            GL30.glBlitFramebuffer(0, 0, mainTarget.width, mainTarget.height, 0, 0, pipTarget.width, pipTarget.height, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);

        } finally {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, backupTarget.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mainTarget.frameBufferId);
            GL30.glBlitFramebuffer(0, 0, backupTarget.width, backupTarget.height, 0, 0, mainTarget.width, mainTarget.height, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);

            mainTarget.bindWrite(true);
            mc.setCameraEntity(originalCamera);
            mc.options.setCameraType(originalCameraType);
            
            mc.levelRenderer.prepareCullFrustum(event.getPoseStack(), mc.gameRenderer.getMainCamera().getPosition(), event.getProjectionMatrix());
            isRenderingPip = false;
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (isRenderingPip) {
            if (event.getEntity() == targetEntity || event.getEntity() == Minecraft.getInstance().player) {
                event.setCanceled(true);
            }
        }
    }

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

        guiGraphics.fill(x, y, x + boxWidth, y + boxHeight, 0xFF000000);
        guiGraphics.fill(x, y, x + boxWidth, y + 16, 0xFF000000);
        guiGraphics.fill(x, y + 115, x + boxWidth, y + boxHeight, 0xFF000000);
        guiGraphics.fill(x, y, x + boxWidth, y + 1, 0xFF555555);
        guiGraphics.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + boxHeight, 0xFF555555);
        guiGraphics.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, 0xFF555555);

        boolean isBlinking = (System.currentTimeMillis() % 1000) > 500;
        guiGraphics.fill(x + 5, y + 5, x + 11, y + 11, isBlinking ? 0xFFFF0000 : 0xFF550000);
        
        // Find squad tag from head or hand
        ItemStack headItem = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack handItem = mc.player.getMainHandItem();
        String mySquad = "NONE";
        if (headItem.hasTag() && headItem.getTag().contains("squad_name")) {
            mySquad = headItem.getTag().getString("squad_name");
        } else if (handItem.hasTag() && handItem.getTag().contains("squad_name")) {
            mySquad = handItem.getTag().getString("squad_name");
        }
        
        guiGraphics.drawString(mc.font, "CAM: " + targetEntity.getDisplayName().getString().toUpperCase() + " [" + mySquad + "]", x + 16, y + 4, 0xFFFFFF);

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

        float targetAspect = drawW / drawH;
        float screenAspect = (float)pipTarget.width / (float)pipTarget.height;
        float u0 = 0.0F, u1 = 1.0F, v0 = 0.0F, v1 = 1.0F;
        
        if (screenAspect > targetAspect) {
            float scale = targetAspect / screenAspect;
            float margin = (1.0F - scale) / 2.0F;
            u0 = margin;
            u1 = 1.0F - margin;
        } else {
            float scale = screenAspect / targetAspect;
            float margin = (1.0F - scale) / 2.0F;
            v0 = margin;
            v1 = 1.0F - margin;
        }

        bufferbuilder.vertex(matrix4f, drawX,         drawY + drawH, 0).uv(u0, v0).endVertex();
        bufferbuilder.vertex(matrix4f, drawX + drawW, drawY + drawH, 0).uv(u1, v0).endVertex();
        bufferbuilder.vertex(matrix4f, drawX + drawW, drawY,         0).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix4f, drawX,         drawY,         0).uv(u0, v1).endVertex();

        Tesselator.getInstance().end();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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
            event.register(SQUAD_MENU_KEY);
            event.register(CAMERA_CYCLE_KEY);
            event.register(CAMERA_OFF_KEY);
        }
    }
}