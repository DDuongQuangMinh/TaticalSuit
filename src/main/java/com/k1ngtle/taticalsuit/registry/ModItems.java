package com.k1ngtle.taticalsuit.registry;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SandItem;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SnowItem;
import com.k1ngtle.taticalsuit.item.HelmetGhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31GhillieItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import com.k1ngtle.taticalsuit.item.HelmetPVS31SandItem;
import com.k1ngtle.taticalsuit.item.HelmetPVS31SnowItem;
import com.k1ngtle.taticalsuit.item.HelmetSandItem;
import com.k1ngtle.taticalsuit.item.HelmetSnowItem;
import com.k1ngtle.taticalsuit.item.RadioItem;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
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

    public static final RegistryObject<Item> HELMET_GPNVG18 = ITEMS.register("helmet_gpnvg18",
            () -> new HelmetGPNVG18Item(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> HELMET_GHILLIE = ITEMS.register("helmet_ghillie",
            () -> new HelmetGhillieItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> HELMET_SAND = ITEMS.register("helmet_sand",
            () -> new HelmetSandItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> HELMET_SNOW = ITEMS.register("helmet_snow",
            () -> new HelmetSnowItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> HELMET_GPNVG18_GHILLIE = ITEMS.register("helmet_gpnvg18_ghillie",
            () -> new HelmetGPNVG18GhillieItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> HELMET_GPNVG18_SAND = ITEMS.register("helmet_gpnvg18_sand",
            () -> new HelmetGPNVG18SandItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
            
    public static final RegistryObject<Item> HELMET_GPNVG18_SNOW = ITEMS.register("helmet_gpnvg18_snow",
            () -> new HelmetGPNVG18SnowItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
            
    public static final RegistryObject<Item> HELMET_PVS31_GHILLIE = ITEMS.register("helmet_pvs31_ghillie",
            () -> new HelmetPVS31GhillieItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
            
    public static final RegistryObject<Item> HELMET_PVS31_SAND = ITEMS.register("helmet_pvs31_sand",
            () -> new HelmetPVS31SandItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
            
    public static final RegistryObject<Item> HELMET_PVS31_SNOW = ITEMS.register("helmet_pvs31_snow",
            () -> new HelmetPVS31SnowItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));        

    //Item: Radio
    public static final RegistryObject<Item> PRC_152A_RADIO = ITEMS.register("prc_152a",
            () -> new RadioItem(new Item.Properties())); 
            
    public static final RegistryObject<Item> PRC_163_RADIO = ITEMS.register("prc_163",
            () -> new RadioItem(new Item.Properties()));       
            
    public static final RegistryObject<Item> PRC_150_ITEM = ITEMS.register("prc_150",
            () -> new BlockItem(ModBlocks.PRC_150.get(), new Item.Properties()));        

    // This method hooks the registry into your main mod event bus
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}