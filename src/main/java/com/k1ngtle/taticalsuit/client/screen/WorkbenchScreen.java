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
    private boolean inCustomizationTab = false; 
    private boolean inGunsmith = false; 
    private boolean showAmmunitionTab = true; 
    
    // 0=AR, 1=BR, 2=LMG, 3=PDW, 4=SMG, 5=Shotgun, 6=Sniper, 7=Launcher, 8=Sidearm
    private int currentWeaponTab = 0; 
    
    private boolean inWeaponSelection = false; 
    private boolean inAttachmentSelection = false;
    private boolean inMunitionSelection = false; 
    private boolean inHeadwearSelection = false;
    private boolean inArmorSelection = false; 
    private boolean inCustomizationSelection = false; 
    
    private String editingAttachmentCategory = "";
    private String editingMunitionCategory = "";
    private String expandedHeadwearCategory = ""; 
    private String expandedArmorCategory = ""; 
    private String customizationCategory = "";
    
    // Default Headwear Loadout
    private String selectedHelmet = "HELMET ONLY";
    private String selectedMount = "GPNVGS";
    private String selectedFacewear = "ANTI-FLASH GOGGLES";
    private String selectedPhosphor = "WHITE PHOSPHOR";

    // Default Armor Loadout
    private String selectedVest = "LIGHT ARMOR";
    private String selectedMaterial = "STEEL";
    private String selectedCoverage = "FRONT/BACK";
    private String selectedAmmunitionDeployable = "13 SLOTS";
    
    // Scroll Trackers
    private float scrollOffset = 0f;
    private float maxScroll = 0f;
    
    // Anti-Duplication Security Timer
    private long lastClickTime = 0;

    private ItemStack[] assaultRifleStacks;
    private ItemStack[] battleRifleStacks;
    private ItemStack[] lmgStacks;
    private ItemStack[] pdwStacks;
    private ItemStack[] smgStacks;
    private ItemStack[] shotgunStacks;
    private ItemStack[] sniperRifleStacks;
    private ItemStack[] launcherStacks;
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
        this.inCustomizationTab = false;
        this.inGunsmith = false;
        this.inWeaponSelection = false;
        this.inAttachmentSelection = false;
        this.inMunitionSelection = false;
        this.inHeadwearSelection = false;
        this.inArmorSelection = false;
        this.inCustomizationSelection = false;
        
        this.editingMunitionCategory = "";
        this.expandedHeadwearCategory = "";
        this.expandedArmorCategory = "";
        this.customizationCategory = "";
        
        this.showAmmunitionTab = true;
        this.currentWeaponTab = 0;
        this.scrollOffset = 0f;

        this.assaultRifleStacks = resolveStacks(WorkbenchData.ASSAULT_RIFLE_IDS, "WEAPON");
        this.battleRifleStacks = resolveStacks(WorkbenchData.BATTLE_RIFLE_IDS, "WEAPON");
        this.lmgStacks = resolveStacks(WorkbenchData.LMG_IDS, "WEAPON");
        this.pdwStacks = resolveStacks(WorkbenchData.PDW_IDS, "WEAPON");
        this.smgStacks = resolveStacks(WorkbenchData.SMG_IDS, "WEAPON");
        this.shotgunStacks = resolveStacks(WorkbenchData.SHOTGUN_IDS, "WEAPON");
        this.sniperRifleStacks = resolveStacks(WorkbenchData.SNIPER_RIFLE_IDS, "WEAPON");
        this.launcherStacks = resolveStacks(WorkbenchData.LAUNCHER_IDS, "WEAPON");
        this.sidearmWeaponStacks = resolveStacks(WorkbenchData.SIDEARM_WEAPON_IDS, "WEAPON");
    }

    private ItemStack[] resolveStacks(String[] ids, String category) {
        ItemStack[] stacks = new ItemStack[ids.length];
        
        String[] keywords;
        switch (category.toUpperCase()) {
            case "OPTIC": keywords = new String[]{"scope", "sight", "optic", "reflex", "holo", "acog", "dot", "rmr", "sro", "micro", "deltapoint", "moa", "delta"}; break; 
            case "UNDERBARREL": keywords = new String[]{"grip", "underbarrel", "foregrip", "bipod", "angled"}; break;
            case "BARREL": keywords = new String[]{"barrel", "handguard", "choke"}; break;
            case "MUZZLE": keywords = new String[]{"muzzle", "silencer", "suppressor", "compensator", "flash", "osprey", "omega", "ti_rant", "rotor"}; break;
            case "LASER": keywords = new String[]{"laser", "tactical", "light", "peq", "flashlight", "tlr", "x300", "surefire", "m600"}; break;
            default: keywords = new String[]{category.toLowerCase()}; break;
        }

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
                ItemStack bestMatch = ItemStack.EMPTY;
                ItemStack fallback = ItemStack.EMPTY;
                int longestMatch = 0;
                String targetId = ids[i].toLowerCase().replace("pointblank:", "");
                
                for (net.minecraft.world.item.Item regItem : net.minecraftforge.registries.ForgeRegistries.ITEMS) {
                    net.minecraft.resources.ResourceLocation regLoc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(regItem);
                    if (regLoc != null && "pointblank".equals(regLoc.getNamespace())) {
                        String path = regLoc.getPath().toLowerCase();
                        
                        boolean matchesCategory = false;
                        if (category.equals("WEAPON")) {
                            matchesCategory = true;
                        } else {
                            for (String kw : keywords) {
                                if (path.contains(kw)) {
                                    matchesCategory = true;
                                    break;
                                }
                            }
                        }

                        if (matchesCategory) {
                            if (fallback.isEmpty()) fallback = new ItemStack(regItem); 
                            
                            if (path.equals(targetId)) {
                                bestMatch = new ItemStack(regItem);
                                break;
                            }

                            if (targetId.contains(path) || path.contains(targetId)) {
                                if (path.length() > longestMatch) {
                                    longestMatch = path.length();
                                    bestMatch = new ItemStack(regItem);
                                }
                            }
                        }
                    }
                }
                stacks[i] = !bestMatch.isEmpty() ? bestMatch : fallback;
            }
        }
        return stacks;
    }

    private String[] getActiveWeaponPool() {
        return switch (this.currentWeaponTab) {
            case 0 -> WorkbenchData.ASSAULT_RIFLE_IDS;
            case 1 -> WorkbenchData.BATTLE_RIFLE_IDS;
            case 2 -> WorkbenchData.LMG_IDS;
            case 3 -> WorkbenchData.PDW_IDS;
            case 4 -> WorkbenchData.SMG_IDS;
            case 5 -> WorkbenchData.SHOTGUN_IDS;
            case 6 -> WorkbenchData.SNIPER_RIFLE_IDS;
            case 7 -> WorkbenchData.LAUNCHER_IDS;
            case 8 -> WorkbenchData.SIDEARM_WEAPON_IDS;
            default -> WorkbenchData.ASSAULT_RIFLE_IDS;
        };
    }

    private ItemStack[] getActiveWeaponStacks() {
        return switch (this.currentWeaponTab) {
            case 0 -> assaultRifleStacks;
            case 1 -> battleRifleStacks;
            case 2 -> lmgStacks;
            case 3 -> pdwStacks;
            case 4 -> smgStacks;
            case 5 -> shotgunStacks;
            case 6 -> sniperRifleStacks;
            case 7 -> launcherStacks;
            case 8 -> sidearmWeaponStacks;
            default -> assaultRifleStacks;
        };
    }

    private String[] getActiveAttachmentPool() {
        boolean isSidearm = this.currentWeaponTab == 8;
        
        return switch (this.editingAttachmentCategory) {
            case "OPTIC" -> isSidearm ? WorkbenchData.SIDEARM_OPTIC_IDS : WorkbenchData.OPTIC_IDS;
            case "BARREL" -> isSidearm ? WorkbenchData.BARREL_IDS : WorkbenchData.BARREL_IDS; 
            case "MUZZLE" -> isSidearm ? WorkbenchData.SIDEARM_MUZZLE_IDS : WorkbenchData.MUZZLE_IDS;
            case "UNDERBARREL" -> isSidearm ? new String[]{"NONE"} : WorkbenchData.UNDERBARREL_IDS; 
            case "LASER" -> isSidearm ? new String[]{"NONE"} : WorkbenchData.LASER_IDS;
            case "STOCK" -> isSidearm ? WorkbenchData.SIDEARM_STOCK_IDS : new String[]{"NONE"};
            default -> new String[]{"NONE"};
        };
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton != 0) return super.mouseClicked(pMouseX, pMouseY, pButton);
        
        // --- CUSTOMIZATION GRID SELECTION LOGIC ---
        if (this.inCustomizationTab && this.inCustomizationSelection) {
            boolean isLargeGrid = this.customizationCategory.equals("SHIRT") || this.customizationCategory.equals("PANTS") || this.customizationCategory.equals("ARMOR");
            int cols = isLargeGrid ? 3 : 2;
            int rows = isLargeGrid ? 7 : 6;
            
            int panelWidth = isLargeGrid ? 170 : 120;
            int panelX = this.width - panelWidth;

            if (pMouseX < panelX) {
                // Clicked outside the right-side grid panel, close it
                this.inCustomizationSelection = false;
                this.scrollOffset = 0f;
                return true;
            }

            int gridStartX = panelX + 15;
            int startY = 50 - (int)this.scrollOffset;
            
            for (int i = 0; i < (rows * cols); i++) {
                int col = i % cols;
                int row = i / cols;
                int boxX = gridStartX + (col * 45); // 40px box + 5px gap
                int boxY = startY + (row * 45);
                
                if (pMouseX >= boxX && pMouseX <= boxX + 40 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                    if (System.currentTimeMillis() - this.lastClickTime < 500) return true;
                    this.lastClickTime = System.currentTimeMillis();
                    
                    // Future equipped logic here
                    
                    this.inCustomizationSelection = false;
                    this.scrollOffset = 0f;
                    return true;
                }
            }
            return true;
        }

        if (this.inArmorSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                this.inArmorSelection = false;
                this.scrollOffset = 0f;
                return true;
            }

            int currentY = 100 - (int)this.scrollOffset;
            
            // --- VEST LOGIC ---
            currentY += 20; 
            String[] vestList = {"NO ARMOR", "LIGHT ARMOR", "HEAVY ARMOR", "STAB VEST"};
            int vestDropdownY = currentY;
            if (this.expandedArmorCategory.equals("VEST")) {
                int listY = vestDropdownY;
                for (String item : vestList) {
                    if (pMouseY >= listY && pMouseY <= listY + 30 && pMouseX >= 20 && pMouseX <= 220) {
                        this.selectedVest = item;
                        this.expandedArmorCategory = "";
                        return true;
                    }
                    listY += 35;
                }
                
                // Block clicks to elements underneath if dropdown is open
                if (pMouseY >= vestDropdownY && pMouseY <= listY && pMouseX >= 20 && pMouseX <= 220) {
                    return true;
                }
            } else {
                if (pMouseY >= currentY && pMouseY <= currentY + 30 && pMouseX >= 20 && pMouseX <= 220) {
                    this.expandedArmorCategory = "VEST";
                    return true;
                }
            }
            currentY += 45;
            
            // --- COVERAGE LOGIC ---
            currentY += 20;
            if (pMouseY >= currentY && pMouseY <= currentY + 30) {
                String[] covList = {"NONE", "FRONT", "FRONT/BACK", "FULL"};
                for(int i = 0; i < 4; i++) {
                    int boxX = 20 + (i * 50);
                    if (pMouseX >= boxX && pMouseX <= boxX + 45) {
                        this.selectedCoverage = covList[i];
                        return true;
                    }
                }
            }
            currentY += 40;
            
            // --- MATERIAL LOGIC ---
            currentY += 20; 
            if (pMouseY >= currentY && pMouseY <= currentY + 30) {
                String[] matList = {"KEVLAR", "STEEL", "CERAMIC"};
                for(int i = 0; i < 3; i++) {
                    int boxX = 20 + (i * 66);
                    if (pMouseX >= boxX && pMouseX <= boxX + 60) {
                        this.selectedMaterial = matList[i];
                        return true;
                    }
                }
            }
            currentY += 40; 

            // --- AMMUNITION & DEPLOYABLE TABS LOGIC ---
            if (pMouseY >= currentY && pMouseY <= currentY + 20) {
                if (pMouseX >= 20 && pMouseX <= 110) {
                    this.showAmmunitionTab = true;
                    return true;
                } else if (pMouseX > 110 && pMouseX <= 220) {
                    this.showAmmunitionTab = false;
                    return true;
                }
            }
            
            return true;
        } else if (this.inHeadwearSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                this.inHeadwearSelection = false;
                this.scrollOffset = 0f;
                return true;
            }

            int currentY = 100 - (int)this.scrollOffset;
            
            // --- HELMET LOGIC ---
            if (pMouseY >= currentY && pMouseY <= currentY + 40 && pMouseX >= 20 && pMouseX <= 220) {
                this.expandedHeadwearCategory = this.expandedHeadwearCategory.equals("HELMET") ? "" : "HELMET";
                return true;
            }
            currentY += 45;
            
            if (this.expandedHeadwearCategory.equals("HELMET")) {
                String[] list = {"NO HELMET", "HELMET ONLY"};
                for (String item : list) {
                    if (pMouseY >= currentY && pMouseY <= currentY + 30 && pMouseX >= 20 && pMouseX <= 220) {
                        this.selectedHelmet = item;
                        this.expandedHeadwearCategory = "";
                        return true;
                    }
                    currentY += 35;
                }
            }
            
            // --- MOUNT LOGIC ---
            if (pMouseY >= currentY && pMouseY <= currentY + 40 && pMouseX >= 20 && pMouseX <= 220) {
                this.expandedHeadwearCategory = this.expandedHeadwearCategory.equals("MOUNT") ? "" : "MOUNT";
                return true;
            }
            currentY += 45;
            
            if (this.expandedHeadwearCategory.equals("MOUNT")) {
                String[] list = {"NONE", "NVGS", "GPNVGS"};
                for (String item : list) {
                    if (pMouseY >= currentY && pMouseY <= currentY + 30 && pMouseX >= 20 && pMouseX <= 220) {
                        this.selectedMount = item;
                        this.expandedHeadwearCategory = "";
                        return true;
                    }
                    currentY += 35;
                }
            }
            
            if (!this.selectedMount.equals("NONE")) {
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 120) {
                        this.selectedPhosphor = "GREEN PHOSPHOR";
                        return true;
                    } else if (pMouseX > 120 && pMouseX <= 220) {
                        this.selectedPhosphor = "WHITE PHOSPHOR";
                        return true;
                    }
                }
                currentY += 45;
            }
            
            // --- FACEWEAR LOGIC ---
            if (pMouseY >= currentY && pMouseY <= currentY + 40 && pMouseX >= 20 && pMouseX <= 220) {
                this.expandedHeadwearCategory = this.expandedHeadwearCategory.equals("FACEWEAR") ? "" : "FACEWEAR";
                return true;
            }
            currentY += 45;
            
            if (this.expandedHeadwearCategory.equals("FACEWEAR")) {
                String[] list = {"NONE", "GOGGLES", "GAS MASK"};
                for (String item : list) {
                    if (pMouseY >= currentY && pMouseY <= currentY + 30 && pMouseX >= 20 && pMouseX <= 220) {
                        this.selectedFacewear = item;
                        this.expandedHeadwearCategory = "";
                        return true;
                    }
                    currentY += 35;
                }
            }
            
            return true;
        } else if (this.inMunitionSelection) {
            if (pMouseX >= 20 && pMouseX <= 100 && pMouseY >= 15 && pMouseY <= 35) {
                this.inMunitionSelection = false;
                this.scrollOffset = 0f;
                return true;
            }

            if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 90) {
                if (System.currentTimeMillis() - this.lastClickTime < 500) return true;
                this.lastClickTime = System.currentTimeMillis();

                this.inMunitionSelection = false; 
                this.scrollOffset = 0f;
                return true;
            }
            return true;
        } else if (this.inAttachmentSelection) {
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

                    int menuSlotIndex = (this.currentWeaponTab == 8) ? 1 : 0; 

                    String vpbCategory = switch (this.editingAttachmentCategory) {
                        case "OPTIC"       -> "scope";
                        case "BARREL"      -> "barrel";
                        case "MUZZLE"      -> "muzzle";
                        case "UNDERBARREL" -> "underbarrel";
                        case "LASER"       -> "rail";
                        case "STOCK"       -> "stock";
                        case "MAGAZINE"    -> "magazine";
                        default -> this.editingAttachmentCategory.toLowerCase();
                    };

                    ItemStack currentWeapon = this.menu.getSlot(menuSlotIndex).getItem().copy();
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
                                net.minecraft.world.item.Item eItem = net.minecraftforge.registries.ForgeRegistries.ITEMS
                                        .getValue(new net.minecraft.resources.ResourceLocation(eid.isEmpty() ? "minecraft:air" : eid));
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
                                net.minecraft.world.item.Item eItem = net.minecraftforge.registries.ForgeRegistries.ITEMS
                                        .getValue(new net.minecraft.resources.ResourceLocation(eid.isEmpty() ? "minecraft:air" : eid));
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
                        this.menu.getSlot(menuSlotIndex).set(currentWeapon);
                    }

                    com.k1ngtle.taticalsuit.network.ModNetworking.CHANNEL.sendToServer(
                            new com.k1ngtle.taticalsuit.network.EquipWeaponPacket(menuSlotIndex, idPool[i], true, vpbCategory)
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

            // --- 8 HORIZONTAL TAB SCROLLER FOR PRIMARIES ---
            if (pMouseY >= 70 && pMouseY <= 85 && this.currentWeaponTab != 8) {
                int currentX = 8;
                int[] tabWidths = {20, 20, 25, 25, 25, 38, 35, 44};
                for (int i = 0; i < 8; i++) {
                    int tabWidth = tabWidths[i];
                    if (pMouseX >= currentX && pMouseX <= currentX + tabWidth - 1) {
                        this.currentWeaponTab = i;
                        this.scrollOffset = 0f;
                        return true;
                    }
                    currentX += tabWidth;
                }
            }

            String[] idPool = getActiveWeaponPool();
            int startY = 100 - (int)this.scrollOffset;
            
            for (int i = 0; i < idPool.length; i++) {
                int boxY = startY + (i * 45);
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= boxY && pMouseY <= boxY + 40) {
                    if (System.currentTimeMillis() - this.lastClickTime < 500) return true;
                    this.lastClickTime = System.currentTimeMillis();
                    
                    ItemStack currentEquipped = (this.currentWeaponTab == 8) ? getDisplayedSidearm() : getDisplayedPrimary();
                    if (!currentEquipped.isEmpty()) {
                        net.minecraft.resources.ResourceLocation currentLoc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(currentEquipped.getItem());
                        if (currentLoc != null && currentLoc.toString().equals(idPool[i])) {
                            this.inWeaponSelection = false; 
                            this.scrollOffset = 0f;
                            return true;
                        }
                    }

                    int menuSlotIndex = (this.currentWeaponTab == 8) ? 1 : 0; 

                    ItemStack optimisticStack = ItemStack.EMPTY;
                    if (Minecraft.getInstance().player != null) {
                        for (int j = 0; j < Minecraft.getInstance().player.getInventory().getContainerSize(); j++) {
                            ItemStack invStack = Minecraft.getInstance().player.getInventory().getItem(j);
                            if (!invStack.isEmpty()) {
                                net.minecraft.resources.ResourceLocation invLoc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(invStack.getItem());
                                if (invLoc != null && invLoc.toString().equals(idPool[i])) {
                                    optimisticStack = invStack.copy();
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (optimisticStack.isEmpty()) {
                        net.minecraft.world.item.Item newItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(idPool[i]));
                        if (newItem != null && newItem != net.minecraft.world.item.Items.AIR) {
                            optimisticStack = newItem.getDefaultInstance().copy();
                        }
                    }
                    
                    if (!optimisticStack.isEmpty()) {
                        this.menu.getSlot(menuSlotIndex).set(optimisticStack);
                    }

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

            // --- RESTORED: PRIMARY vs SIDE ARM TOGGLE ---
            if (pMouseY >= 70 && pMouseY <= 90) {
                if (pMouseX >= 20 && pMouseX <= 90) {
                    if (this.currentWeaponTab == 8) this.currentWeaponTab = 0; // Default back to AR
                    this.scrollOffset = 0f; 
                    return true;
                } else if (pMouseX > 90 && pMouseX <= 180) {
                    this.currentWeaponTab = 8; // Lock onto sidearm
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

            int numCoreAttachments = (this.currentWeaponTab == 8) ? 3 : 5;
            String[] boxCats = (this.currentWeaponTab == 8) 
                    ? new String[]{"OPTIC", "MUZZLE", "STOCK"} 
                    : new String[]{"OPTIC", "BARREL", "MUZZLE", "UNDERBARREL", "LASER"};

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
            // Main Top Tabs Interception
            if (pMouseY >= 4 && pMouseY <= 16) {
                int loadoutWidth = this.font.width("LOADOUT");
                int customX = 20 + loadoutWidth + this.font.width(" / ");
                if (pMouseX >= 20 && pMouseX <= 20 + loadoutWidth) {
                    this.inCustomizationTab = false;
                    this.inCustomizationSelection = false;
                    return true;
                } else if (pMouseX >= customX && pMouseX <= customX + this.font.width("CUSTOMIZATION")) {
                    this.inCustomizationTab = true;
                    this.inCustomizationSelection = false;
                    return true;
                }
            }

            if (this.inCustomizationTab) {
                int startY = 30;
                int currentY = startY + 15;
                
                // UNIFORM
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 115) { this.inCustomizationSelection = true; this.customizationCategory = "SHIRT"; this.scrollOffset = 0f; return true; }
                    if (pMouseX >= 125 && pMouseX <= 220) { this.inCustomizationSelection = true; this.customizationCategory = "PANTS"; this.scrollOffset = 0f; return true; }
                }
                currentY += 45;
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 80) { this.inCustomizationSelection = true; this.customizationCategory = "GLOVES"; this.scrollOffset = 0f; return true; }
                    if (pMouseX >= 90 && pMouseX <= 150) { this.inCustomizationSelection = true; this.customizationCategory = "BOOTS"; this.scrollOffset = 0f; return true; }
                    if (pMouseX >= 160 && pMouseX <= 220) { this.inCustomizationSelection = true; this.customizationCategory = "BELT"; this.scrollOffset = 0f; return true; }
                }
                
                // TACTICAL GEAR
                currentY += 60; 
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 115) { this.inCustomizationSelection = true; this.customizationCategory = "ARMOR"; this.scrollOffset = 0f; return true; }
                    if (pMouseX >= 125 && pMouseX <= 220) { this.inCustomizationSelection = true; this.customizationCategory = "HELMET"; this.scrollOffset = 0f; return true; }
                }
                currentY += 45;
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 80) { this.inCustomizationSelection = true; this.customizationCategory = "FACEWEAR"; this.scrollOffset = 0f; return true; }
                    if (pMouseX >= 90 && pMouseX <= 150) { this.inCustomizationSelection = true; this.customizationCategory = "NVG"; this.scrollOffset = 0f; return true; }
                    if (pMouseX >= 160 && pMouseX <= 220) { this.inCustomizationSelection = true; this.customizationCategory = "BALLISTIC MASK"; this.scrollOffset = 0f; return true; }
                }
                
                // ACCESSORIES
                currentY += 60;
                if (pMouseY >= currentY && pMouseY <= currentY + 40) {
                    if (pMouseX >= 20 && pMouseX <= 80) { this.inCustomizationSelection = true; this.customizationCategory = "TATTOO"; this.scrollOffset = 0f; return true; }
                    if (pMouseX >= 90 && pMouseX <= 150) { this.inCustomizationSelection = true; this.customizationCategory = "EYEWEAR"; this.scrollOffset = 0f; return true; }
                    if (pMouseX >= 160 && pMouseX <= 220) { this.inCustomizationSelection = true; this.customizationCategory = "WATCH"; this.scrollOffset = 0f; return true; }
                }
                
            } else {
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 40 && pMouseY <= 85) {
                    this.inGunsmith = true;
                    this.scrollOffset = 0f; 
                    this.showAmmunitionTab = true; 
                    if (this.currentWeaponTab == 8) this.currentWeaponTab = 0; // Return to primary if currently sidearm
                    return true;
                }
                if (pMouseX >= 20 && pMouseX <= 220 && pMouseY >= 85 && pMouseY <= 130) {
                    this.inGunsmith = true;
                    this.scrollOffset = 0f; 
                    this.showAmmunitionTab = true; 
                    this.currentWeaponTab = 8; // Instantly lock onto Sidearm category
                    return true;
                }
                
                // Trigger Armor Selection Tab
                if (pMouseY >= 190 && pMouseY <= 265 && pMouseX >= 20 && pMouseX <= 220) {
                    this.inArmorSelection = true;
                    this.scrollOffset = 0f;
                    return true;
                }
                
                // Trigger Munition Selection Tab
                if (pMouseY >= 285 && pMouseY <= 309 && pMouseX >= 20 && pMouseX <= 220) {
                    this.inMunitionSelection = true;
                    this.scrollOffset = 0f;
                    return true;
                }
                
                // Trigger Headwear Selection Tab
                if (pMouseY >= 330 && pMouseY <= 400 && pMouseX >= 20 && pMouseX <= 220) {
                    this.inHeadwearSelection = true;
                    this.scrollOffset = 0f;
                    return true;
                }
            }
            
            // Allow drag rotation of the model only if clicking the correct active free-space
            int rightPanelX = this.width;
            if (this.inCustomizationTab && this.inCustomizationSelection) {
                boolean isLargeGrid = this.customizationCategory.equals("SHIRT") || this.customizationCategory.equals("PANTS") || this.customizationCategory.equals("ARMOR");
                rightPanelX = this.width - (isLargeGrid ? 170 : 120);
            }
            if (pMouseX >= 240 && pMouseX < rightPanelX) {
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
        int rightPanelX = this.width;
        if (this.inCustomizationTab && this.inCustomizationSelection) {
            boolean isLargeGrid = this.customizationCategory.equals("SHIRT") || this.customizationCategory.equals("PANTS") || this.customizationCategory.equals("ARMOR");
            rightPanelX = this.width - (isLargeGrid ? 170 : 120);
        }

        if (this.inCustomizationSelection && pMouseX >= rightPanelX) {
            this.scrollOffset -= (float) pDragY;
            this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));
            return true;
        } else if ((this.inGunsmith || this.inWeaponSelection || this.inAttachmentSelection || this.inMunitionSelection || this.inHeadwearSelection || this.inArmorSelection) && pMouseX < 240 && pMouseY >= 90) {
            this.scrollOffset -= (float) pDragY;
            this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));
            return true;
        }
        
        if (this.isDraggingModel && pMouseX < rightPanelX && !this.inGunsmith && !this.inWeaponSelection && !this.inAttachmentSelection && !this.inMunitionSelection && !this.inHeadwearSelection && !this.inArmorSelection && !this.inCustomizationSelection) {
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
        } else if (this.inArmorSelection) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707); 
            this.renderArmorSelectionBg(guiGraphics, this.width, this.height);
            this.renderArmorSelectionLabels(guiGraphics, mouseX, mouseY, this.width, this.height);
        } else if (this.inMunitionSelection) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707); 
            this.renderMunitionSelectionBg(guiGraphics, this.height);
            this.renderMunitionSelectionLabels(guiGraphics, mouseX, mouseY, this.width, this.height);
        } else if (this.inHeadwearSelection) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707); 
            this.renderHeadwearSelectionBg(guiGraphics, this.width, this.height);
            this.renderHeadwearSelectionLabels(guiGraphics, mouseX, mouseY, this.width, this.height);
        } else if (this.inGunsmith) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707); 
            this.renderGunsmithBg(guiGraphics, this.height);
            this.renderGunsmithLabels(guiGraphics);
        } else {
            int renderMouseX = mouseX;
            int renderMouseY = mouseY;
            
            // Hide vanilla slot hover highlight for the invisible right-side slots and bottom left layout
            if (!this.inGunsmith && !this.inWeaponSelection && !this.inAttachmentSelection && !this.inMunitionSelection && !this.inHeadwearSelection && !this.inArmorSelection) {
                if (this.inCustomizationTab) {
                    // Hide all slot hovers when in customization tab
                    if (mouseX < 240) {
                        renderMouseX = -999;
                        renderMouseY = -999;
                    }
                } else {
                    if (mouseX >= 165 && mouseX <= 195 && mouseY >= 35 && mouseY <= 165) {
                        renderMouseX = -999;
                        renderMouseY = -999;
                    } else if (mouseX < 240 && mouseY >= 190) {
                        // Hide vanilla gray highlights for Armor, Munition, and Headwear slot sections
                        renderMouseX = -999;
                        renderMouseY = -999;
                    }
                }
            }
            
            super.render(guiGraphics, renderMouseX, renderMouseY, delta);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF070707);
        if (this.inCustomizationTab) {
            renderCustomizationBg(guiGraphics, this.width, this.height, mouseX, mouseY);
            if (this.inCustomizationSelection) {
                renderCustomizationGridBg(guiGraphics, this.width, this.height, mouseX, mouseY);
            }
        } else {
            renderLoadoutBg(guiGraphics, this.width, this.height, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!this.inGunsmith && !this.inWeaponSelection && !this.inAttachmentSelection && !this.inMunitionSelection && !this.inHeadwearSelection && !this.inArmorSelection) {
            
            int loadoutColor = !this.inCustomizationTab ? 0xFFFFFFFF : 0xFF7A818C;
            int customColor = this.inCustomizationTab ? 0xFFFFFFFF : 0xFF7A818C;
            
            guiGraphics.drawString(this.font, "LOADOUT", 20, 6, loadoutColor, false);
            
            int slashX = 20 + this.font.width("LOADOUT") + 4;
            guiGraphics.drawString(this.font, "/", slashX, 6, 0xFF555555, false);
            
            int customX = slashX + this.font.width("/") + 4;
            guiGraphics.drawString(this.font, "CUSTOMIZATION", customX, 6, customColor, false);
            
            if (this.inCustomizationTab) {
                renderCustomizationLabels(guiGraphics, mouseX, mouseY);
                if (this.inCustomizationSelection) {
                    renderCustomizationGridLabels(guiGraphics, mouseX, mouseY, this.width, this.height);
                }
            } else {
                renderLoadoutLabels(guiGraphics);
            }
        }
    }

    private void render3DOperator(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        if (Minecraft.getInstance().player != null) {
            
            int rightBound = trueWidth;
            
            // If the customization side grid is open, dynamically shift the operator left to stay centered!
            if (this.inCustomizationTab && this.inCustomizationSelection) {
                boolean isLargeGrid = this.customizationCategory.equals("SHIRT") || this.customizationCategory.equals("PANTS") || this.customizationCategory.equals("ARMOR");
                int panelWidth = isLargeGrid ? 170 : 120;
                rightBound -= panelWidth;
            }
            
            int openSpaceCenter = 240 + (rightBound - 240) / 2; 
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

    private void renderCustomizationBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, 240, trueHeight, 0xFF121212);
        guiGraphics.fill(20, 16, 220, 18, 0xFFD62929);

        int startY = 30;
        
        // UNIFORM
        int currentY = startY + 15;
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, currentY, 95, 40);
        WorkbenchDesign.drawCleanBox(guiGraphics, 125, currentY, 95, 40);
        currentY += 45;
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, currentY, 60, 40);
        WorkbenchDesign.drawCleanBox(guiGraphics, 90, currentY, 60, 40);
        WorkbenchDesign.drawCleanBox(guiGraphics, 160, currentY, 60, 40);
        
        // TACTICAL GEAR
        currentY += 60;
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, currentY, 95, 40);
        WorkbenchDesign.drawCleanBox(guiGraphics, 125, currentY, 95, 40);
        currentY += 45;
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, currentY, 60, 40);
        WorkbenchDesign.drawCleanBox(guiGraphics, 90, currentY, 60, 40);
        WorkbenchDesign.drawCleanBox(guiGraphics, 160, currentY, 60, 40);
        
        // ACCESSORIES
        currentY += 60;
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, currentY, 60, 40);
        WorkbenchDesign.drawCleanBox(guiGraphics, 90, currentY, 60, 40);
        WorkbenchDesign.drawCleanBox(guiGraphics, 160, currentY, 60, 40);

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderCustomizationGridBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight, int mouseX, int mouseY) {
        boolean isLargeGrid = this.customizationCategory.equals("SHIRT") || this.customizationCategory.equals("PANTS") || this.customizationCategory.equals("ARMOR");
        int cols = isLargeGrid ? 3 : 2;
        int rows = isLargeGrid ? 7 : 6;
        
        int panelWidth = isLargeGrid ? 170 : 120;
        int panelX = trueWidth - panelWidth;
        int gridStartX = panelX + 15;
        int startY = 50 - (int)this.scrollOffset;
        
        int listHeight = (rows * 45); 
        int visibleHeight = trueHeight - 50;
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        // Draw solid right-side panel
        guiGraphics.fill(panelX, 0, trueWidth, trueHeight, 0xFF121212);
        guiGraphics.fill(panelX, 16, trueWidth, 18, 0xFFD62929);

        guiGraphics.enableScissor(panelX, 40, trueWidth, trueHeight);
        
        for (int i = 0; i < (rows * cols); i++) {
            int col = i % cols;
            int row = i / cols;
            int boxX = gridStartX + (col * 45);
            int boxY = startY + (row * 45);
            
            WorkbenchDesign.drawCleanBox(guiGraphics, boxX, boxY, 40, 40);
        }
        
        if (this.maxScroll > 0) {
            int scrollX = trueWidth - 10;
            guiGraphics.fill(scrollX, 50, scrollX + 2, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 50 + (int)((this.scrollOffset / this.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(scrollX - 1, thumbY, scrollX + 3, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();
    }

    private void renderCustomizationLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startY = 30;
        
        // UNIFORM
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "UNIFORM", 20, startY, 0.65f, 0xFFAAAAAA);
        int currentY = startY + 15;
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SHIRT", 24, currentY + 4, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "PANTS", 129, currentY + 4, 0.45f, 0xFF7A818C);
        currentY += 45;
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "GLOVES", 24, currentY + 4, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "BOOTS", 94, currentY + 4, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "BELT", 164, currentY + 4, 0.45f, 0xFF7A818C);
        
        // TACTICAL GEAR
        currentY += 60;
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "TACTICAL GEAR", 20, currentY - 15, 0.65f, 0xFFAAAAAA);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "ARMOR", 24, currentY + 4, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "HELMET", 129, currentY + 4, 0.45f, 0xFF7A818C);
        currentY += 45;
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "FACEWEAR", 24, currentY + 4, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "NVG", 94, currentY + 4, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "BALLISTIC MASK", 164, currentY + 4, 0.45f, 0xFF7A818C);
        
        // ACCESSORIES
        currentY += 60;
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "ACCESSORIES", 20, currentY - 15, 0.65f, 0xFFAAAAAA);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "TATTOO", 24, currentY + 4, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "EYEWEAR", 94, currentY + 4, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "WATCH", 164, currentY + 4, 0.45f, 0xFF7A818C);
    }

    private void renderCustomizationGridLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        boolean isLargeGrid = this.customizationCategory.equals("SHIRT") || this.customizationCategory.equals("PANTS") || this.customizationCategory.equals("ARMOR");
        int cols = isLargeGrid ? 3 : 2;
        int rows = isLargeGrid ? 7 : 6;
        
        int panelWidth = isLargeGrid ? 170 : 120;
        int panelX = trueWidth - panelWidth;
        int gridStartX = panelX + 15;
        int startY = 50 - (int)this.scrollOffset;
        
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SELECT " + this.customizationCategory, panelX + 15, 25, 0.75f, 0xFFFFFFFF);
        
        guiGraphics.enableScissor(panelX, 40, trueWidth, trueHeight);
        for (int i = 0; i < (rows * cols); i++) {
            int col = i % cols;
            int row = i / cols;
            int boxX = gridStartX + (col * 45);
            int boxY = startY + (row * 45);
            
            if (mouseX >= boxX && mouseX <= boxX + 40 && mouseY >= boxY && mouseY <= boxY + 40) {
                guiGraphics.fill(boxX + 1, boxY + 1, boxX + 39, boxY + 39, 0xFF3E4249);
            }
        }
        guiGraphics.disableScissor();
    }

    private void renderLoadoutBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, 240, trueHeight, 0xFF121212);
        guiGraphics.fill(20, 16, 220, 18, 0xFFD62929);

        WorkbenchDesign.drawCleanBox(guiGraphics, 20, 40, 200, 45);  
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, 85, 200, 45);  
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, 130, 200, 45); 

        WorkbenchDesign.drawCleanBox(guiGraphics, 20, 205, 80, 55);  
        WorkbenchDesign.drawCleanBox(guiGraphics, 100, 205, 120, 55); 
        guiGraphics.fill(100, 232, 220, 233, 0xFF2E3136); 

        // Removed the drawCleanBox generation loop for munition slots, leaving only vertical dividers
        guiGraphics.fill(20, 309, 120, 317, 0xFF2E3136); 
        guiGraphics.fill(123, 285, 124, 317, 0xFF2E3136); 

        guiGraphics.fill(127, 309, 187, 317, 0xFF2E3136); 
        guiGraphics.fill(190, 285, 191, 317, 0xFF2E3136); 

        guiGraphics.fill(194, 309, 214, 317, 0xFF2E3136); 

        WorkbenchDesign.drawCleanBox(guiGraphics, 20, 345, 80, 55);  
        WorkbenchDesign.drawCleanBox(guiGraphics, 100, 345, 120, 55); 
        guiGraphics.fill(100, 372, 220, 373, 0xFF2E3136); 

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderArmorSelectionBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        int visibleHeight = trueHeight - 100;
        
        int currentY = 0;
        currentY += 20; // Vest header gap
        currentY += 3 * 35; // Vest list
        
        currentY += 20; // Coverage header
        currentY += 40; // Coverage boxes
        
        currentY += 20; // Material header
        currentY += 40; // Material boxes
        
        currentY += 20; // Ammo tabs
        int dynamicItemsHeight = this.showAmmunitionTab 
                ? (16 + (2 * 31) + 10 + 16 + (2 * 31)) 
                : (16 + (4 * 31) + 10 + 16 + (5 * 31));
        currentY += dynamicItemsHeight;
        
        int listHeight = currentY + 20;
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        guiGraphics.fill(0, 0, 240, trueHeight, 0xFF121212);
        guiGraphics.fill(20, 16, 220, 18, 0xFFD62929);

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int drawY = 100 - (int)this.scrollOffset;
        
        // VEST
        drawY += 20;
        drawY += 3 * 35;
        
        // COVERAGE Boxes
        drawY += 20;
        for(int i = 0; i < 4; i++) {
            WorkbenchDesign.drawCleanBox(guiGraphics, 20 + (i * 50), drawY, 45, 30);
        }
        drawY += 40;
        
        // MATERIAL Boxes
        drawY += 20;
        for(int i = 0; i < 3; i++) {
            WorkbenchDesign.drawCleanBox(guiGraphics, 20 + (i * 66), drawY, 60, 30);
        }
        drawY += 40;

        // AMMUNITION & DEPLOYABLE BG (Mirrored from Gunsmith)
        guiGraphics.fill(20, drawY + 14, 220, drawY + 15, 0xFF2E3136); 
        if (this.showAmmunitionTab) {
            guiGraphics.fill(20, drawY + 14, 110, drawY + 15, 0xFFD62929); 
        } else {
            guiGraphics.fill(110, drawY + 14, 220, drawY + 15, 0xFFD62929); 
        }
        drawY += 20; 
        
        if (this.showAmmunitionTab) {
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
        
        if (this.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((this.scrollOffset / this.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderHeadwearSelectionBg(GuiGraphics guiGraphics, int trueWidth, int trueHeight) {
        int visibleHeight = trueHeight - 100;
        
        int currentY = 0;
        currentY += 45; // Helmet box
        if (this.expandedHeadwearCategory.equals("HELMET")) currentY += 2 * 35;
        
        currentY += 45; // Mount box
        if (this.expandedHeadwearCategory.equals("MOUNT")) currentY += 3 * 35;
        if (!this.selectedMount.equals("NONE")) {
            currentY += 45; // Phosphor boxes directly underneath
        }
        
        currentY += 45; // Facewear box
        if (this.expandedHeadwearCategory.equals("FACEWEAR")) currentY += 3 * 35;
        
        int listHeight = currentY + 20;
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        guiGraphics.fill(0, 0, 240, trueHeight, 0xFF121212);
        guiGraphics.fill(20, 16, 220, 18, 0xFFD62929);

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int drawY = 100 - (int)this.scrollOffset;
        
        // HELMET Box
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, drawY, 200, 40);
        drawY += 45;
        if (this.expandedHeadwearCategory.equals("HELMET")) drawY += 2 * 35;
        
        // MOUNT Box
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, drawY, 200, 40);
        drawY += 45;
        if (this.expandedHeadwearCategory.equals("MOUNT")) drawY += 3 * 35;
        if (!this.selectedMount.equals("NONE")) {
            WorkbenchDesign.drawCleanBox(guiGraphics, 20, drawY, 100, 40);
            WorkbenchDesign.drawCleanBox(guiGraphics, 120, drawY, 100, 40);
            drawY += 45;
        }
        
        // FACEWEAR Box
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, drawY, 200, 40);
        drawY += 45;
        if (this.expandedHeadwearCategory.equals("FACEWEAR")) drawY += 3 * 35;
        
        if (this.maxScroll > 0) {
            guiGraphics.fill(225, 100, 227, trueHeight - 20, 0xFF2E3136);
            int thumbHeight = Math.max(20, visibleHeight * visibleHeight / listHeight);
            int thumbY = 100 + (int)((this.scrollOffset / this.maxScroll) * (visibleHeight - 20 - thumbHeight));
            guiGraphics.fill(224, thumbY, 228, thumbY + thumbHeight, 0xFFD2D6DE);
        }
        guiGraphics.disableScissor();

        render3DOperator(guiGraphics, trueWidth, trueHeight);
    }

    private void renderWeaponSelectionBg(GuiGraphics guiGraphics, int trueHeight) {
        int startY = 100;
        int visibleHeight = trueHeight - 100;
        
        ItemStack[] weaponPool = getActiveWeaponStacks();
        int numBoxes = weaponPool.length; 
        int listHeight = numBoxes * 45; 
        
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int currentY = startY - (int)this.scrollOffset;
        
        for (int i = 0; i < numBoxes; i++) {
            WorkbenchDesign.drawCleanBox(guiGraphics, 20, currentY, 200, 40);
            
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
        ItemStack[] attachmentPool = resolveStacks(idPool, this.editingAttachmentCategory);
        int numBoxes = attachmentPool.length; 
        int listHeight = numBoxes * 45; 
        
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int currentY = startY - (int)this.scrollOffset;
        
        for (int i = 0; i < numBoxes; i++) {
            WorkbenchDesign.drawCleanBox(guiGraphics, 20, currentY, 200, 40);
            
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

    private void renderMunitionSelectionBg(GuiGraphics guiGraphics, int trueHeight) {
        int visibleHeight = trueHeight - 100;
        
        // 3 Headers (20px each), 2 gaps (10px each), 13 total items (35px each)
        int listHeight = 60 + 20 + (13 * 35); 
        
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        
        // Purposely left completely blank to remove the "slot boxes" behind the munitions!
        
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
        
        int numCoreAttachments = (this.currentWeaponTab == 8) ? 3 : 5; 
        
        int dynamicItemsHeight = this.showAmmunitionTab 
                ? (20 + 16 + (numPrimary * 31) + 10 + 16 + (numSidearm * 31)) 
                : (20 + 16 + (numGrenade * 31) + 10 + 16 + (numTactical * 31));
                
        int listHeight = 75 + 30 + (numCoreAttachments * 45) + 35 + dynamicItemsHeight; 
        
        this.maxScroll = Math.max(0f, (float)(listHeight - visibleHeight + 20));
        this.scrollOffset = Math.max(0f, Math.min(this.scrollOffset, this.maxScroll));

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        int currentY = startY - (int)this.scrollOffset;
        
        WorkbenchDesign.drawCleanBox(guiGraphics, 20, currentY, 200, 70); 
        currentY += 75;

        currentY += 5; 
        guiGraphics.fill(20, currentY + 15, 220, currentY + 16, 0xFF2E3136);
        currentY += 25;

        for (int i = 0; i < numCoreAttachments; i++) {
            WorkbenchDesign.drawCleanBox(guiGraphics, 20, currentY, 200, 40);
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

    private void renderLoadoutLabels(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300.0F); 
        guiGraphics.fill(176, 50, 198, 72, 0xFF0B0C0E); 
        guiGraphics.fill(176, 95, 198, 117, 0xFF0B0C0E); 
        guiGraphics.fill(176, 140, 198, 162, 0xFF0B0C0E); 
        guiGraphics.pose().popPose();

        ItemStack primaryStack = getDisplayedPrimary();
        String primaryName = primaryStack.isEmpty() ? "UNARMED" : primaryStack.getHoverName().getString().toUpperCase();
        
        ItemStack secondaryStack = getDisplayedSidearm();
        String secondaryName = secondaryStack.isEmpty() ? "UNARMED" : secondaryStack.getHoverName().getString().toUpperCase();

        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "WEAPONS", 20, 26, 0.65f, 0xFFAAAAAA);
        
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "PRIMARY", 26, 68, 0.55f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, primaryName, 26, 74, 0.75f, 0xFFD2D6DE);
        if (!primaryStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(110, 44, 350.0F); 
            guiGraphics.pose().scale(2.5f, 2.5f, 1.0f); 
            guiGraphics.renderItem(primaryStack, 0, 0);
            guiGraphics.pose().popPose();
        }
        
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SIDE ARM", 26, 113, 0.55f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, secondaryName, 26, 119, 0.75f, 0xFFD2D6DE);
        if (!secondaryStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(110, 89, 350.0F); 
            guiGraphics.pose().scale(2.5f, 2.5f, 1.0f); 
            guiGraphics.renderItem(secondaryStack, 0, 0);
            guiGraphics.pose().popPose();
        }
        
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "LONG TACTICAL", 26, 158, 0.55f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "MIRRORGUN", 26, 164, 0.75f, 0xFFD2D6DE);

        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "ARMOR & MUNITIONS", 20, 190, 0.65f, 0xFFAAAAAA);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "VEST | ", 26, 243, 0.55f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedAmmunitionDeployable, 26 + (int)(this.font.width("VEST | ") * 0.55f), 243, 0.55f, 0xFFD62929);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedVest, 26, 249, 0.75f, 0xFFD2D6DE);

        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "MATERIAL", 106, 218, 0.55f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedMaterial, 106, 224, 0.75f, 0xFFD2D6DE);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "COVERAGE", 106, 244, 0.55f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedCoverage, 106, 250, 0.75f, 0xFFD2D6DE);

        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "MUNITION SLOTS", 20, 275, 0.65f, 0xFFAAAAAA);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.menu.getMunitionCount() + "/" + this.selectedAmmunitionDeployable, 165, 275, 0.65f, 0xFFD62929);

        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "AP", 66, 310, 0.55f, 0xFFFFFFFF);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "AP", 153, 310, 0.55f, 0xFFFFFFFF);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "5", 201, 310, 0.55f, 0xFFFFFFFF);

        // --- HEADWEAR DYNAMIC LABELS ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "HEADWEAR", 20, 330, 0.65f, 0xFFAAAAAA);
        
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "HELMET", 26, 383, 0.55f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedHelmet, 26, 389, 0.75f, 0xFFD2D6DE);
        
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "MOUNT | ", 106, 358, 0.55f, 0xFF7A818C);
        if (!this.selectedMount.equals("NONE")) {
            String phosphorTrim = this.selectedPhosphor.equals("WHITE PHOSPHOR") ? "WHITE" : "GREEN";
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, phosphorTrim, 106 + (int)(this.font.width("MOUNT | ") * 0.55f), 358, 0.55f, 0xFFD62929);
        }
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedMount, 106, 364, 0.75f, 0xFFD2D6DE);
        
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "FACEWEAR", 106, 384, 0.55f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedFacewear, 106, 390, 0.75f, 0xFFD2D6DE);
    }

    private void renderArmorSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "< LOADOUT", 20, 25, 0.75f, 0xFFFFFF);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "ARMOR", 20, 55, 1.1f, 0xFFFFFF); 
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SELECT EQUIPMENT", 20, 75, 0.65f, 0xFFD62929); 

        int currentY = 100 - (int)this.scrollOffset;
        
        // Define effective mouse to prevent underlying items lighting up when Vest Dropdown is open
        int effMouseX = mouseX;
        int effMouseY = mouseY;
        if (this.expandedArmorCategory.equals("VEST")) {
            effMouseX = -999;
            effMouseY = -999;
        }
        
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        
        // --- VEST SECTION ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "VEST", 20, currentY + 8, 0.65f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedVest, 20, currentY + 18, 0.75f, 0xFFFFFFFF);
        
        int vestDropdownY = currentY + 45;
        currentY += 45;
        
        // --- COVERAGE SECTION ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "COVERAGE", 20, currentY + 8, 0.65f, 0xFF7A818C);
        currentY += 20;
        
        String[] covList = {"NONE", "FRONT", "FRONT/BACK", "FULL"};
        for(int i = 0; i < 4; i++) {
            int boxX = 20 + (i * 50);
            boolean isSelected = this.selectedCoverage.equals(covList[i]);
            boolean isHovered = effMouseY >= currentY && effMouseY <= currentY + 30 && effMouseX >= boxX && effMouseX <= boxX + 45;
            
            int color = isSelected ? 0xFFD62929 : (isHovered ? 0xFFFFFFFF : 0xFF7A818C);
            float textScale = (i == 2) ? 0.45f : 0.55f;
            int textX = boxX + (i == 2 ? 2 : 8); 
            
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, covList[i], textX, currentY + 12, textScale, color);
            
            if (isSelected) {
                guiGraphics.fill(boxX, currentY + 28, boxX + 45, currentY + 30, 0xFFD62929);
            }
        }
        currentY += 40;

        // --- MATERIAL SECTION ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "MATERIAL", 20, currentY + 8, 0.65f, 0xFF7A818C);
        currentY += 20;
        
        String[] matList = {"KEVLAR", "STEEL", "CERAMIC"};
        for(int i = 0; i < 3; i++) {
            int boxX = 20 + (i * 66);
            boolean isSelected = this.selectedMaterial.equals(matList[i]);
            boolean isHovered = effMouseY >= currentY && effMouseY <= currentY + 30 && effMouseX >= boxX && effMouseX <= boxX + 60;
            
            int color = isSelected ? 0xFFD62929 : (isHovered ? 0xFFFFFFFF : 0xFF7A818C);
            int textX = boxX + 12;
            
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, matList[i], textX, currentY + 12, 0.55f, color);
            
            if (isSelected) {
                guiGraphics.fill(boxX, currentY + 28, boxX + 60, currentY + 30, 0xFFD62929);
            }
        }
        currentY += 40;

        // --- AMMUNITION & DEPLOYABLE SECTION ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "AMMUNITION", 26, currentY + 6, 0.55f, this.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "DEPLOYABLE", 116, currentY + 6, 0.55f, !this.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        currentY += 20;

        if (this.showAmmunitionTab) {
            String[] primaryCats = {"MAGAZINE", "AMMUNITION"};
            String[] primaryNames = {"STANDARD MAG", "5.56X45MM NATO"};
            
            String[] sidearmCats = {"MAGAZINE", "AMMUNITION"};
            String[] sidearmNames = {"STANDARD MAG", "9X19MM PARABELLUM"};
            
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "PRIMARY AMMUNITION", 26, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < primaryCats.length; i++) {
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, primaryCats[i], 26, currentY + 8, 0.45f, 0xFF7A818C);
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, primaryNames[i], 26, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }

            currentY += 10; 
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SIDEARM AMMUNITION", 26, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < sidearmCats.length; i++) {
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, sidearmCats[i], 26, currentY + 8, 0.45f, 0xFF7A818C);
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, sidearmNames[i], 26, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }
        } else {
            String[] grenadeCats = {"GRENADE", "GRENADE", "GRENADE", "GRENADE"};
            String[] grenadeNames = {"9-BANG FLASH GRENADE", "CS GAS", "FLASHBANGS", "STINGER"};
            String[] tacticalCats = {"TACTICAL", "TACTICAL", "TACTICAL", "TACTICAL", "TACTICAL"};
            String[] tacticalNames = {"C2", "LOCKPICK GUN", "PEPPER SPRAY", "TASER", "WEDGE"};
            
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "GRENADE", 26, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < grenadeCats.length; i++) {
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, grenadeCats[i], 26, currentY + 8, 0.45f, 0xFF7A818C);
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, grenadeNames[i], 26, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }

            currentY += 10; 
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "TACTICAL", 26, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < tacticalCats.length; i++) {
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, tacticalCats[i], 26, currentY + 8, 0.45f, 0xFF7A818C);
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, tacticalNames[i], 26, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }
        }
        
        // --- DRAW VEST DROPDOWN ON TOP ---
        if (this.expandedArmorCategory.equals("VEST")) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 150); 
            
            String[] vestList = {"NO ARMOR", "LIGHT ARMOR", "HEAVY ARMOR", "STAB VEST"};
            int bgHeight = vestList.length * 35 + 10;
            
            guiGraphics.fill(15, vestDropdownY - 5, 235, vestDropdownY + bgHeight, 0xFF121212);
            
            int listY = vestDropdownY;
            for (String item : vestList) {
                renderTextListItem(guiGraphics, item, 20, listY, mouseX, mouseY); 
                if (this.selectedVest.equals(item)) {
                    WorkbenchDesign.drawSmallText(guiGraphics, this.font, "[EQUIPPED]", 160, listY + 10, 0.6f, 0xFFD62929);
                }
                listY += 35;
            }
            
            guiGraphics.pose().popPose();
        }
        
        guiGraphics.disableScissor();
    }

    private void renderHeadwearSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "< LOADOUT", 20, 25, 0.75f, 0xFFFFFF);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "HEADWEAR", 20, 55, 1.1f, 0xFFFFFF); 
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SELECT EQUIPMENT", 20, 75, 0.65f, 0xFFD62929); 

        int currentY = 100 - (int)this.scrollOffset;
        int leftX = 26;
        
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        
        // --- HELMET SECTION ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "HELMET", leftX, currentY + 8, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedHelmet, leftX, currentY + 18, 0.75f, 0xFFFFFFFF);
        currentY += 45;
        
        if (this.expandedHeadwearCategory.equals("HELMET")) {
            String[] list = {"NO HELMET", "HELMET ONLY"};
            for (String item : list) {
                renderTextListItem(guiGraphics, item, 20, currentY, mouseX, mouseY);
                currentY += 35;
            }
        }
        
        // --- MOUNT SECTION ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "MOUNT", leftX, currentY + 8, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedMount, leftX, currentY + 18, 0.75f, 0xFFFFFFFF);
        currentY += 45;
        
        if (this.expandedHeadwearCategory.equals("MOUNT")) {
            String[] list = {"NONE", "NVGS", "GPNVGS"};
            for (String item : list) {
                renderTextListItem(guiGraphics, item, 20, currentY, mouseX, mouseY);
                currentY += 35;
            }
        }
        
        // PHOSPHOR SUB-OPTIONS (Only show if NVGs are active)
        if (!this.selectedMount.equals("NONE")) {
            boolean greenHover = mouseY >= currentY && mouseY <= currentY + 40 && mouseX >= 20 && mouseX <= 120;
            boolean whiteHover = mouseY >= currentY && mouseY <= currentY + 40 && mouseX > 120 && mouseX <= 220;
            
            int greenColor = this.selectedPhosphor.equals("GREEN PHOSPHOR") ? 0xFFD62929 : (greenHover ? 0xFFFFFFFF : 0xFF7A818C);
            int whiteColor = this.selectedPhosphor.equals("WHITE PHOSPHOR") ? 0xFFD62929 : (whiteHover ? 0xFFFFFFFF : 0xFF7A818C);
            
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "GREEN", 26, currentY + 10, 0.65f, greenColor);
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "PHOSPHOR", 26, currentY + 22, 0.65f, greenColor);
            
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "WHITE", 126, currentY + 10, 0.65f, whiteColor);
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "PHOSPHOR", 126, currentY + 22, 0.65f, whiteColor);
            
            if (this.selectedPhosphor.equals("GREEN PHOSPHOR")) guiGraphics.fill(20, currentY + 38, 120, currentY + 40, 0xFFD62929);
            if (this.selectedPhosphor.equals("WHITE PHOSPHOR")) guiGraphics.fill(120, currentY + 38, 220, currentY + 40, 0xFFD62929);
            
            currentY += 45;
        }
        
        // --- FACEWEAR SECTION ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "FACEWEAR", leftX, currentY + 8, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, this.selectedFacewear, leftX, currentY + 18, 0.75f, 0xFFFFFFFF);
        currentY += 45;
        
        if (this.expandedHeadwearCategory.equals("FACEWEAR")) {
            String[] list = {"NONE", "GOGGLES", "GAS MASK"};
            for (String item : list) {
                renderTextListItem(guiGraphics, item, 20, currentY, mouseX, mouseY);
                currentY += 35;
            }
        }
        
        guiGraphics.disableScissor();
    }

    private void renderAttachmentSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "< ATTACHMENT BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        String title = this.editingAttachmentCategory;
        
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, title, 20, 55, 1.1f, 0xFFFFFF); 
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SELECT MODIFICATION", 20, 75, 0.65f, 0xFFD62929); 

        String[] idPool = getActiveAttachmentPool();
        ItemStack[] attachmentPool = resolveStacks(idPool, this.editingAttachmentCategory);
        int numBoxes = attachmentPool.length;

        int currentY = 100 - (int)this.scrollOffset;
        int leftX = 26;
        
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        for (int i = 0; i < numBoxes; i++) {
            int y = currentY + (i * 45);
            
            if (attachmentPool[i] != null && !attachmentPool[i].isEmpty()) {
                String cleanName = idPool[i].replace("pointblank:", "").replace("_", " ").toUpperCase();
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, cleanName, leftX + 45, y + 16, 0.7f, 0xFFFFFFFF);
            } else {
                if (idPool[i].equals("NONE")) {
                    WorkbenchDesign.drawSmallText(guiGraphics, this.font, "REMOVE ATTACHMENT", leftX + 45, y + 16, 0.7f, 0xFFD62929);
                } else {
                    String rawName = idPool[i].replace("pointblank:", "").replace("_", " ").toUpperCase();
                    WorkbenchDesign.drawSmallText(guiGraphics, this.font, rawName + " (MISSING)", leftX + 45, y + 16, 0.65f, 0xFF555555);
                }
            }
        }
        guiGraphics.disableScissor();
    }

    private void renderWeaponSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "< WEAPON BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        if (this.currentWeaponTab != 8) {
            int currentX = 8;
            // Expanded widths to add more gaps between AR, BR, LMG, PDW, SMG
            int[] tabWidths = {20, 20, 25, 25, 25, 38, 35, 44};
            for (int i = 0; i < 8; i++) {
                int tabWidth = tabWidths[i];
                
                String name = WorkbenchData.SHORT_TAB_NAMES[i];
                float scale = 0.55f;
                int textColor = (this.currentWeaponTab == i) ? 0xFFFFFFFF : 0xFF7A818C; // White if active, Gray if inactive
                int textWidth = this.font.width(name);
                int textX = currentX + (tabWidth / 2) - (int)((textWidth * scale) / 2);
                
                // Draw just the text, no box
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, name, textX, 74, scale, textColor);
                
                // Draw bold red underline if this tab is active
                if (this.currentWeaponTab == i) {
                    guiGraphics.fill(currentX + 2, 83, currentX + tabWidth - 3, 85, 0xFFD62929);
                }
                
                currentX += tabWidth;
            }
        } else {
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SIDE ARM", 20, 75, 0.85f, 0xFFD62929);
        }

        ItemStack[] weaponPool = getActiveWeaponStacks();
        String[] idPool = getActiveWeaponPool();
        int numBoxes = weaponPool.length;

        int currentY = 100 - (int)this.scrollOffset;
        int leftX = 26;
        
        ItemStack previewStack = (this.currentWeaponTab == 8) ? getDisplayedSidearm() : getDisplayedPrimary(); 

        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        for (int i = 0; i < numBoxes; i++) {
            int y = currentY + (i * 45);
            
            // Re-map the preview stack on hover to render off to the right
            if (mouseY >= y && mouseY <= y + 40 && mouseX >= 20 && mouseX <= 220) {
                if (weaponPool[i] != null && !weaponPool[i].isEmpty()) {
                    previewStack = weaponPool[i]; 
                }
            }
            
            if (weaponPool[i] != null && !weaponPool[i].isEmpty()) {
                String gunName = idPool[i].replace("pointblank:", "").replace("_", " ").toUpperCase();
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, gunName, leftX + 45, y + 16, 0.7f, 0xFFFFFFFF);
            } else {
                String rawName = idPool[i].replace("pointblank:", "").replace("_", " ").toUpperCase();
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, rawName + " (MISSING)", leftX + 45, y + 16, 0.65f, 0xFF555555);
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

    private void renderMunitionSelectionLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, int trueWidth, int trueHeight) {
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "< LOADOUT", 20, 25, 0.75f, 0xFFFFFF);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "MUNITIONS", 20, 55, 1.1f, 0xFFFFFF); 
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SELECT EQUIPMENT", 20, 75, 0.65f, 0xFFD62929); 

        String[] ammoNames = {"5.56X45MM NATO", "9X19MM PARABELLUM", "12 GAUGE BUCKSHOT", ".300 BLACKOUT"};
        String[] grenadeNames = {"9-BANG FLASH GRENADE", "CS GAS", "FLASHBANGS", "STINGER"};
        String[] tacticalNames = {"C2", "LOCKPICK GUN", "PEPPER SPRAY", "TASER", "WEDGE"};

        int currentY = 100 - (int)this.scrollOffset;
        int leftX = 20;
        
        guiGraphics.enableScissor(0, 90, 240, trueHeight);
        
        // --- PRIMARY AMMUNITION SECTION ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "PRIMARY AMMUNITION", leftX, currentY, 0.65f, 0xFF7A818C);
        currentY += 20;
        for (String name : ammoNames) {
            renderTextListItem(guiGraphics, name, leftX, currentY, mouseX, mouseY);
            currentY += 35;
        }
        
        currentY += 10; 
        
        // --- GRENADE SECTION ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "GRENADE", leftX, currentY, 0.65f, 0xFF7A818C);
        currentY += 20;
        for (String name : grenadeNames) {
            renderTextListItem(guiGraphics, name, leftX, currentY, mouseX, mouseY);
            currentY += 35;
        }
        
        currentY += 10;
        
        // --- TACTICAL SECTION ---
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "TACTICAL", leftX, currentY, 0.65f, 0xFF7A818C);
        currentY += 20;
        for (String name : tacticalNames) {
            renderTextListItem(guiGraphics, name, leftX, currentY, mouseX, mouseY);
            currentY += 35;
        }

        guiGraphics.disableScissor();
    }

    private void renderTextListItem(GuiGraphics guiGraphics, String name, int x, int y, int mouseX, int mouseY) {
        boolean isHovered = mouseY >= y && mouseY <= y + 30 && mouseX >= x && mouseX <= x + 200;
        int textColor = isHovered ? 0xFFFFFFFF : 0xFF7A818C;
        
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, name, x, y + 10, 0.8f, textColor);
        guiGraphics.fill(x, y + 25, 220, y + 26, 0xFF2E3136);
        
        if (isHovered) {
            int textWidth = this.font.width(name);
            int scaledWidth = (int)(textWidth * 0.8f);
            guiGraphics.fill(x, y + 25, x + scaledWidth, y + 26, 0xFFD62929); 
        }
    }

    private void renderGunsmithLabels(GuiGraphics guiGraphics) {
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "< WEAPON BUILD", 20, 25, 0.75f, 0xFFFFFF);
        
        boolean isPrimary = (this.currentWeaponTab != 8);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "PRIMARY", 20, 75, 0.85f, isPrimary ? 0xFFFFFFFF : 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SIDE ARM", 100, 75, 0.85f, !isPrimary ? 0xFFFFFFFF : 0xFF7A818C);
        
        if (isPrimary) {
            guiGraphics.fill(20, 87, 80, 89, 0xFFD62929); 
        } else {
            guiGraphics.fill(100, 87, 160, 89, 0xFFD62929); 
        }

        int startY = 100;
        int currentY = startY - (int)this.scrollOffset;
        int leftX = 26;

        guiGraphics.enableScissor(0, 90, 240, guiGraphics.guiHeight());
        
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "WEAPON", leftX, currentY + 50, 0.45f, 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "CURRENT", leftX, currentY + 58, 0.65f, 0xFFD2D6DE);
        
        ItemStack weaponStack = (this.currentWeaponTab == 8) ? getDisplayedSidearm() : getDisplayedPrimary();
        if (!weaponStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(110, currentY + 8, 350.0F); 
            guiGraphics.pose().scale(3.5f, 3.5f, 1.0f); 
            guiGraphics.renderItem(weaponStack, 0, 0);
            guiGraphics.pose().popPose();
        } else {
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "NO WEAPON EQUIPPED", 90, currentY + 32, 0.55f, 0xFF555555);
        }

        currentY += 75;

        currentY += 5; 
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "ATTACHMENTS", leftX, currentY + 6, 0.65f, 0xFF7A818C);
        currentY += 25;

        int numCoreAttachments = (this.currentWeaponTab == 8) ? 3 : 5;
        String[] boxCats = (this.currentWeaponTab == 8) 
                ? new String[]{"OPTIC", "MUZZLE", "STOCK"} 
                : new String[]{"OPTIC", "BARREL", "MUZZLE", "UNDERBARREL", "LASER"};

        AttachmentInfo[] attachments = new AttachmentInfo[numCoreAttachments];
        for (int i = 0; i < numCoreAttachments; i++) {
            attachments[i] = getAttachmentInfo(weaponStack, boxCats[i]);
        }

        for (int i = 0; i < numCoreAttachments; i++) {
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, boxCats[i], leftX, currentY + 12, 0.45f, 0xFF7A818C);
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, attachments[i].name, leftX, currentY + 22, 0.65f, 0xFFD2D6DE);
            
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
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "AMMUNITION", leftX, tabY + 6, 0.55f, this.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
        WorkbenchDesign.drawSmallText(guiGraphics, this.font, "DEPLOYABLE", 116, tabY + 6, 0.55f, !this.showAmmunitionTab ? 0xFFFFFFFF : 0xFF7A818C);
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
            
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "PRIMARY AMMUNITION", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < primaryCats.length; i++) {
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, primaryCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, primaryNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
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
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "SIDEARM AMMUNITION", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < sidearmCats.length; i++) {
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, sidearmCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, sidearmNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
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
            
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "GRENADE", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < grenadeCats.length; i++) {
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, grenadeCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, grenadeNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }

            currentY += 10; 
            WorkbenchDesign.drawSmallText(guiGraphics, this.font, "TACTICAL", leftX, currentY + 6, 0.65f, 0xFF7A818C);
            currentY += 16;
            for (int i = 0; i < tacticalCats.length; i++) {
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, tacticalCats[i], leftX, currentY + 8, 0.45f, 0xFF7A818C);
                WorkbenchDesign.drawSmallText(guiGraphics, this.font, tacticalNames[i], leftX, currentY + 18, 0.65f, 0xFFFFFFFF);
                currentY += 31;
            }
        }

        guiGraphics.disableScissor();
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
                            .getValue(new net.minecraft.resources.ResourceLocation(rl));
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
                        .getValue(new net.minecraft.resources.ResourceLocation(eid));
                if (eItem != null && eItem != net.minecraft.world.item.Items.AIR
                        && com.k1ngtle.taticalsuit.network.EquipWeaponPacket.isItemInCategory(eItem, vpbCategory)) {
                    String name = eid.contains(":") ? eid.substring(eid.indexOf(':') + 1).replace("_", " ").toUpperCase() : eid.toUpperCase();
                    return new AttachmentInfo(new ItemStack(eItem), name);
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
        
        String[][] allPrimary = {WorkbenchData.ASSAULT_RIFLE_IDS, WorkbenchData.BATTLE_RIFLE_IDS, WorkbenchData.LMG_IDS, WorkbenchData.PDW_IDS, WorkbenchData.SMG_IDS, WorkbenchData.SHOTGUN_IDS, WorkbenchData.SNIPER_RIFLE_IDS, WorkbenchData.LAUNCHER_IDS};
        for (String[] pool : allPrimary) {
            for (String wId : pool) {
                if (wId.equals(id)) return true;
            }
        }
        return false;
    }

    private boolean isSidearmWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        net.minecraft.resources.ResourceLocation loc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (loc == null) return false;
        String id = loc.toString();
        for (String wId : WorkbenchData.SIDEARM_WEAPON_IDS) {
            if (wId.equals(id)) return true;
        }
        return false;
    }

    private ItemStack getDisplayedPrimary() {
        if (Minecraft.getInstance().player == null) return ItemStack.EMPTY;
        ItemStack menuStack = this.menu.getSlot(0).getItem();
        if (isPrimaryWeapon(menuStack)) return menuStack;
        ItemStack hotbarStack = Minecraft.getInstance().player.getInventory().getItem(0);
        if (isPrimaryWeapon(hotbarStack)) return hotbarStack;
        return ItemStack.EMPTY;
    }

    private ItemStack getDisplayedSidearm() {
        if (Minecraft.getInstance().player == null) return ItemStack.EMPTY;
        ItemStack menuStack = this.menu.getSlot(1).getItem();
        if (isSidearmWeapon(menuStack)) return menuStack;
        ItemStack hotbarStack = Minecraft.getInstance().player.getInventory().getItem(1);
        if (isSidearmWeapon(hotbarStack)) return hotbarStack;
        return ItemStack.EMPTY;
    }
}