package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetSandItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HelmetSandModel extends GeoModel<HelmetSandItem> {
    @Override
    public ResourceLocation getModelResource(HelmetSandItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_sand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetSandItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_sand.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetSandItem animatable) {
        return null; 
    }
}