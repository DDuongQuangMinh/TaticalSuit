package com.k1ngtle.taticalsuit.menu;

import com.k1ngtle.taticalsuit.capability.EquipmentSlotType;
import com.k1ngtle.taticalsuit.capability.TacticalEquipmentProvider;
import com.k1ngtle.taticalsuit.registry.ModMenuTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TacticalEquipmentMenu extends AbstractContainerMenu {

    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public TacticalEquipmentMenu(int containerId, Inventory playerInv) {
        super(ModMenuTypes.TACTICAL_EQUIPMENT_MENU.get(), containerId);

        playerInv.player.getCapability(TacticalEquipmentProvider.CAPABILITY).ifPresent(cap -> {
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.SHIRT, -28, 8));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.PANTS, -28, 26));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.GLOVES, -28, 44));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.BOOTS, -28, 62));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.BELT, -28, 80));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.TATTOO, -28, 98));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.EYEWEAR, -28, 116));
            this.addSlot(new TacticalEquipmentSlot(cap, EquipmentSlotType.WATCH, -28, 134));
        });

        for (int i = 0; i < 4; ++i) {
            final EquipmentSlot equipmentslot = ARMOR_SLOTS[i];
            this.addSlot(new Slot(playerInv, 39 - i, 8, 8 + i * 18) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.canEquip(equipmentslot, playerInv.player);
                }
            });
        }

        this.addSlot(new Slot(playerInv, 40, 77, 62));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

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

            if (index < 13) { 
                // Tactical, Armor, or Offhand -> Inventory
                if (!this.moveItemStackTo(slotStack, 13, 49, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Inventory -> Equipment
                boolean moved = false;
                
                // 1. Try Armor Slots
                if (!moved) {
                    for(int i = 8; i < 12; i++) {
                        if (this.slots.get(i).mayPlace(slotStack)) {
                            moved = this.moveItemStackTo(slotStack, i, i + 1, false);
                            if (moved) break;
                        }
                    }
                }
                // 2. Try Custom Tactical Slots
                if (!moved) {
                    moved = this.moveItemStackTo(slotStack, 0, 8, false);
                }
                // 3. Try Offhand Slot
                if (!moved && this.slots.get(12).mayPlace(slotStack)) {
                    moved = this.moveItemStackTo(slotStack, 12, 13, false);
                }

                // Inventory <-> Hotbar Routing
                if (!moved) {
                    if (index >= 13 && index < 40) {
                        if (!this.moveItemStackTo(slotStack, 40, 49, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= 40 && index < 49) {
                        if (!this.moveItemStackTo(slotStack, 13, 40, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
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