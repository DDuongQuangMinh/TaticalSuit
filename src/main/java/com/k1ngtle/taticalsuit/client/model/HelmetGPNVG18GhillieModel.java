package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18GhillieItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HelmetGPNVG18GhillieModel extends GeoModel<HelmetGPNVG18GhillieItem> {
    @Override
    public ResourceLocation getModelResource(HelmetGPNVG18GhillieItem object) {
        // We can reuse the same geometry as the regular ghillie helmet!
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_gpnvg18_ghillie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetGPNVG18GhillieItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_gpnvg18_ghillie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetGPNVG18GhillieItem animatable) {
        return new ResourceLocation(TaticalSuit.MODID, "animations/armor/helmet_gpnvg18.animation.json");
    }
}