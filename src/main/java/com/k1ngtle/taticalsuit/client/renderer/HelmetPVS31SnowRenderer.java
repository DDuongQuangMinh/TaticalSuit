package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.item.HelmetPVS31SnowItem;
import com.k1ngtle.taticalsuit.client.model.HelmetPVS31SnowModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class HelmetPVS31SnowRenderer extends GeoArmorRenderer<HelmetPVS31SnowItem> {
    public HelmetPVS31SnowRenderer() {
        super(new HelmetPVS31SnowModel());
    }
}