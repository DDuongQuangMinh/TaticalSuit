package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.item.HelmetPVS31Item;
import com.k1ngtle.taticalsuit.client.model.HelmetPVS31Model;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class HelmetPVS31Renderer extends GeoArmorRenderer<HelmetPVS31Item> {
    public HelmetPVS31Renderer() {
        super(new HelmetPVS31Model());
    }
}