package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.entity.WorkbenchBlockEntity;
import com.k1ngtle.taticalsuit.client.model.WorkbenchModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class WorkbenchRenderer extends GeoBlockRenderer<WorkbenchBlockEntity> {
    public WorkbenchRenderer(BlockEntityRendererProvider.Context context) {
        super(new WorkbenchModel());
    }
}