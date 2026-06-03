package com.devorcification.entity;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntityTypes {
    public static final EntityType<WatcherEntity> WATCHER = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(Devorcification.MOD_ID, "watcher"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, WatcherEntity::new)
            .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
            .build()
    );

    public static void register() {
        Devorcification.LOGGER.info("[Devorcification] Entity types registered");
    }
}
