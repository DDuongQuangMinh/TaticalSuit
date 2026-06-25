package com.k1ngtle.taticalsuit.menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Server-safe utility for classifying Point Blank weapon and munition stacks.
 * This is the single source of truth for weapon type detection, replacing duplicate
 * keyword-matching spread across WorkbenchMenu, EquipWeaponPacket, and WorkbenchScreen.
 * No client-only imports — safe to use on dedicated servers.
 */
public class WeaponClassifier {

    // Must stay in sync with WorkbenchData.SIDEARM_WEAPON_IDS (client-side mirror)
    private static final java.util.Set<String> SIDEARM_IDS = java.util.Set.of(
        "pointblank:glock17", "pointblank:glock18", "pointblank:m9",  "pointblank:m1911a1",
        "pointblank:tti_viper", "pointblank:p30l",  "pointblank:mk23", "pointblank:deserteagle",
        "pointblank:rhino"
    );

    /** True if the stack is any Point Blank item that is NOT ammo, mag, or grenade. */
    public static boolean isPointBlankGun(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation loc = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (loc == null || !"pointblank".equals(loc.getNamespace())) return false;
        String path = loc.getPath();
        return !path.contains("mag") && !path.contains("ammo") && !path.contains("grenade");
    }

    /** True if the stack is a Point Blank sidearm (pistol). */
    public static boolean isSidearm(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation loc = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return loc != null && SIDEARM_IDS.contains(loc.toString());
    }

    /** True if the stack is a Point Blank primary weapon (rifle, SMG, LMG, etc.). */
    public static boolean isPrimary(ItemStack stack) {
        return isPointBlankGun(stack) && !isSidearm(stack);
    }

    /** True if the stack is a Point Blank munition (ammo, magazine, or grenade). */
    public static boolean isMunition(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation loc = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (loc == null || !"pointblank".equals(loc.getNamespace())) return false;
        String path = loc.getPath();
        return path.contains("mag") || path.contains("ammo") || path.contains("grenade");
    }

    /** Counts Point Blank munition stacks in the given inventory, capped at 13. */
    public static int countMunitions(Inventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (isMunition(inventory.getItem(i))) count++;
        }
        return Math.min(count, 13);
    }
}