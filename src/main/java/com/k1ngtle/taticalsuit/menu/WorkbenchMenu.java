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

public class WorkbenchMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final ItemStackHandler inventory = new ItemStackHandler(6); 

    public WorkbenchMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, ContainerLevelAccess.NULL);
    }

    public WorkbenchMenu(int id, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.WORKBENCH_MENU.get(), id);
        this.access = access;

        // --- WEAPON SLOTS ---
        // Pushed down and spaced out for the wide weapon boxes
        // Weapons (Updated to match the 140 width boxes)
        this.addSlot(new SlotItemHandler(inventory, 0, 82, 62));  // Centered in 140w box
        this.addSlot(new SlotItemHandler(inventory, 1, 82, 107)); // Centered in 140w box
        
        // Armor (Updated to match the 60x60 boxes)
        this.addSlot(new SlotItemHandler(inventory, 2, 42, 172)); // Centered in 60w box
        this.addSlot(new SlotItemHandler(inventory, 3, 112, 172)); // Centered in 60w box
        this.addSlot(new SlotItemHandler(inventory, 4, 42, 242)); // Centered in 60w box
        this.addSlot(new SlotItemHandler(inventory, 5, 112, 242)); // Centered in 60w box

        // The Backpack slots have been entirely removed per your design!
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