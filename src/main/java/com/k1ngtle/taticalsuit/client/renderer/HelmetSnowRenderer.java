package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.item.HelmetSnowItem;
import com.k1ngtle.taticalsuit.client.model.HelmetSnowModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class HelmetSnowRenderer extends GeoArmorRenderer<HelmetSnowItem> {
    public HelmetSnowRenderer() {
        super(new HelmetSnowModel());
    }
}