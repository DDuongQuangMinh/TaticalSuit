package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.TaticalSuit;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Central registration point for this mod's network packets.
 */
public class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    // Notice we changed the name from "main" to "main_channel" and used the correct 1.20.1 ResourceLocation syntax!
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TaticalSuit.MODID, "main_channel"), 
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