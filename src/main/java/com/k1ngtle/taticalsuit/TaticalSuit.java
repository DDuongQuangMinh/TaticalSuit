package com.k1ngtle.taticalsuit;

import com.k1ngtle.taticalsuit.client.renderer.WorkbenchRenderer;
import com.k1ngtle.taticalsuit.network.EquipmentNetwork;
import com.k1ngtle.taticalsuit.network.HeadwearNetwork;
import com.k1ngtle.taticalsuit.network.ModNetworking;
import com.k1ngtle.taticalsuit.network.NVGNetwork;
import com.k1ngtle.taticalsuit.network.RadioNetwork;
import com.k1ngtle.taticalsuit.network.SquadNetwork;
import com.k1ngtle.taticalsuit.radar.RadarConfig;
import com.k1ngtle.taticalsuit.radar.api.radar.RadarRegistry;
import com.k1ngtle.taticalsuit.radar.debug.RadarBeaconBlockEntity;
import com.k1ngtle.taticalsuit.radar.integration.dh.DhCompat;
import com.k1ngtle.taticalsuit.radar.integration.vs.VsCompat;
import com.k1ngtle.taticalsuit.radar.registry.RadarBlocks;
import com.k1ngtle.taticalsuit.registry.ModBlockEntities;
import com.k1ngtle.taticalsuit.registry.ModBlocks;
import com.k1ngtle.taticalsuit.registry.ModCreativeTabs;
import com.k1ngtle.taticalsuit.registry.ModItems;
import com.k1ngtle.taticalsuit.registry.ModMenuTypes;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TaticalSuit.MODID)
public class TaticalSuit {
    public static final String MODID = "taticalsuit";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TaticalSuit() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register configs before setting up the Radar Registry
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RadarConfig.SPEC);

        // Register our Blocks, Items, and Entities
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        
        // Add this new line for your Creative Tab!
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        // Register our network packets (e.g. weapon equip from the Workbench GUI)
        ModNetworking.register();
        
        // Register NVG toggle networking (RESTORED so it initializes at startup)
        NVGNetwork.register();
        SquadNetwork.register();
        HeadwearNetwork.register();
        EquipmentNetwork.register();
        RadioNetwork.register();

        RadarBlocks.register(modEventBus);

        // Hook the Common Setup event for VS2 and DH Integrations
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register Debug Beacons as radar targets
            RadarRegistry.addTargetSource(com.k1ngtle.taticalsuit.radar.debug.RadarBeaconBlockEntity::beaconsIn);
            
            // Check config and mod loaded status before applying Valkyrien Skies 2 integration
            if (VsCompat.isLoaded() && RadarConfig.ENABLE_VS_INTEGRATION.get()) {
                RadarRegistry.addTargetSource(VsCompat.hook()::shipTargetsIn);
                LOGGER.info("Registered VS target source with RadarRegistry.");
            }

            // Check config and mod loaded status before applying Distant Horizons integration
            if (DhCompat.isLoaded() && RadarConfig.ENABLE_DH_OCCLUSION.get()) {
                RadarRegistry.addOcclusionProvider(DhCompat.provider());
                LOGGER.info("Registered DH height-occlusion provider with RadarRegistry.");
            }
        });
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