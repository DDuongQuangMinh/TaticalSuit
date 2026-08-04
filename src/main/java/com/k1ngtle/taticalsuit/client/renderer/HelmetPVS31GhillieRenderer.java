package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.item.HelmetPVS31GhillieItem;
import com.k1ngtle.taticalsuit.client.model.HelmetPVS31GhillieModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class HelmetPVS31GhillieRenderer extends GeoArmorRenderer<HelmetPVS31GhillieItem> {
    public HelmetPVS31GhillieRenderer() {
        super(new HelmetPVS31GhillieModel());
    }
}