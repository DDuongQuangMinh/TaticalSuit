package com.k1ngtle.taticalsuit.menu;

import com.k1ngtle.taticalsuit.registry.ModBlocks;
import com.k1ngtle.taticalsuit.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class WorkbenchMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Player player;
    
    private final ItemStackHandler inventory = new ItemStackHandler(14); 

    public WorkbenchMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, ContainerLevelAccess.NULL);
    }

    public WorkbenchMenu(int id, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.WORKBENCH_MENU.get(), id);
        this.access = access;
        this.player = playerInventory.player; 

        // --- SECTION 1: WEAPONS (3 Tabs) ---
        // MOVED OFF-SCREEN (-1000)! This permanently hides the vanilla boxes and completely stops invisible click-duplication!
        this.addSlot(new SlotItemHandler(inventory, 0, -1000, -1000));  // Primary Rifle
        this.addSlot(new SlotItemHandler(inventory, 1, -1000, -1000));  // Secondary Pistol
        this.addSlot(new SlotItemHandler(inventory, 2, -1000, -1000)); // Melee / Tactical

        // --- SECTION 2: ARMOR & MUNITIONS ---
        this.addSlot(new SlotItemHandler(inventory, 3, 52, 224)); // Vest Box

        // 9 Munition Slots
        this.addSlot(new SlotItemHandler(inventory, 4, 22, 289));
        this.addSlot(new SlotItemHandler(inventory, 5, 42, 289));
        this.addSlot(new SlotItemHandler(inventory, 6, 62, 289));
        this.addSlot(new SlotItemHandler(inventory, 7, 82, 289));
        this.addSlot(new SlotItemHandler(inventory, 8, 102, 289));
        this.addSlot(new SlotItemHandler(inventory, 9, 129, 289));
        this.addSlot(new SlotItemHandler(inventory, 10, 149, 289));
        this.addSlot(new SlotItemHandler(inventory, 11, 169, 289));
        this.addSlot(new SlotItemHandler(inventory, 12, 196, 289));

        // --- SECTION 3: HEADWEAR ---
        this.addSlot(new SlotItemHandler(inventory, 13, 52, 364)); // Helmet Box

        // --- AUTO-EQUIP EXISTING WEAPONS ---
        if (!this.player.level().isClientSide()) {
            this.scanAndEquipWeapons(playerInventory);
        }
    }

    private void scanAndEquipWeapons(Inventory playerInventory) {
        boolean primaryFound = false;
        boolean sidearmFound = false;

        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (stack.isEmpty()) continue;

            String namespace = ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace().toLowerCase();
            String path = ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath().toLowerCase();

            if (namespace.equals("pointblank") && !path.contains("mag") && !path.contains("ammo") && !path.contains("grenade")) {
                
                boolean isSideArm = path.contains("glock") || path.contains("m1911") || path.contains("deserteagle") || 
                                    path.contains("m9") || path.contains("p30l") || path.contains("viper");

                if (!primaryFound && !isSideArm) {
                    this.inventory.setStackInSlot(0, stack.copy());
                    playerInventory.setItem(i, ItemStack.EMPTY); 
                    primaryFound = true;
                } else if (!sidearmFound && isSideArm) {
                    this.inventory.setStackInSlot(1, stack.copy());
                    playerInventory.setItem(i, ItemStack.EMPTY);
                    sidearmFound = true;
                }

                if (primaryFound && sidearmFound) break; 
            }
        }
    }

    // --- DYNAMIC INVENTORY CHECKER ---
    public int getMunitionCount() {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                String itemName = ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath().toLowerCase();
                if (itemName.contains("mag") || itemName.contains("ammo") || itemName.contains("grenade")) {
                    count++;
                }
            }
        }
        return Math.min(count, 13);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false); 
                }
            }
        }
    }

    public ItemStack getWeaponSlotItem(int slotIndex) {
        if (slotIndex != 0 && slotIndex != 1) return ItemStack.EMPTY;
        return inventory.getStackInSlot(slotIndex);
    }

    public void setWeaponSlot(int slotIndex, ItemStack stack) {
        if (slotIndex != 0 && slotIndex != 1) return;
        inventory.setStackInSlot(slotIndex, stack);
        this.broadcastChanges(); 
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY; 
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.WORKBENCH.get());
    }
}