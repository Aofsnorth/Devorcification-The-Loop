package com.devorcification.entity;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WatcherRenderer extends GeoEntityRenderer<WatcherEntity> {
    public WatcherRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new WatcherModel());
        this.shadowRadius = 0.4f;
    }

    @Override
    public RenderLayer getRenderType(WatcherEntity animatable, Identifier texture, float partialTick, int packedLight) {
        return RenderLayer.getEntityTranslucent(texture);
    }
}
