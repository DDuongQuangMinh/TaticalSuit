package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.menu.TacticalEquipmentMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenEquipmentMenuPacket {
    public OpenEquipmentMenuPacket() {}

    public OpenEquipmentMenuPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                NetworkHooks.openScreen(player, new SimpleMenuProvider(
                        (id, inv, p) -> new TacticalEquipmentMenu(id, inv),
                        Component.literal("Tactical Equipment")
                ));
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}