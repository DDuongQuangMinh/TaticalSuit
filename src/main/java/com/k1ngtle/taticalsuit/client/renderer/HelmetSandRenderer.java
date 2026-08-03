package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.item.HelmetSandItem;
import com.k1ngtle.taticalsuit.client.model.HelmetSandModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class HelmetSandRenderer extends GeoArmorRenderer<HelmetSandItem> {
    public HelmetSandRenderer() {
        super(new HelmetSandModel());
    }
}