package com.k1ngtle.taticalsuit.registry;

import com.k1ngtle.taticalsuit.TaticalSuit;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    // Create the DeferredRegister for Creative Tabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TaticalSuit.MODID);

    // Register your custom tab
    public static final RegistryObject<CreativeModeTab> TATICAL_SUIT_TAB = CREATIVE_MODE_TABS.register("tatical_suit_tab",
            () -> CreativeModeTab.builder()
                    // Set the icon of the tab (We will use your Workbench as the icon!)
                    .icon(() -> new ItemStack(ModBlocks.WORKBENCH_ITEM.get()))
                    // The name of the tab (We will define this in the en_us.json file)
                    .title(Component.translatable("creativetab.tatical_suit_tab"))
                    // Add items to the tab here
                    .displayItems((itemDisplayParameters, output) -> {
                        // Add the Workbench to the tab
                        output.accept(ModBlocks.WORKBENCH_ITEM.get());
                        output.accept(ModItems.BASE_HELMET.get());
                        output.accept(ModItems.HELMET_GHILLIE.get());
                        output.accept(ModItems.HELMET_SAND.get());
                        output.accept(ModItems.HELMET_SNOW.get());
                        output.accept(ModItems.HELMET_PVS31.get());
                        output.accept(ModItems.HELMET_GPNVG18.get());
                        output.accept(ModItems.HELMET_GPNVG18_GHILLIE.get());
                        output.accept(ModItems.HELMET_GPNVG18_SAND.get());
                        // NOTE: When you create more items/armor later, you will add them here like this:
                        // output.accept(ModItems.YOUR_NEW_ITEM.get());
                    })
                    .build());
}