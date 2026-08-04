package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SandItem;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SnowItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import com.k1ngtle.taticalsuit.item.HelmetPVS31GhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31SandItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31SnowItem;
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
            new ResourceLocation(TaticalSuit.MODID + ":nvg_channel"),
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
                    
                    if (helmet.getItem() instanceof HelmetPVS31Item || helmet.getItem() instanceof HelmetPVS31GhillieItem || helmet.getItem() instanceof HelmetPVS31SandItem || helmet.getItem() instanceof HelmetPVS31SnowItem || helmet.getItem() instanceof HelmetGPNVG18Item || helmet.getItem() instanceof HelmetGPNVG18GhillieItem || helmet.getItem() instanceof HelmetGPNVG18SandItem || helmet.getItem() instanceof HelmetGPNVG18SnowItem) {
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
                
                boolean hasNVGHelmet = (helmet.getItem() instanceof HelmetPVS31Item || helmet.getItem() instanceof HelmetPVS31GhillieItem || helmet.getItem() instanceof HelmetPVS31SandItem || helmet.getItem() instanceof HelmetPVS31SnowItem || helmet.getItem() instanceof HelmetGPNVG18Item || helmet.getItem() instanceof HelmetGPNVG18GhillieItem || helmet.getItem() instanceof HelmetGPNVG18SandItem || helmet.getItem() instanceof HelmetGPNVG18SnowItem);
                boolean isNVGActive = hasNVGHelmet && helmet.hasTag() && helmet.getTag().getBoolean("nvg_active");

                if (isNVGActive) {
                    event.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 250, 0, false, false, false));
                } else {
                    // Instantly remove the night vision effect if they unequip the helmet!
                    if (event.player.hasEffect(MobEffects.NIGHT_VISION)) {
                        event.player.removeEffect(MobEffects.NIGHT_VISION);
                    }
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        public static final KeyMapping TOGGLE_KEY = new KeyMapping(
                "key.taticalsuit.toggle_nvg", KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.taticalsuit.keys");

        private static final ResourceLocation GREEN_SHADER = new ResourceLocation(TaticalSuit.MODID, "shaders/post/nv_green.json");
        private static final ResourceLocation WHITE_SHADER = new ResourceLocation(TaticalSuit.MODID, "shaders/post/nv_white.json");

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (TOGGLE_KEY.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
                    
                    if (helmet.getItem() instanceof HelmetPVS31Item || helmet.getItem() instanceof HelmetPVS31GhillieItem || helmet.getItem() instanceof HelmetPVS31SandItem || helmet.getItem() instanceof HelmetPVS31SnowItem || helmet.getItem() instanceof HelmetGPNVG18Item || helmet.getItem() instanceof HelmetGPNVG18GhillieItem || helmet.getItem() instanceof HelmetGPNVG18SandItem || helmet.getItem() instanceof HelmetGPNVG18SnowItem) {
                        CHANNEL.sendToServer(new TogglePacket());
                    } else {
                        // Optional: Show a message when trying to activate without the right helmet
                        mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[!] No Tactical NVG Equipped"), true);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
            
            boolean isWearingActiveNVG = (helmet.getItem() instanceof HelmetPVS31Item || helmet.getItem() instanceof HelmetPVS31GhillieItem || helmet.getItem() instanceof HelmetPVS31SandItem || helmet.getItem() instanceof HelmetPVS31SnowItem || helmet.getItem() instanceof HelmetGPNVG18Item || helmet.getItem() instanceof HelmetGPNVG18GhillieItem || helmet.getItem() instanceof HelmetGPNVG18SandItem || helmet.getItem() instanceof HelmetGPNVG18SnowItem) 
                                         && helmet.hasTag() && helmet.getTag().getBoolean("nvg_active");

            ResourceLocation targetShader = GREEN_SHADER;
            if (isWearingActiveNVG && helmet.hasTag() && "WHITE PHOSPHOR".equals(helmet.getTag().getString("phosphor"))) {
                targetShader = WHITE_SHADER;
            }

            if (isWearingActiveNVG) {
                if (mc.gameRenderer.currentEffect() == null || !mc.gameRenderer.currentEffect().getName().equals(targetShader.toString())) {
                    if (mc.gameRenderer.currentEffect() != null) mc.gameRenderer.shutdownEffect();
                    mc.gameRenderer.loadEffect(targetShader);
                }
            } else {
                if (mc.gameRenderer.currentEffect() != null && 
                   (mc.gameRenderer.currentEffect().getName().equals(GREEN_SHADER.toString()) || 
                    mc.gameRenderer.currentEffect().getName().equals(WHITE_SHADER.toString()))) {
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