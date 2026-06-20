package com.k1ngtle.taticalsuit.entity;

import com.k1ngtle.taticalsuit.menu.WorkbenchMenu;
import com.k1ngtle.taticalsuit.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WorkbenchBlockEntity extends BlockEntity implements GeoBlockEntity, MenuProvider {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public WorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WORKBENCH_BE.get(), pos, state);
    }

    // --- GUI Methods ---
    
    @Override
    public Component getDisplayName() {
        // This sets the title at the top of the GUI
        return Component.translatable("block.taticalsuit.workbench");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        // This passes the world context so the crafting grid knows where it is located
        return new WorkbenchMenu(id, inventory, ContainerLevelAccess.create(level, worldPosition));
    }

    // --- GeckoLib Methods ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> PlayState.CONTINUE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}