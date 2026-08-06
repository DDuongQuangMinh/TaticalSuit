package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetGPNVG18SnowItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class HelmetGPNVG18SnowModel extends DefaultedItemGeoModel<HelmetGPNVG18SnowItem> {
    public HelmetGPNVG18SnowModel() {
        super(new ResourceLocation(TaticalSuit.MODID, "helmet_gpnvg18_snow"));
    }

    @Override
    public ResourceLocation getModelResource(HelmetGPNVG18SnowItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_gpnvg18_snow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetGPNVG18SnowItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_gpnvg18_snow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetGPNVG18SnowItem animatable) {
        return new ResourceLocation(TaticalSuit.MODID, "animations/armor/helmet_gpnvg18.animation.json");
    }
}