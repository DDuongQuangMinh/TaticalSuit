package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetItem;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SandItem;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SnowItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetGhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetSandItem;
import com.k1ngtle.taticalsuit.item.HelmetSnowItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class SquadNetwork {
    private static final String PROTOCOL_VERSION = "1";
    
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryParse(TaticalSuit.MODID + ":squad_channel"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        CHANNEL.messageBuilder(UpdateSquadPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateSquadPacket::new)
                .encoder(UpdateSquadPacket::toBytes)
                .consumerMainThread(UpdateSquadPacket::handle)
                .add();
    }

    public static class UpdateSquadPacket {
        public final String squadName;

        public UpdateSquadPacket(String squadName) {
            this.squadName = squadName;
        }

        public UpdateSquadPacket(FriendlyByteBuf buf) {
            this.squadName = buf.readUtf();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeUtf(this.squadName);
        }

        public boolean handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                    
                    if (helmet.getItem() instanceof HelmetItem || 
                        helmet.getItem() instanceof HelmetPVS31Item || 
                        helmet.getItem() instanceof HelmetGPNVG18Item ||
                        helmet.getItem() instanceof HelmetGPNVG18GhillieItem ||
                        helmet.getItem() instanceof HelmetGPNVG18SandItem ||
                        helmet.getItem() instanceof HelmetGPNVG18SnowItem ||
                        helmet.getItem() instanceof HelmetGhillieItem ||
                        helmet.getItem() instanceof HelmetSandItem ||
                        helmet.getItem() instanceof HelmetSnowItem) {
                        
                        CompoundTag tag = helmet.getOrCreateTag();
                        tag.putString("squad_name", squadName);
                        
                        player.level().playSound(null, player.blockPosition(), 
                                SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS, 0.5f, 1.2f);
                    }
                }
            });
            return true;
        }
    }
}