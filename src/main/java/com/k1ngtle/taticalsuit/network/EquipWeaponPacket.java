package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
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
            if (player != null && player.containerMenu instanceof WorkbenchMenu menu) {
                
                ResourceLocation reqLoc = ResourceLocation.parse(msg.weaponId);
                Optional<Item> optionalItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(reqLoc);
                
                if (optionalItem.isPresent()) {
                    Item requestedItem = optionalItem.get();
                    boolean isPrimary = msg.menuSlotIndex == 0;
                    
                    // Slot 1 is Index 0 (Primary), Slot 2 is Index 1 (Sidearm)
                    int targetHotbarSlot = isPrimary ? 0 : 1; 
                    
                    ItemStack weaponToEquip = ItemStack.EMPTY;
                    
                    // 1. CLEAR THE UI SLOTS: 
                    // We must erase the weapon from the detached Workbench menu slots!
                    for (int i = 0; i <= 1; i++) {
                        ItemStack menuStack = menu.getSlot(i).getItem();
                        if (!menuStack.isEmpty()) {
                            ResourceLocation menuLoc = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(menuStack.getItem());
                            if (menuLoc != null && "pointblank".equals(menuLoc.getNamespace())) {
                                boolean isMenuSidearm = menuLoc.getPath().toLowerCase().contains("glock") || menuLoc.getPath().toLowerCase().contains("m1911") || menuLoc.getPath().toLowerCase().contains("deserteagle") || menuLoc.getPath().toLowerCase().contains("m9") || menuLoc.getPath().toLowerCase().contains("p30l") || menuLoc.getPath().toLowerCase().contains("viper") || menuLoc.getPath().toLowerCase().contains("m17") || menuLoc.getPath().toLowerCase().contains("p250") || menuLoc.getPath().toLowerCase().contains("fn509") || menuLoc.getPath().toLowerCase().contains("usp45") || menuLoc.getPath().toLowerCase().contains("fnx45");
                                boolean isMenuPrimary = !isMenuSidearm && !menuLoc.getPath().toLowerCase().contains("mag") && !menuLoc.getPath().toLowerCase().contains("ammo") && !menuLoc.getPath().toLowerCase().contains("grenade");
                                
                                if ((isPrimary && isMenuPrimary) || (!isPrimary && isMenuSidearm)) {
                                    if (menuLoc.equals(reqLoc) && weaponToEquip.isEmpty()) {
                                        weaponToEquip = menuStack.copy(); // Rescue attachments
                                    }
                                    menu.getSlot(i).set(ItemStack.EMPTY); // Wipe the ghost!
                                }
                            }
                        }
                    }

                    // 2. ENFORCER: Scan entire inventory, wipe old weapons of this type to guarantee no duplication.
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack invStack = player.getInventory().getItem(i);
                        if (!invStack.isEmpty()) {
                            ResourceLocation invLoc = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(invStack.getItem());
                            if (invLoc != null && "pointblank".equals(invLoc.getNamespace())) {
                                String path = invLoc.getPath().toLowerCase();
                                boolean isInvSidearm = path.contains("glock") || path.contains("m1911") || path.contains("deserteagle") || path.contains("m9") || path.contains("p30l") || path.contains("viper") || path.contains("m17") || path.contains("p250") || path.contains("fn509") || path.contains("usp45") || path.contains("fnx45");
                                boolean isInvPrimary = !isInvSidearm && !path.contains("mag") && !path.contains("ammo") && !path.contains("grenade");
                                
                                if ((isPrimary && isInvPrimary) || (!isPrimary && isInvSidearm)) {
                                    if (invLoc.equals(reqLoc) && weaponToEquip.isEmpty()) {
                                        weaponToEquip = invStack.copy(); // Rescue attachments
                                    }
                                    player.getInventory().setItem(i, ItemStack.EMPTY); // Wipe from bag to prevent hoarding!
                                    
                                    // INSTANT CLIENT SYNC: Tell the client we deleted this item!
                                    int syncSlotId = -1;
                                    if (i >= 0 && i <= 8) syncSlotId = 36 + i; // Hotbar slots (0-8) are mapped to 36-44 in InventoryMenu
                                    else if (i >= 9 && i <= 35) syncSlotId = i; // Main inventory slots (9-35) are mapped to 9-35
                                    
                                    if (syncSlotId != -1) {
                                        player.connection.send(new ClientboundContainerSetSlotPacket(
                                            player.inventoryMenu.containerId,
                                            player.inventoryMenu.getStateId(),
                                            syncSlotId,
                                            ItemStack.EMPTY
                                        ));
                                    }
                                }
                            }
                        }
                    }
                    
                    if (weaponToEquip.isEmpty()) {
                        weaponToEquip = new ItemStack(requestedItem);
                    }
                    
                    // 3. FORCE DIRECTLY INTO HOTBAR SLOT 1 OR 2
                    ItemStack existingInTarget = player.getInventory().getItem(targetHotbarSlot);
                    if (!existingInTarget.isEmpty() && existingInTarget.getItem() != requestedItem) {
                        // If there is an unrelated item (like dirt or food) in slot 1 or 2, move it to a free slot safely
                        player.getInventory().setItem(targetHotbarSlot, ItemStack.EMPTY);
                        player.getInventory().add(existingInTarget);
                        player.inventoryMenu.broadcastChanges(); // Broadcast changes for the randomly placed item
                    }
                    
                    // Place the gun explicitly in Hotbar Slot 1 (index 0) or Slot 2 (index 1)
                    player.getInventory().setItem(targetHotbarSlot, weaponToEquip);
                    
                    // INSTANT CLIENT SYNC: Push the newly created gun explicitly to the client!
                    // This forces the GUI to update itself instantly without needing to close the menu.
                    player.connection.send(new ClientboundContainerSetSlotPacket(
                        player.inventoryMenu.containerId,
                        player.inventoryMenu.getStateId(),
                        36 + targetHotbarSlot,
                        weaponToEquip
                    ));
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}