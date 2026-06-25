package com.k1ngtle.taticalsuit.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Server-safe logic for loading and returning player weapons in the WorkbenchMenu.
 * Extracted from WorkbenchMenu so the menu class only handles slot registration.
 * No client-only imports — safe to use on dedicated servers.
 */
public class WeaponScanner {

    /**
     * Called when the WorkbenchMenu opens (server-side only).
     * Scans the player's inventory and pulls the first primary into slot 0
     * and first sidearm into slot 1, removing them from the player's inventory.
     */
    public static void scan(Inventory playerInventory, ItemStackHandler slots) {
        boolean primaryFound = false;
        boolean sidearmFound = false;

        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (!WeaponClassifier.isPointBlankGun(stack)) continue;

            if (!primaryFound && WeaponClassifier.isPrimary(stack)) {
                slots.setStackInSlot(0, stack.copy());
                playerInventory.setItem(i, ItemStack.EMPTY);
                primaryFound = true;
            } else if (!sidearmFound && WeaponClassifier.isSidearm(stack)) {
                slots.setStackInSlot(1, stack.copy());
                playerInventory.setItem(i, ItemStack.EMPTY);
                sidearmFound = true;
            }

            if (primaryFound && sidearmFound) break;
        }
    }

    /**
     * Called when the WorkbenchMenu closes (server-side only).
     * Weapons go to hotbar slots 0 and 1; all other items return to general inventory.
     */
    public static void returnToPlayer(Player player, ItemStackHandler slots) {
        // 1. Clear hotbar slots 0/1 safely — push any existing item into inventory first
        for (int i = 0; i <= 1; i++) {
            ItemStack existing = player.getInventory().getItem(i);
            if (!existing.isEmpty()) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
                if (!player.getInventory().add(existing)) player.drop(existing, false);
            }
        }

        // 2. Force weapons directly into hotbar slots 0 and 1
        //    The WorkbenchTransferTick tag forces Point Blank to re-initialize its capability,
        //    ensuring attachments stored in "sa"/"as" NBT are applied on re-equip.
        for (int i = 0; i <= 1; i++) {
            ItemStack gun = slots.getStackInSlot(i);
            if (!gun.isEmpty()) {
                ItemStack transfer = gun.copy();
                if (transfer.getTag() != null) {
                    transfer.getOrCreateTag().putLong("WorkbenchTransferTick", System.currentTimeMillis());
                }
                player.getInventory().setItem(i, transfer);
                slots.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        // 3. Return all other items (armor, munitions, etc.) to general inventory
        for (int i = 2; i < slots.getSlots(); i++) {
            ItemStack stack = slots.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (!player.getInventory().add(stack.copy())) player.drop(stack, false);
                slots.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        player.getInventory().setChanged();
    }
}