package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class NVGNetwork {
    private static final String PROTOCOL_VERSION = "1";
    
    // CHANGED "main" to "nvg_channel" to prevent the duplicate registration crash!
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TaticalSuit.MODID, "nvg_channel"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        CHANNEL.messageBuilder(TogglePacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .decoder(TogglePacket::new)
                .encoder(TogglePacket::toBytes)
                .consumerMainThread(TogglePacket::handle)
                .add();
    }

    public static class TogglePacket {
        public TogglePacket() {}
        public TogglePacket(FriendlyByteBuf buf) {}
        public void toBytes(FriendlyByteBuf buf) {}

        public boolean handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                    if (helmet.getItem() instanceof HelmetPVS31Item) {
                        CompoundTag tag = helmet.getOrCreateTag();
                        tag.putBoolean("nvg_active", !tag.getBoolean("nvg_active"));
                    }
                }
            });
            return true;
        }
    }

    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        public static final KeyMapping TOGGLE_KEY = new KeyMapping(
                "key.taticalsuit.toggle_nvg", KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.taticalsuit.keys");

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (TOGGLE_KEY.consumeClick()) {
                CHANNEL.sendToServer(new TogglePacket());
            }
        }
    }

    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(ClientEvents.TOGGLE_KEY);
        }
    }
}