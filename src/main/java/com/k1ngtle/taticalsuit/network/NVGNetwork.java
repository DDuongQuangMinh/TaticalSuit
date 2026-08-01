package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
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
                    
                    // --- UPDATED: Check for both PVS-31 and GPNVG-18 ---
                    if (helmet.getItem() instanceof HelmetPVS31Item || helmet.getItem() instanceof HelmetGPNVG18Item) {
                        CompoundTag tag = helmet.getOrCreateTag();
                        boolean isActive = !tag.getBoolean("nvg_active");
                        tag.putBoolean("nvg_active", isActive);
                        
                        player.level().playSound(null, player.blockPosition(), 
                                SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS, 0.4f, isActive ? 1.2f : 0.8f);
                        
                        if (!isActive) {
                            player.removeEffect(MobEffects.NIGHT_VISION);
                        }
                    }
                }
            });
            return true;
        }
    }

    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
                ItemStack helmet = event.player.getItemBySlot(EquipmentSlot.HEAD);
                
                // --- UPDATED: Apply Night Vision Potion Effect for both helmets ---
                if ((helmet.getItem() instanceof HelmetPVS31Item || helmet.getItem() instanceof HelmetGPNVG18Item) && helmet.getOrCreateTag().getBoolean("nvg_active")) {
                    event.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 250, 0, false, false, false));
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        public static final KeyMapping TOGGLE_KEY = new KeyMapping(
                "key.taticalsuit.toggle_nvg", KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.taticalsuit.keys");

        private static final ResourceLocation TARGET_SHADER = new ResourceLocation(TaticalSuit.MODID, "shaders/post/nv_green.json");

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (TOGGLE_KEY.consumeClick()) {
                CHANNEL.sendToServer(new TogglePacket());
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
            
            // --- UPDATED: Load the Shader for both helmets ---
            boolean isWearingActiveNVG = (helmet.getItem() instanceof HelmetPVS31Item || helmet.getItem() instanceof HelmetGPNVG18Item) 
                                         && helmet.getOrCreateTag().getBoolean("nvg_active");

            if (isWearingActiveNVG) {
                if (mc.gameRenderer.currentEffect() == null || !mc.gameRenderer.currentEffect().getName().equals(TARGET_SHADER.toString())) {
                    mc.gameRenderer.loadEffect(TARGET_SHADER);
                }
            } else {
                if (mc.gameRenderer.currentEffect() != null && mc.gameRenderer.currentEffect().getName().equals(TARGET_SHADER.toString())) {
                    mc.gameRenderer.shutdownEffect();
                }
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