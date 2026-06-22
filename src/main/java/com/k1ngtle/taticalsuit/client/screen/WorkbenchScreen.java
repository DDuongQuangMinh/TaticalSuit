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
    
    // UI State Trackers
    private boolean inGunsmith = false; 
    private boolean showAmmunitionTab = true; 
    private boolean showPrimaryWeaponTab = true; 
    
    private boolean inWeaponSelection = false; 
    private boolean inAttachmentSelection = false;
    private String editingAttachmentCategory = "";
    
    // Scroll Trackers
    private float scrollOffset = 0f;
    private float maxScroll = 0f;
    
    // Anti-Duplication Security Timer
    private long lastClickTime = 0;

    // --- WEAPON POOLS ---
    private static final String[] PRIMARY_WEAPON_IDS = new String[]{
            "pointblank:aa12", "pointblank:ak12", "pointblank:ak47", "pointblank:ak74",
            "pointblank:an94", "pointblank:m4a1", "pointblank:m16a1", "pointblank:hk416",
            "pointblank:scarl", "pointblank:g36c", "pointblank:vector"
    };

    // Exactly 12 slots as requested
    private static final String[] SIDEARM_WEAPON_IDS = new String[]{
            "pointblank:glock17", "pointblank:glock18", "pointblank:m1911a1", "pointblank:deserteagle",
            "pointblank:m9", "pointblank:p30l", "pointblank:tti_viper", "pointblank:m17",
            "pointblank:p250", "pointblank:fn509", "pointblank:usp45", "pointblank:fnx45"
    };

    // --- ATTACHMENT POOLS ---
    private static final String[] OPTIC_IDS = {"NONE", "pointblank:eotech", "pointblank:aimpoint", "pointblank:acog", "pointblank:rmr"};
    private static final String[] BARREL_IDS = {"NONE", "pointblank:long_barrel", "pointblank:short_barrel"};
    private static final String[] MUZZLE_IDS = {"NONE", "pointblank:silencer", "pointblank:osprey", "pointblank:compensator"};
    private static final String[] UNDERBARREL_IDS = {"NONE", "pointblank:vertical_grip", "pointblank:angled_grip", "pointblank:bipod"};
    private static final String[] LASER_IDS = {"NONE", "pointblank:peq15", "pointblank:tlr7", "pointblank:flashlight"};

    private ItemStack[] primaryWeaponStacks;
    private ItemStack[] sidearmWeaponStacks;

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
        
        // Ensure ALL states are strictly reset when the menu is opened!
        this.inGunsmith = false;
        this.inWeaponSelection = false;
        this.inAttachmentSelection = false;
        this.showAmmunitionTab = true;
        this.showPrimaryWeaponTab = true;
        this.scrollOffset = 0f;

        this.primaryWeaponStacks = resolveStacks(PRIMARY_WEAPON_IDS);
        this.sidearmWeaponStacks = resolveStacks(SIDEARM_WEAPON_IDS);
    }

    // Safely migrated to Forge 1.20.1 specific native mapping logic
    private ItemStack[] resolveStacks(String[] ids) {
        ItemStack[] stacks = new ItemStack[ids.length];
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals("NONE")) {
                stacks[i] = ItemStack.EMPTY;
                continue;
            }
            net.minecraft.resources.ResourceLocation loc = new net.minecraft.resources.ResourceLocation(ids[i]);
            net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(loc);
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                stacks[i] = new ItemStack(item);
            } else {
                stacks[i] = ItemStack.EMPTY;
            }
        }
        return stacks;
    }

    private String[] getActiveAttachmentPool() {
        return switch (this.editingAttachmentCategory) {
            case "OPTIC" -> OPTIC_IDS;
            case "BARREL" -> BARREL_IDS;
            case "MUZZLE" -> MUZZLE_IDS;
            case "UNDERBARREL" -> UNDERBARREL_IDS;
            case "LASER" -> LASER_IDS;
            default -> new String[]{"NONE"};
        };
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton != 0) return super.mouseClicked(pMouseX, pMouseY, pButton);
        
        if (this.inAttachmentSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                this.inAttachmentSelection = false;
                this.scrollOffset = 0f;
                return true;
            }

            String[] idPool = getActiveAttachmentPool();
            int startY = 100 - (int)this.scrollOffset;
            
            for (int i = 0; i < idPool.length; i++) {
                int boxY = startY + (i * 45);
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                    if (System.currentTimeMillis() - this.lastClickTime < 500) return true;
                    this.lastClickTime = System.currentTimeMillis();

                    int menuSlotIndex = this.showPrimaryWeaponTab ? 0 : 1; 
                    
                    String nbtCategory = switch (this.editingAttachmentCategory) {
                        case "OPTIC" -> "scope";
                        case "BARREL" -> "barrel";
                        case "MUZZLE" -> "muzzle";
                        case "UNDERBARREL" -> "grip";
                        case "LASER" -> "laser";
                        default -> this.editingAttachmentCategory.toLowerCase();
                    };

                    // Send packet to Server - let the Server and Hotbar handle the NBT sync perfectly
                    com.k1ngtle.taticalsuit.network.ModNetworking.CHANNEL.sendToServer(
                            new com.k1ngtle.taticalsuit.network.EquipWeaponPacket(menuSlotIndex, idPool[i], true, nbtCategory)
                    );
                    
                    this.inAttachmentSelection = false; 
                    this.scrollOffset = 0f;
                    return true;
                }
            }
            return true;
        } else if (this.inWeaponSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                this.inWeaponSelection = false;
                this.scrollOffset = 0f;
                return true;
            }

            String[] idPool = this.showPrimaryWeaponTab ? PRIMARY_WEAPON_IDS : SIDEARM_WEAPON_IDS;
            int startY = 100 - (int)this.scrollOffset;
            
            for (int i = 0; i < idPool.length; i++) {
                int boxY = startY + (i * 45);
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                    if (System.currentTimeMillis() - this.lastClickTime < 500) return true;
                    this.lastClickTime = System.currentTimeMillis();
                    
                    ItemStack currentEquipped = this.showPrimaryWeaponTab ? getDisplayedPrimary() : getDisplayedSidearm();
                    if (!currentEquipped.isEmpty()) {
                        net.minecraft.resources.ResourceLocation currentLoc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(currentEquipped.getItem());
                        if (currentLoc != null && currentLoc.toString().equals(idPool[i])) {
                            this.inWeaponSelection = false; 
                            this.scrollOffset = 0f;
                            return true;
                        }
                    }

                    int menuSlotIndex = this.showPrimaryWeaponTab ? 0 : 1; 

                    // Send packet to Server - let the Menu and Server synchronize to keep native attachments
                    com.k1ngtle.taticalsuit.network.ModNetworking.CHANNEL.sendToServer(
                            new com.k1ngtle.taticalsuit.network.EquipWeaponPacket(menuSlotIndex, idPool[i])
                    );
                    this.inWeaponSelection = false; 
                    this.scrollOffset = 0f;
                    return true;
                }
            }
            return true; 
        } else if (this.inGunsmith) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                this.inGunsmith = false;
                return true;
            }

            if (pMouseY >= 70 && pMouseY <= 90) {
                if (pMouseX >= 20 && pMouseX <= 90) {
                    this.showPrimaryWeaponTab = true;
                    this.scrollOffset = 0f; 
                    return true;
                } else if (pMouseX > 90 && pMouseX <= 180) {
                    this.showPrimaryWeaponTab = false;
                    this.scrollOffset = 0f; 
                    return true;
                }
            }

            int weaponBoxY = 100 - (int)this.scrollOffset;
            if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= weaponBoxY && pMouseY <= weaponBoxY + 70) {
                this.inWeaponSelection = true; 
                this.scrollOffset = 0f;
                return true;
            }

            int numCoreAttachments = this.showPrimaryWeaponTab ? 5 : 3;
            String[] boxCats = this.showPrimaryWeaponTab 
                    ? new String[]{"OPTIC", "BARREL", "MUZZLE", "UNDERBARREL", "LASER"}
                    : new String[]{"OPTIC", "MUZZLE", "LASER"};

            int currentY = 100 - (int)this.scrollOffset + 75 + 30;
            for (int i = 0; i < numCoreAttachments; i++) {
                int boxY = currentY;
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                    this.inAttachmentSelection = true;
                    this.editingAttachmentCategory = boxCats[i];
                    this.scrollOffset = 0f;
                    return true;
                }
                currentY += 45;
            }

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
            return true; 
        } else {
            if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 40 && pMouseY <= 85) {
                this.inGunsmith = true;
                this.scrollOffset = 0f; 
                this.showAmmunitionTab = true; 
                this.showPrimaryWeaponTab = true; 
                return true;
            }
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
        if ((this.inGunsmith || this.inWeaponSelection || this.inAttachmentSelection) && pMouseX < 240 && pMouseY >= 90) {
            this.scrollOffset -= (float) pDragY;
            this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));
            return true;
        }
        
        if (this.isDraggingModel && !this.inGunsmith && !this.inWeaponSelection && !this.inAttachmentSelection) {
            this.playerRotation += (float) pDragX * 1.5f; 
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (this.inAttachmentSelection) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707); 
            this.renderAttachmentSelectionBg(guiGraphics, this.height);
            this.renderAttachmentSelectionLabels(guiGraphics, mouseX, mouseY, this.width, this.height);
        } else if (this.inWeaponSelection) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707); 
            this.renderWeaponSelectionBg(guiGraphics, this.height);
            this.renderWeaponSelectionLabels(guiGraphics, mouseX, mouseY, this.width, this.height);
        } else if (this.inGunsmith) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707); 
            this.renderGunsmithBg(guiGraphics, this.height);
            this.renderGunsmithLabels(guiGraphics);
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

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!this.inGunsmith && !this.inWeaponSelection && !this.inAttachmentSelection) {
            // Intercept tooltips for the invisible slots so they act completely disabled
            if (this.hoveredSlot != null && this.hoveredSlot.x >= 170 && this.hoveredSlot.y >= 40 && this.hoveredSlot.y <= 160) {
                return;
            }
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderLoadoutBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, 240, trueHeight, 0xFF121212);
        guiGraphics.fill(20, 16, 220, 18, 0xFFD62929);

        drawCleanBox(guiGraphics, 20, 40, 200, 45);  
        drawCleanBox(guiGraphics, 20, 85, 200, 45);  
        drawCleanBox(guiGraphics, 20, 130, 200, 45); 

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

        drawCleanBox(guiGraphics, 20, 345, 80, 55);  
        drawCleanBox(guiGraphics, 100, 345, 120, 55); 
        guiGraphics.fill(100, 372, 220, 373, 0xFF2E3136); 

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

    private void renderWeaponSelectionBg(GuiGraphics guiGraphics, int trueHeight) {
        int startY = 100;
        int visibleHeight = trueHeight - 100;
        
        ItemStack[] weaponPool = this.showPrimaryWeaponTab ? this.primaryWeaponStacks : this.sidearmWeaponStacks;
        int numBoxes = weaponPool.length; 
        int listHeight = numBoxes * 45; 
        
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int currentY = startY - (int)this.scrollOffset;
        
        for (int i = 0; i < numBoxes; i++) {
            drawCleanBox(guiGraphics, 20, currentY, 200, 40);
            
            if (weaponPool[i] != null && !weaponPool[i].isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(30, currentY + 8, 250); // High Z-depth
                guiGraphics.pose().scale(1.8f, 1.8f, 1.0f);
                guiGraphics.renderItem(weaponPool[i], 0, 0);
                guiGraphics.pose().popPose();
            }
            
            currentY += 45;
        }
        
        if (this.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((this.scrollOffset / this.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();
    }

    private void renderAttachmentSelectionBg(GuiGraphics guiGraphics, int trueHeight) {
        int startY = 100;
        int visibleHeight = trueHeight - 100;
        
        String[] idPool = getActiveAttachmentPool();
        ItemStack[] attachmentPool = resolveStacks(idPool);
        int numBoxes = attachmentPool.length; 
        int listHeight = numBoxes * 45; 
        
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int currentY = startY - (int)this.scrollOffset;
        
        for (int i = 0; i < numBoxes; i++) {
            drawCleanBox(guiGraphics, 20, currentY, 200, 40);
            
            if (attachmentPool[i] != null && !attachmentPool[i].isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(30, currentY + 12, 250); 
                guiGraphics.pose().scale(1.2f, 1.2f, 1.0f);
                guiGraphics.renderItem(attachmentPool[i], 0, 0);
                guiGraphics.pose().popPose();
            }
            currentY += 45;
        }
        
        if (this.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((this.scrollOffset / this.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();
    }

    private void renderGunsmithBg(GuiGraphics guiGraphics, int trueHeight) {
        int startY = 100;
        int visibleHeight = trueHeight - 100;
        
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
    
    private static class AttachmentInfo {
        public final ItemStack stack;
        public final String name;
        public AttachmentInfo(ItemStack stack, String name) {
            this.stack = stack;
            this.name = name;
        }
    }

    private AttachmentInfo getAttachmentInfo(ItemStack weaponStack, String category) {
        if (weaponStack == null || weaponStack.isEmpty() || !weaponStack.hasTag()) {
            return new AttachmentInfo(ItemStack.EMPTY, "NONE");
        }

        String nbtStr = weaponStack.getTag().toString().toLowerCase();

        String[] keywords;
        switch (category.toUpperCase()) {
            case "OPTIC": keywords = new String[]{"scope", "sight", "optic", "reflex", "holo", "acog", "dot", "rmr", "sro", "micro", "deltapoint"}; break; 
            case "UNDERBARREL": keywords = new String[]{"grip", "underbarrel", "foregrip", "bipod", "angled"}; break;
            case "BARREL": keywords = new String[]{"barrel", "handguard", "choke"}; break;
            case "MUZZLE": keywords = new String[]{"muzzle", "silencer", "suppressor", "compensator", "flash", "osprey", "omega", "ti_rant", "rotor"}; break;
            case "LASER": keywords = new String[]{"laser", "tactical", "light", "peq", "flashlight", "tlr", "x300", "surefire", "m600"}; break;
            case "MAGAZINE": keywords = new String[]{"mag", "magazine", "drum", "clip"}; break;
            case "AMMO": keywords = new String[]{"ammo", "ammunition", "bullet", "nato", "parabellum", "acp", "auto"}; break;
            default: keywords = new String[]{category.toLowerCase()}; break;
        }

        for (net.minecraft.world.item.Item regItem : net.minecraftforge.registries.ForgeRegistries.ITEMS) {
            net.minecraft.resources.ResourceLocation regLoc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(regItem);
            
            if (regLoc != null && "pointblank".equals(regLoc.getNamespace())) {
                String path = regLoc.getPath().toLowerCase();
                
                boolean matchesCategory = false;
                for (String kw : keywords) {
                    if (path.contains(kw)) {
                        matchesCategory = true;
                        break;
                    }
                }
                
                if (matchesCategory) {
                    if (nbtStr.contains(path)) {
                        if (!path.equals(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(weaponStack.getItem()).getPath().toLowerCase())) {
                            ItemStack foundStack = new ItemStack(regItem);
                            return new AttachmentInfo(foundStack, foundStack.getHoverName().getString().toUpperCase());
                        }
                    }
                }
            }
        }

        return new AttachmentInfo(ItemStack.EMPTY, "NONE");
    }

    private boolean isPrimaryWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        net.minecraft.resources.ResourceLocation loc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (loc == null) return false;
        String id = loc.toString();
        for (String wId : PRIMARY_WEAPON_IDS) {
            if (wId.equals(id)) return true;
        }
        return false;
    }

    private boolean isSidearmWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        net.minecraft.resources.ResourceLocation loc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (loc == null) return false;
        String id = loc.toString();
        for (String wId : SIDEARM_WEAPON_IDS) {
            if (wId.equals(id)) return true;
        }
        return false;
    }

    // STRICT CHECK: Checks Menu Slot First for instant visual synchronization!
    private ItemStack getDisplayedPrimary() {
        if (Minecraft.getInstance().player == null) return ItemStack.EMPTY;
        
        // 1. Check the active Menu Slot first. The Server updates this slot immediately when choosing a gun/attachment.
        ItemStack menuStack = this.menu.getSlot(0).getItem();
        if (isPrimaryWeapon(menuStack)) return menuStack;
        
        // 2. Fallback to Hotbar Slot 0
        ItemStack hotbarStack = Minecraft.getInstance().player.getInventory().getItem(0);
        if (isPrimaryWeapon(hotbarStack)) return hotbarStack;
        
        return ItemStack.EMPTY;
    }

    // STRICT CHECK: Checks Menu Slot First for instant visual synchronization!
    private ItemStack getDisplayedSidearm() {
        if (Minecraft.getInstance().player == null) return ItemStack.EMPTY;
        
        // 1. Check the active Menu Slot first.
        ItemStack menuStack = this.menu.getSlot(1).getItem();
        if (isSidearmWeapon(menuStack)) return menuStack;
        
        // 2. Fallback to Hotbar Slot 1
        ItemStack hotbarStack = Minecraft.getInstance().player.getInventory().getItem(1);
        if (isSidearmWeapon(hotbarStack)) return hotbarStack;
        
        return ItemStack.EMPTY;
    }

    private void renderLoadoutLabels(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300.0F); 
        guiGraphics.fill(176, 50, 198, 72, 0xFF0B0C0E); 
        guiGraphics.fill(176, 95, 198, 117, 0xFF0B0C0E); 
        guiGraphics.fill(176, 140, 198, 162, 0xFF0B0C0E); 
        guiGraphics.pose().popPose();

        guiGraphics.drawString(this.font, "LOADOUT", 20, 6, 0xFFFFFF, false);

        ItemStack primaryStack = getDisplayedPrimary();
        String primaryName = primaryStack.isEmpty() ? "UNARMED" : primaryStack.getHoverName().getString().toUpperCase();
        
        ItemStack secondaryStack = getDisplayedSidearm();
        String secondaryName = secondaryStack.isEmpty() ? "UNARMED" : secondaryStack.getHoverName().getString().toUpperCase();

        drawSmallText(guiGraphics, "WEAPONS", 20, 26, 0.65f, 0xFFAAAAAA);
        
        drawSmallText(guiGraphics, "PRIMARY", 26, 68, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, primaryName, 26, 74, 0.75f, 0xFFD2D6DE);
        if (!primaryStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(110, 44, 350.0F); 
            guiGraphics.pose().scale(2.5f, 2.5f, 1.0f); 
            guiGraphics.renderItem(primaryStack, 0, 0);
            guiGraphics.pose().popPose();
        }
        
        drawSmallText(guiGraphics, "SIDE ARM", 26, 113, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, secondaryName, 26, 119, 0.75f, 0xFFD2D6DE);
        if (!secondaryStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(110, 89, 350.0F); 
            guiGraphics.pose().scale(2.5f, 2.5f, 1.0f); 
            guiGraphics.renderItem(secondaryStack, 0, 0);
            guiGraphics.pose().popPose();
        }
        
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

    private void renderAttachmentSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        drawSmallText(guiGraphics, "< ATTACHMENT BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        String title = this.editingAttachmentCategory;
        
        drawSmallText(guiGraphics, title, 20, 55, 1.1f, 0xFFFFFF); 
        drawSmallText(guiGraphics, "SELECT MODIFICATION", 20, 75, 0.65f, 0xFFD62929); 

        String[] idPool = getActiveAttachmentPool();
        ItemStack[] attachmentPool = resolveStacks(idPool);
        int numBoxes = attachmentPool.length;

        int currentY = 100 - (int)this.scrollOffset;
        int leftX = 26;
        
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        for (int i = 0; i < numBoxes; i++) {
            int y = currentY + (i * 45);
            
            if (mouseY >= y && mouseY <= y + 40 && mouseX >= 20 && mouseX <= 220) {
                guiGraphics.fill(21, y + 1, 219, y + 39, 0xFF3E4249); 
            }
            
            if (attachmentPool[i] != null && !attachmentPool[i].isEmpty()) {
                String name = attachmentPool[i].getHoverName().getString().toUpperCase();
                drawSmallText(guiGraphics, name, leftX + 45, y + 16, 0.7f, 0xFFFFFFFF);
            } else {
                if (idPool[i].equals("NONE")) {
                    drawSmallText(guiGraphics, "REMOVE ATTACHMENT", leftX + 45, y + 16, 0.7f, 0xFFD62929);
                } else {
                    String rawName = idPool[i].replace("pointblank:", "").toUpperCase();
                    drawSmallText(guiGraphics, rawName + " (MISSING)", leftX + 45, y + 16, 0.65f, 0xFF555555);
                }
            }
        }
        guiGraphics.disableScissor();
    }

    private void renderWeaponSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        drawSmallText(guiGraphics, "< WEAPON BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        String title = this.showPrimaryWeaponTab ? "PRIMARY" : "SIDE ARM";
        String subtitle = this.showPrimaryWeaponTab ? "ASSAULT RIFLE" : "PISTOL";
        
        drawSmallText(guiGraphics, title, 20, 55, 1.1f, 0xFFFFFF); 
        drawSmallText(guiGraphics, subtitle, 20, 75, 0.65f, 0xFFD62929); 

        ItemStack[] weaponPool = this.showPrimaryWeaponTab ? this.primaryWeaponStacks : this.sidearmWeaponStacks;
        String[] idPool = this.showPrimaryWeaponTab ? PRIMARY_WEAPON_IDS : SIDEARM_WEAPON_IDS;
        int numBoxes = weaponPool.length;

        int currentY = 100 - (int)this.scrollOffset;
        int leftX = 26;
        
        ItemStack previewStack = this.showPrimaryWeaponTab ? getDisplayedPrimary() : getDisplayedSidearm(); 

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        for (int i = 0; i < numBoxes; i++) {
            int y = currentY + (i * 45);
            
            if (mouseY >= y && mouseY <= y + 40 && mouseX >= 20 && mouseX <= 220) {
                if (weaponPool[i] != null && !weaponPool[i].isEmpty()) {
                    previewStack = weaponPool[i]; 
                }
                guiGraphics.fill(21, y + 1, 219, y + 39, 0xFF3E4249); 
            }
            
            if (weaponPool[i] != null && !weaponPool[i].isEmpty()) {
                String gunName = weaponPool[i].getHoverName().getString().toUpperCase();
                drawSmallText(guiGraphics, gunName, leftX + 45, y + 16, 0.7f, 0xFFFFFFFF);
            } else {
                String rawName = idPool[i].replace("pointblank:", "").toUpperCase();
                drawSmallText(guiGraphics, rawName + " (MISSING)", leftX + 45, y + 16, 0.65f, 0xFF555555);
            }
        }
        guiGraphics.disableScissor();

        if (!previewStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            int rightCenterX = 240 + (trueWidth - 240) / 2;
            int rightCenterY = trueHeight / 2 - 40; 
            
            guiGraphics.pose().translate(rightCenterX - 40, rightCenterY, 350.0F); 
            guiGraphics.pose().scale(6.0f, 6.0f, 1.0f); 
            guiGraphics.renderItem(previewStack, 0, 0);
            guiGraphics.pose().popPose();
        }
    }

    private void renderGunsmithLabels(GuiGraphics guiGraphics) {
        drawSmallText(guiGraphics, "< WEAPON BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
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
        
        drawSmallText(guiGraphics, "WEAPON", leftX, currentY + 50, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, "CURRENT", leftX, currentY + 58, 0.65f, 0xFFD2D6DE);
        
        ItemStack weaponStack = this.showPrimaryWeaponTab ? getDisplayedPrimary() : getDisplayedSidearm();
        if (!weaponStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(110, currentY + 8, 350.0F); 
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

        // Extract attachment data
        AttachmentInfo[] attachments = new AttachmentInfo[numCoreAttachments];
        for (int i = 0; i < numCoreAttachments; i++) {
            attachments[i] = getAttachmentInfo(weaponStack, boxCats[i]);
        }

        // Render Attachment Text & 3D Icons
        for (int i = 0; i < numCoreAttachments; i++) {
            drawSmallText(guiGraphics, boxCats[i], leftX, currentY + 12, 0.45f, 0xFF7A818C);
            drawSmallText(guiGraphics, attachments[i].name, leftX, currentY + 22, 0.65f, 0xFFD2D6DE);
            
            // DRAW 3D MODEL FOR ALL CORE ATTACHMENTS
            if (!attachments[i].stack.isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(175, currentY + 4, 350.0F); 
                guiGraphics.pose().scale(2.0f, 2.0f, 1.0f); 
                guiGraphics.renderItem(attachments[i].stack, 0, 0);
                guiGraphics.pose().popPose();
            }
            
            currentY += 45; 
        }

        int tabY = currentY + 10;
        drawSmallText(guiGraphics, "AMMUNITION", leftX, tabY + 6, 0.55f, this.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        drawSmallText(guiGraphics, "DEPLOYABLE", 116, tabY + 6, 0.55f, !this.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        currentY = tabY + 20;

        if (this.showAmmunitionTab) {
            String[] primaryCats = {"MAGAZINE", "AMMUNITION"};
            AttachmentInfo pMagInfo = getAttachmentInfo(getDisplayedPrimary(), "MAGAZINE");
            AttachmentInfo pAmmoInfo = getAttachmentInfo(getDisplayedPrimary(), "AMMO");
            AttachmentInfo[] pAmmoInfos = {pMagInfo, pAmmoInfo};
            String[] primaryNames = {
                    pMagInfo.name.equals("NONE") ? "STANDARD MAG" : pMagInfo.name, 
                    pAmmoInfo.name.equals("NONE") ? "5.56X45MM NATO" : pAmmoInfo.name
            };
            
            String[] sidearmCats = {"MAGAZINE", "AMMUNITION"};
            AttachmentInfo sMagInfo = getAttachmentInfo(getDisplayedSidearm(), "MAGAZINE");
            AttachmentInfo sAmmoInfo = getAttachmentInfo(getDisplayedSidearm(), "AMMO");
            AttachmentInfo[] sAmmoInfos = {sMagInfo, sAmmoInfo};
            String[] sidearmNames = {
                    sMagInfo.name.equals("NONE") ? "STANDARD MAG" : sMagInfo.name, 
                    sAmmoInfo.name.equals("NONE") ? "9X19MM PARABELLUM" : sAmmoInfo.name
            };
            
            drawSmallText(guiGraphics, "PRIMARY AMMUNITION", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < primaryCats.length; i++) {
                drawSmallText(guiGraphics, primaryCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, primaryNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                // DRAW 3D MODEL FOR PRIMARY AMMO
                if (!pAmmoInfos[i].stack.isEmpty()) {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(185, currentY + 4, 350.0F); 
                    guiGraphics.pose().scale(1.5f, 1.5f, 1.0f); 
                    guiGraphics.renderItem(pAmmoInfos[i].stack, 0, 0);
                    guiGraphics.pose().popPose();
                }
                currentY += 31;
            }

            currentY += 10; 
            drawSmallText(guiGraphics, "SIDEARM AMMUNITION", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < sidearmCats.length; i++) {
                drawSmallText(guiGraphics, sidearmCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, sidearmNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                // DRAW 3D MODEL FOR SIDEARM AMMO
                if (!sAmmoInfos[i].stack.isEmpty()) {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(185, currentY + 4, 350.0F); 
                    guiGraphics.pose().scale(1.5f, 1.5f, 1.0f); 
                    guiGraphics.renderItem(sAmmoInfos[i].stack, 0, 0);
                    guiGraphics.pose().popPose();
                }
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