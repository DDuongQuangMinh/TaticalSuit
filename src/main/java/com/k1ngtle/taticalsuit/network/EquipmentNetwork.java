package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.TaticalSuit;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class EquipmentNetwork {
    private static final String PROTOCOL_VERSION = "1";
    
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TaticalSuit.MODID, "equipment_channel"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        // Packet to sync the hidden inventory to clients
        CHANNEL.messageBuilder(SyncEquipmentPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncEquipmentPacket::new)
                .encoder(SyncEquipmentPacket::toBytes)
                .consumerMainThread(SyncEquipmentPacket::handle)
                .add();

        // Packet to open the GUI from a client key press
        CHANNEL.messageBuilder(OpenEquipmentMenuPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenEquipmentMenuPacket::new)
                .encoder(OpenEquipmentMenuPacket::toBytes)
                .consumerMainThread(OpenEquipmentMenuPacket::handle)
                .add();
    }
}