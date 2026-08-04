package com.k1ngtle.taticalsuit.event;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.capability.TacticalEquipmentProvider;
import com.k1ngtle.taticalsuit.network.EquipmentNetwork;
import com.k1ngtle.taticalsuit.network.SyncEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = TaticalSuit.MODID)
public class TacticalEquipmentEvents {

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!player.getCapability(TacticalEquipmentProvider.CAPABILITY).isPresent()) {
                event.addCapability(new ResourceLocation(TaticalSuit.MODID, "tactical_equipment"), new TacticalEquipmentProvider(player));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(TacticalEquipmentProvider.CAPABILITY).ifPresent(oldCap -> {
            event.getEntity().getCapability(TacticalEquipmentProvider.CAPABILITY).ifPresent(newCap -> {
                newCap.deserializeNBT(oldCap.serializeNBT());
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            player.getCapability(TacticalEquipmentProvider.CAPABILITY).ifPresent(cap -> {
                for (int i = 0; i < cap.getSlots(); i++) {
                    ItemStack stack = cap.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        player.drop(stack, true, false);
                        cap.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        syncPlayer((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncPlayer((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncPlayer((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player target && event.getEntity() instanceof ServerPlayer observer) {
            target.getCapability(TacticalEquipmentProvider.CAPABILITY).ifPresent(cap -> {
                EquipmentNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> observer), 
                    new SyncEquipmentPacket(target.getId(), cap.serializeNBT()));
            });
        }
    }

    private static void syncPlayer(ServerPlayer player) {
        player.getCapability(TacticalEquipmentProvider.CAPABILITY).ifPresent(cap -> {
            EquipmentNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), 
                new SyncEquipmentPacket(player.getId(), cap.serializeNBT()));
        });
    }
}