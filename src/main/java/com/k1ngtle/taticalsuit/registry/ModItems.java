package com.k1ngtle.taticalsuit.registry;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    
    // Create the DeferredRegister for Items
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, TaticalSuit.MODID);

    // Register your Base Helmet
    public static final RegistryObject<Item> BASE_HELMET = ITEMS.register("base_helmet",
            () -> new HelmetItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> HELMET_PVS31 = ITEMS.register("helmet_pvs31",
            () -> new HelmetPVS31Item(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));

    // This method hooks the registry into your main mod event bus
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}