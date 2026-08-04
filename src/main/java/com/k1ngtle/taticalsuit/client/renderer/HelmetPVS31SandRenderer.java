package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.item.HelmetPVS31SandItem;
import com.k1ngtle.taticalsuit.client.model.HelmetPVS31SandModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class HelmetPVS31SandRenderer extends GeoArmorRenderer<HelmetPVS31SandItem> {
    public HelmetPVS31SandRenderer() {
        super(new HelmetPVS31SandModel());
    }
}