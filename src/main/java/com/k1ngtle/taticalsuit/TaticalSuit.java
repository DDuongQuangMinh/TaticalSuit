package com.k1ngtle.taticalsuit;

import com.k1ngtle.taticalsuit.client.renderer.WorkbenchRenderer;
import com.k1ngtle.taticalsuit.network.ModNetworking;
import com.k1ngtle.taticalsuit.registry.ModBlockEntities;
import com.k1ngtle.taticalsuit.registry.ModBlocks;
import com.k1ngtle.taticalsuit.registry.ModCreativeTabs;
import com.k1ngtle.taticalsuit.registry.ModMenuTypes;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TaticalSuit.MODID)
public class TaticalSuit {
    public static final String MODID = "taticalsuit";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TaticalSuit() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register our Blocks, Items, and Entities
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        
        // Add this new line for your Creative Tab!
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        // Register our network packets (e.g. weapon equip from the Workbench GUI)
        ModNetworking.register();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        
        // Register the 3D GeckoLib Renderers
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.WORKBENCH_BE.get(), WorkbenchRenderer::new);
        }

        // Register the GUI Screens
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                net.minecraft.client.gui.screens.MenuScreens.register(ModMenuTypes.WORKBENCH_MENU.get(), com.k1ngtle.taticalsuit.client.screen.WorkbenchScreen::new);
            });
        }
    }
}