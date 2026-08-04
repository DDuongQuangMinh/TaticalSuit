package com.k1ngtle.taticalsuit.client.camera;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.client.screen.SquadSelectionScreen;
import com.k1ngtle.taticalsuit.item.HelmetItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SandItem;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SnowItem;
import com.k1ngtle.taticalsuit.item.HelmetGhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetSandItem;
import com.k1ngtle.taticalsuit.item.HelmetSnowItem;
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
import net.minecraft.client.renderer.PostChain;
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
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT)
public class HelmetCameraManager {
    
    public static final KeyMapping SQUAD_MENU_KEY = new KeyMapping(
            "key.taticalsuit.squad_menu", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, "category.taticalsuit.keys");

    public static final KeyMapping CAMERA_CYCLE_KEY = new KeyMapping(
            "key.taticalsuit.helmet_cam_cycle", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, "category.taticalsuit.keys");

    public static final KeyMapping CAMERA_OFF_KEY = new KeyMapping(
            "key.taticalsuit.helmet_cam_off", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.taticalsuit.keys");
            
    public static final KeyMapping EDIT_CAM_KEY = new KeyMapping(
            "key.taticalsuit.edit_cam", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.taticalsuit.keys");

    private static boolean isCameraActive = false;
    private static LivingEntity targetEntity = null;
    private static int currentTargetIndex = 0;

    // We keep these instances completely persistent to prevent GL Program deletion!
    private static RenderTarget pipTarget;
    private static RenderTarget backupTarget;
    private static PostChain pipShaderGreen; 
    private static PostChain pipShaderWhite; 
    private static boolean isRenderingPip = false;
    
    public static int pipX = -1;
    public static int pipY = 10;
    public static int pipWidth = 140;
    public static int pipHeight = 160;

    private static boolean hasTacticalHelmet(LivingEntity entity) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack hand = entity.getMainHandItem(); 
        
        boolean wearingHelmet = head.getItem() instanceof HelmetItem || 
                                head.getItem() instanceof HelmetPVS31Item || 
                                head.getItem() instanceof HelmetGPNVG18Item ||
                                head.getItem() instanceof HelmetGPNVG18GhillieItem ||
                                head.getItem() instanceof HelmetGPNVG18SandItem ||
                                head.getItem() instanceof HelmetGPNVG18SnowItem ||
                                head.getItem() instanceof HelmetGhillieItem ||
                                head.getItem() instanceof HelmetSandItem ||
                                head.getItem() instanceof HelmetSnowItem;
                                
        boolean holdingHelmet = hand.getItem() instanceof HelmetItem || 
                                hand.getItem() instanceof HelmetPVS31Item || 
                                hand.getItem() instanceof HelmetGPNVG18Item ||
                                hand.getItem() instanceof HelmetGPNVG18GhillieItem ||
                                hand.getItem() instanceof HelmetGPNVG18SandItem ||
                                hand.getItem() instanceof HelmetGPNVG18SnowItem ||
                                hand.getItem() instanceof HelmetGhillieItem ||
                                hand.getItem() instanceof HelmetSandItem ||
                                hand.getItem() instanceof HelmetSnowItem;

        return wearingHelmet || holdingHelmet;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (SQUAD_MENU_KEY.consumeClick()) {
            if (hasTacticalHelmet(mc.player)) {
                mc.setScreen(new SquadSelectionScreen());
            } else {
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cEQUIP OR HOLD A TACTICAL HELMET TO ACCESS SQUAD LINK"), true);
            }
        }
        
        if (EDIT_CAM_KEY.consumeClick()) {
            mc.setScreen(new PipEditScreen());
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

        List<LivingEntity> teammates = mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(50.0)).stream()
                .filter(e -> e != mc.player && e.isAlive() && mc.player.distanceTo(e) <= 50.0f && hasTacticalHelmet(e))
                .filter(e -> {
                    ItemStack theirHead = e.getItemBySlot(EquipmentSlot.HEAD);
                    ItemStack theirHand = e.getMainHandItem();
                    return (theirHead.hasTag() && theirHead.getTag().getString("squad_name").equals(mySquad)) ||
                           (theirHand.hasTag() && theirHand.getTag().getString("squad_name").equals(mySquad));
                })
                .limit(5)
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
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || !isCameraActive || targetEntity == null || isRenderingPip) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        isRenderingPip = true;
        RenderTarget mainTarget = mc.getMainRenderTarget();
        
        mc.renderBuffers().bufferSource().endBatch();

        if (backupTarget == null || backupTarget.width != mainTarget.width || backupTarget.height != mainTarget.height) {
            if (backupTarget != null) {
                backupTarget.destroyBuffers();
            }
            backupTarget = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
        }
        
        if (pipTarget == null || pipTarget.width != mainTarget.width || pipTarget.height != mainTarget.height) {
            if (pipTarget != null) {
                pipTarget.destroyBuffers();
            }
            pipTarget = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
            if (pipShaderGreen != null) {
                pipShaderGreen.close();
                pipShaderGreen = null;
            }
            if (pipShaderWhite != null) {
                pipShaderWhite.close();
                pipShaderWhite = null;
            }
        }

        // BACKUP MAIN SCREEN (Color AND Depth to save weather/clouds/gui from breaking)
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, backupTarget.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, mainTarget.width, mainTarget.height, 0, 0, backupTarget.width, backupTarget.height, GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, GL30.GL_NEAREST);

