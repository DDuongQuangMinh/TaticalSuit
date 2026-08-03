package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetSnowItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HelmetSnowModel extends GeoModel<HelmetSnowItem> {
    @Override
    public ResourceLocation getModelResource(HelmetSnowItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_snow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetSnowItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_snow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetSnowItem animatable) {
        return null; 
    }
}