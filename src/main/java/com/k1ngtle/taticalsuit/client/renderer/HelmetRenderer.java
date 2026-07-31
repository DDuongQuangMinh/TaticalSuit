package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.item.HelmetItem;
import com.k1ngtle.taticalsuit.client.model.HelmetModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class HelmetRenderer extends GeoArmorRenderer<HelmetItem> {
    public HelmetRenderer() {
        super(new HelmetModel());
    }
}