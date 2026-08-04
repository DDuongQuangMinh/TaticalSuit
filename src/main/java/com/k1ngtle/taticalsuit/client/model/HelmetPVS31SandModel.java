package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetPVS31SandItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HelmetPVS31SandModel extends GeoModel<HelmetPVS31SandItem> {
    @Override
    public ResourceLocation getModelResource(HelmetPVS31SandItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_pvs31_sand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetPVS31SandItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_pvs31_sand.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetPVS31SandItem animatable) {
        return new ResourceLocation(TaticalSuit.MODID, "animations/armor/helmet_pvs31.animation.json");
    }
}