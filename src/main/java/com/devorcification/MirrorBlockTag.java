package com.devorcification;

import com.devorcification.Devorcification;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class MirrorBlockTag {
    public static final TagKey<Block> MIRROR_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier(Devorcification.MOD_ID, "mirror_blocks"));

    public static void register() {
        Registries.BLOCK.getEntry(Blocks.GLASS.getRegistryEntry().registryKey())
            .ifPresent(entry -> entry.registryKey());

        RegistryEntryAddedCallback.event(Registries.BLOCK).register((rawId, id, block) -> {
            // Placeholder hook; the actual tag content is set via data/devorcification/tags/blocks/mirror_blocks.json
        });
    }
}
