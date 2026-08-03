package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item;
import com.k1ngtle.taticalsuit.item.HelmetItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import com.k1ngtle.taticalsuit.item.HelmetGhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetSandItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class HeadwearNetwork {
    private static final String PROTOCOL_VERSION = "1";
    
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TaticalSuit.MODID, "headwear_channel"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        CHANNEL.messageBuilder(EquipHelmetPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .decoder(EquipHelmetPacket::new)
                .encoder(EquipHelmetPacket::toBytes)
                .consumerMainThread(EquipHelmetPacket::handle)
                .add();
    }

    public static class EquipHelmetPacket {
        public final String targetId;
        public final String phosphor;

        public EquipHelmetPacket(String targetId, String phosphor) {
            this.targetId = targetId;
            this.phosphor = phosphor;
        }

        public EquipHelmetPacket(FriendlyByteBuf buf) {
            this.targetId = buf.readUtf();
            this.phosphor = buf.readUtf();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeUtf(this.targetId);
            buf.writeUtf(this.phosphor);
        }

        public boolean handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;

                ItemStack currentHead = player.getItemBySlot(EquipmentSlot.HEAD);

                // 1. UNEQUIP SCENARIO
                if (targetId.equals("NONE")) {
                    // Clear EVERY tactical helmet from the player's inventory completely
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (isTacticalHelmet(stack)) {
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                        }
                    }
                    player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1.0f, 1.0f);
                    return;
                }

                // 2. EQUIP SCENARIO
                ResourceLocation loc = new ResourceLocation(targetId);
                net.minecraft.world.item.Item targetItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(loc);
                if (targetItem == null || targetItem == net.minecraft.world.item.Items.AIR) return;

                // If they are already wearing this exact item, just update the phosphor NBT and exit
                if (currentHead.getItem() == targetItem) {
                    currentHead.getOrCreateTag().putString("phosphor", phosphor);
                    return;
                }

                // Strip off whatever is currently on their head without returning it to the inventory
                if (!currentHead.isEmpty()) {
                    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }

                // Scan inventory for the target helmet
                ItemStack foundStack = ItemStack.EMPTY;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.getItem() == targetItem) {
                        foundStack = stack.copy();
                        player.getInventory().setItem(i, ItemStack.EMPTY); // Remove it from inventory
                        break;
                    }
                }

                // If not found in inventory, spawn a new one (Workbench Magic)
                if (foundStack.isEmpty()) {
                    foundStack = new ItemStack(targetItem);
                }

                // Append the requested phosphor color directly to the helmet
                foundStack.getOrCreateTag().putString("phosphor", phosphor);

                // Equip the new helmet
                player.setItemSlot(EquipmentSlot.HEAD, foundStack);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON, SoundSource.PLAYERS, 1.0f, 1.0f);
            });
            return true;
        }

        private boolean isTacticalHelmet(ItemStack stack) {
            return stack.getItem() instanceof HelmetItem || 
                   stack.getItem() instanceof HelmetPVS31Item || 
                   stack.getItem() instanceof HelmetGPNVG18Item ||
                   stack.getItem() instanceof HelmetGhillieItem ||
                   stack.getItem() instanceof HelmetSandItem;
        }
    }
}