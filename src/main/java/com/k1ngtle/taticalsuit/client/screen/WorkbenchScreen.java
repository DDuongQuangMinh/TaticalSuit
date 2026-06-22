package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    
    private boolean isDraggingModel = false;
    private float playerRotation = 0f;
    
    // UI State Trackers
    private static boolean inGunsmith = false; 
    private static boolean showAmmunitionTab = true; 
    private static boolean showPrimaryWeaponTab = true; 
    
    private boolean inWeaponSelection = false; 
    
    // Scroll Trackers
    private float scrollOffset = 0f;
    private float maxScroll = 0f;

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
        
        // Reset transient states on open to prevent stuck GUI bugs
        this.inWeaponSelection = false;
        this.scrollOffset = 0f;
    }

    // Dynamic scanner to find Point Blank weapons in the player's inventory
    private java.util.List<Integer> getAvailableWeaponSlots() {
        java.util.List<Integer> availableInvSlots = new java.util.ArrayList<>();
        if (Minecraft.getInstance().player == null) return availableInvSlots;
        
        Inventory inv = Minecraft.getInstance().player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                String namespace = ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace();
                String path = ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath().toLowerCase();
                
                // Relaxed Scanner: Automatically picks up ANY modded item that doesn't sound like ammo/attachments
                if (!namespace.equals("minecraft") && 
                    !path.contains("mag") && !path.contains("ammo") && !path.contains("bullet") && 
                    !path.contains("grenade") && !path.contains("vest") && !path.contains("helmet") &&
                    !path.contains("optic") && !path.contains("sight") && !path.contains("grip") && 
                    !path.contains("suppressor") && !path.contains("barrel") && !path.contains("stock") && !path.contains("laser")) {
                    
                    availableInvSlots.add(i);
                }
            }
        }
        return availableInvSlots;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.inGunsmith) {
            
            // 1. Back Button
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                if (this.inWeaponSelection) {
                    this.inWeaponSelection = false; // Go back to Gunsmith Layout
                    this.scrollOffset = 0f;
                    return true;
                } else {
                    this.inGunsmith = false; // Go back to Loadout Layout
                    return true;
                }
            }

            // 2. Primary vs Side Arm Weapon Tabs (Top fixed area)
            if (pMouseY >= 70 && pMouseY <= 90) {
                if (pMouseX >= 20 && pMouseX <= 90) {
                    this.showPrimaryWeaponTab = true;
                    this.scrollOffset = 0f; // Reset scroll on switch
                    return true;
                } else if (pMouseX > 90 && pMouseX <= 180) {
                    this.showPrimaryWeaponTab = false;
                    this.scrollOffset = 0f; // Reset scroll on switch
                    return true;
                }
            }

            // 3. Handle Weapon Selection List Clicks
            if (this.inWeaponSelection) {
                int numBoxes = this.showPrimaryWeaponTab ? 11 : 12;
                int startY = 100 - (int)this.scrollOffset;
                java.util.List<Integer> weaponSlots = getAvailableWeaponSlots();
                
                for (int i = 0; i < numBoxes; i++) {
                    int boxY = startY + (i * 45);
                    if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                        if (i < weaponSlots.size()) {
                            int playerInvIndex = weaponSlots.get(i);
                            int menuSlotIndex = this.showPrimaryWeaponTab ? 0 : 1; 
                            
                            // Send swap packet to the Server!
                            com.k1ngtle.taticalsuit.network.ModNetworking.CHANNEL.sendToServer(
                                    new com.k1ngtle.taticalsuit.network.EquipWeaponPacket(menuSlotIndex, playerInvIndex)
                            );
                        }
                        this.inWeaponSelection = false; 
                        this.scrollOffset = 0f;
                        return true;
                    }
                }
                return true; // Consume clicks so they don't click through the list
            }

            // 4. Weapon Box Click (Opens Weapon Selection List)
            int weaponBoxY = 100 - (int)this.scrollOffset;
            if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= weaponBoxY && pMouseY <= weaponBoxY + 70) {
                this.inWeaponSelection = true; 
                this.scrollOffset = 0f;
                return true;
            }

            // 5. Sub-Tab clicks (Ammunition vs Deployable)
            int numCoreAttachments = this.showPrimaryWeaponTab ? 5 : 3;
            int baseTabY = 100 + 75 + 30 + (numCoreAttachments * 45) + 10;
            int scrolledTabY = baseTabY - (int)this.scrollOffset;
            
            if (pMouseY >= scrolledTabY && pMouseY <= scrolledTabY + 20) {
                if (pMouseX >= 20 && pMouseX <= 110) {
                    this.showAmmunitionTab = true;
                    return true;
                } else if (pMouseX > 110 && pMouseX <= 220) {
                    this.showAmmunitionTab = false;
                    return true;
                }
            }
            
            return true; // Block interaction with hidden vanilla slots behind the custom UI
        } else {
            // Check if clicking the Primary Weapon box on Loadout Screen
            if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 40 && pMouseY <= 85) {
                this.inGunsmith = true;
                this.scrollOffset = 0f; 
                this.showAmmunitionTab = true; 
                this.showPrimaryWeaponTab = true; 
                return true;
            }
            
            // Check if clicking the Side Arm Weapon box on Loadout Screen
            if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 85 && pMouseY <= 130) {
                this.inGunsmith = true;
                this.scrollOffset = 0f; 
                this.showAmmunitionTab = true; 
                this.showPrimaryWeaponTab = false; 
                return true;
            }
            
            if (pMouseX >= 240) {
                this.isDraggingModel = true;
            }
            
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        this.isDraggingModel = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.inGunsmith && pMouseX < 240 && pMouseY >= 90) {
            this.scrollOffset -= (float) pDragY;
            this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));
            return true;
        }
        
        if (this.isDraggingModel && !this.inGunsmith) {
            this.playerRotation += (float) pDragX * 1.5f; 
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (this.inGunsmith) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707); // Studio Void
            this.renderGunsmithBg(guiGraphics, this.height);
            this.renderGunsmithLabels(guiGraphics, mouseX, mouseY);
        } else {
            super.render(guiGraphics, mouseX, mouseY, delta);
            renderTooltip(guiGraphics, mouseX, mouseY); 
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707);
        renderLoadoutBg(guiGraphics, this.width, this.height, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderLoadoutLabels(guiGraphics);
    }

    // --- MAIN LOADOUT BACKGROUND ---
    private void renderLoadoutBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, 240, trueHeight, 0xFF121212);
        guiGraphics.fill(20, 16, 220, 18, 0xFFD62929);

        // --- SECTION 1: WEAPONS ---
        drawCleanBox(guiGraphics, 20, 40, 200, 45);  
        drawCleanBox(guiGraphics, 20, 85, 200, 45);  
        drawCleanBox(guiGraphics, 20, 130, 200, 45); 

        // --- SECTION 2: ARMOR & MUNITIONS ---
        drawCleanBox(guiGraphics, 20, 205, 80, 55);  
        drawCleanBox(guiGraphics, 100, 205, 120, 55); 
        guiGraphics.fill(100, 232, 220, 233, 0xFF2E3136); 

        for(int i = 0; i < 5; i++) drawCleanBox(guiGraphics, 20 + (i * 20), 285, 20, 24);
        guiGraphics.fill(20, 309, 120, 317, 0xFF2E3136); 
        guiGraphics.fill(123, 285, 124, 317, 0xFF2E3136); 

        for(int i = 0; i < 3; i++) drawCleanBox(guiGraphics, 127 + (i * 20), 285, 20, 24);
        guiGraphics.fill(127, 309, 187, 317, 0xFF2E3136); 
        guiGraphics.fill(190, 285, 191, 317, 0xFF2E3136); 

        drawCleanBox(guiGraphics, 194, 285, 20, 24);
        guiGraphics.fill(194, 309, 214, 317, 0xFF2E3136); 

        // --- SECTION 3: HEADWEAR ---
        drawCleanBox(guiGraphics, 20, 345, 80, 55);  
        drawCleanBox(guiGraphics, 100, 345, 120, 55); 
        guiGraphics.fill(100, 372, 220, 373, 0xFF2E3136); 

        // --- THE COLOSSAL OPERATOR MODEL ---
        if (Minecraft.getInstance().player != null) {
            int openSpaceCenter = 240 + (trueWidth - 240) / 2; 
            int operatorScale = 260; 
            int operatorFloorAnchor = trueHeight + 170; 

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(openSpaceCenter, operatorFloorAnchor, 50.0);
            guiGraphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(this.playerRotation));
            guiGraphics.pose().translate(0, 0, -50.0);

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    0, 0, operatorScale, 0f, 0f, Minecraft.getInstance().player 
            );

            guiGraphics.pose().popPose();
        }
    }

    // --- GUNSMITH BACKGROUND ---
    private void renderGunsmithBg(GuiGraphics guiGraphics, int trueHeight) {
        int startY = 100;
        int visibleHeight = trueHeight - 100;
        
        if (this.inWeaponSelection) {
            // WEAPON SELECTION LIST BACKGROUND (11 or 12 Boxes)
            int numBoxes = this.showPrimaryWeaponTab ? 11 : 12;
            int listHeight = numBoxes * 45; 
            
            this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
            this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

            guiGraphics.enableScissor(0, 90, 240, trueHeight);
            int currentY = startY - (int)this.scrollOffset;
            
            for (int i = 0; i < numBoxes; i++) {
                drawCleanBox(guiGraphics, 20, currentY, 200, 40);
                currentY += 45;
            }
            
            if (this.maxScroll > 0) {
                guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
                int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
                int thumbY = 100 + (int)((this.scrollOffset / this.maxScroll) * (visibleHeight - 20 - thumbHeight));
                guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
            }
            guiGraphics.disableScissor();
            
        } else {
            // NORMAL GUNSMITH BUILD BACKGROUND
            int numPrimary = 2; 
            int numSidearm = 2; 
            int numGrenade = 4; 
            int numTactical = 5; 
            
            int dynamicItemsHeight = this.showAmmunitionTab 
                    ? (20 + 16 + (numPrimary * 31) + 10 + 16 + (numSidearm * 31)) 
                    : (20 + 16 + (numGrenade * 31) + 10 + 16 + (numTactical * 31));
                    
            int numCoreAttachments = this.showPrimaryWeaponTab ? 5 : 3;
            int listHeight = 75 + 30 + (numCoreAttachments * 45) + 35 + dynamicItemsHeight; 
            
            this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
            this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

            guiGraphics.enableScissor(0, 90, 240, trueHeight);
            int currentY = startY - (int)this.scrollOffset;
            
            drawCleanBox(guiGraphics, 20, currentY, 200, 70); 
            currentY += 75;

            currentY += 5; 
            guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
            currentY += 25;

            for (int i = 0; i < numCoreAttachments; i++) {
                drawCleanBox(guiGraphics, 20, currentY, 200, 40);
                currentY += 45;
            }

            int tabY = currentY + 10;
            guiGraphics.fill(20, tabY + 14, 220, tabY + 15, 0xFF2E3136); 
            
            if (this.showAmmunitionTab) {
                guiGraphics.fill(20, tabY + 14, 110, tabY + 15, 0xFFD62929); 
            } else {
                guiGraphics.fill(110, tabY + 14, 220, tabY + 15, 0xFFD62929); 
            }
            currentY = tabY + 20; 
            
            if (this.showAmmunitionTab) {
                guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
                currentY += 16;
                for (int i = 0; i < numPrimary; i++) {
                    guiGraphics.fill(20, currentY + 30, 220, currentY + 31, 0xFF2E3136);
                    currentY += 31; 
                }
                
                currentY += 10; 
                guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
                currentY += 16;
                for (int i = 0; i < numSidearm; i++) {
                    guiGraphics.fill(20, currentY + 30, 220, currentY + 31, 0xFF2E3136);
                    currentY += 31; 
                }
            } else {
                guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
                currentY += 16;
                for (int i = 0; i < numGrenade; i++) {
                    guiGraphics.fill(20, currentY + 30, 220, currentY + 31, 0xFF2E3136);
                    currentY += 31; 
                }

                currentY += 10; 
                guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
                currentY += 16;
                for (int i = 0; i < numTactical; i++) {
                    guiGraphics.fill(20, currentY + 30, 220, currentY + 31, 0xFF2E3136);
                    currentY += 31; 
                }
            }

            if (this.maxScroll > 0) {
                guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
                int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
                int thumbY = 100 + (int)((this.scrollOffset / this.maxScroll) * (visibleHeight - 20 - thumbHeight));
                guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
            }
            guiGraphics.disableScissor();
        }
    }

    private void drawCleanBox(GuiGraphics guiGraphics, int x, int y, int w, int h) {
        guiGraphics.fill(x, y, x + w, y + h, 0xFF2E3136); 
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF0B0C0E); 
    }

    private void drawSmallText(GuiGraphics guiGraphics, String text, int x, int y, float scale, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(this.font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }
    
    // --- MAIN LOADOUT TEXT ---
    private void renderLoadoutLabels(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, "LOADOUT", 20, 6, 0xFFFFFF, false);

        // Fetch actual item names from the inventory slots
        ItemStack primaryStack = this.menu.getSlot(0).getItem();
        String primaryName = primaryStack.isEmpty() ? "UNARMED" : primaryStack.getHoverName().getString().toUpperCase();
        
        ItemStack secondaryStack = this.menu.getSlot(1).getItem();
        String secondaryName = secondaryStack.isEmpty() ? "UNARMED" : secondaryStack.getHoverName().getString().toUpperCase();

        drawSmallText(guiGraphics, "WEAPONS", 20, 26, 0.65f, 0xFFAAAAAA);
        drawSmallText(guiGraphics, "PRIMARY", 26, 68, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, primaryName, 26, 74, 0.75f, 0xFFD2D6DE);
        
        drawSmallText(guiGraphics, "SIDE ARM", 26, 113, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, secondaryName, 26, 119, 0.75f, 0xFFD2D6DE);
        
        drawSmallText(guiGraphics, "LONG TACTICAL", 26, 158, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "MIRRORGUN", 26, 164, 0.75f, 0xFFD2D6DE);

        drawSmallText(guiGraphics, "ARMOR & MUNITIONS", 20, 190, 0.65f, 0xFFAAAAAA);
        drawSmallText(guiGraphics, "VEST | ", 26, 243, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "13 SLOTS", 26 + (int)(this.font.width("VEST | ") * 0.55f), 243, 0.55f, 0xFFD62929);
        drawSmallText(guiGraphics, "LIGHT ARMOR", 26, 249, 0.75f, 0xFFD2D6DE);

        drawSmallText(guiGraphics, "MATERIAL", 106, 218, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "STEEL", 106, 224, 0.75f, 0xFFD2D6DE);
        drawSmallText(guiGraphics, "COVERAGE", 106, 244, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "FRONT/BACK", 106, 250, 0.75f, 0xFFD2D6DE);

        drawSmallText(guiGraphics, "MUNITION SLOTS", 20, 275, 0.65f, 0xFFAAAAAA);
        drawSmallText(guiGraphics, this.menu.getMunitionCount() + "/13 SLOTS", 165, 275, 0.65f, 0xFFD62929);

        drawSmallText(guiGraphics, "AP", 66, 310, 0.55f, 0xFFFFFFFF);
        drawSmallText(guiGraphics, "AP", 153, 310, 0.55f, 0xFFFFFFFF);
        drawSmallText(guiGraphics, "5", 201, 310, 0.55f, 0xFFFFFFFF);

        drawSmallText(guiGraphics, "HEADWEAR", 20, 330, 0.65f, 0xFFAAAAAA);
        drawSmallText(guiGraphics, "HELMET", 26, 383, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "HELMET ONLY", 26, 389, 0.75f, 0xFFD2D6DE);
        drawSmallText(guiGraphics, "MOUNT | ", 106, 358, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "WHITE PHOSPHOR", 106 + (int)(this.font.width("MOUNT | ") * 0.55f), 358, 0.55f, 0xFFD62929);
        drawSmallText(guiGraphics, "GPNVGS", 106, 364, 0.75f, 0xFFD2D6DE);
        drawSmallText(guiGraphics, "FACEWEAR", 106, 384, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "ANTI-FLASH GOGGLES", 106, 390, 0.75f, 0xFFD2D6DE);
    }

    // --- GUNSMITH TEXT ---
    private void renderGunsmithLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawSmallText(guiGraphics, "< WEAPON BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        // Primary vs Side Arm Weapon Switch Tabs
        drawSmallText(guiGraphics, "PRIMARY", 20, 75, 0.85f, this.showPrimaryWeaponTab ? 0xFFFFFFFF : 0xFF7A818C);
        drawSmallText(guiGraphics, "SIDE ARM", 100, 75, 0.85f, !this.showPrimaryWeaponTab ? 0xFFFFFFFF : 0xFF7A818C);
        
        if (this.showPrimaryWeaponTab) {
            guiGraphics.fill(20, 87, 80, 89, 0xFFD62929); 
        } else {
            guiGraphics.fill(100, 87, 160, 89, 0xFFD62929); 
        }

        int startY = 100;
        int currentY = startY - (int)this.scrollOffset;
        int leftX = 26;

        guiGraphics.enableScissor(0, 90, 240, guiGraphics.guiHeight());
        
        if (this.inWeaponSelection) {
            // WEAPON SELECTION LIST LABELS (11 or 12 Boxes)
            int numBoxes = this.showPrimaryWeaponTab ? 11 : 12;
            java.util.List<Integer> weaponSlots = getAvailableWeaponSlots();
            Inventory inv = Minecraft.getInstance().player.getInventory();
            
            ItemStack previewStack = this.menu.getSlot(this.showPrimaryWeaponTab ? 0 : 1).getItem(); // Default to equipped
            
            for (int i = 0; i < numBoxes; i++) {
                if (i < weaponSlots.size()) {
                    ItemStack stack = inv.getItem(weaponSlots.get(i));
                    String name = stack.getHoverName().getString().toUpperCase();
                    
                    // Track if this specific box is being hovered to show preview
                    if (mouseY >= currentY && mouseY <= currentY + 40 && mouseX >= 20 && mouseX <= 220) {
                        previewStack = stack; 
                        guiGraphics.fill(21, currentY + 1, 219, currentY + 39, 0xFF3E4249); // Lighter hover effect
                    }
                    
                    drawSmallText(guiGraphics, name, leftX + 5, currentY + 6, 0.55f, 0xFFFFFFFF);
                    
                    // Large item 3D preview inside the 200x40 box
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(leftX + 70, currentY + 8, 100);
                    guiGraphics.pose().scale(2.5f, 2.5f, 1.0f);
                    guiGraphics.renderItem(stack, 0, 0);
                    guiGraphics.pose().popPose();
                } else {
                    drawSmallText(guiGraphics, "EMPTY SLOT", leftX + 60, currentY + 16, 0.65f, 0xFF555555);
                }
                currentY += 45;
            }
            guiGraphics.disableScissor();

            // --- MASSIVE HOVER PREVIEW ON THE RIGHT SIDE ---
            if (!previewStack.isEmpty()) {
                guiGraphics.pose().pushPose();
                int rightCenterX = 240 + (guiGraphics.guiWidth() - 240) / 2;
                int rightCenterY = guiGraphics.guiHeight() / 2 - 40; 
                
                guiGraphics.pose().translate(rightCenterX - 40, rightCenterY, 100); 
                guiGraphics.pose().scale(6.0f, 6.0f, 1.0f); 
                guiGraphics.renderItem(previewStack, 0, 0);
                guiGraphics.pose().popPose();
            }

        } else {
            // NORMAL GUNSMITH LABELS
            drawSmallText(guiGraphics, "WEAPON", leftX, currentY + 50, 0.45f, 0xFF7A818C);
            drawSmallText(guiGraphics, "CURRENT", leftX, currentY + 58, 0.65f, 0xFFD2D6DE);
            
            ItemStack weaponStack = this.menu.getSlot(this.showPrimaryWeaponTab ? 0 : 1).getItem();
            if (!weaponStack.isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(110, currentY + 8, 100); 
                guiGraphics.pose().scale(3.5f, 3.5f, 1.0f); 
                guiGraphics.renderItem(weaponStack, 0, 0);
                guiGraphics.pose().popPose();
            } else {
                drawSmallText(guiGraphics, "NO WEAPON EQUIPPED", 90, currentY + 32, 0.55f, 0xFF555555);
            }

            currentY += 75;

            currentY += 5; 
            drawSmallText(guiGraphics, "ATTACHMENTS", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 25;

            int numCoreAttachments = this.showPrimaryWeaponTab ? 5 : 3;
            String[] boxCats = this.showPrimaryWeaponTab 
                    ? new String[]{"OPTIC", "BARREL", "MUZZLE", "UNDERBARREL", "LASER"}
                    : new String[]{"OPTIC", "MUZZLE", "LASER"};
            String[] boxNames = this.showPrimaryWeaponTab
                    ? new String[]{"EXPS3 HOLOGRAPHIC", "10.3\" CQB BARREL", "SUREFIRE SOCOM556", "MAGPUL RVG", "PEQ-15"}
                    : new String[]{"RMR RED DOT", "SILENCERCO OMEGA", "TLR-7 G"};

            for (int i = 0; i < numCoreAttachments; i++) {
                drawSmallText(guiGraphics, boxCats[i], leftX, currentY + 24, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, boxNames[i], leftX, currentY + 32, 0.65f, 0xFFD2D6DE);
                currentY += 45;
            }

            int tabY = currentY + 10;
            drawSmallText(guiGraphics, "AMMUNITION", leftX, tabY + 6, 0.55f, this.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
            drawSmallText(guiGraphics, "DEPLOYABLE", 116, tabY + 6, 0.55f, !this.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
            currentY = tabY + 20;

            if (this.showAmmunitionTab) {
                String[] primaryCats = {"MAGAZINE", "AMMUNITION"};
                String[] primaryNames = {"PMAG 30RND", "5.56X45MM NATO"};
                String[] sidearmCats = {"MAGAZINE", "AMMUNITION"};
                String[] sidearmNames = {"G19 MAG", "9X19MM PARABELLUM"};
                
                drawSmallText(guiGraphics, "PRIMARY AMMUNITION", leftX, currentY + 6, 0.65f, 0xFF7A818C);
                currentY += 16;
                for (int i = 0; i < primaryCats.length; i++) {
                    drawSmallText(guiGraphics, primaryCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                    drawSmallText(guiGraphics, primaryNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                    currentY += 31;
                }

                currentY += 10; 
                drawSmallText(guiGraphics, "SIDEARM AMMUNITION", leftX, currentY + 6, 0.65f, 0xFF7A818C);
                currentY += 16;
                for (int i = 0; i < sidearmCats.length; i++) {
                    drawSmallText(guiGraphics, sidearmCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                    drawSmallText(guiGraphics, sidearmNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                    currentY += 31;
                }
            } else {
                String[] grenadeCats = {"GRENADE", "GRENADE", "GRENADE", "GRENADE"};
                String[] grenadeNames = {"9-BANG FLASH GRENADE", "CS GAS", "FLASHBANGS", "STINGER"};
                String[] tacticalCats = {"TACTICAL", "TACTICAL", "TACTICAL", "TACTICAL", "TACTICAL"};
                String[] tacticalNames = {"C2", "LOCKPICK GUN", "PEPPER SPRAY", "TASER", "WEDGE"};
                
                drawSmallText(guiGraphics, "GRENADE", leftX, currentY + 6, 0.65f, 0xFF7A818C);
                currentY += 16;
                for (int i = 0; i < grenadeCats.length; i++) {
                    drawSmallText(guiGraphics, grenadeCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                    drawSmallText(guiGraphics, grenadeNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                    currentY += 31;
                }

                currentY += 10; 
                drawSmallText(guiGraphics, "TACTICAL", leftX, currentY + 6, 0.65f, 0xFF7A818C);
                currentY += 16;
                for (int i = 0; i < tacticalCats.length; i++) {
                    drawSmallText(guiGraphics, tacticalCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                    drawSmallText(guiGraphics, tacticalNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                    currentY += 31;
                }
            }

            guiGraphics.disableScissor();
        }
    }
}