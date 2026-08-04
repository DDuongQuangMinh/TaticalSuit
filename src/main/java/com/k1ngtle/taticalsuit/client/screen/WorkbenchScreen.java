package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    
    public boolean isDraggingModel = false;
    public float playerRotation = 0f;
    
    // UI State Trackers
    public boolean inCustomizationTab = false; 
    public boolean inGunsmith = false; 
    public boolean showAmmunitionTab = true; 
    
    // 0=AR, 1=BR, 2=LMG, 3=PDW, 4=SMG, 5=Shotgun, 6=Sniper, 7=Launcher, 8=Sidearm
    public int currentWeaponTab = 0; 
    
    public boolean inWeaponSelection = false; 
    public boolean inAttachmentSelection = false;
    public boolean inTacticalSelection = false;
    public boolean inMunitionSelection = false; 
    public boolean inHeadwearSelection = false;
    public boolean inArmorSelection = false; 
    public boolean inCustomizationSelection = false; 
    public boolean inStyleSelection = false; 
    
    public String editingAttachmentCategory = "";
    public String editingMunitionCategory = "";
    public String expandedHeadwearCategory = ""; 
    public String expandedArmorCategory = ""; 
    public String customizationCategory = "";
    
    // Default Equipment Loadouts
    public String selectedTactical = "MIRRORGUN";
    
    public String selectedHelmet = "HELMET ONLY";
    public String selectedMount = "GPNVGS";
    public String selectedFacewear = "ANTI-FLASH GOGGLES";
    public String selectedPhosphor = "WHITE PHOSPHOR";

    public String selectedVest = "LIGHT ARMOR";
    public String selectedMaterial = "STEEL";
    public String selectedCoverage = "FRONT/BACK";
    public String selectedAmmunitionDeployable = "13 SLOTS";
    
    // Scroll Trackers
    public float scrollOffset = 0f;
    public float maxScroll = 0f;
    
    // Anti-Duplication Security Timer
    public long lastClickTime = 0;

    // Dynamic Lists for Auto-Sorting Datapacks
    private List<String> dynamicAR = new ArrayList<>();
    private List<String> dynamicBR = new ArrayList<>();
    private List<String> dynamicLMG = new ArrayList<>();
    private List<String> dynamicPDW = new ArrayList<>();
    private List<String> dynamicSMG = new ArrayList<>();
    private List<String> dynamicShotgun = new ArrayList<>();
    private List<String> dynamicSniper = new ArrayList<>();
    private List<String> dynamicLauncher = new ArrayList<>();
    private List<String> dynamicSidearm = new ArrayList<>();

    private ItemStack[] assaultRifleStacks;
    private ItemStack[] battleRifleStacks;
    private ItemStack[] lmgStacks;
    private ItemStack[] pdwStacks;
    private ItemStack[] smgStacks;
    private ItemStack[] shotgunStacks;
    private ItemStack[] sniperRifleStacks;
    private ItemStack[] launcherStacks;
    private ItemStack[] sidearmWeaponStacks;

    private ItemStack[] helmetStacks;
    
    public WorkbenchDesign designLayer;
    public WorkbenchRenderDesign renderLayer;

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
        
        this.designLayer = new WorkbenchDesign(this);
        this.renderLayer = new WorkbenchRenderDesign(this);
        
        // Ensure ALL states are strictly reset when the menu is opened!
        this.inCustomizationTab = false;
        this.inGunsmith = false;
        this.inWeaponSelection = false;
        this.inAttachmentSelection = false;
        this.inTacticalSelection = false;
        this.inMunitionSelection = false;
        this.inHeadwearSelection = false;
        this.inArmorSelection = false;
        this.inCustomizationSelection = false;
        this.inStyleSelection = false; 
        
        this.editingMunitionCategory = "";
        this.expandedHeadwearCategory = "";
        this.expandedArmorCategory = "";
        this.customizationCategory = "";
        
        this.showAmmunitionTab = true;
        this.currentWeaponTab = 0;
        this.scrollOffset = 0f;

        if (Minecraft.getInstance().player != null) {
            ItemStack helmetStack = Minecraft.getInstance().player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
            if (!helmetStack.isEmpty()) {
                if (helmetStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetItem) {
                    this.selectedHelmet = "HELMET ONLY";
                    this.selectedMount = "NONE";
                } else if (helmetStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31Item) {
                    this.selectedHelmet = "HELMET ONLY";
                    this.selectedMount = "NVGS";
                } else if (helmetStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31GhillieItem) {
                    this.selectedHelmet = "GHILLIE HELMET";
                    this.selectedMount = "NVGS";
                } else if (helmetStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31SandItem) {
                    this.selectedHelmet = "SAND GHILLIE HELMET";
                    this.selectedMount = "NVGS";
                } else if (helmetStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31SnowItem) {
                    this.selectedHelmet = "SNOW GHILLIE HELMET";
                    this.selectedMount = "NVGS";
                } else if (helmetStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item) {
                    this.selectedHelmet = "HELMET ONLY";
                    this.selectedMount = "GPNVGS";
                } else if (helmetStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18SnowItem) {
                    this.selectedHelmet = "SNOW GHILLIE HELMET";
                    this.selectedMount = "GPNVGS";
                } else if (helmetStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGhillieItem) {
                    this.selectedHelmet = "GHILLIE HELMET";
                    this.selectedMount = "NONE";
                } else if (helmetStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetSandItem) {
                    this.selectedHelmet = "SAND GHILLIE HELMET";
                    this.selectedMount = "NONE";
                } else if (helmetStack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetSnowItem) {
                    this.selectedHelmet = "SNOW GHILLIE HELMET";
                    this.selectedMount = "NONE";
                }
                
                if (helmetStack.hasTag() && helmetStack.getTag().contains("phosphor")) {
                    this.selectedPhosphor = helmetStack.getTag().getString("phosphor");
                }
            } else {
                this.selectedHelmet = "NO HELMET";
                this.selectedMount = "NONE";
            }
        }

        buildDynamicPools();

        this.assaultRifleStacks = resolveStacks(dynamicAR.toArray(new String[0]), "WEAPON");
        this.battleRifleStacks = resolveStacks(dynamicBR.toArray(new String[0]), "WEAPON");
        this.lmgStacks = resolveStacks(dynamicLMG.toArray(new String[0]), "WEAPON");
        this.pdwStacks = resolveStacks(dynamicPDW.toArray(new String[0]), "WEAPON");
        this.smgStacks = resolveStacks(dynamicSMG.toArray(new String[0]), "WEAPON");
        this.shotgunStacks = resolveStacks(dynamicShotgun.toArray(new String[0]), "WEAPON");
        this.sniperRifleStacks = resolveStacks(dynamicSniper.toArray(new String[0]), "WEAPON");
        this.launcherStacks = resolveStacks(dynamicLauncher.toArray(new String[0]), "WEAPON");
        this.sidearmWeaponStacks = resolveStacks(dynamicSidearm.toArray(new String[0]), "WEAPON");

        this.helmetStacks = resolveStacks(WorkbenchData.HELMET_IDS, "GEAR");
    }

    private void buildDynamicPools() {
        dynamicAR = new ArrayList<>(Arrays.asList(WorkbenchData.ASSAULT_RIFLE_IDS));
        dynamicBR = new ArrayList<>(Arrays.asList(WorkbenchData.BATTLE_RIFLE_IDS));
        dynamicLMG = new ArrayList<>(Arrays.asList(WorkbenchData.LMG_IDS));
        dynamicPDW = new ArrayList<>(Arrays.asList(WorkbenchData.PDW_IDS));
        dynamicSMG = new ArrayList<>(Arrays.asList(WorkbenchData.SMG_IDS));
        dynamicShotgun = new ArrayList<>(Arrays.asList(WorkbenchData.SHOTGUN_IDS));
        dynamicSniper = new ArrayList<>(Arrays.asList(WorkbenchData.SNIPER_RIFLE_IDS));
        dynamicLauncher = new ArrayList<>(Arrays.asList(WorkbenchData.LAUNCHER_IDS));
        dynamicSidearm = new ArrayList<>(Arrays.asList(WorkbenchData.SIDEARM_WEAPON_IDS));

        Set<String> processed = new HashSet<>();
        processed.addAll(dynamicAR);
        processed.addAll(dynamicBR);
        processed.addAll(dynamicLMG);
        processed.addAll(dynamicPDW);
        processed.addAll(dynamicSMG);
        processed.addAll(dynamicShotgun);
        processed.addAll(dynamicSniper);
        processed.addAll(dynamicLauncher);
        processed.addAll(dynamicSidearm);

        for (net.minecraft.world.item.Item item : net.minecraftforge.registries.ForgeRegistries.ITEMS) {
            ResourceLocation loc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
            if (loc != null && loc.getNamespace().equals("pointblank")) {
                String id = loc.toString();
                if (processed.contains(id)) continue;
                
                if (item.getDefaultInstance().getMaxStackSize() > 1) continue; 
                
                String path = loc.getPath().toLowerCase();
                
                boolean isAccessory = false;
                for (String kw : WorkbenchData.IGNORE_KEYWORDS) {
                    if (path.contains(kw)) {
                        isAccessory = true; break;
                    }
                }
                if (isAccessory) continue;

                if (path.contains("pistol") || path.contains("glock") || path.contains("m9") || path.contains("eagle") || path.contains("revolver") || path.contains("hg") || path.contains("makarov") || path.contains("1911") || path.contains("usp")) {
                    dynamicSidearm.add(id);
                } else if (path.contains("shotgun") || path.contains("spas") || path.contains("870") || path.contains("12g") || path.contains("m1014") || path.contains("saiga") || path.contains("super90") || path.contains("mossberg")) {
                    dynamicShotgun.add(id);
                } else if (path.contains("sniper") || path.contains("awp") || path.contains("svd") || path.contains("m82") || path.contains("m24") || path.contains("barrett") || path.contains("intervention") || path.contains("l96") || path.contains("dragunov")) {
                    dynamicSniper.add(id);
                } else if (path.contains("lmg") || path.contains("m249") || path.contains("minigun") || path.contains("m60") || path.contains("pkp") || path.contains("pkm") || path.contains("mg42") || path.contains("rpd")) {
                    dynamicLMG.add(id);
                } else if (path.contains("smg") || path.contains("mp5") || path.contains("vector") || path.contains("mac") || path.contains("ump") || path.contains("uzi") || path.contains("bizon") || path.contains("mp9")) {
                    dynamicSMG.add(id);
                } else if (path.contains("pdw") || path.contains("p90") || path.contains("mp7")) {
                    dynamicPDW.add(id);
                } else if (path.contains("br") || path.contains("fal") || path.contains("scarh") || path.contains("m14") || path.contains("g3")) {
                    dynamicBR.add(id);
                } else if (path.contains("launcher") || path.contains("rpg") || path.contains("m32") || path.contains("smaw") || path.contains("thumper") || path.contains("m203") || path.contains("m320")) {
                    dynamicLauncher.add(id);
                } else {
                    dynamicAR.add(id); 
                }
                processed.add(id);
            }
        }
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
            case "GEAR": keywords = new String[]{""}; break; 
            default: keywords = new String[]{category.toLowerCase()}; break;
        }

        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals("NONE")) {
                stacks[i] = ItemStack.EMPTY;
                continue;
            }
            
            ResourceLocation loc = ResourceLocation.tryParse(ids[i]);
            if(loc == null) continue;
            net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(loc);
            
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                stacks[i] = new ItemStack(item);
            } else {
                ItemStack bestMatch = ItemStack.EMPTY;
                ItemStack fallback = ItemStack.EMPTY;
                int longestMatch = 0;
                String targetId = ids[i].toLowerCase().replace("pointblank:", "");
                
                for (net.minecraft.world.item.Item regItem : net.minecraftforge.registries.ForgeRegistries.ITEMS) {
                    ResourceLocation regLoc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(regItem);
                    if (regLoc != null && "pointblank".equals(regLoc.getNamespace())) {
                        String path = regLoc.getPath().toLowerCase();
                        
                        boolean matchesCategory = false;
                        if (category.equals("WEAPON") || category.equals("GEAR")) {
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

    public String[] getActiveWeaponPool() {
        return switch (this.currentWeaponTab) {
            case 0 -> dynamicAR.toArray(new String[0]);
            case 1 -> dynamicBR.toArray(new String[0]);
            case 2 -> dynamicLMG.toArray(new String[0]);
            case 3 -> dynamicPDW.toArray(new String[0]);
            case 4 -> dynamicSMG.toArray(new String[0]);
            case 5 -> dynamicShotgun.toArray(new String[0]);
            case 6 -> dynamicSniper.toArray(new String[0]);
            case 7 -> dynamicLauncher.toArray(new String[0]);
            case 8 -> dynamicSidearm.toArray(new String[0]);
            default -> dynamicAR.toArray(new String[0]);
        };
    }

    public ItemStack[] getActiveWeaponStacks() {
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

    public String[] getActiveAttachmentPool() {
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
    
    public ItemStack[] resolveAttachmentStacks(String[] ids, String category) {
        return resolveStacks(ids, category);
    }

    public boolean isPrimaryWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation loc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (loc == null) return false;
        String id = loc.toString();
        
        List<List<String>> allPrimary = Arrays.asList(dynamicAR, dynamicBR, dynamicLMG, dynamicPDW, dynamicSMG, dynamicShotgun, dynamicSniper, dynamicLauncher);
        for (List<String> pool : allPrimary) {
            if (pool.contains(id)) return true;
        }
        return false;
    }

    public boolean isSidearmWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation loc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (loc == null) return false;
        String id = loc.toString();
        return dynamicSidearm.contains(id);
    }

    public ItemStack getDisplayedPrimary() {
        if (Minecraft.getInstance().player == null) return ItemStack.EMPTY;
        ItemStack menuStack = this.menu.getSlot(0).getItem();
        if (isPrimaryWeapon(menuStack)) return menuStack;
        ItemStack hotbarStack = Minecraft.getInstance().player.getInventory().getItem(0);
        if (isPrimaryWeapon(hotbarStack)) return hotbarStack;
        return ItemStack.EMPTY;
    }

    public ItemStack getDisplayedSidearm() {
        if (Minecraft.getInstance().player == null) return ItemStack.EMPTY;
        ItemStack menuStack = this.menu.getSlot(1).getItem();
        if (isSidearmWeapon(menuStack)) return menuStack;
        ItemStack hotbarStack = Minecraft.getInstance().player.getInventory().getItem(1);
        if (isSidearmWeapon(hotbarStack)) return hotbarStack;
        return ItemStack.EMPTY;
    }

    public void updateHelmetEquip() {
        String targetId = "NONE";
        
        if (this.selectedHelmet.equals("HELMET ONLY")) {
            if (this.selectedMount.equals("NONE")) {
                targetId = "taticalsuit:base_helmet";
            } else if (this.selectedMount.equals("NVGS")) {
                targetId = "taticalsuit:helmet_pvs31";
            } else if (this.selectedMount.equals("GPNVGS")) {
                targetId = "taticalsuit:helmet_gpnvg18";
            }
        } else if (this.selectedHelmet.equals("GHILLIE HELMET")) {
            if (this.selectedMount.equals("NONE")) targetId = "taticalsuit:helmet_ghillie";
            else if (this.selectedMount.equals("NVGS")) targetId = "taticalsuit:helmet_pvs31_ghillie";
            else if (this.selectedMount.equals("GPNVGS")) targetId = "taticalsuit:helmet_gpnvg18_ghillie";
        } else if (this.selectedHelmet.equals("SAND GHILLIE HELMET")) {
            if (this.selectedMount.equals("NONE")) {
                targetId = "taticalsuit:helmet_sand";
            } else if (this.selectedMount.equals("NVGS")) {
                targetId = "taticalsuit:helmet_pvs31_sand";
            } else if (this.selectedMount.equals("GPNVGS")) {
                targetId = "taticalsuit:helmet_gpnvg18_sand";
            }
        } else if (this.selectedHelmet.equals("SNOW GHILLIE HELMET")) {
            if (this.selectedMount.equals("NONE")) {
                targetId = "taticalsuit:helmet_snow";
            } else if (this.selectedMount.equals("NVGS")) {
                targetId = "taticalsuit:helmet_pvs31_snow";
            } else if (this.selectedMount.equals("GPNVGS")) {
                targetId = "taticalsuit:helmet_gpnvg18_snow";
            } else {
                targetId = "taticalsuit:helmet_snow";
            }
        }
        
        net.minecraft.world.item.Item targetItem = null;
        
        if (!targetId.equals("NONE")) {
            targetItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(targetId));
            
            if (targetItem == null || targetItem == net.minecraft.world.item.Items.AIR) {
                for (net.minecraft.world.item.Item regItem : net.minecraftforge.registries.ForgeRegistries.ITEMS) {
                    if (regItem instanceof com.k1ngtle.taticalsuit.item.HelmetItem && 
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31Item) && 
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31GhillieItem) && 
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31SandItem) && 
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31SnowItem) &&
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item) &&
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem) &&
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18SandItem) &&
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18SnowItem) &&
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetGhillieItem) &&
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetSandItem) &&
                       !(regItem instanceof com.k1ngtle.taticalsuit.item.HelmetSnowItem)) {
                        targetItem = regItem;
                        targetId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(regItem).toString();
                        break;
                    }
                }
            }
        }
        
        if (Minecraft.getInstance().player != null) {
            for (int i = 0; i < Minecraft.getInstance().player.getInventory().getContainerSize(); i++) {
                ItemStack stack = Minecraft.getInstance().player.getInventory().getItem(i);
                if (stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetItem ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31Item ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31GhillieItem ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31SandItem ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31SnowItem ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18SandItem ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18SnowItem ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGhillieItem ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetSandItem ||
                    stack.getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetSnowItem) {
                    Minecraft.getInstance().player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
            
            if (this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetItem ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31Item ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31GhillieItem ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31SandItem ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetPVS31SnowItem ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18SandItem ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGPNVG18SnowItem ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetGhillieItem ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetSandItem ||
                this.menu.getCarried().getItem() instanceof com.k1ngtle.taticalsuit.item.HelmetSnowItem) {
                this.menu.setCarried(ItemStack.EMPTY);
            }

            if (targetId.equals("NONE")) {
                Minecraft.getInstance().player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
            } else if (targetItem != null && targetItem != net.minecraft.world.item.Items.AIR) {
                ItemStack localEquip = new ItemStack(targetItem);
                localEquip.getOrCreateTag().putString("phosphor", this.selectedPhosphor);
                Minecraft.getInstance().player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, localEquip);
            }
        }
        
        com.k1ngtle.taticalsuit.network.HeadwearNetwork.CHANNEL.sendToServer(
                new com.k1ngtle.taticalsuit.network.HeadwearNetwork.EquipHelmetPacket(targetId, this.selectedPhosphor)
        );
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.designLayer.handleMouseClicked(pMouseX, pMouseY, pButton)) return true;
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        this.isDraggingModel = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.designLayer.handleMouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.designLayer.handleMouseScrolled(pMouseX, pMouseY, pDelta)) return true;
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int renderMouseX = mouseX;
        int renderMouseY = mouseY;
        
        if (!this.inGunsmith && !this.inWeaponSelection && !this.inAttachmentSelection && !this.inMunitionSelection && !this.inHeadwearSelection && !this.inArmorSelection && !this.inTacticalSelection) {
            if (this.inCustomizationTab) {
                if (mouseX < 240) {
                    renderMouseX = -999;
                    renderMouseY = -999;
                }
            } else {
                if (mouseX >= 165 && mouseX <= 195 && mouseY >= 35 && mouseY <= 165) {
                    renderMouseX = -999;
                    renderMouseY = -999;
                } else if (mouseX < 240 && mouseY >= 190) {
                    renderMouseX = -999;
                    renderMouseY = -999;
                }
            }
        }
        
        super.render(guiGraphics, renderMouseX, renderMouseY, delta);
        this.renderLayer.renderMain(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.renderLayer.renderBg(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!this.inGunsmith && !this.inWeaponSelection && !this.inAttachmentSelection && !this.inMunitionSelection && !this.inHeadwearSelection && !this.inArmorSelection && !this.inTacticalSelection) {
            
            int loadoutColor = !this.inCustomizationTab ? 0xFFFFFFFF : 0xFF7A818C;
            int customColor = this.inCustomizationTab ? 0xFFFFFFFF : 0xFF7A818C;
            
            guiGraphics.drawString(this.font, "LOADOUT", 20, 6, loadoutColor, false);
            
            int slashX = 20 + this.font.width("LOADOUT") + 4;
            guiGraphics.drawString(this.font, "/", slashX, 6, 0xFF555555, false);
            
            int customX = slashX + this.font.width("/") + 4;
            guiGraphics.drawString(this.font, "CUSTOMIZATION", customX, 6, customColor, false);
            
            this.renderLayer.renderLabels(guiGraphics, mouseX, mouseY);
        }
    }
}