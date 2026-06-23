package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.item.custom.WorkbenchItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WorkbenchItemModel extends GeoModel<WorkbenchItem> {
    @Override
    public ResourceLocation getModelResource(WorkbenchItem animatable) {
        return new ResourceLocation("taticalsuit", "geo/block/workbench.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WorkbenchItem animatable) {
        return new ResourceLocation("taticalsuit", "textures/block/workbench.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WorkbenchItem animatable) {
        return new ResourceLocation("taticalsuit", "animations/block/workbench.animation.json");
    }
}