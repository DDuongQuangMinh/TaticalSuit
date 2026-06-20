package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.entity.WorkbenchBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WorkbenchModel extends GeoModel<WorkbenchBlockEntity> {
    @Override
    public ResourceLocation getModelResource(WorkbenchBlockEntity animatable) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/block/workbench.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WorkbenchBlockEntity animatable) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/block/workbench.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WorkbenchBlockEntity animatable) {
        // You can leave this pointing to a non-existent file if you don't have animations
        return new ResourceLocation(TaticalSuit.MODID, "animations/block/workbench.animation.json");
    }
}