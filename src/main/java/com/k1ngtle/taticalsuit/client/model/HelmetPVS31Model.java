package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HelmetPVS31Model extends GeoModel<HelmetPVS31Item> {
    @Override
    public ResourceLocation getModelResource(HelmetPVS31Item object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_pvs31.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetPVS31Item object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_pvs31.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetPVS31Item animatable) {
        return new ResourceLocation(TaticalSuit.MODID, "animations/armor/helmet_pvs31.animation.json");
    }
}