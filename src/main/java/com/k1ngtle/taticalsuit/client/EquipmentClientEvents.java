package com.k1ngtle.taticalsuit.client;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.client.renderer.TacticalEquipmentLayer;
import com.k1ngtle.taticalsuit.client.screen.TacticalEquipmentScreen;
import com.k1ngtle.taticalsuit.network.EquipmentNetwork;
import com.k1ngtle.taticalsuit.network.OpenEquipmentMenuPacket;
import com.k1ngtle.taticalsuit.registry.ModMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class EquipmentClientEvents {

    // Define the custom ring/gear icon texture you provided
    private static final ResourceLocation GEAR_ICON = new ResourceLocation(TaticalSuit.MODID, "textures/gui/gear_button.png");

    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenuTypes.TACTICAL_EQUIPMENT_MENU.get(), TacticalEquipmentScreen::new);
            });
        }

        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            // Apply our layer to all standard player models (Slim and Wide)
            for (String skinName : event.getSkins()) {
                LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer = event.getSkin(skinName);
                if (renderer != null) {
                    renderer.addLayer(new TacticalEquipmentLayer<>(renderer, event.getEntityModels()));
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = TaticalSuit.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {
        
        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            // 1. SURVIVAL INVENTORY
            if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
                
                // Position in the top-right corner of the player model box
                int x = inventoryScreen.getGuiLeft() + 65; 
                int y = inventoryScreen.getGuiTop() + 9;
                
                event.addListener(new ImageButton(
                    x, y, 
                    10, 10,  // Width and height of the clickable box
                    0, 0,    // Texture X and Y start
                    10,      // Y-offset when hovered
                    GEAR_ICON, 
                    256, 256, // Total texture size
                    button -> {
                        if (Minecraft.getInstance().player != null) {
                            EquipmentNetwork.CHANNEL.sendToServer(new OpenEquipmentMenuPacket());
                        }
                    }
                ));
            } 
            // 2. CREATIVE INVENTORY
            else if (event.getScreen() instanceof CreativeModeInventoryScreen creativeScreen) {
                
                // Position in the top-right corner of the player model box (in the Survival Inventory tab)
                int x = creativeScreen.getGuiLeft() + 164; 
                int y = creativeScreen.getGuiTop() + 9;
                
                event.addListener(new ImageButton(
                    x, y, 
                    10, 10,  
                    0, 0,    
                    10,      
                    GEAR_ICON, 
                    256, 256, 
                    button -> {
                        if (Minecraft.getInstance().player != null) {
                            EquipmentNetwork.CHANNEL.sendToServer(new OpenEquipmentMenuPacket());
                        }
                    }
                ));
            }
        }

        @SubscribeEvent
        public static void onScreenRender(ScreenEvent.Render.Pre event) {
            if (event.getScreen() instanceof CreativeModeInventoryScreen creativeScreen) {
                for (var widget : event.getScreen().children()) {
                    if (widget instanceof ImageButton button) {
                        // Check if it's OUR specific button by matching the coordinates
                        if (button.getX() == creativeScreen.getGuiLeft() + 164 && button.getY() == creativeScreen.getGuiTop() + 9) {
                            // Only make it visible and clickable when the "Inventory" (survival) tab is open
                            button.visible = creativeScreen.isInventoryOpen();
                        }
                    }
                }
            }
        }
    }
}