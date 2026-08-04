package com.k1ngtle.taticalsuit.capability;

import com.k1ngtle.taticalsuit.network.EquipmentNetwork;
import com.k1ngtle.taticalsuit.network.SyncEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;

public class TacticalEquipmentHandler extends ItemStackHandler {
    
    private final LivingEntity entity;

    public TacticalEquipmentHandler(LivingEntity entity) {
        super(EquipmentSlotType.values().length);
        this.entity = entity;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (this.entity instanceof ServerPlayer player) {
            EquipmentNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), 
                new SyncEquipmentPacket(player.getId(), this.serializeNBT()));
        }
    }
}