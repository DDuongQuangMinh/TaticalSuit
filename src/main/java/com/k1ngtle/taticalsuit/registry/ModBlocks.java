package com.k1ngtle.taticalsuit.registry;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.block.WorkbenchBlock;
import com.k1ngtle.taticalsuit.item.custom.WorkbenchItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TaticalSuit.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TaticalSuit.MODID);

    // Register the Block
    public static final RegistryObject<Block> WORKBENCH = BLOCKS.register("workbench", 
            () -> new WorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    // Register the Item to place the block
    public static final RegistryObject<Item> WORKBENCH_ITEM = ITEMS.register("workbench", 
            () -> new WorkbenchItem(WORKBENCH.get(), new Item.Properties()));
}