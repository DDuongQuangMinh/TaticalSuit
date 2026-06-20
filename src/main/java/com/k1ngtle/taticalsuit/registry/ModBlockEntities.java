package com.k1ngtle.taticalsuit.registry;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.entity.WorkbenchBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TaticalSuit.MODID);

    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_BE = BLOCK_ENTITIES.register("workbench",
            () -> BlockEntityType.Builder.of(WorkbenchBlockEntity::new, ModBlocks.WORKBENCH.get()).build(null));
}