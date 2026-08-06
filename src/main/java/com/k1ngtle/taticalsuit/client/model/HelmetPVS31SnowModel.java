package com.k1ngtle.taticalsuit.client.model;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.item.HelmetPVS31SnowItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class HelmetPVS31SnowModel extends DefaultedItemGeoModel<HelmetPVS31SnowItem> {
    public HelmetPVS31SnowModel() {
        super(new ResourceLocation(TaticalSuit.MODID, "helmet_pvs31_snow"));
    }

    @Override
    public ResourceLocation getModelResource(HelmetPVS31SnowItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "geo/armor/helmet_pvs31_snow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HelmetPVS31SnowItem object) {
        return new ResourceLocation(TaticalSuit.MODID, "textures/armor/helmet_pvs31_snow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetPVS31SnowItem animatable) {
        return new ResourceLocation(TaticalSuit.MODID, "animations/armor/helmet_pvs31.animation.json");
    }
}