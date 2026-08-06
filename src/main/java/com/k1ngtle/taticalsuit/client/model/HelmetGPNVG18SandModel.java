package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SandItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class HelmetGPNVG18SandModel extends DefaultedItemGeoModel<HelmetGPNVG18SandItem> {
    public HelmetGPNVG18SandModel() {
        super(new ResourceLocation(TaticalSuit.MODID, "helmet_gpnvg18_sand"));
    }

    @Override
    public ResourceLocation getModelResource(HelmetGPNVG18SandItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_gpnvg18_sand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetGPNVG18SandItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_gpnvg18_sand.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetGPNVG18SandItem animatable) {
        return new ResourceLocation(TaticalSuit.MODID, "animations/armor/helmet_gpnvg18.animation.json");
    }
}