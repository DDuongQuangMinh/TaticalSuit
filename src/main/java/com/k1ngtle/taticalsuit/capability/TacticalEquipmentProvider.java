package com.k1ngtle.taticalsuit.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TacticalEquipmentProvider implements ICapabilitySerializable<CompoundTag> {
    
    public static final Capability<TacticalEquipmentHandler> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final TacticalEquipmentHandler handler;
    private final LazyOptional<TacticalEquipmentHandler> optional;

    public TacticalEquipmentProvider(LivingEntity entity) {
        this.handler = new TacticalEquipmentHandler(entity);
        this.optional = LazyOptional.of(() -> this.handler);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return handler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        handler.deserializeNBT(nbt);
    }
}