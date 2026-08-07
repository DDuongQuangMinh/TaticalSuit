package com.k1ngtle.taticalsuit.client.audio;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

@Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VoiceClientEvents {

    public static final KeyMapping PTT_KEY = new KeyMapping(
            "key.taticalsuit.ptt", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.taticalsuit.keys");

    public static final KeyMapping AUDIO_MENU_KEY = new KeyMapping(
            "key.taticalsuit.audio_menu", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, "category.taticalsuit.keys");

    public static final KeyMapping SQUELCH_KEY = new KeyMapping(
            "key.taticalsuit.squelch", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "category.taticalsuit.keys");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        VoiceManager.init();
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(PTT_KEY);
        event.register(AUDIO_MENU_KEY);
        event.register(SQUELCH_KEY);
    }

    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                // Push microphone state to the thread
                VoiceManager.setTransmitting(PTT_KEY.isDown());
                VoiceManager.isSquelchOverrideHeld = SQUELCH_KEY.isDown();
                
                // Safely caches variables for the Async Audio Engine
                VoiceManager.updateState();

                if (AUDIO_MENU_KEY.consumeClick()) {
                    Minecraft.getInstance().setScreen(new com.k1ngtle.taticalsuit.client.screen.AudioDeviceScreen());
                }
            }
        }

        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("radiodebug")
                    .executes(context -> {
                        VoiceManager.loopbackDebug = !VoiceManager.loopbackDebug;
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("§e[RADIO DEBUG] Local Voice Loopback is now: " + (VoiceManager.loopbackDebug ? "§aON" : "§cOFF")), false
                            );
                        }
                        return 1;
                    })
            );
        }
    }
}