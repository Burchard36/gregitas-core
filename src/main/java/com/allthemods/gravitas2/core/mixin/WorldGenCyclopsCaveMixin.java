package com.allthemods.gravitas2.core.mixin;

import com.allthemods.gravitas2.GregitasCore;
import com.github.alexthe666.iceandfire.world.gen.TypedFeature;
import com.github.alexthe666.iceandfire.world.gen.WorldGenCyclopsCave;
import com.mojang.serialization.Codec;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.entities.TFCEntities;
import net.dries007.tfc.common.entities.livestock.WoolyAnimal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.stream.Collectors;

@Mixin(value = WorldGenCyclopsCave.class, remap = false)
public abstract class WorldGenCyclopsCaveMixin extends Feature<NoneFeatureConfiguration> implements TypedFeature {

    public WorldGenCyclopsCaveMixin(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    /**
     * @author thevortex
     * @reason placeholder
     */
    @Overwrite
    private static void generateShell(final FeaturePlaceContext<NoneFeatureConfiguration> context, int size) {
        int x = size + context.random().nextInt(2);
        int y = 12 + context.random().nextInt(2);
        int z = size + context.random().nextInt(2);
        float radius = (x + y + z) * 0.333F + 0.5F;

        for (BlockPos position : BlockPos.betweenClosedStream(context.origin().offset(-x, -y, -z), context.origin().offset(x, y, z)).map(BlockPos::immutable).collect(Collectors.toSet())) {
            boolean doorwayX = position.getX() >= context.origin().getX() - 2 + context.random().nextInt(2) && position.getX() <= context.origin().getX() + 2 + context.random().nextInt(2);
            boolean doorwayZ = position.getZ() >= context.origin().getZ() - 2 + context.random().nextInt(2) && position.getZ() <= context.origin().getZ() + 2 + context.random().nextInt(2);
            boolean isNotInDoorway = !doorwayX && !doorwayZ && position.getY() > context.origin().getY() || position.getY() > context.origin().getY() + y - (3 + context.random().nextInt(2));

            if (position.distSqr(context.origin()) <= radius * radius) {
                BlockState state = context.level().getBlockState(position);

                if (!(state.getBlock() instanceof AbstractChestBlock) && state.getDestroySpeed(context.level(), position) >= 0 && isNotInDoorway) {
                    context.level().setBlock(position, TFCBlocks.PLAIN_ALABASTER.get().defaultBlockState(), Block.UPDATE_ALL);
                }
                if (position.getY() == context.origin().getY()) {
                    context.level().setBlock(position, TFCBlocks.PLAIN_ALABASTER_BRICKS.get().defaultBlockState(), Block.UPDATE_ALL);
                }
                if (position.getY() <= context.origin().getY() - 1 && !state.canOcclude()) {
                    context.level().setBlock(position, TFCBlocks.PLAIN_ALABASTER_BRICKS.get().defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }
    /**
     * @author thevortex
     * @reason placeholder
     */
    @Overwrite
    private void generateSheepPen(final ServerLevelAccessor level, BlockPos position, final RandomSource random, final BlockPos origin, final float radius) {
        int width = 5 + random.nextInt(3);
        int sheepAmount = 2 + random.nextInt(3);
        Direction direction = Direction.NORTH;

        int sideCount;
        int side;
        BlockPos relativePosition;
        for(sideCount = 0; sideCount < 4; ++sideCount) {
            for(side = 0; side < width; ++side) {
                relativePosition = position.relative(direction, side);
                if (origin.distSqr(relativePosition) <= (double)(radius * radius)) {
                    level.setBlock(relativePosition, this.getFenceState(level, relativePosition), 3);
                    if (level.isEmptyBlock(relativePosition.relative(direction.getClockWise())) && sheepAmount > 0) {
                        BlockPos sheepPos = relativePosition.relative(direction.getClockWise());
                        WoolyAnimal sheep = TFCEntities.SHEEP.get().create(level.getLevel());
                        if (sheep == null) {
                            GregitasCore.LOGGER.warn("Sheep was null when checked for TFCEntities.SHEEP at location %s".formatted(position.toString()));
                            continue;
                        }
                        sheep.setPos(((float)sheepPos.getX() + 0.5F), ((float)sheepPos.getY() + 0.5F), ((float)sheepPos.getZ() + 0.5F));
                        level.addFreshEntity(sheep);
                        --sheepAmount;
                    }
                }
            }

            position = position.relative(direction, width);
            direction = direction.getClockWise();
        }

        for(sideCount = 0; sideCount < 4; ++sideCount) {
            for(side = 0; side < width; ++side) {
                relativePosition = position.relative(direction, side);
                if (origin.distSqr(relativePosition) <= (double)(radius * radius)) {
                    level.setBlock(relativePosition, this.getFenceState(level, relativePosition), 3);
                }
            }

            position = position.relative(direction, width);
            direction = direction.getClockWise();
        }

    }
    /**
     * @author thevortex
     * @reason placeholder
     */
    @Overwrite
    private BlockState getFenceState(LevelAccessor level, BlockPos position) {
        Block calcite = TFCBlocks.CALCITE.get();
        return calcite.defaultBlockState();
    }
}


