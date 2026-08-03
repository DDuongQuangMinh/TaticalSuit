package com.k1ngtle.taticalsuit.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class WorkbenchRenderDesign {

    private final WorkbenchScreen screen;
    private final Font font;

    public WorkbenchRenderDesign(WorkbenchScreen screen) {
        this.screen = screen;
        this.font = Minecraft.getInstance().font;
    }

    private void drawCleanBox(GuiGraphics guiGraphics, int x, int y, int w, int h) {
        guiGraphics.fill(x, y, x + w, y + h, 0xFF2E3136); 
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF0B0C0E); 
    }

    private void drawSmallText(GuiGraphics guiGraphics, Font font, String text, int x, int y, float scale, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    // --- ATTACHMENT INFO CLASS FOR RENDERING ---
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

        net.minecraft.nbt.CompoundTag tag = weaponStack.getTag();
        String vpbCategory = switch (category.toUpperCase()) {
            case "OPTIC"       -> "scope";
            case "BARREL"      -> "barrel";
            case "MUZZLE"      -> "muzzle";
            case "UNDERBARREL" -> "underbarrel";
            case "LASER"       -> "rail";
            case "STOCK"       -> "stock";
            case "MAGAZINE"    -> "magazine";
            default -> category.toLowerCase();
        };

        if (tag.contains("sa", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            net.minecraft.nbt.CompoundTag sa = tag.getCompound("sa");
            if (sa.contains(vpbCategory, net.minecraft.nbt.Tag.TAG_STRING)) {
                String rl = sa.getString(vpbCategory);
                if (!rl.isEmpty()) {
                    net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS
                            .getValue(ResourceLocation.tryParse(rl));
                    if (item != null && item != net.minecraft.world.item.Items.AIR) {
                        String name = rl.contains(":") ? rl.substring(rl.indexOf(':') + 1).replace("_", " ").toUpperCase() : rl.toUpperCase();
                        return new AttachmentInfo(new ItemStack(item), name);
                    }
                }
            }
        }

        if (tag.contains("as", net.minecraft.nbt.Tag.TAG_LIST)) {
            net.minecraft.nbt.ListTag asList = tag.getList("as", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int k = 0; k < asList.size(); k++) {
                net.minecraft.nbt.CompoundTag entry = asList.getCompound(k);
                String eid = entry.getString("id");
                if (eid.isEmpty()) continue;
                net.minecraft.world.item.Item eItem = net.minecraftforge.registries.ForgeRegistries.ITEMS
                        .getValue(ResourceLocation.tryParse(eid));
                if (eItem != null && eItem != net.minecraft.world.item.Items.AIR
                        && com.k1ngtle.taticalsuit.network.EquipWeaponPacket.isItemInCategory(eItem, vpbCategory)) {
                    String name = eid.contains(":") ? eid.substring(eid.indexOf(':') + 1).replace("_", " ").toUpperCase() : eid.toUpperCase();
                    return new AttachmentInfo(new ItemStack(eItem), name);
                }
            }
        }

        return new AttachmentInfo(ItemStack.EMPTY, "NONE");
    }

    public void renderMain(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (screen.inAttachmentSelection) {
            guiGraphics.fill(0, 0, screen.width, screen.height, 0xFF070707); 
            renderAttachmentSelectionBg(guiGraphics, screen.width, screen.height);
            renderAttachmentSelectionLabels(guiGraphics, mouseX, mouseY, screen.width, screen.height);
        } else if (screen.inWeaponSelection) {
            guiGraphics.fill(0, 0, screen.width, screen.height, 0xFF070707); 
            renderWeaponSelectionBg(guiGraphics, screen.width, screen.height);
            renderWeaponSelectionLabels(guiGraphics, mouseX, mouseY, screen.width, screen.height);
        } else if (screen.inArmorSelection) {
            guiGraphics.fill(0, 0, screen.width, screen.height, 0xFF070707); 
            renderArmorSelectionBg(guiGraphics, screen.width, screen.height);
            renderArmorSelectionLabels(guiGraphics, mouseX, mouseY, screen.width, screen.height);
        } else if (screen.inMunitionSelection) {
            guiGraphics.fill(0, 0, screen.width, screen.height, 0xFF070707); 
            renderMunitionSelectionBg(guiGraphics, screen.width, screen.height);
            renderMunitionSelectionLabels(guiGraphics, mouseX, mouseY, screen.width, screen.height);
        } else if (screen.inHeadwearSelection) {
            guiGraphics.fill(0, 0, screen.width, screen.height, 0xFF070707); 
            renderHeadwearSelectionBg(guiGraphics, screen.width, screen.height);
            renderHeadwearSelectionLabels(guiGraphics, mouseX, mouseY, screen.width, screen.height);
        } else if (screen.inTacticalSelection) {
            guiGraphics.fill(0, 0, screen.width, screen.height, 0xFF070707); 
            renderTacticalSelectionBg(guiGraphics, screen.width, screen.height);
            renderTacticalSelectionLabels(guiGraphics, mouseX, mouseY, screen.width, screen.height);
        } else if (screen.inGunsmith) {
            guiGraphics.fill(0, 0, screen.width, screen.height, 0xFF070707); 
            renderGunsmithBg(guiGraphics, screen.width, screen.height);
            renderGunsmithLabels(guiGraphics);
        }
    }

    public void renderBg(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, screen.width, screen.height, 0xFF070707);
        
        if (screen.inCustomizationTab) {
            renderCustomizationBg(guiGraphics, screen.width, screen.height, mouseX, mouseY);
            if (screen.inCustomizationSelection) {
                renderCustomizationGridBg(guiGraphics, screen.width, screen.height, mouseX, mouseY);
            }
        } else {
            renderLoadoutBg(guiGraphics, screen.width, screen.height, mouseX, mouseY);
        }
    }

    public void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (screen.inCustomizationTab) {
            renderCustomizationLabels(guiGraphics, mouseX, mouseY);
            if (screen.inCustomizationSelection) {
                renderCustomizationGridLabels(guiGraphics, mouseX, mouseY, screen.width, screen.height);
                
                if (screen.inStyleSelection) {
                    renderStyleSelectionBar(guiGraphics, mouseX, mouseY, screen.width, screen.height);
                }
            }
        } else {
            renderLoadoutLabels(guiGraphics);
        }
    }

    private void renderStyleSelectionBar(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        int openSpaceWidth = trueWidth - 120; 
        int centerX = 240 + (openSpaceWidth - 240) / 2;
        int boxSize = 70;
        int gap = 15;
        int totalWidth = (boxSize * 4) + (gap * 3);
        int startX = centerX - (totalWidth / 2);
        int startY = trueHeight - 100; 

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 800.0F);

        drawSmallText(guiGraphics, font, "STYLES", centerX - (font.width("STYLES") / 4), startY - 15, 0.5f, 0xFFAAAAAA);
        guiGraphics.fill(centerX - 30, startY - 4, centerX + 30, startY - 3, 0xFFD62929);

        String[] styleItems = {"taticalsuit:base_helmet", "taticalsuit:helmet_ghillie", "taticalsuit:helmet_sand", "taticalsuit:helmet_snow"};
        
        for (int i = 0; i < 4; i++) {
            int boxX = startX + (i * (boxSize + gap));
            
            guiGraphics.fill(boxX, startY, boxX + boxSize, startY + boxSize, 0xAA000000);
            
            if (mouseX >= boxX && mouseX <= boxX + boxSize && mouseY >= startY && mouseY <= startY + boxSize) {
                guiGraphics.fill(boxX, startY, boxX + boxSize, startY + boxSize, 0x44FFFFFF);
            }
            
            ResourceLocation loc = ResourceLocation.tryParse(styleItems[i]);
            if (loc != null) {
                net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(loc);
                if (item != null && item != net.minecraft.world.item.Items.AIR) {
                    float scale = 4.0f;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(boxX + (boxSize / 2f) - (8 * scale), startY + (boxSize / 2f) - (8 * scale), 150.0F); 
                    guiGraphics.pose().scale(scale, scale, scale);
                    guiGraphics.renderItem(new ItemStack(item), 0, 0);
                    guiGraphics.pose().popPose();
                }
            }
            
            boolean isEquipped = false;
            if (i == 0 && screen.selectedHelmet.equals("HELMET ONLY")) isEquipped = true;
            if (i == 1 && screen.selectedHelmet.equals("GHILLIE HELMET")) isEquipped = true;
            if (i == 2 && screen.selectedHelmet.equals("SAND GHILLIE HELMET")) isEquipped = true;
            if (i == 3 && screen.selectedHelmet.equals("SNOW GHILLIE HELMET")) isEquipped = true;
            
            if (isEquipped) {
                guiGraphics.fill(boxX, startY + boxSize, boxX + boxSize, startY + boxSize + 2, 0xFF00FF00);
            }
        }
        guiGraphics.pose().popPose();
    }

    private void render3DOperator(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        if (Minecraft.getInstance().player != null) {
            int rightBound = trueWidth;
            if (screen.inCustomizationTab && screen.inCustomizationSelection) {
                boolean isLargeGrid = screen.customizationCategory.equals("SHIRT") || screen.customizationCategory.equals("PANTS") || screen.customizationCategory.equals("ARMOR");
                int panelWidth = isLargeGrid ? 170 : 120;
                rightBound -= panelWidth;
            }
            int openSpaceCenter = 240 + (rightBound - 240) / 2; 
            int operatorScale = 260; 
            int operatorFloorAnchor = trueHeight + 170; 

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(openSpaceCenter, operatorFloorAnchor, 50.0);
            guiGraphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(screen.playerRotation));
            guiGraphics.pose().translate(0, 0, -50.0);

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    0, 0, operatorScale, 0f, 0f, Minecraft.getInstance().player 
            );
            guiGraphics.pose().popPose();
        }
    }

    private void renderCustomizationBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, 240, trueHeight, 0xFF121212);
        guiGraphics.fill(20, 16, 220, 18, 0xFFD62929);

        int startY = 30;
        int currentY = startY + 15;
        drawCleanBox(guiGraphics, 20, currentY, 95, 40);
        drawCleanBox(guiGraphics, 125, currentY, 95, 40);
        currentY += 45;
        drawCleanBox(guiGraphics, 20, currentY, 60, 40);
        drawCleanBox(guiGraphics, 90, currentY, 60, 40);
        drawCleanBox(guiGraphics, 160, currentY, 60, 40);
        
        currentY += 60;
        drawCleanBox(guiGraphics, 20, currentY, 95, 40);
        drawCleanBox(guiGraphics, 125, currentY, 95, 40);
        currentY += 45;
        drawCleanBox(guiGraphics, 20, currentY, 60, 40);
        drawCleanBox(guiGraphics, 90, currentY, 60, 40);
        drawCleanBox(guiGraphics, 160, currentY, 60, 40);
        
        currentY += 60;
        drawCleanBox(guiGraphics, 20, currentY, 60, 40);
        drawCleanBox(guiGraphics, 90, currentY, 60, 40);
        drawCleanBox(guiGraphics, 160, currentY, 60, 40);

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderCustomizationGridBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight, int mouseX, int mouseY) {
        boolean isLargeGrid = screen.customizationCategory.equals("SHIRT") || screen.customizationCategory.equals("PANTS") || screen.customizationCategory.equals("ARMOR");
        int cols = isLargeGrid ? 3 : 2;
        int rows = isLargeGrid ? 7 : 6;
        
        int itemsToRender = screen.customizationCategory.equals("HELMET") ? 1 : (rows * cols);
        
        int panelWidth = isLargeGrid ? 170 : 120;
        int panelX = trueWidth - panelWidth;
        int gridStartX = panelX + 15;
        int startY = 50 - (int)screen.scrollOffset;
        
        int listHeight = (rows * 45); 
        int visibleHeight = trueHeight - 50;
        screen.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));

        guiGraphics.fill(panelX, 0, trueWidth, trueHeight, 0xFF121212);
        guiGraphics.fill(panelX, 16, trueWidth, 18, 0xFFD62929);

        guiGraphics.enableScissor(panelX, 40, trueWidth, trueHeight);
        
        for (int i = 0; i < itemsToRender; i++) {
            int col = i % cols;
            int row = i / cols;
            int boxX = gridStartX + (col * 45);
            int boxY = startY + (row * 45);
            drawCleanBox(guiGraphics, boxX, boxY, 40, 40);
        }
        
        if (screen.maxScroll > 0) {
            int scrollX = trueWidth - 10;
            guiGraphics.fill(scrollX, 50, scrollX + 2, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 50 + (int)((screen.scrollOffset / screen.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(scrollX - 1, thumbY, scrollX + 3, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();
    }

    private void renderCustomizationLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startY = 30;
        
        drawSmallText(guiGraphics, font, "UNIFORM", 20, startY, 0.65f, 0xFFAAAAAA);
        int currentY = startY + 15;
        drawSmallText(guiGraphics, font, "SHIRT", 24, currentY + 4, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, "PANTS", 129, currentY + 4, 0.45f, 0xFF7A818C);
        currentY += 45;
        drawSmallText(guiGraphics, font, "GLOVES", 24, currentY + 4, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, "BOOTS", 94, currentY + 4, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, "BELT", 164, currentY + 4, 0.45f, 0xFF7A818C);
        
        currentY += 60;
        drawSmallText(guiGraphics, font, "TACTICAL GEAR", 20, currentY - 15, 0.65f, 0xFFAAAAAA);
        drawSmallText(guiGraphics, font, "ARMOR", 24, currentY + 4, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, "HELMET", 129, currentY + 4, 0.45f, 0xFF7A818C);
        currentY += 45;
        drawSmallText(guiGraphics, font, "FACEWEAR", 24, currentY + 4, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, "NVG", 94, currentY + 4, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, "BALLISTIC MASK", 164, currentY + 4, 0.45f, 0xFF7A818C);
        
        currentY += 60;
        drawSmallText(guiGraphics, font, "ACCESSORIES", 20, currentY - 15, 0.65f, 0xFFAAAAAA);
        drawSmallText(guiGraphics, font, "TATTOO", 24, currentY + 4, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, "EYEWEAR", 94, currentY + 4, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, "WATCH", 164, currentY + 4, 0.45f, 0xFF7A818C);

        if (!screen.selectedHelmet.equals("NO HELMET")) {
            String targetId = "taticalsuit:base_helmet";
            if (screen.selectedHelmet.equals("GHILLIE HELMET")) targetId = "taticalsuit:helmet_ghillie";
            if (screen.selectedHelmet.equals("SAND GHILLIE HELMET")) targetId = "taticalsuit:helmet_sand";
            if (screen.selectedHelmet.equals("SNOW GHILLIE HELMET")) targetId = "taticalsuit:helmet_snow";
            
            ResourceLocation loc = ResourceLocation.tryParse(targetId);
            if (loc != null) {
                net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(loc);
                if (item != null && item != net.minecraft.world.item.Items.AIR) {
                    float hScale = 2.0f;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(125 + (95 / 2f) - (8 * hScale), 150 + (40 / 2f) - (8 * hScale), 250.0F);
                    guiGraphics.pose().scale(hScale, hScale, hScale);
                    guiGraphics.renderItem(new ItemStack(item), 0, 0);
                    guiGraphics.pose().popPose();
                }
            }
        }
    }

    private void renderCustomizationGridLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        boolean isLargeGrid = screen.customizationCategory.equals("SHIRT") || screen.customizationCategory.equals("PANTS") || screen.customizationCategory.equals("ARMOR");
        int cols = isLargeGrid ? 3 : 2;
        int rows = isLargeGrid ? 7 : 6;
        
        int itemsToRender = screen.customizationCategory.equals("HELMET") ? 1 : (rows * cols);
        
        int panelWidth = isLargeGrid ? 170 : 120;
        int panelX = trueWidth - panelWidth;
        int gridStartX = panelX + 15;
        int startY = 50 - (int)screen.scrollOffset;
        
        drawSmallText(guiGraphics, font, "SELECT " + screen.customizationCategory, panelX + 15, 25, 0.75f, 0xFFFFFFFF);
        
        guiGraphics.enableScissor(panelX, 40, trueWidth, trueHeight);
        
        for (int i = 0; i < itemsToRender; i++) {
            int col = i % cols;
            int row = i / cols;
            int boxX = gridStartX + (col * 45);
            int boxY = startY + (row * 45);
            
            if (mouseX >= boxX && mouseX <= boxX + 40 && mouseY >= boxY && mouseY <= boxY + 40) {
                guiGraphics.fill(boxX + 1, boxY + 1, boxX + 39, boxY + 39, 0xFF3E4249);
            }

            if (screen.customizationCategory.equals("HELMET")) {
                String targetId = "taticalsuit:base_helmet";
                if (screen.selectedHelmet.equals("GHILLIE HELMET")) targetId = "taticalsuit:helmet_ghillie";
                if (screen.selectedHelmet.equals("SAND GHILLIE HELMET")) targetId = "taticalsuit:helmet_sand";
                if (screen.selectedHelmet.equals("SNOW GHILLIE HELMET")) targetId = "taticalsuit:helmet_snow";
                
                ResourceLocation loc = ResourceLocation.tryParse(targetId);
                if (loc != null) {
                    net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(loc);
                    if (item != null && item != net.minecraft.world.item.Items.AIR) {
                        float scale = 2.0f;
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(boxX + 4, boxY + 4, 350.0F); 
                        guiGraphics.pose().scale(scale, scale, scale);
                        guiGraphics.renderItem(new ItemStack(item), 0, 0);
                        guiGraphics.pose().popPose();
                    }
                }
                
                if (!screen.selectedHelmet.equals("NO HELMET")) {
                     guiGraphics.fill(boxX, boxY + 40, boxX + 40, boxY + 42, 0xFF00FF00); 
                }
            }
        }
        guiGraphics.disableScissor();
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

        guiGraphics.fill(20, 309, 120, 317, 0xFF2E3136); 
        guiGraphics.fill(123, 285, 124, 317, 0xFF2E3136); 

        guiGraphics.fill(127, 309, 187, 317, 0xFF2E3136); 
        guiGraphics.fill(190, 285, 191, 317, 0xFF2E3136); 

        guiGraphics.fill(194, 309, 214, 317, 0xFF2E3136); 

        drawCleanBox(guiGraphics, 20, 345, 80, 55);  
        drawCleanBox(guiGraphics, 100, 345, 120, 55); 
        guiGraphics.fill(100, 372, 220, 373, 0xFF2E3136); 

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderTacticalSelectionBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        int startY = 100;
        int visibleHeight = trueHeight - 100;
        int listHeight = 6 * 45; 
        
        screen.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int currentY = startY - (int)screen.scrollOffset;
        
        for (int i = 0; i < 6; i++) {
            currentY += 45;
        }
        
        if (screen.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((screen.scrollOffset / screen.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderArmorSelectionBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        int visibleHeight = trueHeight - 100;
        
        int currentY = 0;
        currentY += 45; 
        currentY += 20; 
        currentY += 40; 
        currentY += 20; 
        currentY += 40; 
        currentY += 20; 
        currentY += 20; 
        int dynamicItemsHeight = screen.showAmmunitionTab 
                ? (16 + (2 * 31) + 10 + 16 + (2 * 31)) 
                : (16 + (4 * 31) + 10 + 16 + (5 * 31));
        currentY += dynamicItemsHeight;
        
        int listHeight = currentY + 20;
        screen.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight));
        screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));

        guiGraphics.fill(20, 16, 220, 18, 0xFFD62929);

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int drawY = 100 - (int)screen.scrollOffset;
        
        drawY += 45;
        drawY += 20;
        for(int i = 0; i < 4; i++) {
            drawCleanBox(guiGraphics, 20 + (i * 50), drawY, 45, 30);
        }
        drawY += 40;
        
        drawY += 20;
        for(int i = 0; i < 3; i++) {
            drawCleanBox(guiGraphics, 20 + (i * 66), drawY, 60, 30);
        }
        drawY += 40;
        drawY += 20; 

        guiGraphics.fill(20, drawY + 14, 220, drawY + 15, 0xFF2E3136); 
        if (screen.showAmmunitionTab) {
            guiGraphics.fill(20, drawY + 14, 110, drawY + 15, 0xFFD62929); 
        } else {
            guiGraphics.fill(110, drawY + 14, 220, drawY + 15, 0xFFD62929); 
        }
        drawY += 20; 
        
        if (screen.showAmmunitionTab) {
            guiGraphics.fill(20, drawY + 15, 220, drawY + 16, 0xFF2E3136);
            drawY += 16;
            for (int i = 0; i < 2; i++) {
                guiGraphics.fill(20, drawY + 30, 220, drawY + 31, 0xFF2E3136);
                drawY += 31; 
            }
            
            drawY += 10; 
            guiGraphics.fill(20, drawY + 15, 220, drawY + 16, 0xFF2E3136);
            drawY += 16;
            for (int i = 0; i < 2; i++) {
                guiGraphics.fill(20, drawY + 30, 220, drawY + 31, 0xFF2E3136);
                drawY += 31; 
            }
        } else {
            guiGraphics.fill(20, drawY + 15, 220, drawY + 16, 0xFF2E3136);
            drawY += 16;
            for (int i = 0; i < 4; i++) {
                guiGraphics.fill(20, drawY + 30, 220, drawY + 31, 0xFF2E3136);
                drawY += 31; 
            }

            drawY += 10; 
            guiGraphics.fill(20, drawY + 15, 220, drawY + 16, 0xFF2E3136);
            drawY += 16;
            for (int i = 0; i < 5; i++) {
                guiGraphics.fill(20, drawY + 30, 220, drawY + 31, 0xFF2E3136);
                drawY += 31; 
            }
        }
        
        if (screen.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((screen.scrollOffset / screen.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderHeadwearSelectionBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        int visibleHeight = trueHeight - 100;
        
        int currentY = 0;
        currentY += 45; 
        if (screen.expandedHeadwearCategory.equals("HELMET")) currentY += 2 * 35;
        
        currentY += 45; 
        if (screen.expandedHeadwearCategory.equals("MOUNT")) currentY += 3 * 35;
        if (!screen.selectedMount.equals("NONE")) {
            currentY += 45; 
        }
        
        currentY += 45; 
        if (screen.expandedHeadwearCategory.equals("FACEWEAR")) currentY += 3 * 35;
        
        int listHeight = currentY + 20;
        screen.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight));
        screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));

        guiGraphics.fill(20, 16, 220, 18, 0xFFD62929);

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int drawY = 100 - (int)screen.scrollOffset;
        
        drawCleanBox(guiGraphics, 20, drawY, 200, 40);
        drawY += 45;
        if (screen.expandedHeadwearCategory.equals("HELMET")) drawY += 2 * 35;
        
        drawCleanBox(guiGraphics, 20, drawY, 200, 40);
        drawY += 45;
        if (screen.expandedHeadwearCategory.equals("MOUNT")) drawY += 3 * 35;
        if (!screen.selectedMount.equals("NONE")) {
            drawCleanBox(guiGraphics, 20, drawY, 100, 40);
            drawCleanBox(guiGraphics, 120, drawY, 100, 40);
            drawY += 45;
        }
        
        drawCleanBox(guiGraphics, 20, drawY, 200, 40);
        drawY += 45;
        if (screen.expandedHeadwearCategory.equals("FACEWEAR")) drawY += 3 * 35;
        
        if (screen.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((screen.scrollOffset / screen.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderWeaponSelectionBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        int startY = 100;
        int visibleHeight = trueHeight - 100;
        
        ItemStack[] weaponPool = screen.getActiveWeaponStacks();
        int numBoxes = weaponPool.length; 
        int listHeight = numBoxes * 45; 
        
        screen.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int currentY = startY - (int)screen.scrollOffset;
        
        for (int i = 0; i < numBoxes; i++) {
            drawCleanBox(guiGraphics, 20, currentY, 200, 40);
            
            if (weaponPool[i] != null && !weaponPool[i].isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(30, currentY + 8, 250); 
                guiGraphics.pose().scale(1.8f, 1.8f, 1.8f); 
                guiGraphics.renderItem(weaponPool[i], 0, 0);
                guiGraphics.pose().popPose();
            }
            
            currentY += 45;
        }
        
        if (screen.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((screen.scrollOffset / screen.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderAttachmentSelectionBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        int startY = 100;
        int visibleHeight = trueHeight - 100;
        
        String[] idPool = screen.getActiveAttachmentPool();
        ItemStack[] attachmentPool = screen.resolveAttachmentStacks(idPool, screen.editingAttachmentCategory);
        int numBoxes = attachmentPool.length; 
        int listHeight = numBoxes * 45; 
        
        screen.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int currentY = startY - (int)screen.scrollOffset;
        
        for (int i = 0; i < numBoxes; i++) {
            drawCleanBox(guiGraphics, 20, currentY, 200, 40);
            
            if (attachmentPool[i] != null && !attachmentPool[i].isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(30, currentY + 12, 250); 
                guiGraphics.pose().scale(1.2f, 1.2f, 1.2f); 
                guiGraphics.renderItem(attachmentPool[i], 0, 0);
                guiGraphics.pose().popPose();
            }
            currentY += 45;
        }
        
        if (screen.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((screen.scrollOffset / screen.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderMunitionSelectionBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        int visibleHeight = trueHeight - 100;
        int listHeight = 60 + 20 + (13 * 35); 
        
        screen.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        
        if (screen.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((screen.scrollOffset / screen.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderGunsmithBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        int startY = 100;
        int visibleHeight = trueHeight - 100;
        
        int numPrimary = 2; 
        int numSidearm = 2; 
        int numGrenade = 4; 
        int numTactical = 5; 
        
        int numCoreAttachments = (screen.currentWeaponTab == 8) ? 3 : 5; 
        
        int dynamicItemsHeight = screen.showAmmunitionTab 
                ? (20 + 16 + (numPrimary * 31) + 10 + 16 + (numSidearm * 31)) 
                : (20 + 16 + (numGrenade * 31) + 10 + 16 + (numTactical * 31));
                
        int listHeight = 75 + 30 + (numCoreAttachments * 45) + 35 + dynamicItemsHeight; 
        
        screen.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int currentY = startY - (int)screen.scrollOffset;
        
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
        
        if (screen.showAmmunitionTab) {
            guiGraphics.fill(20, tabY + 14, 110, tabY + 15, 0xFFD62929); 
        } else {
            guiGraphics.fill(110, tabY + 14, 220, tabY + 15, 0xFFD62929); 
        }
        currentY = tabY + 20; 
        
        if (screen.showAmmunitionTab) {
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

        if (screen.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((screen.scrollOffset / screen.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderLoadoutLabels(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300.0F); 
        guiGraphics.fill(176, 50, 198, 72, 0xFF0B0C0E); 
        guiGraphics.fill(176, 95, 198, 117, 0xFF0B0C0E); 
        guiGraphics.fill(176, 140, 198, 162, 0xFF0B0C0E); 
        guiGraphics.pose().popPose();

        ItemStack primaryStack = screen.getDisplayedPrimary();
        String primaryName = primaryStack.isEmpty() ? "UNARMED" : primaryStack.getHoverName().getString().toUpperCase();
        
        ItemStack secondaryStack = screen.getDisplayedSidearm();
        String secondaryName = secondaryStack.isEmpty() ? "UNARMED" : secondaryStack.getHoverName().getString().toUpperCase();

        drawSmallText(guiGraphics, font, "WEAPONS", 20, 26, 0.65f, 0xFFAAAAAA);
        
        drawSmallText(guiGraphics, font, "PRIMARY", 26, 68, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, primaryName, 26, 74, 0.75f, 0xFFD2D6DE);
        if (!primaryStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(110, 44, 350.0F); 
            guiGraphics.pose().scale(2.5f, 2.5f, 2.5f); 
            guiGraphics.renderItem(primaryStack, 0, 0);
            guiGraphics.pose().popPose();
        }
        
        drawSmallText(guiGraphics, font, "SIDE ARM", 26, 113, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, secondaryName, 26, 119, 0.75f, 0xFFD2D6DE);
        if (!secondaryStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(110, 89, 350.0F); 
            guiGraphics.pose().scale(2.5f, 2.5f, 2.5f); 
            guiGraphics.renderItem(secondaryStack, 0, 0);
            guiGraphics.pose().popPose();
        }
        
        drawSmallText(guiGraphics, font, "LONG TACTICAL", 26, 158, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, screen.selectedTactical, 26, 164, 0.75f, 0xFFD2D6DE);

        drawSmallText(guiGraphics, font, "ARMOR & MUNITIONS", 20, 190, 0.65f, 0xFFAAAAAA);
        drawSmallText(guiGraphics, font, "VEST | ", 26, 243, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, screen.selectedAmmunitionDeployable, 26 + (int)(font.width("VEST | ") * 0.55f), 243, 0.55f, 0xFFD62929);
        drawSmallText(guiGraphics, font, screen.selectedVest, 26, 249, 0.75f, 0xFFD2D6DE);

        drawSmallText(guiGraphics, font, "MATERIAL", 106, 218, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, screen.selectedMaterial, 106, 224, 0.75f, 0xFFD2D6DE);
        drawSmallText(guiGraphics, font, "COVERAGE", 106, 244, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, screen.selectedCoverage, 106, 250, 0.75f, 0xFFD2D6DE);

        drawSmallText(guiGraphics, font, "MUNITION SLOTS", 20, 275, 0.65f, 0xFFAAAAAA);
        drawSmallText(guiGraphics, font, screen.getMenu().getMunitionCount() + "/" + screen.selectedAmmunitionDeployable, 165, 275, 0.65f, 0xFFD62929);

        drawSmallText(guiGraphics, font, "AP", 66, 310, 0.55f, 0xFFFFFFFF);
        drawSmallText(guiGraphics, font, "AP", 153, 310, 0.55f, 0xFFFFFFFF);
        drawSmallText(guiGraphics, font, "5", 201, 310, 0.55f, 0xFFFFFFFF);

        if (Minecraft.getInstance().player != null) {
            ItemStack headStack = Minecraft.getInstance().player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
            if (!headStack.isEmpty() && (
                headStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetItem ||
                headStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31Item ||
                headStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item ||
                headStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem ||
                headStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18SandItem ||
                headStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGhillieItem ||
                headStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetSandItem ||
                headStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetSnowItem)) {
                
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(39, 357, 150.0F); 
                guiGraphics.pose().scale(2.5f, 2.5f, 2.5f); 
                guiGraphics.renderItem(headStack, 0, 0);
                guiGraphics.pose().popPose();
            }
        }

        drawSmallText(guiGraphics, font, "HEADWEAR", 20, 330, 0.65f, 0xFFAAAAAA);
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 400.0F); 
        drawSmallText(guiGraphics, font, "HELMET", 26, 383, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, screen.selectedHelmet, 26, 389, 0.75f, 0xFFD2D6DE);
        guiGraphics.pose().popPose();
        
        drawSmallText(guiGraphics, font, "MOUNT | ", 106, 358, 0.55f, 0xFF7A818C);
        if (!screen.selectedMount.equals("NONE")) {
            String phosphorTrim = screen.selectedPhosphor.equals("WHITE PHOSPHOR") ? "WHITE" : "GREEN";
            drawSmallText(guiGraphics, font, phosphorTrim, 106 + (int)(font.width("MOUNT | ") * 0.55f), 358, 0.55f, 0xFFD62929);
        }
        drawSmallText(guiGraphics, font, screen.selectedMount, 106, 364, 0.75f, 0xFFD2D6DE);
        
        drawSmallText(guiGraphics, font, "FACEWEAR", 106, 384, 0.55f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, screen.selectedFacewear, 106, 390, 0.75f, 0xFFD2D6DE);
    }

    private void renderArmorSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        drawSmallText(guiGraphics, font, "< LOADOUT", 20, 25, 0.75f, 0xFFFFFF);
        drawSmallText(guiGraphics, font, "ARMOR", 20, 55, 1.1f, 0xFFFFFF); 
        drawSmallText(guiGraphics, font, "SELECT EQUIPMENT", 20, 75, 0.65f, 0xFFD62929); 

        int currentY = 100 - (int)screen.scrollOffset;
        
        int effMouseX = mouseX;
        int effMouseY = mouseY;
        if (screen.expandedArmorCategory.equals("VEST")) {
            effMouseX = -999;
            effMouseY = -999;
        }
        
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        
        drawSmallText(guiGraphics, font, "VEST", 20, currentY + 8, 0.65f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, screen.selectedVest, 20, currentY + 18, 0.75f, 0xFFFFFFFF);
        
        int vestDropdownY = currentY + 45;
        currentY += 45;
        
        drawSmallText(guiGraphics, font, "COVERAGE", 20, currentY + 8, 0.65f, 0xFF7A818C);
        currentY += 20;
        
        String[] covList = {"NONE", "FRONT", "FRONT/BACK", "FULL"};
        for(int i = 0; i < 4; i++) {
            int boxX = 20 + (i * 50);
            boolean isSelected = screen.selectedCoverage.equals(covList[i]);
            boolean isHovered = effMouseY >= currentY && effMouseY <= currentY + 30 && effMouseX >= boxX && effMouseX <= boxX + 45;
            
            int color = isSelected ? 0xFFD62929 : (isHovered ? 0xFFFFFFFF : 0xFF7A818C);
            float textScale = (i == 2) ? 0.45f : 0.55f;
            int textX = boxX + (i == 2 ? 2 : 8); 
            
            drawSmallText(guiGraphics, font, covList[i], textX, currentY + 12, textScale, color);
        }
        currentY += 40;

        drawSmallText(guiGraphics, font, "MATERIAL", 20, currentY + 8, 0.65f, 0xFF7A818C);
        currentY += 20;
        
        String[] matList = {"KEVLAR", "STEEL", "CERAMIC"};
        for(int i = 0; i < 3; i++) {
            int boxX = 20 + (i * 66);
            boolean isSelected = screen.selectedMaterial.equals(matList[i]);
            boolean isHovered = effMouseY >= currentY && effMouseY <= currentY + 30 && effMouseX >= boxX && effMouseX <= boxX + 60;
            
            int color = isSelected ? 0xFFD62929 : (isHovered ? 0xFFFFFFFF : 0xFF7A818C);
            int textX = boxX + 12;
            
            drawSmallText(guiGraphics, font, matList[i], textX, currentY + 12, 0.55f, color);
        }
        currentY += 40;

        currentY += 20; 

        drawSmallText(guiGraphics, font, "AMMUNITION", 26, currentY + 6, 0.55f, screen.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        drawSmallText(guiGraphics, font, "DEPLOYABLE", 116, currentY + 6, 0.55f, !screen.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        currentY += 20;

        if (screen.showAmmunitionTab) {
            String[] primaryCats = {"MAGAZINE", "AMMUNITION"};
            String[] primaryNames = {"STANDARD MAG", "5.56X45MM NATO"};
            
            String[] sidearmCats = {"MAGAZINE", "AMMUNITION"};
            String[] sidearmNames = {"STANDARD MAG", "9X19MM PARABELLUM"};
            
            drawSmallText(guiGraphics, font, "PRIMARY AMMUNITION", 26, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < primaryCats.length; i++) {
                drawSmallText(guiGraphics, font, primaryCats[i], 26, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, font, primaryNames[i], 26, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }

            currentY += 10; 
            drawSmallText(guiGraphics, font, "SIDEARM AMMUNITION", 26, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < sidearmCats.length; i++) {
                drawSmallText(guiGraphics, font, sidearmCats[i], 26, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, font, sidearmNames[i], 26, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }
        } else {
            String[] grenadeCats = {"GRENADE", "GRENADE", "GRENADE", "GRENADE"};
            String[] grenadeNames = {"9-BANG FLASH GRENADE", "CS GAS", "FLASHBANGS", "STINGER"};
            String[] tacticalCats = {"TACTICAL", "TACTICAL", "TACTICAL", "TACTICAL", "TACTICAL"};
            String[] tacticalNames = {"C2", "LOCKPICK GUN", "PEPPER SPRAY", "TASER", "WEDGE"};
            
            drawSmallText(guiGraphics, font, "GRENADE", 26, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < grenadeCats.length; i++) {
                drawSmallText(guiGraphics, font, grenadeCats[i], 26, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, font, grenadeNames[i], 26, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }

            currentY += 10; 
            drawSmallText(guiGraphics, font, "TACTICAL", 26, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < tacticalCats.length; i++) {
                drawSmallText(guiGraphics, font, tacticalCats[i], 26, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, font, tacticalNames[i], 26, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }
        }
        
        if (screen.expandedArmorCategory.equals("VEST")) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 150); 
            
            String[] vestList = {"NO ARMOR", "LIGHT ARMOR", "HEAVY ARMOR", "STAB VEST"};
            int bgHeight = vestList.length * 35 + 10;
            
            guiGraphics.fill(15, vestDropdownY - 5, 235, vestDropdownY + bgHeight, 0xFF000000);
            
            int listY = vestDropdownY;
            for (String item : vestList) {
                renderTextListItem(guiGraphics, item, 20, listY, mouseX, mouseY); 
                if (screen.selectedVest.equals(item)) {
                    drawSmallText(guiGraphics, font, "[EQUIPPED]", 160, listY + 10, 0.6f, 0xFFD62929);
                }
                listY += 35;
            }
            
            guiGraphics.pose().popPose();
        }
        
        guiGraphics.disableScissor();
    }

    private void renderHeadwearSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        drawSmallText(guiGraphics, font, "< LOADOUT", 20, 25, 0.75f, 0xFFFFFF);
        drawSmallText(guiGraphics, font, "HEADWEAR", 20, 55, 1.1f, 0xFFFFFF); 
        drawSmallText(guiGraphics, font, "SELECT EQUIPMENT", 20, 75, 0.65f, 0xFFD62929); 

        int currentY = 100 - (int)screen.scrollOffset;
        int leftX = 26;
        
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        
        drawSmallText(guiGraphics, font, "HELMET", leftX, currentY + 12, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, screen.selectedHelmet, leftX, currentY + 22, 0.75f, 0xFFFFFFFF);
        currentY += 45;
        
        if (screen.expandedHeadwearCategory.equals("HELMET")) {
            String[] list = {"NO HELMET", "HELMET ONLY"};
            for (String item : list) {
                renderTextListItem(guiGraphics, item, 20, currentY, mouseX, mouseY);
                currentY += 35;
            }
        }
        
        drawSmallText(guiGraphics, font, "MOUNT", leftX, currentY + 12, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, screen.selectedMount, leftX, currentY + 22, 0.75f, 0xFFFFFFFF);
        currentY += 45;
        
        if (screen.expandedHeadwearCategory.equals("MOUNT")) {
            String[] list = {"NONE", "NVGS", "GPNVGS"};
            for (String item : list) {
                renderTextListItem(guiGraphics, item, 20, currentY, mouseX, mouseY);
                currentY += 35;
            }
        }
        
        if (!screen.selectedMount.equals("NONE")) {
            boolean greenHover = mouseY >= currentY && mouseY <= currentY + 40 && mouseX >= 20 && mouseX <= 120;
            boolean whiteHover = mouseY >= currentY && mouseY <= currentY + 40 && mouseX > 120 && mouseX <= 220;
            
            int greenColor = screen.selectedPhosphor.equals("GREEN PHOSPHOR") ? 0xFFD62929 : (greenHover ? 0xFFFFFFFF : 0xFF7A818C);
            int whiteColor = screen.selectedPhosphor.equals("WHITE PHOSPHOR") ? 0xFFD62929 : (whiteHover ? 0xFFFFFFFF : 0xFF7A818C);
            
            drawSmallText(guiGraphics, font, "GREEN", 26, currentY + 10, 0.65f, greenColor);
            drawSmallText(guiGraphics, font, "PHOSPHOR", 26, currentY + 22, 0.65f, greenColor);
            
            drawSmallText(guiGraphics, font, "WHITE", 126, currentY + 10, 0.65f, whiteColor);
            drawSmallText(guiGraphics, font, "PHOSPHOR", 126, currentY + 22, 0.65f, whiteColor);
            
            if (screen.selectedPhosphor.equals("GREEN PHOSPHOR")) guiGraphics.fill(20, currentY + 38, 120, currentY + 40, 0xFFD62929);
            if (screen.selectedPhosphor.equals("WHITE PHOSPHOR")) guiGraphics.fill(120, currentY + 38, 220, currentY + 40, 0xFFD62929);
            
            currentY += 45;
        }
        
        drawSmallText(guiGraphics, font, "FACEWEAR", leftX, currentY + 12, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, screen.selectedFacewear, leftX, currentY + 22, 0.75f, 0xFFFFFFFF);
        currentY += 45;
        
        if (screen.expandedHeadwearCategory.equals("FACEWEAR")) {
            String[] list = {"NONE", "GOGGLES", "GAS MASK"};
            for (String item : list) {
                renderTextListItem(guiGraphics, item, 20, currentY, mouseX, mouseY);
                currentY += 35;
            }
        }
        
        guiGraphics.disableScissor();
    }

    private void renderTacticalSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        drawSmallText(guiGraphics, font, "< LOADOUT", 20, 25, 0.75f, 0xFFFFFF);
        drawSmallText(guiGraphics, font, "LONG TACTICAL", 20, 55, 1.1f, 0xFFFFFF); 
        drawSmallText(guiGraphics, font, "SELECT EQUIPMENT", 20, 75, 0.65f, 0xFFD62929); 

        String[] tacticals = {"MIRRORGUN", "BREACHING SHOTGUN", "RIOT SHIELD", "TACTICAL DRONE", "BOLT CUTTERS", "BATTERING RAM"};
        String[] descriptions = {
            "PEEK UNDER DOORS AND CORNERS",
            "DESTROY DOOR LOCKS AND HINGES",
            "BLOCK INCOMING PROJECTILES",
            "RECON SCOUTING DEVICE",
            "CUT THROUGH CHAINLINK AND LOCKS",
            "HEAVY DOOR BREACHING TOOL"
        };

        int currentY = 100 - (int)screen.scrollOffset;
        int leftX = 26;
        
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        for (int i = 0; i < 6; i++) {
            int y = currentY + (i * 45);
            
            boolean isSelected = screen.selectedTactical.equals(tacticals[i]);
            int textColor = isSelected ? 0xFFD62929 : 0xFFFFFFFF;
            
            drawSmallText(guiGraphics, font, descriptions[i], leftX, y + 14, 0.45f, 0xFF7A818C);
            drawSmallText(guiGraphics, font, tacticals[i], leftX, y + 24, 0.7f, textColor);
            
            guiGraphics.fill(20, y + 40, 220, y + 41, 0xFF2E3136);
            
            if (isSelected) {
                guiGraphics.fill(20, y + 40, 220, y + 41, 0xFFD62929);
            }
        }
        guiGraphics.disableScissor();
    }

    private void renderAttachmentSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        drawSmallText(guiGraphics, font, "< ATTACHMENT BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        String title = screen.editingAttachmentCategory;
        
        drawSmallText(guiGraphics, font, title, 20, 55, 1.1f, 0xFFFFFF); 
        drawSmallText(guiGraphics, font, "SELECT MODIFICATION", 20, 75, 0.65f, 0xFFD62929); 

        String[] idPool = screen.getActiveAttachmentPool();
        ItemStack[] attachmentPool = screen.resolveAttachmentStacks(idPool, screen.editingAttachmentCategory);
        int numBoxes = attachmentPool.length;

        int currentY = 100 - (int)screen.scrollOffset;
        int leftX = 26;
        
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        for (int i = 0; i < numBoxes; i++) {
            int y = currentY + (i * 45);
            
            if (attachmentPool[i] != null && !attachmentPool[i].isEmpty()) {
                String cleanName = idPool[i].replace("pointblank:", "").replace("_", " ").toUpperCase();
                drawSmallText(guiGraphics, font, cleanName, leftX + 45, y + 16, 0.7f, 0xFFFFFFFF);
            } else {
                if (idPool[i].equals("NONE")) {
                    drawSmallText(guiGraphics, font, "REMOVE ATTACHMENT", leftX + 45, y + 16, 0.7f, 0xFFD62929);
                } else {
                    String rawName = idPool[i].replace("pointblank:", "").replace("_", " ").toUpperCase();
                    drawSmallText(guiGraphics, font, rawName + " (MISSING)", leftX + 45, y + 16, 0.65f, 0xFF555555);
                }
            }
        }
        guiGraphics.disableScissor();
    }

    private void renderWeaponSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        drawSmallText(guiGraphics, font, "< WEAPON BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        if (screen.currentWeaponTab != 8) {
            int currentX = 8;
            int[] tabWidths = {20, 20, 25, 25, 25, 38, 35, 44};
            for (int i = 0; i < 8; i++) {
                int tabWidth = tabWidths[i];
                
                String name = WorkbenchData.SHORT_TAB_NAMES[i];
                float scale = 0.55f;
                int textColor = (screen.currentWeaponTab == i) ? 0xFFFFFFFF : 0xFF7A818C; 
                int textWidth = font.width(name);
                int textX = currentX + (tabWidth / 2) - (int)((textWidth * scale) / 2);
                
                drawSmallText(guiGraphics, font, name, textX, 74, scale, textColor);
                
                if (screen.currentWeaponTab == i) {
                    guiGraphics.fill(currentX + 2, 83, currentX + tabWidth - 3, 85, 0xFFD62929);
                }
                
                currentX += tabWidth;
            }
        } else {
            drawSmallText(guiGraphics, font, "SIDE ARM", 20, 75, 0.85f, 0xFFD62929);
        }

        ItemStack[] weaponPool = screen.getActiveWeaponStacks();
        String[] idPool = screen.getActiveWeaponPool();
        int numBoxes = weaponPool.length;

        int currentY = 100 - (int)screen.scrollOffset;
        int leftX = 26;
        
        ItemStack equippedStack = (screen.currentWeaponTab == 8) ? screen.getDisplayedSidearm() : screen.getDisplayedPrimary(); 
        ItemStack previewStack = equippedStack;
        boolean hoveringAny = false;

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        for (int i = 0; i < numBoxes; i++) {
            int y = currentY + (i * 45);
            
            if (mouseY >= Math.max(90, y) && mouseY <= Math.min(trueHeight, y + 40) && mouseX >= 20 && mouseX <= 220) {
                if (weaponPool[i] != null && !weaponPool[i].isEmpty()) {
                    if (ItemStack.isSameItem(equippedStack, weaponPool[i])) {
                        previewStack = equippedStack; 
                    } else {
                        ItemStack invMatch = ItemStack.EMPTY;
                        if (Minecraft.getInstance().player != null) {
                            for (int j = 0; j < Minecraft.getInstance().player.getInventory().getContainerSize(); j++) {
                                ItemStack invStack = Minecraft.getInstance().player.getInventory().getItem(j);
                                if (!invStack.isEmpty() && ItemStack.isSameItem(invStack, weaponPool[i])) {
                                    invMatch = invStack;
                                    break;
                                }
                            }
                        }
                        previewStack = invMatch.isEmpty() ? weaponPool[i] : invMatch;
                    }
                    hoveringAny = true;
                }
            }
            
            if (weaponPool[i] != null && !weaponPool[i].isEmpty()) {
                String gunName = idPool[i].replace("pointblank:", "").replace("_", " ").toUpperCase();
                drawSmallText(guiGraphics, font, gunName, leftX + 45, y + 16, 0.7f, 0xFFFFFFFF);
            } else {
                String rawName = idPool[i].replace("pointblank:", "").replace("_", " ").toUpperCase();
                drawSmallText(guiGraphics, font, rawName + " (MISSING)", leftX + 45, y + 16, 0.65f, 0xFF555555);
            }
        }
        guiGraphics.disableScissor();

        if (!hoveringAny) {
            previewStack = equippedStack;
        }

        if (!previewStack.isEmpty()) {
            int infoX = 260;
            int infoY = 35; 
            
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(infoX + 15, infoY, 350.0F); 
            guiGraphics.pose().scale(4.0f, 4.0f, 4.0f); 
            guiGraphics.renderItem(previewStack, 0, 0);
            guiGraphics.pose().popPose();

            infoY += 80; 

            String gunName = previewStack.getHoverName().getString().toUpperCase();
            drawSmallText(guiGraphics, font, gunName, infoX, infoY, 1.2f, 0xFFFFFFFF);
            
            infoY += 25;
            drawSmallText(guiGraphics, font, "BRIEF", infoX, infoY, 0.65f, 0xFF7A818C);
            infoY += 12;
            
            String brief1 = "A standardized tactical weapon designed for";
            String brief2 = "modern combat operations.";
            String round = "5.56X45MM NATO";
            String recoil = "MODERATE";
            String fireRate = "750 RPM";
            String capacity = "30 ROUNDS";
            String platform = "TACTICAL PLATFORM";
            
            switch (screen.currentWeaponTab) {
                case 0: 
                    brief1 = "A versatile assault rifle providing high fire";
                    brief2 = "rate and reliable medium range accuracy.";
                    round = "5.56X45MM NATO"; recoil = "MODERATE"; fireRate = "750 RPM"; capacity = "30 ROUNDS";
                    platform = "ASSAULT RIFLE PLATFORM";
                    break;
                case 1: 
                    brief1 = "Heavy hitting battle rifle firing high caliber";
                    brief2 = "rounds for maximum stopping power.";
                    round = "7.62X51MM NATO"; recoil = "HIGH"; fireRate = "600 RPM"; capacity = "20 ROUNDS";
                    platform = "BATTLE RIFLE PLATFORM";
                    break;
                case 2: 
                    brief1 = "Light machine gun designed to lay down";
                    brief2 = "sustained suppressive fire in combat.";
                    round = "5.56X45MM NATO"; recoil = "MODERATE"; fireRate = "800 RPM"; capacity = "100 ROUNDS";
                    platform = "LMG PLATFORM";
                    break;
                case 3: 
                    brief1 = "Personal defense weapon prioritizing mobility";
                    brief2 = "and high fire rate for close quarters.";
                    round = "5.7X28MM"; recoil = "LOW"; fireRate = "900 RPM"; capacity = "50 ROUNDS";
                    platform = "PDW PLATFORM";
                    break;
                case 4: 
                    brief1 = "Submachine gun offering excellent handling";
                    brief2 = "and extreme fire rate in tight spaces.";
                    round = "9X19MM PARABELLUM"; recoil = "LOW"; fireRate = "850 RPM"; capacity = "30 ROUNDS";
                    platform = "SMG PLATFORM";
                    break;
                case 5: 
                    brief1 = "Devastating close-range scattergun capable";
                    brief2 = "of breaching doors and clearing rooms.";
                    round = "12 GAUGE"; recoil = "HIGH"; fireRate = "PUMP-ACTION"; capacity = "8 ROUNDS";
                    platform = "SHOTGUN PLATFORM";
                    break;
                case 6: 
                    brief1 = "High-precision marksman rifle designed for";
                    brief2 = "extreme long-range engagements.";
                    round = ".338 LAPUA MAGNUM"; recoil = "VERY HIGH"; fireRate = "BOLT-ACTION"; capacity = "5 ROUNDS";
                    platform = "SNIPER PLATFORM";
                    break;
                case 7: 
                    brief1 = "Anti-armor munition launcher intended to";
                    brief2 = "destroy heavy vehicles and emplacements.";
                    round = "84MM HE"; recoil = "EXTREME"; fireRate = "SINGLE SHOT"; capacity = "1 TUBE";
                    platform = "LAUNCHER PLATFORM";
                    break;
                case 8: 
                    brief1 = "Compact sidearm providing reliable backup";
                    brief2 = "firepower when the primary weapon is dry.";
                    round = "9X19MM PARABELLUM"; recoil = "LOW"; fireRate = "SEMI-AUTO"; capacity = "15 ROUNDS";
                    platform = "SIDEARM PLATFORM";
                    break;
            }
            
            drawSmallText(guiGraphics, font, brief1, infoX, infoY, 0.65f, 0xFFFFFFFF);
            infoY += 12;
            drawSmallText(guiGraphics, font, brief2, infoX, infoY, 0.65f, 0xFFFFFFFF);
            
            infoY += 25;
            
            drawSmallText(guiGraphics, font, "PLATFORM", infoX + 75, infoY, 0.65f, 0xFF7A818C);
            infoY += 12;
            
            int col1X = infoX;
            int col2X = infoX + 75; 
            
            drawSmallText(guiGraphics, font, "ROUND", col1X, infoY, 0.65f, 0xFF7A818C);
            drawSmallText(guiGraphics, font, platform, col2X, infoY, 0.65f, 0xFFFFFFFF);
            infoY += 14;
            
            drawSmallText(guiGraphics, font, round, col1X, infoY, 0.65f, 0xFFFFFFFF);
            infoY += 14;
            
            drawSmallText(guiGraphics, font, "RECOIL", col1X, infoY, 0.65f, 0xFF7A818C);
            infoY += 14;
            
            drawSmallText(guiGraphics, font, recoil, col1X, infoY, 0.65f, 0xFFFFFFFF);
            infoY += 14;
            
            drawSmallText(guiGraphics, font, "FIRE-RATE", col1X, infoY, 0.65f, 0xFF7A818C);
            infoY += 14;
            
            drawSmallText(guiGraphics, font, fireRate, col1X, infoY, 0.65f, 0xFFFFFFFF);
            infoY += 14;
            
            drawSmallText(guiGraphics, font, "CAPACITY", col1X, infoY, 0.65f, 0xFF7A818C);
            infoY += 14;
            
            drawSmallText(guiGraphics, font, capacity, col1X, infoY, 0.65f, 0xFFFFFFFF);
            
            infoY += 25;
            drawSmallText(guiGraphics, font, "ATTACHMENTS", infoX, infoY, 0.65f, 0xFF7A818C);
            infoY += 12;
            
            String[] cats = (screen.currentWeaponTab == 8) ? new String[]{"OPTIC", "MUZZLE", "STOCK", "MAGAZINE"} : new String[]{"OPTIC", "BARREL", "MUZZLE", "UNDERBARREL", "LASER", "MAGAZINE"};
            int attachY = infoY;
            boolean hasAtt = false;
            for (String cat : cats) {
                AttachmentInfo att = getAttachmentInfo(previewStack, cat);
                if (!att.name.equals("NONE")) {
                    drawSmallText(guiGraphics, font, "- " + att.name, infoX, attachY, 0.65f, 0xFFD2D6DE);
                    attachY += 12;
                    hasAtt = true;
                }
            }
            if (!hasAtt) {
                drawSmallText(guiGraphics, font, "FACTORY STANDARD", infoX, attachY, 0.65f, 0xFF555555);
                drawSmallText(guiGraphics, font, "(NO ATTACHMENTS)", infoX, attachY+12, 0.65f, 0xFF555555);
            }
        }
    }

    private void renderMunitionSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        drawSmallText(guiGraphics, font, "< LOADOUT", 20, 25, 0.75f, 0xFFFFFF);
        drawSmallText(guiGraphics, font, "MUNITIONS", 20, 55, 1.1f, 0xFFFFFF); 
        drawSmallText(guiGraphics, font, "SELECT EQUIPMENT", 20, 75, 0.65f, 0xFFD62929); 

        String[] ammoNames = {"5.56X45MM NATO", "9X19MM PARABELLUM", "12 GAUGE BUCKSHOT", ".300 BLACKOUT"};
        String[] grenadeNames = {"9-BANG FLASH GRENADE", "CS GAS", "FLASHBANGS", "STINGER"};
        String[] tacticalNames = {"C2", "LOCKPICK GUN", "PEPPER SPRAY", "TASER", "WEDGE"};

        int currentY = 100 - (int)screen.scrollOffset;
        int leftX = 20;
        
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        
        drawSmallText(guiGraphics, font, "PRIMARY AMMUNITION", leftX, currentY, 0.65f, 0xFF7A818C);
        currentY += 20;
        for (String name : ammoNames) {
            renderTextListItem(guiGraphics, name, leftX, currentY, mouseX, mouseY);
            currentY += 35;
        }
        
        currentY += 10; 
        
        drawSmallText(guiGraphics, font, "GRENADE", leftX, currentY, 0.65f, 0xFF7A818C);
        currentY += 20;
        for (String name : grenadeNames) {
            renderTextListItem(guiGraphics, name, leftX, currentY, mouseX, mouseY);
            currentY += 35;
        }
        
        currentY += 10;
        
        drawSmallText(guiGraphics, font, "TACTICAL", leftX, currentY, 0.65f, 0xFF7A818C);
        currentY += 20;
        for (String name : tacticalNames) {
            renderTextListItem(guiGraphics, name, leftX, currentY, mouseX, mouseY);
            currentY += 35;
        }

        guiGraphics.disableScissor();
    }

    private void renderTextListItem(GuiGraphics guiGraphics, String name, int x, int y, int mouseX, int mouseY) {
        boolean isHovered = mouseY >= y && mouseY <= y + 35 && mouseX >= x && mouseX <= x + 200;
        int textColor = isHovered ? 0xFFFFFFFF : 0xFF7A818C;
        
        drawSmallText(guiGraphics, font, name, x, y + 10, 0.8f, textColor);
        guiGraphics.fill(x, y + 25, 220, y + 26, 0xFF2E3136);
        
        if (isHovered) {
            int textWidth = font.width(name);
            int scaledWidth = (int)(textWidth * 0.8f);
            guiGraphics.fill(x, y + 25, x + scaledWidth, y + 26, 0xFFD62929); 
        }
    }

    private void renderGunsmithLabels(GuiGraphics guiGraphics) {
        drawSmallText(guiGraphics, font, "< WEAPON BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        boolean isPrimary = (screen.currentWeaponTab != 8);
        drawSmallText(guiGraphics, font, "PRIMARY", 20, 75, 0.85f, isPrimary ? 0xFFFFFFFF : 0xFF7A818C);
        drawSmallText(guiGraphics, font, "SIDE ARM", 100, 75, 0.85f, !isPrimary ? 0xFFFFFFFF : 0xFF7A818C);
        
        if (isPrimary) {
            guiGraphics.fill(20, 87, 80, 89, 0xFFD62929); 
        } else {
            guiGraphics.fill(100, 87, 160, 89, 0xFFD62929); 
        }

        int startY = 100;
        int currentY = startY - (int)screen.scrollOffset;
        int leftX = 26;

        guiGraphics.enableScissor(0, 90, 240, guiGraphics.guiHeight());
        
        drawSmallText(guiGraphics, font, "WEAPON", leftX, currentY + 50, 0.45f, 0xFF7A818C);
        drawSmallText(guiGraphics, font, "CURRENT", leftX, currentY + 58, 0.65f, 0xFFD2D6DE);
        
        ItemStack weaponStack = (screen.currentWeaponTab == 8) ? screen.getDisplayedSidearm() : screen.getDisplayedPrimary();
        if (!weaponStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(110, currentY + 8, 350.0F); 
            guiGraphics.pose().scale(3.5f, 3.5f, 1.0f); 
            guiGraphics.renderItem(weaponStack, 0, 0);
            guiGraphics.pose().popPose();
        } else {
            drawSmallText(guiGraphics, font, "NO WEAPON EQUIPPED", 90, currentY + 32, 0.55f, 0xFF555555);
        }

        currentY += 75;

        currentY += 5; 
        drawSmallText(guiGraphics, font, "ATTACHMENTS", leftX, currentY + 6, 0.65f, 0xFF7A818C);
        currentY += 25;

        int numCoreAttachments = (screen.currentWeaponTab == 8) ? 3 : 5;
        String[] boxCats = (screen.currentWeaponTab == 8) 
                ? new String[]{"OPTIC", "MUZZLE", "STOCK"} 
                : new String[]{"OPTIC", "BARREL", "MUZZLE", "UNDERBARREL", "LASER"};

        AttachmentInfo[] attachments = new AttachmentInfo[numCoreAttachments];
        for (int i = 0; i < numCoreAttachments; i++) {
            attachments[i] = getAttachmentInfo(weaponStack, boxCats[i]);
        }

        for (int i = 0; i < numCoreAttachments; i++) {
            drawSmallText(guiGraphics, font, boxCats[i], leftX, currentY + 12, 0.45f, 0xFF7A818C);
            drawSmallText(guiGraphics, font, attachments[i].name, leftX, currentY + 22, 0.65f, 0xFFD2D6DE);
            
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
        drawSmallText(guiGraphics, font, "AMMUNITION", leftX, tabY + 6, 0.55f, screen.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        drawSmallText(guiGraphics, font, "DEPLOYABLE", 116, tabY + 6, 0.55f, !screen.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        currentY = tabY + 20;

        if (screen.showAmmunitionTab) {
            String[] primaryCats = {"MAGAZINE", "AMMUNITION"};
            AttachmentInfo pMagInfo = getAttachmentInfo(screen.getDisplayedPrimary(), "MAGAZINE");
            AttachmentInfo pAmmoInfo = getAttachmentInfo(screen.getDisplayedPrimary(), "AMMO");
            AttachmentInfo[] pAmmoInfos = {pMagInfo, pAmmoInfo};
            String[] primaryNames = {
                    pMagInfo.name.equals("NONE") ? "STANDARD MAG" : pMagInfo.name, 
                    pAmmoInfo.name.equals("NONE") ? "5.56X45MM NATO" : pAmmoInfo.name
            };
            
            String[] sidearmCats = {"MAGAZINE", "AMMUNITION"};
            AttachmentInfo sMagInfo = getAttachmentInfo(screen.getDisplayedSidearm(), "MAGAZINE");
            AttachmentInfo sAmmoInfo = getAttachmentInfo(screen.getDisplayedSidearm(), "AMMO");
            AttachmentInfo[] sAmmoInfos = {sMagInfo, sAmmoInfo};
            String[] sidearmNames = {
                    sMagInfo.name.equals("NONE") ? "STANDARD MAG" : sMagInfo.name, 
                    sAmmoInfo.name.equals("NONE") ? "9X19MM PARABELLUM" : sAmmoInfo.name
            };
            
            drawSmallText(guiGraphics, font, "PRIMARY AMMUNITION", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < primaryCats.length; i++) {
                drawSmallText(guiGraphics, font, primaryCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, font, primaryNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
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
            drawSmallText(guiGraphics, font, "SIDEARM AMMUNITION", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < sidearmCats.length; i++) {
                drawSmallText(guiGraphics, font, sidearmCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, font, sidearmNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
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
            
            drawSmallText(guiGraphics, font, "GRENADE", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < grenadeCats.length; i++) {
                drawSmallText(guiGraphics, font, grenadeCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, font, grenadeNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }

            currentY += 10; 
            drawSmallText(guiGraphics, font, "TACTICAL", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < tacticalCats.length; i++) {
                drawSmallText(guiGraphics, font, tacticalCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                drawSmallText(guiGraphics, font, tacticalNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }
        }

        guiGraphics.disableScissor();
    }
}