package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    
    private boolean isDraggingModel = false;
    private float playerRotation = 0f;

    public WorkbenchScreen(WorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = this.width; 
        this.imageHeight = this.height;
        this.leftPos = 0;
        this.topPos = 0;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        // Sidebar is 240px wide. Clicking anywhere in the right-side void grabs the model.
        if (pMouseX >= 240) {
            this.isDraggingModel = true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        this.isDraggingModel = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.isDraggingModel) {
            // Dragging horizontally rotates the player model
            this.playerRotation += (float) pDragX * 1.5f; 
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int trueHeight = guiGraphics.guiHeight();
        int trueWidth = guiGraphics.guiWidth();

        // 1. Studio Dark Void
        guiGraphics.fill(0, 0, trueWidth, trueHeight, 0xFF070707);

        // 2. Main Left Sidebar (Shrunk to 240px width)
        guiGraphics.fill(0, 0, 240, trueHeight, 0xFF121212);

        // 3. Top Red Accent Line
        guiGraphics.fill(20, 16, 220, 18, 0xFFD62929);

        // --- SECTION 1: WEAPONS ---
        // Boxes stick together vertically (Y: 40, 85, 130)
        drawCleanBox(guiGraphics, 20, 40, 200, 45);  // Primary Tab
        drawCleanBox(guiGraphics, 20, 85, 200, 45);  // Secondary Tab
        drawCleanBox(guiGraphics, 20, 130, 200, 45); // Tactical Tab

        // --- SECTION 2: ARMOR & MUNITIONS ---
        // Boxes stick horizontally
        drawCleanBox(guiGraphics, 20, 205, 80, 55);  // Vest Box
        drawCleanBox(guiGraphics, 100, 205, 120, 55); // Specs Box
        guiGraphics.fill(100, 232, 220, 233, 0xFF2E3136); // Divider

        // Munition Slots (Vertical lines to separate groups)
        // Group 1 (5 slots packed)
        for(int i = 0; i < 5; i++) drawCleanBox(guiGraphics, 20 + (i * 20), 285, 20, 24);
        guiGraphics.fill(20, 309, 120, 317, 0xFF2E3136); // "AP" Bar 1
        guiGraphics.fill(123, 285, 124, 317, 0xFF2E3136); // Vertical Separator 1

        // Group 2 (3 slots packed)
        for(int i = 0; i < 3; i++) drawCleanBox(guiGraphics, 127 + (i * 20), 285, 20, 24);
        guiGraphics.fill(127, 309, 187, 317, 0xFF2E3136); // "AP" Bar 2
        guiGraphics.fill(190, 285, 191, 317, 0xFF2E3136); // Vertical Separator 2

        // Group 3 (1 slot)
        drawCleanBox(guiGraphics, 194, 285, 20, 24);
        guiGraphics.fill(194, 309, 214, 317, 0xFF2E3136); // "5" Bar 3

        // --- SECTION 3: HEADWEAR ---
        drawCleanBox(guiGraphics, 20, 345, 80, 55);  // Helmet Box
        drawCleanBox(guiGraphics, 100, 345, 120, 55); // Specs Box
        guiGraphics.fill(100, 372, 220, 373, 0xFF2E3136); // Divider

        // --- THE COLOSSAL OPERATOR MODEL ---
        if (Minecraft.getInstance().player != null) {
            int openSpaceCenter = 240 + (trueWidth - 240) / 2; 
            int operatorScale = 260; 
            int operatorFloorAnchor = trueHeight + 170; 

            guiGraphics.pose().pushPose();
            
            // Move origin to the player's center, accounting for the internal method's 50.0 Z-depth translation
            guiGraphics.pose().translate(openSpaceCenter, operatorFloorAnchor, 50.0);
            
            // Apply our 360-degree drag rotation
            guiGraphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(this.playerRotation));
            
            // Counter-translate so the model spins exactly on its axis instead of orbiting in a circle
            guiGraphics.pose().translate(0, 0, -50.0);

            // Render at 0,0 with 0,0 mouse tracking to force the model to stand perfectly rigid and straight
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    0, 
                    0, 
                    operatorScale, 
                    0f, 
                    0f,  
                    Minecraft.getInstance().player 
            );

            guiGraphics.pose().popPose();
        }
    }

    private void drawCleanBox(GuiGraphics guiGraphics, int x, int y, int w, int h) {
        guiGraphics.fill(x, y, x + w, y + h, 0xFF2E3136); // Subtle gray border
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF0B0C0E); // Deep interior
    }

    // Custom text scaler so bulky text fits cleanly into the bottom corners
    private void drawSmallText(GuiGraphics guiGraphics, String text, int x, int y, float scale, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(this.font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY); 
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // --- TOP HEADER ---
        guiGraphics.drawString(this.font, "LOADOUT", 20, 6, 0xFFFFFF, false);

        // --- SECTION 1: WEAPONS ---
        drawSmallText(guiGraphics, "WEAPONS", 20, 26, 0.65f, 0xFFAAAAAA);
        
        // Tab 1 (Bottom Left)
        drawSmallText(guiGraphics, "ASSAULT RIFLE", 26, 68, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "MK18", 26, 74, 0.75f, 0xFFD2D6DE);

        // Tab 2 
        drawSmallText(guiGraphics, "PISTOL", 26, 113, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "G19", 26, 119, 0.75f, 0xFFD2D6DE);

        // Tab 3
        drawSmallText(guiGraphics, "LONG TACTICAL", 26, 158, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "MIRRORGUN", 26, 164, 0.75f, 0xFFD2D6DE);

        // --- SECTION 2: ARMOR & MUNITIONS ---
        drawSmallText(guiGraphics, "ARMOR & MUNITIONS", 20, 190, 0.65f, 0xFFAAAAAA);
        
        // Vest Box
        drawSmallText(guiGraphics, "VEST | ", 26, 243, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "13 SLOTS", 26 + (int)(this.font.width("VEST | ") * 0.55f), 243, 0.55f, 0xFFD62929);
        drawSmallText(guiGraphics, "LIGHT ARMOR", 26, 249, 0.75f, 0xFFD2D6DE);

        // Specs Box
        drawSmallText(guiGraphics, "MATERIAL", 106, 218, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "STEEL", 106, 224, 0.75f, 0xFFD2D6DE);
        drawSmallText(guiGraphics, "COVERAGE", 106, 244, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "FRONT/BACK", 106, 250, 0.75f, 0xFFD2D6DE);

        // Munitions Line
        drawSmallText(guiGraphics, "MUNITION SLOTS", 20, 275, 0.65f, 0xFFAAAAAA);
        drawSmallText(guiGraphics, this.menu.getMunitionCount() + "/13 SLOTS", 165, 275, 0.65f, 0xFFD62929);

        drawSmallText(guiGraphics, "AP", 66, 310, 0.55f, 0xFFFFFFFF);
        drawSmallText(guiGraphics, "AP", 153, 310, 0.55f, 0xFFFFFFFF);
        drawSmallText(guiGraphics, "5", 201, 310, 0.55f, 0xFFFFFFFF);

        // --- SECTION 3: HEADWEAR ---
        drawSmallText(guiGraphics, "HEADWEAR", 20, 330, 0.65f, 0xFFAAAAAA);

        // Helmet Box
        drawSmallText(guiGraphics, "HELMET", 26, 383, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "HELMET ONLY", 26, 389, 0.75f, 0xFFD2D6DE);

        // Specs Box
        drawSmallText(guiGraphics, "MOUNT | ", 106, 358, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "WHITE PHOSPHOR", 106 + (int)(this.font.width("MOUNT | ") * 0.55f), 358, 0.55f, 0xFFD62929);
        drawSmallText(guiGraphics, "GPNVGS", 106, 364, 0.75f, 0xFFD2D6DE);
        drawSmallText(guiGraphics, "FACEWEAR", 106, 384, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "ANTI-FLASH GOGGLES", 106, 390, 0.75f, 0xFFD2D6DE);
    }
}