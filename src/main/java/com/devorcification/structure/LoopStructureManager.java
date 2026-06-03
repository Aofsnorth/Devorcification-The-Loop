package com.devorcification.structure;

import com.devorcification.Devorcification;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class LoopStructureManager {
    private static final Identifier CORRIDOR_NBT =
        new Identifier(Devorcification.MOD_ID, "loop_corridor");
    private static final int CORRIDOR_LENGTH = 60;
    private static final int CORRIDOR_WIDTH = 3;
    private static final int CORRIDOR_HEIGHT = 4;

    public static void placeLoopStructure(ServerWorld world, BlockPos origin) {
        if (world == null) return;
        StructureTemplateManager templateManager = world.getStructureTemplateManager();
        StructureTemplate template = templateManager.getTemplateOrBlank(CORRIDOR_NBT);

        if (template.getSize().getX() == 0 || template.getSize().getY() == 0) {
            Devorcification.LOGGER.info("[Devorcification] No NBT structure found, generating fallback corridor in code.");
            generateFallbackCorridor(world, origin);
            return;
        }

        template.place(
            world,
            origin,
            origin,
            new StructurePlacementData()
                .setMirror(BlockMirror.NONE)
                .setRotation(BlockRotation.NONE)
                .setIgnoreEntities(false),
            world.getRandom(),
            2
        );
        Devorcification.LOGGER.info("[Devorcification] Placed loop_corridor structure at {}", origin);
    }

    private static void generateFallbackCorridor(ServerWorld world, BlockPos origin) {
        for (int z = 0; z < CORRIDOR_LENGTH; z++) {
            for (int x = 0; x < CORRIDOR_WIDTH; x++) {
                world.setBlockState(origin.add(x, 0, z), Blocks.DARK_OAK_PLANKS.getDefaultState());
                world.setBlockState(origin.add(x, 1, z), Blocks.AIR.getDefaultState());
                world.setBlockState(origin.add(x, 2, z), Blocks.AIR.getDefaultState());
                world.setBlockState(origin.add(x, 3, z), Blocks.REDSTONE_LAMP.getDefaultState());
            }
            world.setBlockState(origin.add(0, 1, z), Blocks.STONE_BRICKS.getDefaultState());
            world.setBlockState(origin.add(0, 2, z), Blocks.STONE_BRICKS.getDefaultState());
            world.setBlockState(origin.add(CORRIDOR_WIDTH - 1, 1, z), Blocks.STONE_BRICKS.getDefaultState());
            world.setBlockState(origin.add(CORRIDOR_WIDTH - 1, 2, z), Blocks.STONE_BRICKS.getDefaultState());
        }

        world.setBlockState(origin.add(0, 1, 0), Blocks.IRON_DOOR.getDefaultState());
        world.setBlockState(origin.add(0, 2, 0), Blocks.IRON_DOOR.getDefaultState());

        BlockPos endDoorPos = origin.add(CORRIDOR_WIDTH - 1, 1, CORRIDOR_LENGTH - 1);
        world.setBlockState(endDoorPos, Blocks.IRON_DOOR.getDefaultState());
        world.setBlockState(endDoorPos.up(), Blocks.IRON_DOOR.getDefaultState());
        world.setBlockState(origin.add(1, 1, CORRIDOR_LENGTH - 1), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState());

        Devorcification.LOGGER.info("[Devorcification] Fallback corridor generated: length={}, origin={}",
            CORRIDOR_LENGTH, origin);
    }

    public static int getCorridorLength() {
        return CORRIDOR_LENGTH;
    }
}
