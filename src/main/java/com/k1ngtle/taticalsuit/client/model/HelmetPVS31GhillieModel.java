package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetPVS31GhillieItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HelmetPVS31GhillieModel extends GeoModel<HelmetPVS31GhillieItem> {
    @Override
    public ResourceLocation getModelResource(HelmetPVS31GhillieItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_pvs31_ghillie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetPVS31GhillieItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_pvs31_ghillie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetPVS31GhillieItem animatable) {
        return new ResourceLocation(TaticalSuit.MODID, "animations/armor/helmet_pvs31.animation.json");
    }
}