package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HelmetModel extends GeoModel<HelmetItem> {
    @Override
    public ResourceLocation getModelResource(HelmetItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetItem animatable) {
        // Return null if your helmet doesn't have moving animated parts
        return null; 
    }
}