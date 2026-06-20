package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    
    public WorkbenchScreen(WorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        // Force the GUI to cover the entire screen
        this.imageWidth = this.width; 
        this.imageHeight = this.height;
        this.leftPos = 0;
        this.topPos = 0;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 1. Full screen background
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF0A0A0A);

        // 2. Left Sidebar (Now slightly wider for the bigger slots)
        guiGraphics.fill(0, 0, 180, this.height, 0xFF141414);

        // 3. Red Accent Line
        guiGraphics.fill(20, 25, 160, 27, 0xFFD62929);

        // 4. BIGGER SLOT DRAWING (Width: 120 -> 140, Height: 32 -> 40)
        // Weapons (Pushed down slightly)
        drawCustomBox(guiGraphics, 20, 50, 140, 40); // Primary 
        drawCustomBox(guiGraphics, 20, 95, 140, 40); // Secondary
        
        // Armor (Chunky squares: 48 -> 60)
        drawCustomBox(guiGraphics, 20, 150, 60, 60); // Helmet
        drawCustomBox(guiGraphics, 90, 150, 60, 60); // Chest
        drawCustomBox(guiGraphics, 20, 220, 60, 60); // Legs
        drawCustomBox(guiGraphics, 90, 220, 60, 60); // Boots

        // 5. HUGE PLAYER MODEL
        if (Minecraft.getInstance().player != null) {
            // Place model to the right of the sidebar
            int playerX = 180 + (this.width - 180) / 2; 
            int playerY = this.height / 2 + 150;        
            
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    playerX, 
                    playerY, 
                    120, // Increased scale for a massive, high-detail look
                    (float)(playerX - mouseX),
                    (float)(playerY - 120 - mouseY),
                    Minecraft.getInstance().player 
            );
        }
    }

    private void drawCustomBox(GuiGraphics guiGraphics, int x, int y, int w, int h) {
        guiGraphics.fill(x, y, x + w, y + h, 0xFF333333); // Border
        guiGraphics.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xFF000000); // Inner
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY); 
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Because we forced leftPos and topPos to 0, these draw at absolute screen coordinates
        
        // Main Title
        guiGraphics.drawString(this.font, "LOADOUT", 20, 12, 0xFFFFFF, false);

        // Category Subheaders
        guiGraphics.drawString(this.font, "WEAPONS", 20, 35, 0xFFAAAAAA, false);
        guiGraphics.drawString(this.font, "ARMOR & MUNITIONS", 20, 125, 0xFFAAAAAA, false);
    }
}