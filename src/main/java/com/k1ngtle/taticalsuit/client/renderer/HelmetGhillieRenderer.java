package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.item.HelmetGhillieItem;
import com.k1ngtle.taticalsuit.client.model.HelmetGhillieModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class HelmetGhillieRenderer extends GeoArmorRenderer<HelmetGhillieItem> {
    public HelmetGhillieRenderer() {
        super(new HelmetGhillieModel());
    }
}