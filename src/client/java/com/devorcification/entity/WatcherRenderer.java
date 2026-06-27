package com.devorcification.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WatcherRenderer extends GeoEntityRenderer<WatcherEntity> {
    public WatcherRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new WatcherModel());
        this.shadowRadius = 0.4f;
    }
}
