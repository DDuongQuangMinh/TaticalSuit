package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Sent from client -> server when the player picks a weapon in the Weapon
 * Selection dropdown. Tells the server to swap the menu slot with the selected
 * weapon ID.
 */
public class EquipWeaponPacket {

    private final int menuSlotIndex; // 0 for Primary UI Slot, 1 for Side Arm UI Slot
    private final String weaponId; // The String ID of the item (e.g., "pointblank:m4a1")

    public EquipWeaponPacket(int menuSlotIndex, String weaponId) {
        this.menuSlotIndex = menuSlotIndex;
        this.weaponId = weaponId;
    }

    public static void encode(EquipWeaponPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.menuSlotIndex);
        buf.writeUtf(msg.weaponId);
    }

    public static EquipWeaponPacket decode(FriendlyByteBuf buf) {
        return new EquipWeaponPacket(buf.readInt(), buf.readUtf());
    }

    public static void handle(EquipWeaponPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            
            // Security Checks
            if (player != null && player.containerMenu instanceof WorkbenchMenu menu) {
                
                // Parse the item from the String ID
                ResourceLocation loc = ResourceLocation.parse(msg.weaponId);
                Optional<Item> optionalItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(loc);
                
                if (optionalItem.isPresent()) {
                    // 1. Get the gun currently sitting in the Workbench UI
                    ItemStack currentInMenu = menu.getSlot(msg.menuSlotIndex).getItem().copy();
                    
                    // 2. Return the old gun to the player's inventory so nothing gets deleted
                    if (!currentInMenu.isEmpty()) {
                        if (!player.getInventory().add(currentInMenu)) {
                            player.drop(currentInMenu, false); // Drop on ground if inventory is completely full
                        }
                    }
                    
                    // 3. Equip the newly selected gun into the Workbench slot!
                    menu.getSlot(msg.menuSlotIndex).set(new ItemStack(optionalItem.get()));
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}