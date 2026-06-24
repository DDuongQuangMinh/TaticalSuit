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
                
                // --- ATTACHMENT MODIFICATION LOGIC ---
                if (msg.isAttachment) {
                    ItemStack weaponToMod = menu.getSlot(msg.menuSlotIndex).getItem().copy();

                    if (!weaponToMod.isEmpty()) {
                        net.minecraft.nbt.CompoundTag tag = weaponToMod.getOrCreateTag();

                        // ---------------------------------------------------------------
                        // VPB NBT STRUCTURE (verified by decompiling pointblank jar):
                        //
                        // gun.tag {
                        //   "sa": CompoundTag {              ← drives GUI selection state
                        //     "scope":       "pointblank:moa_sight",
                        //     "muzzle":      "pointblank:ar_muzzle_brake",
                        //     "underbarrel": "pointblank:foregrip",
                        //     "rail":        "pointblank:...",
                        //     ...
                        //   },
                        //   "as": ListTag [                  ← drives actual rendering/gameplay
                        //     CompoundTag { "id": "pointblank:moa_sight", "rmv": true },
                        //     CompoundTag { "id": "pointblank:ar_muzzle_brake", "rmv": true },
                        //     ...
                        //   ]
                        // }
                        //
                        // Screen sends category strings: "scope","muzzle","underbarrel","rail","stock","magazine"
                        // (mapped from OPTIC→scope, MUZZLE→muzzle, UNDERBARREL→underbarrel, LASER→rail)
                        // ---------------------------------------------------------------

                        // 1. Get or create the "sa" selected-attachments compound
                        net.minecraft.nbt.CompoundTag saTag = tag.contains("sa", net.minecraft.nbt.Tag.TAG_COMPOUND)
                                ? tag.getCompound("sa").copy()
                                : new net.minecraft.nbt.CompoundTag();

                        // 2. Get or create the "as" active-attachments list
                        net.minecraft.nbt.ListTag asList = tag.contains("as", net.minecraft.nbt.Tag.TAG_LIST)
                                ? tag.getList("as", net.minecraft.nbt.Tag.TAG_COMPOUND).copy()
                                : new net.minecraft.nbt.ListTag();

                        String vpbCategoryName = msg.attachmentCategory; // already "scope","muzzle","underbarrel","rail" etc.

                        if (msg.weaponId.equals("NONE")) {
                            // Remove from "sa"
                            saTag.remove(vpbCategoryName);

                            // Remove matching entry from "as" list by finding same category
                            // VPB identifies category via the attachment item's own AttachmentCategory;
                            // we remove any entry whose id resolves to an item in this category.
                            // Simplest correct approach: remove any entry whose id was previously
                            // set for this category (we track it in "sa" before removing above).
                            // Since we already cleared sa, we rebuild "as" without the removed item.
                            net.minecraft.nbt.ListTag newAsList = new net.minecraft.nbt.ListTag();
                            for (int k = 0; k < asList.size(); k++) {
                                net.minecraft.nbt.CompoundTag entry = asList.getCompound(k);
                                String entryId = entry.getString("id");
                                // If this entry's id was the one mapped to our category, skip it
                                // We can detect this by checking if the old sa value matches
                                // (The old sa value was already removed, so we stored it before removal)
                                // Instead, keep all entries that don't belong to this category:
                                // Re-check via ForgeRegistries if the item is in the same category
                                ResourceLocation entryLoc = new ResourceLocation(entryId.isEmpty() ? "minecraft:air" : entryId);
                                Item entryItem = ForgeRegistries.ITEMS.getValue(entryLoc);
                                if (entryItem != null && entryItem != net.minecraft.world.item.Items.AIR) {
                                    // Keep the entry only if it's NOT in the category we're clearing
                                    // We check by seeing if its path contains category-related keywords
                                    String entryPath = entryLoc.getPath().toLowerCase();
                                    boolean isThisCategory = isItemInCategory(entryPath, vpbCategoryName);
                                    if (!isThisCategory) {
                                        newAsList.add(entry);
                                    }
                                }
                                // If item is unknown/air, skip it (stale entry)
                            }
                            asList = newAsList;

                        } else {
                            Item attItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(msg.weaponId));
                            if (attItem != null && attItem != net.minecraft.world.item.Items.AIR) {

                                // --- Update "sa": put the ResourceLocation string keyed by category name ---
                                saTag.putString(vpbCategoryName, msg.weaponId);

                                // --- Update "as": remove old entry for this category, add new one ---
                                net.minecraft.nbt.ListTag newAsList = new net.minecraft.nbt.ListTag();
                                for (int k = 0; k < asList.size(); k++) {
                                    net.minecraft.nbt.CompoundTag entry = asList.getCompound(k);
                                    String entryId = entry.getString("id");
                                    ResourceLocation entryLoc = new ResourceLocation(entryId.isEmpty() ? "minecraft:air" : entryId);
                                    Item entryItem = ForgeRegistries.ITEMS.getValue(entryLoc);
                                    if (entryItem != null && entryItem != net.minecraft.world.item.Items.AIR) {
                                        String entryPath = entryLoc.getPath().toLowerCase();
                                        if (!isItemInCategory(entryPath, vpbCategoryName)) {
                                            newAsList.add(entry);
                                        }
                                        // else: drop the old entry for this category — we're replacing it
                                    }
                                }
                                // Add new entry: { "id": "pointblank:moa_sight", "rmv": true }
                                net.minecraft.nbt.CompoundTag newEntry = new net.minecraft.nbt.CompoundTag();
                                newEntry.putString("id", msg.weaponId);
                                newEntry.putBoolean("rmv", true); // removeable = true for player-chosen attachments
                                // Optionally merge the attachment item's own tag data (for items that carry extra NBT)
                                ItemStack attStack = new ItemStack(attItem);
                                if (attStack.hasTag()) {
                                    net.minecraft.nbt.CompoundTag attTag = attStack.getTag();
                                    for (String key : attTag.getAllKeys()) {
                                        newEntry.put(key, attTag.get(key).copy());
                                    }
                                }
                                newAsList.add(newEntry);
                                asList = newAsList;
                            }
                        }

                        // Write both structures back to the gun tag
                        tag.put("sa", saTag);
                        tag.put("as", asList);

                        menu.getSlot(msg.menuSlotIndex).set(weaponToMod);
                        menu.broadcastChanges();

                        player.connection.send(new ClientboundContainerSetSlotPacket(
                            menu.containerId, menu.incrementStateId(),
                            msg.menuSlotIndex, weaponToMod
                        ));
                    }
                    return;
                }

                // --- WEAPON SWAPPING LOGIC ---
                ResourceLocation reqLoc = new ResourceLocation(msg.weaponId);
                Item requestedItem = ForgeRegistries.ITEMS.getValue(reqLoc);
                
                if (requestedItem != null && requestedItem != net.minecraft.world.item.Items.AIR) {
                    
                    ItemStack currentInMenu = menu.getSlot(msg.menuSlotIndex).getItem();
                    ResourceLocation currentMenuLoc = ForgeRegistries.ITEMS.getKey(currentInMenu.getItem());
                    
                    if (currentMenuLoc != null && currentMenuLoc.equals(reqLoc)) return; 
                    
                    // Safely return current gun to inventory to prevent deletion!
                    if (!currentInMenu.isEmpty()) {
                        if (!player.getInventory().add(currentInMenu.copy())) {
                            player.drop(currentInMenu.copy(), false);
                        }
                    }
                    menu.getSlot(msg.menuSlotIndex).set(ItemStack.EMPTY);

                    ItemStack weaponToEquip = ItemStack.EMPTY;
                    
                    // Scan inventory to rescue customized gun
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
                                        weaponToEquip = invStack.copy(); 
                                    }
                                    player.getInventory().setItem(i, ItemStack.EMPTY); 
                                    
                                    int syncSlotId = (i >= 0 && i <= 8) ? 36 + i : i; 
                                    player.connection.send(new ClientboundContainerSetSlotPacket(
                                        player.inventoryMenu.containerId, player.inventoryMenu.getStateId(),
                                        syncSlotId, ItemStack.EMPTY
                                    ));
                                }
                            }
                        }
                    }
                    
                    if (weaponToEquip.isEmpty()) weaponToEquip = requestedItem.getDefaultInstance().copy();
                    
                    // 1. Update Menu Slot
                    menu.getSlot(msg.menuSlotIndex).set(weaponToEquip);

                    menu.broadcastChanges(); 

                    // Sync Menu Slot explicitly
                    player.connection.send(new ClientboundContainerSetSlotPacket(
                        menu.containerId, menu.incrementStateId(),
                        msg.menuSlotIndex, weaponToEquip
                    ));
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    /**
     * Determines if an attachment item's registry path belongs to a given VPB category name.
     * VPB category names: "scope", "muzzle", "underbarrel", "rail", "stock", "magazine", "skin"
     */
    private static boolean isItemInCategory(String itemPath, String vpbCategoryName) {
        return switch (vpbCategoryName) {
            case "scope" -> itemPath.contains("scope") || itemPath.contains("sight") || itemPath.contains("optic")
                    || itemPath.contains("reflex") || itemPath.contains("holo") || itemPath.contains("acog")
                    || itemPath.contains("dot") || itemPath.contains("rmr") || itemPath.contains("sro")
                    || itemPath.contains("micro") || itemPath.contains("deltapoint");
            case "muzzle" -> itemPath.contains("muzzle") || itemPath.contains("silencer")
                    || itemPath.contains("suppressor") || itemPath.contains("compensator")
                    || itemPath.contains("flash") || itemPath.contains("brake");
            case "underbarrel" -> itemPath.contains("grip") || itemPath.contains("underbarrel")
                    || itemPath.contains("foregrip") || itemPath.contains("bipod") || itemPath.contains("angled");
            case "rail" -> itemPath.contains("laser") || itemPath.contains("tactical")
                    || itemPath.contains("light") || itemPath.contains("peq") || itemPath.contains("flashlight")
                    || itemPath.contains("tlr") || itemPath.contains("x300");
            case "stock" -> itemPath.contains("stock") || itemPath.contains("brace") || itemPath.contains("buffer");
            case "magazine" -> itemPath.contains("mag") || itemPath.contains("magazine")
                    || itemPath.contains("drum") || itemPath.contains("extended");
            case "skin" -> itemPath.contains("skin") || itemPath.contains("camo") || itemPath.contains("wrap");
            default -> false;
        };
    }
}