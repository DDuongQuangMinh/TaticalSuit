package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.menu.TacticalEquipmentMenu;
import com.k1ngtle.taticalsuit.menu.TacticalEquipmentSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TacticalEquipmentScreen extends AbstractContainerScreen<TacticalEquipmentMenu> {

    public TacticalEquipmentScreen(TacticalEquipmentMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        
        // Small, unobtrusive back button in the top left corner
        this.addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
            }
        }).bounds(this.leftPos + 6, this.topPos + 6, 16, 16).build());
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Disabled standard labels to keep the model area clean
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Sleek dark background
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF121212);
        guiGraphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF2E3136);

        // 3D Player Model Background Box
        int boxX = leftPos + 48;
        int boxY = topPos + 7;
        guiGraphics.fill(boxX, boxY, boxX + 80, boxY + 72, 0xFF000000);

        // Draw backgrounds for all slots so they fit the dark theme
        for (net.minecraft.world.inventory.Slot slot : this.menu.slots) {
            guiGraphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, 0xFF0B0C0E);
        }

        // Render the 3D Player in the center!
        if (this.minecraft != null && this.minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics, 
                    leftPos + 88, topPos + 75, 
                    30, 
                    (float)(leftPos + 88 - mouseX), 
                    (float)(topPos + 25 - mouseY), 
                    this.minecraft.player
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        
        // Render custom descriptions for empty tactical slots
        if (this.hoveredSlot instanceof TacticalEquipmentSlot tacSlot && !this.hoveredSlot.hasItem()) {
            guiGraphics.renderTooltip(this.font, Component.literal("§7" + tacSlot.getSlotType().getDisplayName()), mouseX, mouseY);
        }
    }
}