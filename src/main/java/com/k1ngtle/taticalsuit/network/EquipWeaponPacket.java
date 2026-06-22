package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class EquipWeaponPacket {

    private final int menuSlotIndex; 
    private final String weaponId; 
    private final boolean isAttachment;
    private final String attachmentCategory;

    public EquipWeaponPacket(int menuSlotIndex, String itemId, boolean isAttachment, String attachmentCategory) {
        this.menuSlotIndex = menuSlotIndex;
        this.weaponId = itemId;
        this.isAttachment = isAttachment;
        this.attachmentCategory = attachmentCategory;
    }

    public EquipWeaponPacket(int menuSlotIndex, String weaponId) {
        this(menuSlotIndex, weaponId, false, "");
    }

    public static void encode(EquipWeaponPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.menuSlotIndex);
        buf.writeUtf(msg.weaponId);
        buf.writeBoolean(msg.isAttachment);
        if (msg.isAttachment) {
            buf.writeUtf(msg.attachmentCategory);
        }
    }

    public static EquipWeaponPacket decode(FriendlyByteBuf buf) {
        int slot = buf.readInt();
        String wId = buf.readUtf();
        boolean isAtt = buf.readBoolean();
        String cat = isAtt ? buf.readUtf() : "";
        return new EquipWeaponPacket(slot, wId, isAtt, cat);
    }

    public static void handle(EquipWeaponPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.containerMenu instanceof WorkbenchMenu menu) {
                
                boolean isPrimary = msg.menuSlotIndex == 0;
                
                // --- BUG 4 FIX (ATTACHMENTS FAILING) ---
                if (msg.isAttachment) {
                    // CRUCIAL: We MUST use .copy() so Minecraft detects it as a new object. 
                    // Otherwise, it thinks the slot didn't change and refuses to sync the GUI!
                    ItemStack weaponToMod = menu.getSlot(msg.menuSlotIndex).getItem().copy();
                    
                    if (!weaponToMod.isEmpty()) {
                        if (msg.weaponId.equals("NONE")) {
                            weaponToMod.getOrCreateTag().remove(msg.attachmentCategory);
                        } else {
                            weaponToMod.getOrCreateTag().putString(msg.attachmentCategory, msg.weaponId);
                        }
                        
                        // Set the newly created copy back into the menu slot
                        menu.getSlot(msg.menuSlotIndex).set(weaponToMod);
                        menu.broadcastChanges(); // This now guarantees the client GUI gets the update!
                    }
                    return; 
                }

                // --- BUG 1 & 2 FIX (OLD GUNS AND DUPLICATION) ---
                ResourceLocation reqLoc = new ResourceLocation(msg.weaponId);
                Item requestedItem = ForgeRegistries.ITEMS.getValue(reqLoc);
                
                if (requestedItem != null && requestedItem != net.minecraft.world.item.Items.AIR) {
                    
                    ItemStack currentInMenu = menu.getSlot(msg.menuSlotIndex).getItem();
                    ResourceLocation currentMenuLoc = ForgeRegistries.ITEMS.getKey(currentInMenu.getItem());
                    
                    if (currentMenuLoc != null && currentMenuLoc.equals(reqLoc)) return; 
                    
                    // Annihilate the old weapon in the UI to prevent overlapping
                    menu.getSlot(msg.menuSlotIndex).set(ItemStack.EMPTY);
                    ItemStack weaponToEquip = ItemStack.EMPTY;
                    
                    // DEEP SCAN: Eradicate ALL existing guns of this type from the player's inventory
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack invStack = player.getInventory().getItem(i);
                        if (!invStack.isEmpty()) {
                            ResourceLocation invLoc = ForgeRegistries.ITEMS.getKey(invStack.getItem());
                            if (invLoc != null && "pointblank".equals(invLoc.getNamespace())) {
                                String path = invLoc.getPath().toLowerCase();
                                boolean isInvSidearm = path.contains("glock") || path.contains("m1911") || path.contains("deserteagle") || path.contains("m9") || path.contains("p30l") || path.contains("viper") || path.contains("m17") || path.contains("p250") || path.contains("fn509") || path.contains("usp45") || path.contains("fnx45");
                                boolean isInvPrimary = !isInvSidearm && !path.contains("mag") && !path.contains("ammo") && !path.contains("grenade");
                                
                                if ((isPrimary && isInvPrimary) || (!isPrimary && isInvSidearm)) {
                                    if (invLoc.equals(reqLoc) && weaponToEquip.isEmpty()) {
                                        weaponToEquip = invStack.copy(); // Rescue the gun if they already have it
                                    }
                                    
                                    // Physically delete the old gun from the player's inventory
                                    player.getInventory().setItem(i, ItemStack.EMPTY); 
                                    
                                    // Send a sync packet so the client's GUI visually removes the old gun instantly
                                    int syncSlotId = (i >= 0 && i <= 8) ? 36 + i : i; 
                                    player.connection.send(new ClientboundContainerSetSlotPacket(
                                        player.inventoryMenu.containerId, player.inventoryMenu.getStateId(),
                                        syncSlotId, ItemStack.EMPTY
                                    ));
                                }
                            }
                        }
                    }
                    
                    if (weaponToEquip.isEmpty()) weaponToEquip = new ItemStack(requestedItem);
                    
                    // We ONLY set the new gun in the active Menu slot. 
                    // Do NOT set it in the Hotbar here. The WorkbenchMenu#removed event will handle that cleanly.
                    menu.getSlot(msg.menuSlotIndex).set(weaponToEquip);
                    menu.broadcastChanges(); 
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}