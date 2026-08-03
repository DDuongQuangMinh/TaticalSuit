package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetGhillieItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HelmetGhillieModel extends GeoModel<HelmetGhillieItem> {
    @Override
    public ResourceLocation getModelResource(HelmetGhillieItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_ghillie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetGhillieItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_ghillie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetGhillieItem animatable) {
        return null; 
    }
}