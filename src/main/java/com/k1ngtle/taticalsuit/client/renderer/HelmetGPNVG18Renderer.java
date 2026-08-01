package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.item.HelmetGPNVG18Item;
import com.k1ngtle.taticalsuit.client.model.HelmetGPNVG18Model;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class HelmetGPNVG18Renderer extends GeoArmorRenderer<HelmetGPNVG18Item> {
    public HelmetGPNVG18Renderer() {
        super(new HelmetGPNVG18Model());
    }
}