package com.k1ngtle.taticalsuit.menu;

import com.k1ngtle.taticalsuit.capability.EquipmentSlotType;
import com.k1ngtle.taticalsuit.capability.TacticalEquipmentProvider;
import com.k1ngtle.taticalsuit.registry.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TacticalEquipmentMenu extends AbstractContainerMenu {

    public TacticalEquipmentMenu(int containerId, Inventory playerInv) {
        super(ModMenuTypes.TACTICAL_EQUIPMENT_MENU.get(), containerId);

        // Fetch our hidden 8-slot capability inventory attached to the player
        playerInv.player.getCapability(TacticalEquipmentProvider.CAPABILITY).ifPresent(cap -> {
            
            // Left Side Slots (Flanking the player model)
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.SHIRT, 26, 8));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.PANTS, 26, 26));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.GLOVES, 26, 44));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.BOOTS, 26, 62));

            // Right Side Slots (Flanking the player model)
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.BELT, 134, 8));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.TATTOO, 134, 26));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.EYEWEAR, 134, 44));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.WATCH, 134, 62));
        });

        // Add Standard Player Inventory (27 slots)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add Standard Player Hotbar (9 slots)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // Custom Slots -> Player Inventory
            if (index < 8) { 
                if (!this.moveItemStackTo(slotStack, 8, 44, true)) {
                    return ItemStack.EMPTY;
                }
            } 
            // Player Inventory -> Custom Slots
            else if (!this.moveItemStackTo(slotStack, 0, 8, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; 
    }
}