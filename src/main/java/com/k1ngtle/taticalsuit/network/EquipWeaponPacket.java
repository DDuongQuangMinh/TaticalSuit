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

public class EquipWeaponPacket {

    private final int menuSlotIndex; 
    private final String weaponId; 
    private final int oldWeaponInvIndex;

    public EquipWeaponPacket(int menuSlotIndex, String weaponId, int oldWeaponInvIndex) {
        this.menuSlotIndex = menuSlotIndex;
        this.weaponId = weaponId;
        this.oldWeaponInvIndex = oldWeaponInvIndex;
    }

    public static void encode(EquipWeaponPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.menuSlotIndex);
        buf.writeUtf(msg.weaponId);
        buf.writeInt(msg.oldWeaponInvIndex);
    }

    public static EquipWeaponPacket decode(FriendlyByteBuf buf) {
        return new EquipWeaponPacket(buf.readInt(), buf.readUtf(), buf.readInt());
    }

    public static void handle(EquipWeaponPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.containerMenu instanceof WorkbenchMenu menu) {
                
                ResourceLocation reqLoc = ResourceLocation.parse(msg.weaponId);
                Optional<Item> optionalItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(reqLoc);
                
                if (optionalItem.isPresent()) {
                    Item requestedItem = optionalItem.get();
                    
                    // 1. DELETE THE OLD GHOST GUN FROM INVENTORY (If it exists)
                    if (msg.oldWeaponInvIndex >= 0 && msg.oldWeaponInvIndex < player.getInventory().getContainerSize()) {
                        ItemStack oldStack = player.getInventory().getItem(msg.oldWeaponInvIndex);
                        ResourceLocation oldLoc = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(oldStack.getItem());
                        
                        // Only delete if it's NOT the exact same gun (to preserve attachments if they re-click it)
                        if (oldLoc != null && !oldLoc.equals(reqLoc)) {
                            player.getInventory().setItem(msg.oldWeaponInvIndex, ItemStack.EMPTY);
                        }
                    }

                    // 2. WIPE THE OLD GUN FROM THE MENU SLOT (Prevents hoarding when swapping!)
                    ItemStack currentInMenu = menu.getSlot(msg.menuSlotIndex).getItem();
                    ResourceLocation currentMenuLoc = currentInMenu.isEmpty() ? null : net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(currentInMenu.getItem());
                    
                    if (currentMenuLoc != null && !currentMenuLoc.equals(reqLoc)) {
                        // Erase the old gun completely so it doesn't duplicate into their inventory.
                        menu.getSlot(msg.menuSlotIndex).set(ItemStack.EMPTY);
                    } else if (currentMenuLoc != null && currentMenuLoc.equals(reqLoc)) {
                        // If they clicked the gun they are ALREADY holding, do nothing.
                        return; 
                    }

                    // 3. EQUIP THE NEW GUN
                    ItemStack weaponToEquip = ItemStack.EMPTY;
                    
                    // Try to find the requested gun in their inventory (to preserve their attachments)
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack invStack = player.getInventory().getItem(i);
                        if (invStack.getItem() == requestedItem) {
                            weaponToEquip = invStack.copy();
                            player.getInventory().setItem(i, ItemStack.EMPTY); // Extract it
                            break;
                        }
                    }
                    
                    // If they don't own it, give them a fresh new one
                    if (weaponToEquip.isEmpty()) {
                        weaponToEquip = new ItemStack(requestedItem);
                    }
                    
                    // Put it into the UI slot
                    menu.getSlot(msg.menuSlotIndex).set(weaponToEquip);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}