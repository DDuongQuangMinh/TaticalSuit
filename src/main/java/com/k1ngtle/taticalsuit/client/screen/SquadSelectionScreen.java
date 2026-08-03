package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.item.HelmetItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SandItem;
import com.k1ngtle.taticalsuit.item.HelmetGhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetSandItem;
import com.k1ngtle.taticalsuit.item.HelmetSnowItem;
import com.k1ngtle.taticalsuit.network.SquadNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SquadSelectionScreen extends Screen {
    private static final String[] SQUADS = {
        "ALPHA", "BRAVO", "CHARLIE", "DELTA", "ECHO", "FOXTROT", "GOLF", "HOTEL", "INDIA"
    };

    private final Map<String, List<String>> squadMembers = new HashMap<>();
    private final Map<String, Float> animationProgress = new HashMap<>();
    private long lastRenderTime = 0;

    public SquadSelectionScreen() {
        super(Component.literal("Squad Frequency Selection"));
    }

    @Override
    protected void init() {
        super.init();
        squadMembers.clear();
        animationProgress.clear();
        
        for (String squad : SQUADS) {
            squadMembers.put(squad, new ArrayList<>());
            animationProgress.put(squad, 0.0f);
        }

        lastRenderTime = System.currentTimeMillis();

        // Scan nearby entities when the screen opens to see who is already registered
        if (this.minecraft != null && this.minecraft.level != null && this.minecraft.player != null) {
            List<LivingEntity> entities = this.minecraft.level.getEntitiesOfClass(LivingEntity.class, this.minecraft.player.getBoundingBox().inflate(100.0));
            for (LivingEntity e : entities) {
                if (hasTacticalHelmet(e)) {
                    ItemStack head = e.getItemBySlot(EquipmentSlot.HEAD);
                    ItemStack hand = e.getMainHandItem();
                    String squad = null;

                    if (head.hasTag() && head.getTag().contains("squad_name")) {
                        squad = head.getTag().getString("squad_name");
                    } else if (hand.hasTag() && hand.getTag().contains("squad_name")) {
                        squad = hand.getTag().getString("squad_name");
                    }

                    if (squad != null && squadMembers.containsKey(squad)) {
                        squadMembers.get(squad).add(e.getDisplayName().getString().toUpperCase());
                    }
                }
            }
        }
    }

    private boolean hasTacticalHelmet(LivingEntity entity) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack hand = entity.getMainHandItem(); 
        boolean wearing = head.getItem() instanceof HelmetItem || head.getItem() instanceof HelmetPVS31Item || head.getItem() instanceof HelmetGPNVG18Item || head.getItem() instanceof HelmetGPNVG18GhillieItem || head.getItem() instanceof HelmetGPNVG18SandItem || head.getItem() instanceof HelmetGhillieItem || head.getItem() instanceof HelmetSandItem || head.getItem() instanceof HelmetSnowItem;
        boolean holding = hand.getItem() instanceof HelmetItem || hand.getItem() instanceof HelmetPVS31Item || hand.getItem() instanceof HelmetGPNVG18Item || hand.getItem() instanceof HelmetGPNVG18GhillieItem || hand.getItem() instanceof HelmetGPNVG18SandItem || hand.getItem() instanceof HelmetGhillieItem || hand.getItem() instanceof HelmetSandItem || hand.getItem() instanceof HelmetSnowItem;
        return wearing || holding;
    }

    // Helper method to smoothly blend between colors
    private int lerpColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics); 

        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastRenderTime) / 1000.0f;
        lastRenderTime = currentTime;

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 1. Draw Sleek Top Bar
        guiGraphics.fill(0, 0, this.width, 35, 0xDD0B0C0E);
        guiGraphics.fill(0, 35, this.width, 37, 0xFFD62929);
        guiGraphics.drawCenteredString(this.font, "TACTICAL SQUAD FREQUENCY OVERRIDE", centerX, 13, 0xFFFFFF);

        // 2. Compute Layout (Wider Buttons to match Dropdown)
        int btnWidth = 85;
        int btnHeight = 20;
        int spacing = 4;
        int totalWidth = (SQUADS.length * btnWidth) + ((SQUADS.length - 1) * spacing);
        int startX = centerX - (totalWidth / 2);
        int startY = centerY - 10;

        // 3. Draw Buttons & Update Animations
        for (int i = 0; i < SQUADS.length; i++) {
            String squad = SQUADS[i];
            int btnX = startX + (i * (btnWidth + spacing));
            
            boolean isHovered = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= startY && mouseY <= startY + btnHeight;
            
            // Calculate smooth hover transition
            float prog = animationProgress.getOrDefault(squad, 0.0f);
            if (isHovered) {
                prog = Math.min(1.0f, prog + delta * 8.0f); // Speed of hover IN
            } else {
                prog = Math.max(0.0f, prog - delta * 8.0f); // Speed of hover OUT
            }
            animationProgress.put(squad, prog);

            // Smoothly blend from Dark Gray to Red
            int color = lerpColor(0xFF2E3136, 0xFFD62929, prog);

            guiGraphics.fill(btnX, startY, btnX + btnWidth, startY + btnHeight, color);
            
            // Draw Full Squad Name instead of abbreviation since the box is larger now
            guiGraphics.drawCenteredString(this.font, squad, btnX + (btnWidth / 2), startY + 6, 0xFFFFFF);
        }

        // 4. Draw Animated Dropdown Lists (Rendered after buttons so they overlap correctly)
        for (int i = 0; i < SQUADS.length; i++) {
            String squad = SQUADS[i];
            float prog = animationProgress.getOrDefault(squad, 0.0f);
            
            if (prog > 0.01f) {
                List<String> members = squadMembers.get(squad);
                int btnX = startX + (i * (btnWidth + spacing));
                int hoverY = startY + btnHeight;
                
                // Set Dropdown width identically to Button width
                int dropWidth = btnWidth; 
                int maxDropHeight = Math.max(26, members.size() * 12 + 20);
                
                // Expand height based on animation progress!
                int dropHeight = (int) (maxDropHeight * prog); 
                
                // Lock the dropdown position directly under the button perfectly
                int renderX = btnX;

                // Use OpenGL Scissor to clip text while the box unrolls
                guiGraphics.enableScissor(renderX, hoverY, renderX + dropWidth, hoverY + dropHeight);

                // Expanding Background Panel
                guiGraphics.fill(renderX, hoverY, renderX + dropWidth, hoverY + dropHeight, 0xFFAAAAAA); 
                guiGraphics.fill(renderX + 1, hoverY, renderX + dropWidth - 1, hoverY + dropHeight - 1, 0xFF0B0C0E); 
                
                if (dropHeight >= 2) {
                    guiGraphics.fill(renderX + 1, hoverY, renderX + dropWidth - 1, hoverY + 2, 0xFFD62929); 
                }

                // Render content statically, letting the scissor reveal it naturally
                guiGraphics.drawString(this.font, squad + " LINK", renderX + 5, hoverY + 6, 0xFFD62929, false);

                if (members.isEmpty()) {
                    guiGraphics.drawString(this.font, "NO SIGNAL", renderX + 5, hoverY + 18, 0xFF555555, false);
                } else {
                    for (int j = 0; j < members.size(); j++) {
                        guiGraphics.drawString(this.font, "> " + members.get(j), renderX + 5, hoverY + 20 + (j * 12), 0xFF00FF00, false);
                    }
                }

                guiGraphics.disableScissor();
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            
            // Make sure these match the rendering layout exactly
            int btnWidth = 85;
            int btnHeight = 20;
            int spacing = 4;
            int totalWidth = (SQUADS.length * btnWidth) + ((SQUADS.length - 1) * spacing);
            int startX = centerX - (totalWidth / 2);
            int startY = centerY - 10;

            for (int i = 0; i < SQUADS.length; i++) {
                int btnX = startX + (i * (btnWidth + spacing));

                if (mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= startY && mouseY <= startY + btnHeight) {
                    SquadNetwork.CHANNEL.sendToServer(new SquadNetwork.UpdateSquadPacket(SQUADS[i]));
                    
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.displayClientMessage(Component.literal("§aLINKED TO FREQUENCY: " + SQUADS[i]), true);
                        this.minecraft.setScreen(null); 
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}