        try {
            mainTarget.bindWrite(true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);

            net.minecraft.client.Camera pipCamera = new net.minecraft.client.Camera();
            
            float prevXRot = targetEntity.getXRot();
            float prevYRot = targetEntity.getYRot();
            targetEntity.setXRot(targetEntity.xRotO + (targetEntity.getXRot() - targetEntity.xRotO) * event.getPartialTick());
            targetEntity.setYRot(targetEntity.yHeadRotO + (targetEntity.yHeadRot - targetEntity.yHeadRotO) * event.getPartialTick());
            
            pipCamera.setup(mc.level, targetEntity, false, false, event.getPartialTick());
            
            targetEntity.setXRot(prevXRot);
            targetEntity.setYRot(prevYRot);
            
            PoseStack pipPoseStack = new PoseStack();
            pipPoseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pipCamera.getXRot()));
            pipPoseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(pipCamera.getYRot() + 180.0F));
            
            pipPoseStack.translate(0.0D, -1.2D, 0.0D);

            Matrix4f pipProjection = mc.gameRenderer.getProjectionMatrix((double)mc.options.fov().get());
            Matrix4f oldProj = RenderSystem.getProjectionMatrix();
            RenderSystem.setProjectionMatrix(pipProjection, com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);

            net.minecraft.world.phys.Vec3 trueCamPos = pipCamera.getPosition().add(0.0D, 1.2D, 0.0D);
            mc.levelRenderer.prepareCullFrustum(pipPoseStack, trueCamPos, pipProjection);

            mc.gameRenderer.lightTexture().turnOnLightLayer();
            mc.levelRenderer.renderLevel(pipPoseStack, event.getPartialTick(), event.getRenderTick(), false, pipCamera, mc.gameRenderer, mc.gameRenderer.lightTexture(), pipProjection);
            mc.gameRenderer.lightTexture().turnOffLightLayer();

            mc.renderBuffers().bufferSource().endBatch();

            RenderSystem.setProjectionMatrix(oldProj, com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);

            // COPY FRAME TO PIP TEXTURE
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, pipTarget.frameBufferId);
            GL30.glBlitFramebuffer(0, 0, mainTarget.width, mainTarget.height, 0, 0, pipTarget.width, pipTarget.height, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);

        } finally {
            // RESTORE MAIN SCREEN
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, backupTarget.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mainTarget.frameBufferId);
            GL30.glBlitFramebuffer(0, 0, backupTarget.width, backupTarget.height, 0, 0, mainTarget.width, mainTarget.height, GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, GL30.GL_NEAREST);

            mainTarget.bindWrite(true);
            mc.levelRenderer.prepareCullFrustum(event.getPoseStack(), mc.gameRenderer.getMainCamera().getPosition(), event.getProjectionMatrix());
            isRenderingPip = false;
        }
        
        PostChain currentMainEffect = mc.gameRenderer.currentEffect();
        if (currentMainEffect != null) {
            String currentEffectName = currentMainEffect.getName();
            ResourceLocation targetShader = null;
            boolean useWhite = false;

            ResourceLocation greenShader = ResourceLocation.tryParse(TaticalSuit.MODID + ":shaders/post/nv_green.json");
            ResourceLocation whiteShader = ResourceLocation.tryParse(TaticalSuit.MODID + ":shaders/post/nv_white.json");

            if (greenShader != null && currentEffectName.equals(greenShader.toString())) {
                targetShader = greenShader;
            } else if (whiteShader != null && currentEffectName.equals(whiteShader.toString())) {
                targetShader = whiteShader;
                useWhite = true;
            }

            if (targetShader != null) {
                PostChain activeShader = null;
                if (useWhite) {
                    if (pipShaderWhite == null) {
                        try {
                            pipShaderWhite = new PostChain(mc.getTextureManager(), mc.getResourceManager(), pipTarget, whiteShader);
                            pipShaderWhite.resize(pipTarget.width, pipTarget.height);
                        } catch (Exception e) { }
                    }
                    activeShader = pipShaderWhite;
                } else {
                    if (pipShaderGreen == null) {
                        try {
                            pipShaderGreen = new PostChain(mc.getTextureManager(), mc.getResourceManager(), pipTarget, greenShader);
                            pipShaderGreen.resize(pipTarget.width, pipTarget.height);
                        } catch (Exception e) { }
                    }
                    activeShader = pipShaderGreen;
                }

                if (activeShader != null) {
                    RenderSystem.disableBlend();
                    RenderSystem.disableDepthTest();
                    RenderSystem.resetTextureMatrix();
                    
                    Matrix4f shaderProjBackup = RenderSystem.getProjectionMatrix();
                    
                    activeShader.process(event.getPartialTick()); 
                    
                    RenderSystem.setProjectionMatrix(shaderProjBackup, com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);
                }
            }
        }
        mainTarget.bindWrite(true);
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (isRenderingPip) {
            Minecraft mc = Minecraft.getInstance();
            if (event.getEntity() == targetEntity && event.getEntity() != mc.player) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.type() || !isCameraActive || targetEntity == null || pipTarget == null) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        
        if (pipX == -1) pipX = guiGraphics.guiWidth() - pipWidth - 10;
        
        int x = pipX;
        int y = pipY;
        int boxWidth = pipWidth;
        int boxHeight = pipHeight;
        int footerY = y + boxHeight - 45;

        guiGraphics.fill(x, y, x + boxWidth, y + boxHeight, 0xFF000000);
        guiGraphics.fill(x, y, x + boxWidth, y + 16, 0xFF000000);
        guiGraphics.fill(x, footerY, x + boxWidth, y + boxHeight, 0xFF000000);
        guiGraphics.fill(x, y, x + boxWidth, y + 1, 0xFF555555);
        guiGraphics.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + boxHeight, 0xFF555555);
        guiGraphics.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, 0xFF555555);

        boolean isBlinking = (System.currentTimeMillis() % 1000) > 500;
        guiGraphics.fill(x + 5, y + 5, x + 11, y + 11, isBlinking ? 0xFFFF0000 : 0xFF550000);
        
        ItemStack headItem = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack handItem = mc.player.getMainHandItem();
        String mySquad = "NONE";
        if (headItem.hasTag() && headItem.getTag().contains("squad_name")) {
            mySquad = headItem.getTag().getString("squad_name");
        } else if (handItem.hasTag() && handItem.getTag().contains("squad_name")) {
            mySquad = handItem.getTag().getString("squad_name");
        }
        
        String headerText = "CAM: " + targetEntity.getDisplayName().getString().toUpperCase() + " [" + mySquad + "]";
        int textWidth = mc.font.width(headerText);
        int maxTextWidth = boxWidth - 20; 

        guiGraphics.pose().pushPose();
        if (textWidth > maxTextWidth) {
            float textScale = (float) maxTextWidth / (float) textWidth;
            guiGraphics.pose().translate(x + 16, y + 4 + ((8.0f - (8.0f * textScale)) / 2.0f), 0);
            guiGraphics.pose().scale(textScale, textScale, 1.0f);
        } else {
            guiGraphics.pose().translate(x + 16, y + 4, 0);
        }
        guiGraphics.drawString(mc.font, headerText, 0, 0, 0xFFFFFF, false);
        guiGraphics.pose().popPose();

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
        float drawH = boxHeight - 62;

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

        float healthPct = targetEntity.getHealth() / targetEntity.getMaxHealth();
        guiGraphics.drawString(mc.font, "HP: " + (int)targetEntity.getHealth(), x + 10, footerY + 3, 0xFFFFFF, false);
        guiGraphics.fill(x + 10, footerY + 13, x + boxWidth - 10, footerY + 17, 0xFF550000); 
        guiGraphics.fill(x + 10, footerY + 13, x + 10 + (int)((boxWidth - 20) * healthPct), footerY + 17, 0xFF00FF00); 

        guiGraphics.pose().pushPose();
        float textScale = 0.7f;
        guiGraphics.pose().scale(textScale, textScale, textScale);
        
        ItemStack weapon = targetEntity.getMainHandItem();
        String weaponName = weapon.isEmpty() ? "UNARMED" : weapon.getHoverName().getString().toUpperCase();
        guiGraphics.drawString(mc.font, "WEAPON: " + weaponName, (int)((x + 10) / textScale), (int)((footerY + 23) / textScale), 0xFFAAAAAA, false);
        int dist = (int)mc.player.distanceTo(targetEntity);
        guiGraphics.drawString(mc.font, "DIST: " + dist + "M", (int)((x + 10) / textScale), (int)((footerY + 33) / textScale), 0xFFAAAAAA, false);
        guiGraphics.pose().popPose();
    }

    public static class PipEditScreen extends net.minecraft.client.gui.screens.Screen {
        private boolean isDragging = false;
        private boolean isResizing = false;
        private double dragOffsetX = 0;
        private double dragOffsetY = 0;

        public PipEditScreen() {
            super(net.minecraft.network.chat.Component.literal("Edit Helmet Cam"));
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.drawCenteredString(this.font, "HELMET CAM EDIT MODE", this.width / 2, 20, 0xFFFFFF);
            guiGraphics.drawCenteredString(this.font, "Drag the box to move. Drag the yellow corner to resize.", this.width / 2, 35, 0xAAAAAA);

            if (pipX == -1) pipX = this.width - pipWidth - 10;

            if (!isCameraActive) {
                guiGraphics.fill(pipX, pipY, pipX + pipWidth, pipY + pipHeight, 0xAA000000);
                guiGraphics.drawCenteredString(this.font, "[ NO SIGNAL ]", pipX + pipWidth / 2, pipY + pipHeight / 2, 0xFF5555);
            }

            guiGraphics.fill(pipX + pipWidth - 10, pipY + pipHeight - 10, pipX + pipWidth, pipY + pipHeight, 0xFFFFFF00);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                int handleSize = 10;
                if (mouseX >= pipX + pipWidth - handleSize && mouseX <= pipX + pipWidth && mouseY >= pipY + pipHeight - handleSize && mouseY <= pipY + pipHeight) {
                    isResizing = true;
                    return true;
                } else if (mouseX >= pipX && mouseX <= pipX + pipWidth && mouseY >= pipY && mouseY <= pipY + pipHeight) {
                    isDragging = true;
                    dragOffsetX = mouseX - pipX;
                    dragOffsetY = mouseY - pipY;
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            isDragging = false;
            isResizing = false;
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (isResizing) {
                pipWidth = Math.max(100, (int)mouseX - pipX); 
                pipHeight = Math.max(120, (int)mouseY - pipY); 
                return true;
            } else if (isDragging) {
                pipX = (int)(mouseX - dragOffsetX);
                pipY = (int)(mouseY - dragOffsetY);
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        
        @Override
        public boolean isPauseScreen() { return false; }
    }

    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(SQUAD_MENU_KEY);
            event.register(CAMERA_CYCLE_KEY);
            event.register(CAMERA_OFF_KEY);
            event.register(EDIT_CAM_KEY); 
        }
    }
}