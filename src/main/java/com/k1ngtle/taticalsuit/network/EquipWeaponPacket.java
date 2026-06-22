package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent from client -> server when the player picks a weapon in the Weapon
 * Selection dropdown. Tells the server to swap the menu slot with the selected
 * item from the player's personal inventory.
 */
public class EquipWeaponPacket {

    private final int menuSlotIndex; // 0 for Primary UI Slot, 1 for Side Arm UI Slot
    private final int playerInvIndex; // The literal inventory slot index the item is in

    public EquipWeaponPacket(int menuSlotIndex, int playerInvIndex) {
        this.menuSlotIndex = menuSlotIndex;
        this.playerInvIndex = playerInvIndex;
    }

    public static void encode(EquipWeaponPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.menuSlotIndex);
        buf.writeInt(msg.playerInvIndex);
    }

    public static EquipWeaponPacket decode(FriendlyByteBuf buf) {
        return new EquipWeaponPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(EquipWeaponPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            
            // Security Checks
            if (player != null && player.containerMenu instanceof WorkbenchMenu menu) {
                
                // 1. Get the gun currently sitting in the Workbench UI
                ItemStack currentInMenu = menu.getSlot(msg.menuSlotIndex).getItem().copy();
                
                // 2. Get the specific gun the player clicked in their inventory
                ItemStack selectedFromInv = player.getInventory().getItem(msg.playerInvIndex).copy();
                
                // 3. PERFECT SWAP: This perfectly preserves NBT data (Point Blank attachments, loaded ammo, wear/tear).
                // It fixes the "duplicate item" and "old item not returning" bugs because the old gun is injected 
                // exactly where the new gun just came out of!
                menu.getSlot(msg.menuSlotIndex).set(selectedFromInv);
                player.getInventory().setItem(msg.playerInvIndex, currentInMenu);
            }
        });
        ctx.setPacketHandled(true);
    }
}