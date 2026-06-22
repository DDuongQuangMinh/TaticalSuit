package com.k1ngtle.taticalsuit.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Central registration point for this mod's network packets.
 * Call ModNetworking.register() once during mod setup (e.g. in your main mod
 * class's constructor or FMLCommonSetupEvent).
 */
public class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("taticalsuit", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        CHANNEL.messageBuilder(EquipWeaponPacket.class, nextId())
                .encoder(EquipWeaponPacket::encode)
                .decoder(EquipWeaponPacket::decode)
                .consumerMainThread(EquipWeaponPacket::handle)
                .add();
    }
}