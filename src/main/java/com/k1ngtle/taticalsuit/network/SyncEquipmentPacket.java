package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.capability.TacticalEquipmentProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEquipmentPacket {
    private final int entityId;
    private final CompoundTag tag;

    public SyncEquipmentPacket(int entityId, CompoundTag tag) {
        this.entityId = entityId;
        this.tag = tag;
    }

    public SyncEquipmentPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.tag = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeNbt(this.tag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(this.entityId);
                if (entity instanceof LivingEntity living) {
                    living.getCapability(TacticalEquipmentProvider.CAPABILITY).ifPresent(cap -> {
                        cap.deserializeNBT(this.tag);
                    });
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}