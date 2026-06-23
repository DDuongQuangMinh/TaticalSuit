package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.client.model.WorkbenchItemModel;
import com.k1ngtle.taticalsuit.item.custom.WorkbenchItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class WorkbenchItemRenderer extends GeoItemRenderer<WorkbenchItem> {
    public WorkbenchItemRenderer() {
        super(new WorkbenchItemModel());
    }
}