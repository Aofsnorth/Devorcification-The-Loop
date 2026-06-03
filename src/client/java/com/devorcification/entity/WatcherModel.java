package com.devorcification.entity;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class WatcherModel extends GeoModel<WatcherEntity> {
    private static final Identifier MODEL = new Identifier("devorcification", "geo/watcher.geo.json");
    private static final Identifier TEXTURE = new Identifier("devorcification", "textures/entity/watcher.png");
    private static final Identifier ANIMATION = new Identifier("devorcification", "animations/watcher.animation.json");

    @Override
    public Identifier getModelResource(WatcherEntity animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(WatcherEntity animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(WatcherEntity animatable) {
        return ANIMATION;
    }
}
