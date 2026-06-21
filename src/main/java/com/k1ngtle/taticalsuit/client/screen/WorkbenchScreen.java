package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    
    private boolean isDraggingModel = false;
    private float playerRotation = 0f;
    private boolean inGunsmith = false; // UI State Tracker
    private boolean inWeaponSelection = false; // Weapon Grid State Tracker
    private boolean showAmmunitionTab = true; // Sub-Tab Tracker for Gunsmith
    private boolean showPrimaryWeaponTab = true; // Weapon Tracker for Gunsmith
    
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
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.inWeaponSelection) {
            // Back Button inside Weapon Selection
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                this.inWeaponSelection = false;
                this.scrollOffset = 0f;
                return true;
            }

            // Weapon Grid Clicks
            int startX = 22;
            int startY = 100 - (int)this.scrollOffset;
            int boxSize = 46;
            int spacing = 6;
            
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 4; col++) {
                    int x = startX + (col * (boxSize + spacing));
                    int y = startY + (row * (boxSize + spacing));
                    
                    if (pMouseX >= x && pMouseX <= x + boxSize && pMouseY >= y && pMouseY <= y + boxSize) {
                        // Clicked a gun! Transition back to Gunsmith layout
                        this.inWeaponSelection = false;
                        this.scrollOffset = 0f;
                        return true;
                    }
                }
            }
            return true;
        } else if (this.inGunsmith) {
            // Back Button inside the Gunsmith
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                this.inGunsmith = false;
                return true;
            }

            // Primary vs Side Arm Weapon Tabs (Top fixed area)
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

            // Weapon Box Click (Opens Weapon Selection Grid)
            int weaponBoxY = 100 - (int)this.scrollOffset;
            if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= weaponBoxY && pMouseY <= weaponBoxY + 70) {
                this.inWeaponSelection = true;
                this.scrollOffset = 0f; // Reset scroll for the new grid layout
                return true;
            }

            // Sub-Tab clicks (Ammunition vs Deployable)
            // The Y coordinate dynamically shifts based on the number of core attachments
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
            
            return true; // Block interaction with hidden vanilla slots behind the custom UI!
        } else {
            // Check if clicking the Primary Weapon tab
            if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 40 && pMouseY <= 85) {
                this.inGunsmith = true;
                this.scrollOffset = 0f; // Reset scroll when entering Gunsmith
                this.showAmmunitionTab = true; // Default to Ammunition tab
                this.showPrimaryWeaponTab = true; // Default to Primary Weapon
                return true;
            }
            
            // Grab model rotation
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
        // Drag up/down on the left side to scroll lists (Works for both Gunsmith and Weapon Selection)
        if ((this.inGunsmith || this.inWeaponSelection) && pMouseX < 240 && pMouseY >= 90) {
            this.scrollOffset -= (float) pDragY;
            this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));
            return true;
        }
        
        // Drag horizontally on the right side to rotate model
        if (this.isDraggingModel && !this.inGunsmith && !this.inWeaponSelection) {
            this.playerRotation += (float) pDragX * 1.5f; 
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (this.inWeaponSelection) {
            // Weapon Grid Selection Screen
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707); // Studio Void
            this.renderWeaponSelectionBg(guiGraphics, this.height);
            this.renderWeaponSelectionLabels(guiGraphics, this.width, this.height);
        } else if (this.inGunsmith) {
            // Gunsmith Build Screen
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707); // Studio Void
            this.renderGunsmithBg(guiGraphics, this.height);
            this.renderGunsmithLabels(guiGraphics);
        } else {
            // Normal Loadout screen with slots
            super.render(guiGraphics, mouseX, mouseY, delta);
            renderTooltip(guiGraphics, mouseX, mouseY); 
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // This is only called when !inGunsmith, thanks to our custom render() branch.
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707);
        renderLoadoutBg(guiGraphics, this.width, this.height, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Only called when !inGunsmith
        renderLoadoutLabels(guiGraphics);
    }

    // --- WEAPON SELECTION BACKGROUND ---
    private void renderWeaponSelectionBg(GuiGraphics guiGraphics, int trueHeight) {
        int startY = 100;
        int boxSize = 46;
        int spacing = 6;
        int rows = 8; // Dynamically adjusts grid bounds
        int cols = 4;
        
        int listHeight = rows * (boxSize + spacing); 
        int visibleHeight = trueHeight - 100;
        
        // Calculate max scroll bounds
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        // Clip rendering so grid items don't draw over the header
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        
        int currentY = startY - (int)this.scrollOffset;
        int startX = 22;

        ItemStack currentWeapon = this.menu.getSlot(this.showPrimaryWeaponTab ? 0 : 1).getItem();

        // Render the Grid
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = startX + (c * (boxSize + spacing));
                int y = currentY + (r * (boxSize + spacing));
                drawCleanBox(guiGraphics, x, y, boxSize, boxSize);
                
                // Draw a mock item visually to fill the grid up
                if (!currentWeapon.isEmpty() && (r * cols + c) < 20) {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(x + 5, y + 5, 10);
                    guiGraphics.pose().scale(2.2f, 2.2f, 1.0f);
                    guiGraphics.renderItem(currentWeapon, 0, 0);
                    guiGraphics.pose().popPose();
                }
            }
        }
        
        // Dynamic Visual Scrollbar Track
        if (this.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((this.scrollOffset / this.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        
        guiGraphics.disableScissor();
    }

    // --- WEAPON SELECTION TEXT ---
    private void renderWeaponSelectionLabels(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        drawSmallText(guiGraphics, "< WEAPON BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        String title = this.showPrimaryWeaponTab ? "PRIMARY" : "SIDE ARM";
        String subtitle = this.showPrimaryWeaponTab ? "ASSAULT RIFLE" : "PISTOL";
        
        drawSmallText(guiGraphics, title, 20, 55, 1.1f, 0xFFFFFF); 
        drawSmallText(guiGraphics, subtitle, 20, 75, 0.65f, 0xFFD62929); 

        // --- DISPLAY MASSIVE PREVIEW MODEL ON THE RIGHT SIDE ---
        ItemStack weaponStack = this.menu.getSlot(this.showPrimaryWeaponTab ? 0 : 1).getItem();
        if (!weaponStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            int rightCenterX = 240 + (trueWidth - 240) / 2;
            int rightCenterY = trueHeight / 2 - 40; 
            
            guiGraphics.pose().translate(rightCenterX - 40, rightCenterY, 100); 
            guiGraphics.pose().scale(6.0f, 6.0f, 1.0f); 
            guiGraphics.renderItem(weaponStack, 0, 0);
            guiGraphics.pose().popPose();
        }
    }

    // --- MAIN LOADOUT BACKGROUND ---
    private void renderLoadoutBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight, int mouseX, int mouseY) {
        // Sidebar (240px width)
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
        
        int numPrimary = 2; 
        int numSidearm = 2; 
        int numGrenade = 4; 
        int numTactical = 5; 
        
        int dynamicItemsHeight = this.showAmmunitionTab 
                ? (20 + 16 + (numPrimary * 31) + 10 + 16 + (numSidearm * 31)) 
                : (20 + 16 + (numGrenade * 31) + 10 + 16 + (numTactical * 31));
                
        // Calculate core boxes (5 for primary, 3 for sidearm)
        int numCoreAttachments = this.showPrimaryWeaponTab ? 5 : 3;
        
        // 75 (Weapon) + 30 (Header) + CoreBoxes + 35 (Tabs)
        int listHeight = 75 + 30 + (numCoreAttachments * 45) + 35 + dynamicItemsHeight; 
        int visibleHeight = trueHeight - 100;
        
        // Calculate max scroll bounds
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        // Clip rendering so items don't draw over the header or off the screen
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        
        int currentY = startY - (int)this.scrollOffset;
        
        // 1. Top Weapon Box (Larger to display the gun, 70px tall)
        drawCleanBox(guiGraphics, 20, currentY, 200, 70); 
        currentY += 75;

        // 2. Attachments Header Line
        currentY += 5; // Padding
        guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
        currentY += 25;

        // 3. Core Attachment Boxes (40px tall dynamically generated)
        for (int i = 0; i < numCoreAttachments; i++) {
            drawCleanBox(guiGraphics, 20, currentY, 200, 40);
            currentY += 45;
        }

        // 4. Dual Tabs (Ammunition & Deployable) - Underlines
        int tabY = currentY + 10;
        guiGraphics.fill(20, tabY + 14, 220, tabY + 15, 0xFF2E3136); // Base line
        
        if (this.showAmmunitionTab) {
            guiGraphics.fill(20, tabY + 14, 110, tabY + 15, 0xFFD62929); // Red line under Ammo
        } else {
            guiGraphics.fill(110, tabY + 14, 220, tabY + 15, 0xFFD62929); // Red line under Deployable
        }
        currentY = tabY + 20; // 20px pad below the tabs line
        
        // 5. Sub-items (Underlines ONLY, perfectly synchronized with labels)
        if (this.showAmmunitionTab) {
            // Primary Ammunition Header Line
            guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
            currentY += 16;
            
            // Primary Items Lines (dynamically loops)
            for (int i = 0; i < numPrimary; i++) {
                guiGraphics.fill(20, currentY + 30, 220, currentY + 31, 0xFF2E3136);
                currentY += 31; // Tighter 31px spacing
            }
            
            // Sidearm Ammunition Header Line
            currentY += 10; // Padding
            guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
            currentY += 16;
            
            // Sidearm Items Lines (dynamically loops)
            for (int i = 0; i < numSidearm; i++) {
                guiGraphics.fill(20, currentY + 30, 220, currentY + 31, 0xFF2E3136);
                currentY += 31; // Tighter 31px spacing
            }
        } else {
            // Grenade Header Line
            guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
            currentY += 16;

            // Grenade Items Lines (dynamically loops)
            for (int i = 0; i < numGrenade; i++) {
                guiGraphics.fill(20, currentY + 30, 220, currentY + 31, 0xFF2E3136);
                currentY += 31; // Tighter 31px spacing
            }

            // Tactical Header Line
            currentY += 10; // Padding
            guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
            currentY += 16;

            // Tactical Items Lines (dynamically loops)
            for (int i = 0; i < numTactical; i++) {
                guiGraphics.fill(20, currentY + 30, 220, currentY + 31, 0xFF2E3136);
                currentY += 31; // Tighter 31px spacing
            }
        }

        // Dynamic Visual Scrollbar Track
        if (this.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((this.scrollOffset / this.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        
        guiGraphics.disableScissor();
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

        drawSmallText(guiGraphics, "WEAPONS", 20, 26, 0.65f, 0xFFAAAAAA);
        drawSmallText(guiGraphics, "ASSAULT RIFLE", 26, 68, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "MK18", 26, 74, 0.75f, 0xFFD2D6DE);
        drawSmallText(guiGraphics, "PISTOL", 26, 113, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, "G19", 26, 119, 0.75f, 0xFFD2D6DE);
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
    private void renderGunsmithLabels(GuiGraphics guiGraphics) {
        drawSmallText(guiGraphics, "< WEAPON BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        // Primary vs Side Arm Weapon Switch Tabs
        drawSmallText(guiGraphics, "PRIMARY", 20, 75, 0.85f, this.showPrimaryWeaponTab ? 0xFFFFFFFF : 0xFF7A818C);
        drawSmallText(guiGraphics, "SIDE ARM", 100, 75, 0.85f, !this.showPrimaryWeaponTab ? 0xFFFFFFFF : 0xFF7A818C);
        
        if (this.showPrimaryWeaponTab) {
            guiGraphics.fill(20, 87, 80, 89, 0xFFD62929); // Red line under Primary
        } else {
            guiGraphics.fill(100, 87, 160, 89, 0xFFD62929); // Red line under Side Arm
        }

        int startY = 100;
        int currentY = startY - (int)this.scrollOffset;
        int leftX = 26;

        guiGraphics.enableScissor(0, 90, 240, guiGraphics.guiHeight());
        
        // 1. Weapon Box Text
        drawSmallText(guiGraphics, "WEAPON", leftX, currentY + 50, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, "CURRENT", leftX, currentY + 58, 0.65f, 0xFFD2D6DE);
        
        // --- DISPLAY VIC'S POINT BLANK GUN MODEL ---
        ItemStack weaponStack = this.menu.getSlot(this.showPrimaryWeaponTab ? 0 : 1).getItem();
        if (!weaponStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            // Position the weapon gracefully on the right side of the box, above background layer
            guiGraphics.pose().translate(110, currentY + 8, 100); 
            // Scale up to 3.5x so the Vic's Point Blank 3D model looks massive and detailed
            guiGraphics.pose().scale(3.5f, 3.5f, 1.0f); 
            guiGraphics.renderItem(weaponStack, 0, 0);
            guiGraphics.pose().popPose();
        } else {
            drawSmallText(guiGraphics, "NO WEAPON EQUIPPED", 90, currentY + 32, 0.55f, 0xFF555555);
        }

        currentY += 75;

        // 2. Attachments Header Text
        currentY += 5; // Padding
        drawSmallText(guiGraphics, "ATTACHMENTS", leftX, currentY + 6, 0.65f, 0xFF7A818C);
        currentY += 25;

        // 3. Core Attachments Boxes Text (Dynamic between Primary and Side Arm)
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

        // 4. Tabs Header Text
        int tabY = currentY + 10;
        drawSmallText(guiGraphics, "AMMUNITION", leftX, tabY + 6, 0.55f, this.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        drawSmallText(guiGraphics, "DEPLOYABLE", 116, tabY + 6, 0.55f, !this.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        currentY = tabY + 20;

        // 5. Sub-items Text (Synchronized with renderGunsmithBg line offsets)
        if (this.showAmmunitionTab) {
            
            // --- EDIT THESE ARRAYS TO CHANGE AMMUNITION LOADOUT ---
            String[] primaryCats = {"MAGAZINE", "AMMUNITION"};
            String[] primaryNames = {"PMAG 30RND", "5.56X45MM NATO"};
            
            String[] sidearmCats = {"MAGAZINE", "AMMUNITION"};
            String[] sidearmNames = {"G19 MAG", "9X19MM PARABELLUM"};
            // ------------------------------------------------------
            
            // Primary Header Text
            drawSmallText(guiGraphics, "PRIMARY AMMUNITION", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            
            // Primary Items Text
            for (int i = 0; i < primaryCats.length; i++) {
                drawSmallText(guiGraphics, primaryCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, primaryNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }

            // Sidearm Header Text
            currentY += 10; // Padding
            drawSmallText(guiGraphics, "SIDEARM AMMUNITION", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            
            // Sidearm Items Text
            for (int i = 0; i < sidearmCats.length; i++) {
                drawSmallText(guiGraphics, sidearmCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, sidearmNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }
        } else {

            // --- EDIT THESE ARRAYS TO CHANGE DEPLOYABLE LOADOUT ---
            String[] grenadeCats = {"GRENADE", "GRENADE", "GRENADE", "GRENADE"};
            String[] grenadeNames = {"9-BANG FLASH GRENADE", "CS GAS", "FLASHBANGS", "STINGER"};
            
            String[] tacticalCats = {"TACTICAL", "TACTICAL", "TACTICAL", "TACTICAL", "TACTICAL"};
            String[] tacticalNames = {"C2", "LOCKPICK GUN", "PEPPER SPRAY", "TASER", "WEDGE"};
            // ------------------------------------------------------

            // Grenade Header Text
            drawSmallText(guiGraphics, "GRENADE", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;

            // Grenade Items Text
            for (int i = 0; i < grenadeCats.length; i++) {
                drawSmallText(guiGraphics, grenadeCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, grenadeNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }

            // Tactical Header Text
            currentY += 10; // Padding
            drawSmallText(guiGraphics, "TACTICAL", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;

            // Tactical Items Text
            for (int i = 0; i < tacticalCats.length; i++) {
                drawSmallText(guiGraphics, tacticalCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, tacticalNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }
        }

        guiGraphics.disableScissor();
    }
}