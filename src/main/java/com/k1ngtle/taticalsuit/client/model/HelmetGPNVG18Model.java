package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HelmetGPNVG18Model extends GeoModel<HelmetGPNVG18Item> {
    @Override
    public ResourceLocation getModelResource(HelmetGPNVG18Item object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_gpnvg18.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetGPNVG18Item object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_gpnvg18.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetGPNVG18Item animatable) {
        return new ResourceLocation(TaticalSuit.MODID, "animations/armor/helmet_gpnvg18.animation.json");
    }
}