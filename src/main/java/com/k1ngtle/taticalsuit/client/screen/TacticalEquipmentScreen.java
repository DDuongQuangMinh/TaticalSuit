package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.menu.TacticalEquipmentMenu;
import com.k1ngtle.taticalsuit.menu.TacticalEquipmentSlot;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class TacticalEquipmentScreen extends AbstractContainerScreen<TacticalEquipmentMenu> {

    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/inventory.png");
    private static final ResourceLocation GEAR_ICON = new ResourceLocation(TaticalSuit.MODID, "textures/gui/gear_button.png");

    public TacticalEquipmentScreen(TacticalEquipmentMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        
        // Setup toggle button inside the player model black box
        this.addRenderableWidget(new ImageButton(
            this.leftPos + 65, this.topPos + 9, 
            10, 10, 
            0, 0, 
            10, 
            GEAR_ICON, 
            256, 256, 
            button -> {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
                }
            }
        ));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Intentionally left empty to completely delete the "Tactical Equipment" text 
        // and allow the crafting grid text to show natively.
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Rendering the Base Vanilla UI
        guiGraphics.blit(INVENTORY_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Drawing the sleek Curios-style Left Fly-out Panel
        int panelW = 34;
        int panelH = 156; 
        int panelX = leftPos - panelW;
        int panelY = topPos + 2;

        // Draw solid light-gray panel base
        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFFC6C6C6);
        
        // Draw the 3D raised white/dark bevel border
        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + 1, 0xFFFFFFFF); // Top White
        guiGraphics.fill(panelX, panelY, panelX + 1, panelY + panelH, 0xFFFFFFFF); // Left White
        guiGraphics.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, 0xFF555555); // Bottom Dark
        
        // Seamlessly blend the panel into the main inventory
        guiGraphics.fill(leftPos, panelY + 1, leftPos + 1, panelY + panelH - 1, 0xFFC6C6C6);

        // Hardcoded loop to guarantee exactly 8 slot boxes render
        for (int i = 0; i < 8; i++) {
            int sx = leftPos - 29; 
            int sy = topPos + 8 + (i * 18) - 1;

            guiGraphics.fill(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B); // Outer Base Dark Fill
            
            guiGraphics.fill(sx, sy, sx + 18, sy + 1, 0xFF373737); // Top Dark Bevel
            guiGraphics.fill(sx, sy, sx + 1, sy + 18, 0xFF373737); // Left Dark Bevel
            guiGraphics.fill(sx, sy + 17, sx + 18, sy + 18, 0xFFFFFFFF); // Bottom White Bevel
            guiGraphics.fill(sx + 17, sy, sx + 18, sy + 18, 0xFFFFFFFF); // Right White Bevel
        }

        // Rendering the 3D Player
        if (this.minecraft != null && this.minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics, 
                    leftPos + 51, topPos + 75, 
                    30, 
                    (float)(leftPos + 51 - mouseX), 
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
        
        boolean renderedCustomTooltip = false;
        
        // 1. Standard Hover Check
        if (this.hoveredSlot instanceof TacticalEquipmentSlot tacSlot && !this.hoveredSlot.hasItem()) {
            renderTacTooltip(guiGraphics, tacSlot, mouseX, mouseY);
            renderedCustomTooltip = true;
        }
        
        // 2. Bulletproof Manual Fallback Hover Check
        if (!renderedCustomTooltip) {
            for (int i = 0; i < 8; i++) {
                int sx = leftPos - 28;
                int sy = topPos + 8 + (i * 18);
                
                // If the mouse is mathematically inside the 16x16 slot bounds
                if (mouseX >= sx && mouseX < sx + 16 && mouseY >= sy && mouseY < sy + 16) {
                    
                    // Find the matching slot from the menu
                    for (Slot slot : this.menu.slots) {
                        if (slot instanceof TacticalEquipmentSlot tacSlot && tacSlot.getSlotType().getIndex() == i) {
                            
                            // Force draw the hover highlight box (Vanilla ignores this sometimes for external slots)
                            guiGraphics.fill(sx, sy, sx + 16, sy + 16, 0x80FFFFFF); 
                            
                            // Render Tooltip
                            if (!tacSlot.hasItem()) {
                                renderTacTooltip(guiGraphics, tacSlot, mouseX, mouseY);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private void renderTacTooltip(GuiGraphics guiGraphics, TacticalEquipmentSlot tacSlot, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(tacSlot.getSlotType().getDisplayName()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Equip tactical " + tacSlot.getSlotType().getDisplayName().toLowerCase() + " here.").withStyle(ChatFormatting.GRAY));
        guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        boolean clickedOutsideStandard = super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
        
        // Define the safe bounding box of our custom left fly-out panel
        boolean clickedInsidePanel = mouseX >= guiLeft - 34 && mouseX < guiLeft && mouseY >= guiTop + 2 && mouseY < guiTop + 158;
        
        // Only trigger item-drop if they clicked outside BOTH the standard inventory AND our custom panel
        return clickedOutsideStandard && !clickedInsidePanel;
    }
}