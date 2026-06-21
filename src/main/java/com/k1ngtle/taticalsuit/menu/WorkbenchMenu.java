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
        this.player = playerInventory.player; // Save the player instance to check their inventory

        // --- SECTION 1: WEAPONS (3 Tabs) ---
        this.addSlot(new SlotItemHandler(inventory, 0, 180, 54));  // Primary Rifle
        this.addSlot(new SlotItemHandler(inventory, 1, 180, 99));  // Secondary Pistol
        this.addSlot(new SlotItemHandler(inventory, 2, 180, 144)); // Melee / Tactical

        // --- SECTION 2: ARMOR & MUNITIONS ---
        this.addSlot(new SlotItemHandler(inventory, 3, 52, 224)); // Vest Box

        // 9 Munition Slots
        // Group 1 (5 Slots)
        this.addSlot(new SlotItemHandler(inventory, 4, 22, 289));
        this.addSlot(new SlotItemHandler(inventory, 5, 42, 289));
        this.addSlot(new SlotItemHandler(inventory, 6, 62, 289));
        this.addSlot(new SlotItemHandler(inventory, 7, 82, 289));
        this.addSlot(new SlotItemHandler(inventory, 8, 102, 289));
        // Group 2 (3 Slots)
        this.addSlot(new SlotItemHandler(inventory, 9, 129, 289));
        this.addSlot(new SlotItemHandler(inventory, 10, 149, 289));
        this.addSlot(new SlotItemHandler(inventory, 11, 169, 289));
        // Group 3 (1 Slot)
        this.addSlot(new SlotItemHandler(inventory, 12, 196, 289));

        // --- SECTION 3: HEADWEAR ---
        this.addSlot(new SlotItemHandler(inventory, 13, 52, 364)); // Helmet Box
    }

    // --- DYNAMIC INVENTORY CHECKER ---
    public int getMunitionCount() {
        int count = 0;
        
        // Scan every slot in the player's personal inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                // Get the item's registry name (e.g. "pointblank:m4_mag")
                String itemName = ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath().toLowerCase();
                
                // If it's a magazine, ammo, or grenade, count it as a munition slot taken
                if (itemName.contains("mag") || itemName.contains("ammo") || itemName.contains("grenade")) {
                    count++;
                }
            }
        }
        
        // Return the count, capped at 13 max slots to match the UI limitation
        return Math.min(count, 13);
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