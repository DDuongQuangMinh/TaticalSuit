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
    private final Player player;
    final ItemStackHandler inventory = new ItemStackHandler(14);

    public WorkbenchMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, ContainerLevelAccess.NULL);
    }

    public WorkbenchMenu(int id, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.WORKBENCH_MENU.get(), id);
        this.access = access;
        this.player = playerInventory.player;
        setupSlots();
        if (!this.player.level().isClientSide()) {
            WeaponScanner.scan(playerInventory, this.inventory);
        }
    }

    private void setupSlots() {
        // Weapon slots moved off-screen: hides vanilla slot boxes and prevents invisible click-duplication
        addSlot(new SlotItemHandler(inventory, 0, -1000, -1000)); // Primary
        addSlot(new SlotItemHandler(inventory, 1, -1000, -1000)); // Sidearm
        addSlot(new SlotItemHandler(inventory, 2, -1000, -1000)); // Melee / Tactical

        // Armor
        addSlot(new SlotItemHandler(inventory, 3, 52, 224));      // Vest

        // Munitions (9 slots)
        int[] munitionX = {22, 42, 62, 82, 102, 129, 149, 169, 196};
        for (int i = 0; i < munitionX.length; i++) {
            addSlot(new SlotItemHandler(inventory, 4 + i, munitionX[i], 289));
        }

        // Headwear
        addSlot(new SlotItemHandler(inventory, 13, 52, 364));     // Helmet
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            WeaponScanner.returnToPlayer(player, inventory);
        }
    }

    public ItemStack getWeaponSlotItem(int slotIndex) {
        if (slotIndex < 0 || slotIndex > 1) return ItemStack.EMPTY;
        return inventory.getStackInSlot(slotIndex);
    }

    public void setWeaponSlot(int slotIndex, ItemStack stack) {
        if (slotIndex < 0 || slotIndex > 1) return;
        inventory.setStackInSlot(slotIndex, stack);
        broadcastChanges();
    }

    public int getMunitionCount() {
        return WeaponClassifier.countMunitions(player.getInventory());
    }

    @Override
    public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.WORKBENCH.get());
    }
}