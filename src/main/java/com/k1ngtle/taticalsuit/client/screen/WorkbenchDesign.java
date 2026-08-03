package com.k1ngtle.taticalsuit.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class WorkbenchDesign {

    private final WorkbenchScreen screen;
    private final Font font;

    public WorkbenchDesign(WorkbenchScreen screen) {
        this.screen = screen;
        this.font = Minecraft.getInstance().font;
    }

    public boolean handleMouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton != 0) return false;

        if (screen.inCustomizationTab && screen.inStyleSelection) {
            int openSpaceWidth = screen.width - 120; 
            int centerX = 240 + (openSpaceWidth - 240) / 2;
            int boxSize = 70;
            int gap = 15;
            
            int totalWidth = (boxSize * 4) + (gap * 3);
            int startX = centerX - (totalWidth / 2);
            int startY = screen.height - 100; 
            
            boolean clickedInsideBoxes = false;

            for (int i = 0; i < 4; i++) {
                int boxX = startX + (i * (boxSize + gap));
                if (pMouseX >= boxX && pMouseX <= boxX + boxSize && pMouseY >= startY && pMouseY <= startY + boxSize) {
                    clickedInsideBoxes = true;
                    if (System.currentTimeMillis() - screen.lastClickTime < 500) return true;
                    screen.lastClickTime = System.currentTimeMillis();
                    
                    if (i == 0) screen.selectedHelmet = "HELMET ONLY";
                    else if (i == 1) screen.selectedHelmet = "GHILLIE HELMET";
                    else if (i == 2) screen.selectedHelmet = "SAND GHILLIE HELMET";
                    else if (i == 3) screen.selectedHelmet = "SNOW GHILLIE HELMET";
                    
                    screen.updateHelmetEquip();
                    
                    screen.inStyleSelection = false;
                    return true;
                }
            }
            
            if (!clickedInsideBoxes && pMouseX > 240 && pMouseX < (screen.width - 120)) {
                 screen.inStyleSelection = false;
                 return true;
            }
        }

        if (screen.inCustomizationTab && screen.inCustomizationSelection) {
            boolean isLargeGrid = screen.customizationCategory.equals("SHIRT") || screen.customizationCategory.equals("PANTS") || screen.customizationCategory.equals("ARMOR");
            int cols = isLargeGrid ? 3 : 2;
            int rows = isLargeGrid ? 7 : 6;
            
            int panelWidth = isLargeGrid ? 170 : 120;
            int panelX = screen.width - panelWidth;

            if (pMouseX < panelX) {
                if (!screen.inStyleSelection) {
                    screen.inCustomizationSelection = false;
                    screen.scrollOffset = 0f;
                }
                return true;
            }

            int gridStartX = panelX + 15;
            int startY = 50 - (int)screen.scrollOffset;
            
            for (int i = 0; i < (rows * cols); i++) {
                int col = i % cols;
                int row = i / cols;
                int boxX = gridStartX + (col * 45); 
                int boxY = startY + (row * 45);
                
                if (pMouseX >= boxX && pMouseX <= boxX + 40 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                    if (System.currentTimeMillis() - screen.lastClickTime < 500) return true;
                    screen.lastClickTime = System.currentTimeMillis();
                    
                    if (screen.customizationCategory.equals("HELMET")) {
                        if (i == 0) {
                            screen.inStyleSelection = true;
                        }
                    } else {
                        screen.inCustomizationSelection = false;
                        screen.scrollOffset = 0f;
                    }
                    return true;
                }
            }
            return true;
        }

        if (screen.inTacticalSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                screen.inTacticalSelection = false;
                screen.scrollOffset = 0f;
                return true;
            }

            int currentY = 100 - (int)screen.scrollOffset;
            for (int i = 0; i < 6; i++) {
                int boxY = currentY + (i * 45);
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                    if (System.currentTimeMillis() - screen.lastClickTime < 500) return true;
                    screen.lastClickTime = System.currentTimeMillis();
                    
                    String[] tacticals = {"MIRRORGUN", "BREACHING SHOTGUN", "RIOT SHIELD", "TACTICAL DRONE", "BOLT CUTTERS", "BATTERING RAM"};
                    screen.selectedTactical = tacticals[i];
                    screen.inTacticalSelection = false;
                    screen.scrollOffset = 0f;
                    return true;
                }
            }
            return true;
        } else if (screen.inArmorSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                screen.inArmorSelection = false;
                screen.scrollOffset = 0f;
                return true;
            }

            int currentY = 100 - (int)screen.scrollOffset;
            int vestDropdownY = currentY + 45;
            
            if (screen.expandedArmorCategory.equals("VEST")) {
                String[] vestList = {"NO ARMOR", "LIGHT ARMOR", "HEAVY ARMOR", "STAB VEST"};
                int listY = vestDropdownY;
                for (String item : vestList) {
                    if (pMouseY >= listY && pMouseY <= listY + 35 && pMouseX >= 20 && pMouseX <= 220) {
                        screen.selectedVest = item;
                        screen.expandedArmorCategory = "";
                        return true;
                    }
                    listY += 35;
                }
                
                if (pMouseY >= vestDropdownY && pMouseY <= listY && pMouseX >= 20 && pMouseX <= 220) {
                    return true;
                }
            } else {
                if (pMouseY >= currentY && pMouseY <= currentY + 30 && pMouseX >= 20 && pMouseX <= 220) {
                    screen.expandedArmorCategory = "VEST";
                    return true;
                }
            }
            currentY += 45;
            currentY += 20;
            if (pMouseY >= currentY && pMouseY <= currentY + 30) {
                String[] covList = {"NONE", "FRONT", "FRONT/BACK", "FULL"};
                for(int i = 0; i < 4; i++) {
                    int boxX = 20 + (i * 50);
                    if (pMouseX >= boxX && pMouseX <= boxX + 45) {
                        screen.selectedCoverage = covList[i];
                        return true;
                    }
                }
            }
            currentY += 40;
            currentY += 20; 
            if (pMouseY >= currentY && pMouseY <= currentY + 30) {
                String[] matList = {"KEVLAR", "STEEL", "CERAMIC"};
                for(int i = 0; i < 3; i++) {
                    int boxX = 20 + (i * 66);
                    if (pMouseX >= boxX && pMouseX <= boxX + 60) {
                        screen.selectedMaterial = matList[i];
                        return true;
                    }
                }
            }
            currentY += 40; 
            currentY += 20; 

            if (pMouseY >= currentY && pMouseY <= currentY + 20) {
                if (pMouseX >= 20 && pMouseX <= 110) {
                    screen.showAmmunitionTab = true;
                    return true;
                } else if (pMouseX > 110 && pMouseX <= 220) {
                    screen.showAmmunitionTab = false;
                    return true;
                }
            }
            currentY += 20;
            return true;
        } else if (screen.inHeadwearSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                screen.inHeadwearSelection = false;
                screen.scrollOffset = 0f;
                return true;
            }

            int currentY = 100 - (int)screen.scrollOffset;
            
            if (pMouseY >= currentY && pMouseY <= currentY + 40 && pMouseX >= 20 && pMouseX <= 220) {
                screen.expandedHeadwearCategory = screen.expandedHeadwearCategory.equals("HELMET") ? "" : "HELMET";
                return true;
            }
            currentY += 45;
            
            if (screen.expandedHeadwearCategory.equals("HELMET")) {
                String[] list = {"NO HELMET", "HELMET ONLY"};
                for (String item : list) {
                    if (pMouseY >= currentY && pMouseY <= currentY + 35 && pMouseX >= 20 && pMouseX <= 220) {
                        screen.selectedHelmet = item;
                        screen.expandedHeadwearCategory = "";
                        if (item.equals("NO HELMET")) {
                            screen.selectedMount = "NONE";
                        }
                        screen.updateHelmetEquip();
                        return true;
                    }
                    currentY += 35;
                }
            }
            
            if (pMouseY >= currentY && pMouseY <= currentY + 40 && pMouseX >= 20 && pMouseX <= 220) {
                screen.expandedHeadwearCategory = screen.expandedHeadwearCategory.equals("MOUNT") ? "" : "MOUNT";
                return true;
            }
            currentY += 45;
            
            if (screen.expandedHeadwearCategory.equals("MOUNT")) {
                String[] list = {"NONE", "NVGS", "GPNVGS"};
                for (String item : list) {
                    if (pMouseY >= currentY && pMouseY <= currentY + 35 && pMouseX >= 20 && pMouseX <= 220) {
                        screen.selectedMount = item;
                        screen.expandedHeadwearCategory = "";
                        
                        // Ghillie allows GPNVG18s, but Sand and Snow don't (per your current models)
                        if (!screen.selectedHelmet.equals("GHILLIE HELMET") && !screen.selectedHelmet.equals("SAND GHILLIE HELMET") && !screen.selectedHelmet.equals("SNOW GHILLIE HELMET")) {
                            screen.selectedHelmet = "HELMET ONLY";
                        }
                        
                        // If they select NVGS while wearing Ghillie, force it back to default helmet, as you don't have a PVS-31 Ghillie model yet
                        if (screen.selectedHelmet.equals("GHILLIE HELMET") && screen.selectedMount.equals("NVGS")) {
                             screen.selectedHelmet = "HELMET ONLY";
                        }

                        screen.updateHelmetEquip();
                        return true;
                    }
                    currentY += 35;
                }
            }
            
            if (!screen.selectedMount.equals("NONE")) {
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 120) {
                        screen.selectedPhosphor = "GREEN PHOSPHOR";
                        screen.updateHelmetEquip();
                        return true;
                    } else if (pMouseX > 120 && pMouseX <= 220) {
                        screen.selectedPhosphor = "WHITE PHOSPHOR";
                        screen.updateHelmetEquip();
                        return true;
                    }
                }
                currentY += 45;
            }
            
            if (pMouseY >= currentY && pMouseY <= currentY + 40 && pMouseX >= 20 && pMouseX <= 220) {
                screen.expandedHeadwearCategory = screen.expandedHeadwearCategory.equals("FACEWEAR") ? "" : "FACEWEAR";
                return true;
            }
            currentY += 45;
            
            if (screen.expandedHeadwearCategory.equals("FACEWEAR")) {
                String[] list = {"NONE", "GOGGLES", "GAS MASK"};
                for (String item : list) {
                    if (pMouseY >= currentY && pMouseY <= currentY + 35 && pMouseX >= 20 && pMouseX <= 220) {
                        screen.selectedFacewear = item;
                        screen.expandedHeadwearCategory = "";
                        return true;
                    }
                    currentY += 35;
                }
            }
            
            return true;
        } else if (screen.inMunitionSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                screen.inMunitionSelection = false;
                screen.scrollOffset = 0f;
                return true;
            }

            if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 90) {
                if (System.currentTimeMillis() - screen.lastClickTime < 500) return true;
                screen.lastClickTime = System.currentTimeMillis();

                screen.inMunitionSelection = false; 
                screen.scrollOffset = 0f;
                return true;
            }
            return true;
        } else if (screen.inAttachmentSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                screen.inAttachmentSelection = false;
                screen.scrollOffset = 0f;
                return true;
            }

            String[] idPool = screen.getActiveAttachmentPool();
            int startY = 100 - (int)screen.scrollOffset;
            
            for (int i = 0; i < idPool.length; i++) {
                int boxY = startY + (i * 45);
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                    if (System.currentTimeMillis() - screen.lastClickTime < 500) return true;
                    screen.lastClickTime = System.currentTimeMillis();

                    int menuSlotIndex = (screen.currentWeaponTab == 8) ? 1 : 0; 

                    String vpbCategory = switch (screen.editingAttachmentCategory) {
                        case "OPTIC"       -> "scope";
                        case "BARREL"      -> "barrel";
                        case "MUZZLE"      -> "muzzle";
                        case "UNDERBARREL" -> "underbarrel";
                        case "LASER"       -> "rail";
                        case "STOCK"       -> "stock";
                        case "MAGAZINE"    -> "magazine";
                        default -> screen.editingAttachmentCategory.toLowerCase();
                    };

                    ItemStack currentWeapon = screen.getMenu().getSlot(menuSlotIndex).getItem().copy();
                    if (currentWeapon.isEmpty() && Minecraft.getInstance().player != null) {
                        currentWeapon = Minecraft.getInstance().player.getInventory().getItem(menuSlotIndex).copy();
                    }
                    if (!currentWeapon.isEmpty()) {
                        net.minecraft.nbt.CompoundTag tag = currentWeapon.getOrCreateTag();

                        net.minecraft.nbt.CompoundTag saTag = tag.contains("sa", net.minecraft.nbt.Tag.TAG_COMPOUND)
                                ? tag.getCompound("sa").copy() : new net.minecraft.nbt.CompoundTag();
                        net.minecraft.nbt.ListTag asList = tag.contains("as", net.minecraft.nbt.Tag.TAG_LIST)
                                ? tag.getList("as", net.minecraft.nbt.Tag.TAG_COMPOUND).copy() : new net.minecraft.nbt.ListTag();

                        if (idPool[i].equals("NONE")) {
                            saTag.remove(vpbCategory);
                            net.minecraft.nbt.ListTag newAs = new net.minecraft.nbt.ListTag();
                            for (int k = 0; k < asList.size(); k++) {
                                net.minecraft.nbt.CompoundTag entry = asList.getCompound(k);
                                String eid = entry.getString("id");
                                ResourceLocation resLoc = ResourceLocation.tryParse(eid.isEmpty() ? "minecraft:air" : eid);
                                net.minecraft.world.item.Item eItem = resLoc == null ? net.minecraft.world.item.Items.AIR : net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(resLoc);
                                if (eItem != null && eItem != net.minecraft.world.item.Items.AIR
                                        && !com.k1ngtle.taticalsuit.network.EquipWeaponPacket.isItemInCategory(eItem, vpbCategory)) {
                                    newAs.add(entry);
                                }
                            }
                            asList = newAs;
                        } else {
                            saTag.putString(vpbCategory, idPool[i]);
                            net.minecraft.nbt.ListTag newAs = new net.minecraft.nbt.ListTag();
                            for (int k = 0; k < asList.size(); k++) {
                                net.minecraft.nbt.CompoundTag entry = asList.getCompound(k);
                                String eid = entry.getString("id");
                                ResourceLocation resLoc = ResourceLocation.tryParse(eid.isEmpty() ? "minecraft:air" : eid);
                                net.minecraft.world.item.Item eItem = resLoc == null ? net.minecraft.world.item.Items.AIR : net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(resLoc);
                                if (eItem != null && eItem != net.minecraft.world.item.Items.AIR
                                        && !com.k1ngtle.taticalsuit.network.EquipWeaponPacket.isItemInCategory(eItem, vpbCategory)) {
                                    newAs.add(entry);
                                }
                            }
                            net.minecraft.nbt.CompoundTag newEntry = new net.minecraft.nbt.CompoundTag();
                            newEntry.putString("id", idPool[i]);
                            newEntry.putBoolean("rmv", true);
                            newAs.add(newEntry);
                            asList = newAs;
                        }

                        tag.put("sa", saTag);
                        tag.put("as", asList);
                        screen.getMenu().getSlot(menuSlotIndex).set(currentWeapon);
                    }

                    com.k1ngtle.taticalsuit.network.ModNetworking.CHANNEL.sendToServer(
                            new com.k1ngtle.taticalsuit.network.EquipWeaponPacket(menuSlotIndex, idPool[i], true, vpbCategory)
                    );
                    
                    screen.inAttachmentSelection = false; 
                    screen.scrollOffset = 0f;
                    return true;
                }
            }
            return true;
        } else if (screen.inWeaponSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                screen.inWeaponSelection = false;
                screen.scrollOffset = 0f;
                return true;
            }

            if (pMouseY >= 70 && pMouseY <= 85 && screen.currentWeaponTab != 8) {
                int currentX = 8;
                int[] tabWidths = {20, 20, 25, 25, 25, 38, 35, 44};
                for (int i = 0; i < 8; i++) {
                    int tabWidth = tabWidths[i];
                    if (pMouseX >= currentX && pMouseX <= currentX + tabWidth - 1) {
                        screen.currentWeaponTab = i;
                        screen.scrollOffset = 0f;
                        return true;
                    }
                    currentX += tabWidth;
                }
            }

            String[] idPool = screen.getActiveWeaponPool();
            int startY = 100 - (int)screen.scrollOffset;
            
            for (int i = 0; i < idPool.length; i++) {
                int boxY = startY + (i * 45);
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                    if (System.currentTimeMillis() - screen.lastClickTime < 500) return true;
                    screen.lastClickTime = System.currentTimeMillis();
                    
                    ItemStack currentEquipped = (screen.currentWeaponTab == 8) ? screen.getDisplayedSidearm() : screen.getDisplayedPrimary();
                    if (!currentEquipped.isEmpty()) {
                        ResourceLocation currentLoc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(currentEquipped.getItem());
                        if (currentLoc != null && currentLoc.toString().equals(idPool[i])) {
                            screen.inWeaponSelection = false; 
                            screen.scrollOffset = 0f;
                            return true;
                        }
                    }

                    int menuSlotIndex = (screen.currentWeaponTab == 8) ? 1 : 0; 

                    ItemStack optimisticStack = ItemStack.EMPTY;
                    if (Minecraft.getInstance().player != null) {
                        for (int j = 0; j < Minecraft.getInstance().player.getInventory().getContainerSize(); j++) {
                            ItemStack invStack = Minecraft.getInstance().player.getInventory().getItem(j);
                            if (!invStack.isEmpty()) {
                                ResourceLocation invLoc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(invStack.getItem());
                                if (invLoc != null && invLoc.toString().equals(idPool[i])) {
                                    optimisticStack = invStack.copy();
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (optimisticStack.isEmpty()) {
                        ResourceLocation resLoc = ResourceLocation.tryParse(idPool[i]);
                        if(resLoc != null) {
                            net.minecraft.world.item.Item newItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(resLoc);
                            if (newItem != null && newItem != net.minecraft.world.item.Items.AIR) {
                                optimisticStack = newItem.getDefaultInstance().copy();
                            }
                        }
                    }
                    
                    if (!optimisticStack.isEmpty()) {
                        screen.getMenu().getSlot(menuSlotIndex).set(optimisticStack);
                    }

                    com.k1ngtle.taticalsuit.network.ModNetworking.CHANNEL.sendToServer(
                            new com.k1ngtle.taticalsuit.network.EquipWeaponPacket(menuSlotIndex, idPool[i])
                    );
                    screen.inWeaponSelection = false; 
                    screen.scrollOffset = 0f;
                    return true;
                }
            }
            return true; 
        } else if (screen.inGunsmith) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                screen.inGunsmith = false;
                return true;
            }

            if (pMouseY >= 70 && pMouseY <= 90) {
                if (pMouseX >= 20 && pMouseX <= 90) {
                    if (screen.currentWeaponTab == 8) screen.currentWeaponTab = 0; 
                    screen.scrollOffset = 0f; 
                    return true;
                } else if (pMouseX > 90 && pMouseX <= 180) {
                    screen.currentWeaponTab = 8; 
                    screen.scrollOffset = 0f; 
                    return true;
                }
            }

            int weaponBoxY = 100 - (int)screen.scrollOffset;
            if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= weaponBoxY && pMouseY <= weaponBoxY + 70) {
                screen.inWeaponSelection = true; 
                screen.scrollOffset = 0f;
                return true;
            }

            int numCoreAttachments = (screen.currentWeaponTab == 8) ? 3 : 5;
            String[] boxCats = (screen.currentWeaponTab == 8) 
                    ? new String[]{"OPTIC", "MUZZLE", "STOCK"} 
                    : new String[]{"OPTIC", "BARREL", "MUZZLE", "UNDERBARREL", "LASER"};

            int currentY = 100 - (int)screen.scrollOffset + 75 + 30;
            for (int i = 0; i < numCoreAttachments; i++) {
                int boxY = currentY;
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                    screen.inAttachmentSelection = true;
                    screen.editingAttachmentCategory = boxCats[i];
                    screen.scrollOffset = 0f;
                    return true;
                }
                currentY += 45;
            }

            int baseTabY = 100 + 75 + 30 + (numCoreAttachments * 45) + 10; 
            int scrolledTabY = baseTabY - (int)screen.scrollOffset;
            if (pMouseY >= scrolledTabY && pMouseY <= scrolledTabY + 20) {
                if (pMouseX >= 20 && pMouseX <= 110) {
                    screen.showAmmunitionTab = true;
                    return true;
                } else if (pMouseX > 110 && pMouseX <= 220) {
                    screen.showAmmunitionTab = false;
                    return true;
                }
            }
            return true; 
        } else {
            if (pMouseY >= 4 && pMouseY <= 16) {
                int loadoutWidth = font.width("LOADOUT");
                int customX = 20 + loadoutWidth + font.width(" / ");
                if (pMouseX >= 20 && pMouseX <= 20 + loadoutWidth) {
                    screen.inCustomizationTab = false;
                    screen.inCustomizationSelection = false;
                    screen.inStyleSelection = false;
                    return true;
                } else if (pMouseX >= customX && pMouseX <= customX + font.width("CUSTOMIZATION")) {
                    screen.inCustomizationTab = true;
                    screen.inCustomizationSelection = false;
                    screen.inStyleSelection = false;
                    return true;
                }
            }

            if (screen.inCustomizationTab) {
                int startY = 30;
                int currentY = startY + 15;
                
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 115) { screen.inCustomizationSelection = true; screen.customizationCategory = "SHIRT"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                    if (pMouseX >= 125 && pMouseX <= 220) { screen.inCustomizationSelection = true; screen.customizationCategory = "PANTS"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                }
                currentY += 45;
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 80) { screen.inCustomizationSelection = true; screen.customizationCategory = "GLOVES"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                    if (pMouseX >= 90 && pMouseX <= 150) { screen.inCustomizationSelection = true; screen.customizationCategory = "BOOTS"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                    if (pMouseX >= 160 && pMouseX <= 220) { screen.inCustomizationSelection = true; screen.customizationCategory = "BELT"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                }
                
                currentY += 60; 
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 115) { screen.inCustomizationSelection = true; screen.customizationCategory = "ARMOR"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                    if (pMouseX >= 125 && pMouseX <= 220) { screen.inCustomizationSelection = true; screen.customizationCategory = "HELMET"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                }
                currentY += 45;
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 80) { screen.inCustomizationSelection = true; screen.customizationCategory = "FACEWEAR"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                    if (pMouseX >= 90 && pMouseX <= 150) { screen.inCustomizationSelection = true; screen.customizationCategory = "NVG"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                    if (pMouseX >= 160 && pMouseX <= 220) { screen.inCustomizationSelection = true; screen.customizationCategory = "BALLISTIC MASK"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                }
                
                currentY += 60;
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 80) { screen.inCustomizationSelection = true; screen.customizationCategory = "TATTOO"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                    if (pMouseX >= 90 && pMouseX <= 150) { screen.inCustomizationSelection = true; screen.customizationCategory = "EYEWEAR"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                    if (pMouseX >= 160 && pMouseX <= 220) { screen.inCustomizationSelection = true; screen.customizationCategory = "WATCH"; screen.scrollOffset = 0f; screen.inStyleSelection = false; return true; }
                }
                
            } else {
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 40 && pMouseY <= 85) {
                    screen.inGunsmith = true;
                    screen.scrollOffset = 0f; 
                    screen.showAmmunitionTab = true; 
                    if (screen.currentWeaponTab == 8) screen.currentWeaponTab = 0; 
                    return true;
                }
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 85 && pMouseY <= 130) {
                    screen.inGunsmith = true;
                    screen.scrollOffset = 0f; 
                    screen.showAmmunitionTab = true; 
                    screen.currentWeaponTab = 8; 
                    return true;
                }
                
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 130 && pMouseY <= 175) {
                    screen.inTacticalSelection = true;
                    screen.scrollOffset = 0f;
                    return true;
                }
                
                if (pMouseY >= 190 && pMouseY <= 265 && pMouseX >= 20 && pMouseX <= 220) {
                    screen.inArmorSelection = true;
                    screen.scrollOffset = 0f;
                    return true;
                }
                
                if (pMouseY >= 285 && pMouseY <= 309 && pMouseX >= 20 && pMouseX <= 220) {
                    screen.inMunitionSelection = true;
                    screen.scrollOffset = 0f;
                    return true;
                }
                
                if (pMouseY >= 330 && pMouseY <= 400 && pMouseX >= 20 && pMouseX <= 220) {
                    screen.inHeadwearSelection = true;
                    screen.scrollOffset = 0f;
                    return true;
                }
            }
            
            int rightPanelX = screen.width;
            if (screen.inCustomizationTab && screen.inCustomizationSelection) {
                boolean isLargeGrid = screen.customizationCategory.equals("SHIRT") || screen.customizationCategory.equals("PANTS") || screen.customizationCategory.equals("ARMOR");
                rightPanelX = screen.width - (isLargeGrid ? 170 : 120);
            }
            if (pMouseX >= 240 && pMouseX < rightPanelX) {
                screen.isDraggingModel = true;
            }
        }
        return false;
    }

    public boolean handleMouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        int rightPanelX = screen.width;
        if (screen.inCustomizationTab && screen.inCustomizationSelection) {
            boolean isLargeGrid = screen.customizationCategory.equals("SHIRT") || screen.customizationCategory.equals("PANTS") || screen.customizationCategory.equals("ARMOR");
            rightPanelX = screen.width - (isLargeGrid ? 170 : 120);
        }

        if (screen.inCustomizationSelection && pMouseX >= rightPanelX) {
            screen.scrollOffset -= (float) pDragY;
            screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));
            return true;
        } else if ((screen.inGunsmith || screen.inWeaponSelection || screen.inAttachmentSelection || screen.inMunitionSelection || screen.inHeadwearSelection || screen.inArmorSelection || screen.inTacticalSelection) && pMouseX < 240 && pMouseY >= 90) {
            screen.scrollOffset -= (float) pDragY;
            screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));
            return true;
        }
        
        if (screen.isDraggingModel && pMouseX < rightPanelX && !screen.inGunsmith && !screen.inWeaponSelection && !screen.inAttachmentSelection && !screen.inMunitionSelection && !screen.inHeadwearSelection && !screen.inArmorSelection && !screen.inCustomizationSelection && !screen.inTacticalSelection) {
            screen.playerRotation += (float) pDragX * 1.5f; 
            return true;
        }
        return false;
    }

    public boolean handleMouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        float scrollSpeed = 25.0f;
        
        int rightPanelX = screen.width;
        if (screen.inCustomizationTab && screen.inCustomizationSelection) {
            boolean isLargeGrid = screen.customizationCategory.equals("SHIRT") || screen.customizationCategory.equals("PANTS") || screen.customizationCategory.equals("ARMOR");
            rightPanelX = screen.width - (isLargeGrid ? 170 : 120);
        }

        if (screen.inCustomizationSelection && pMouseX >= rightPanelX) {
            screen.scrollOffset -= (float) (pDelta * scrollSpeed);
            screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));
            return true;
        } 
        else if ((screen.inGunsmith || screen.inWeaponSelection || screen.inAttachmentSelection || screen.inMunitionSelection || screen.inHeadwearSelection || screen.inArmorSelection || screen.inTacticalSelection) && pMouseX < 240) {
            screen.scrollOffset -= (float) (pDelta * scrollSpeed);
            screen.scrollOffset = Math.max(0f, Math.min(screen.scrollOffset, screen.maxScroll));
            return true;
        }
        return false;
    }
}