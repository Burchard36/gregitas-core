package com.allthemods.gravitas2.core.mixin;

import com.allthemods.gravitas2.GregitasCore;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.block.BlockGoldPile;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.entity.EntityCyclops;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.util.WorldUtil;
import com.github.alexthe666.iceandfire.world.gen.TypedFeature;
import com.github.alexthe666.iceandfire.world.gen.WorldGenCyclopsCave;
import com.mojang.serialization.Codec;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.entities.TFCEntities;
import net.dries007.tfc.common.entities.livestock.WoolyAnimal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(value = WorldGenCyclopsCave.class, remap = false)
public abstract class WorldGenCyclopsCaveMixin extends Feature<NoneFeatureConfiguration> implements TypedFeature {


    @Shadow
    public static final ResourceLocation CYCLOPS_CHEST = new ResourceLocation("iceandfire", "chest/cyclops_cave");
    @Shadow
    private static Direction[] HORIZONTALS;

    public WorldGenCyclopsCaveMixin(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Overwrite
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos anActualUsableLocation = context.origin().offset(0, Math.abs(context.origin().getY()) + context.random().nextInt(36), 0);

        if (!WorldUtil.canGenerate(IafConfig.spawnCyclopsCaveChance, context.level(), context.random(), anActualUsableLocation, this.getId(), true)) {
            GregitasCore.LOGGER.info("Failed to generate a cyclops cave at: " + anActualUsableLocation);
            return false;
        } else {
            GregitasCore.LOGGER.info("Attempting to generate a cyclops cave @ ... %s " + anActualUsableLocation);
            int size = 16;
            int distance = 6;
            //if (!context.level().isEmptyBlock(context.origin().offset(size - distance, -3, -size + distance)) || !context.level().isEmptyBlock(context.origin().offset(size - distance, -3, size - distance)) || !context.level().isEmptyBlock(context.origin().offset(-size + distance, -3, -size + distance)) || !context.level().isEmptyBlock(context.origin().offset(-size + distance, -3, size - distance))) {
                generateShell(context, size, anActualUsableLocation);
                int innerSize = size - 2;
                int x = innerSize + context.random().nextInt(2);
                int y = 10 + context.random().nextInt(2);
                int z = innerSize + context.random().nextInt(2);
                float radius = (float)(x + y + z) * 0.333F + 0.5F;
                int sheepPenCount = 0;
                Iterator var10 = ((Set)BlockPos.betweenClosedStream(anActualUsableLocation.offset(-x, -y, -z), anActualUsableLocation.offset(x, y, z)).map(BlockPos::immutable).collect(Collectors.toSet())).iterator();

                BlockPos position;
                while(var10.hasNext()) {
                    position = (BlockPos)var10.next();
                    if (position.distSqr(anActualUsableLocation) <= (double)(radius * radius) && position.getY() > anActualUsableLocation.getY() && !(context.level().getBlockState(anActualUsableLocation).getBlock() instanceof AbstractChestBlock)) {
                        context.level().setBlock(position, Blocks.AIR.defaultBlockState(), 3);
                    }
                }

                var10 = ((Set)BlockPos.betweenClosedStream(anActualUsableLocation.offset(-x, -y, -z), anActualUsableLocation.offset(x, y, z)).map(BlockPos::immutable).collect(Collectors.toSet())).iterator();

                while(true) {
                    do {
                        do {
                            do {
                                do {
                                    if (!var10.hasNext()) {
                                        EntityCyclops cyclops = (EntityCyclops)((EntityType) IafEntityRegistry.CYCLOPS.get()).create(context.level().getLevel());
                                        cyclops.absMoveTo((double)anActualUsableLocation.getX() + 0.5, (double)anActualUsableLocation.getY() + 1.5, (double)anActualUsableLocation.getZ() + 0.5, context.random().nextFloat() * 360.0F, 0.0F);
                                        context.level().addFreshEntity(cyclops);
                                        return true;
                                    }

                                    position = (BlockPos)var10.next();
                                } while(!(position.distSqr(anActualUsableLocation) <= (double)(radius * radius)));
                            } while(position.getY() != anActualUsableLocation.getY());

                            if (context.random().nextInt(130) == 0 && this.isTouchingAir(context.level(), position.above())) {
                                this.generateSkeleton(context.level(), position.above(), context.random(), anActualUsableLocation, radius);
                            }

                            if (context.random().nextInt(130) == 0 && position.distSqr(anActualUsableLocation) <= (double)(radius * radius) * 0.800000011920929 && sheepPenCount < 2) {
                                this.generateSheepPen(context.level(), position.above(), context.random(), anActualUsableLocation, radius);
                                ++sheepPenCount;
                            }

                            if (context.random().nextInt(80) == 0 && this.isTouchingAir(context.level(), position.above())) {
                                context.level().setBlock(position.above(), IafBlockRegistry.GOLD_PILE.get().defaultBlockState().setValue(BlockGoldPile.LAYERS, 8), 3);
                                context.level().setBlock(position.above().north(), IafBlockRegistry.GOLD_PILE.get().defaultBlockState().setValue(BlockGoldPile.LAYERS, 1 + (new Random()).nextInt(7)), 3);
                                context.level().setBlock(position.above().south(), IafBlockRegistry.GOLD_PILE.get().defaultBlockState().setValue(BlockGoldPile.LAYERS, 1 + (new Random()).nextInt(7)), 3);
                                context.level().setBlock(position.above().west(), IafBlockRegistry.GOLD_PILE.get().defaultBlockState().setValue(BlockGoldPile.LAYERS, 1 + (new Random()).nextInt(7)), 3);
                                context.level().setBlock(position.above().east(), IafBlockRegistry.GOLD_PILE.get().defaultBlockState().setValue(BlockGoldPile.LAYERS, 1 + (new Random()).nextInt(7)), 3);
                                context.level().setBlock(position.above(2), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, HORIZONTALS[(new Random()).nextInt(3)]), 2);
                                if (context.level().getBlockState(position.above(2)).getBlock() instanceof AbstractChestBlock) {
                                    BlockEntity blockEntity = context.level().getBlockEntity(position.above(2));
                                    if (blockEntity instanceof ChestBlockEntity) {
                                        ChestBlockEntity chestBlockEntity = (ChestBlockEntity)blockEntity;
                                        chestBlockEntity.setLootTable(CYCLOPS_CHEST, context.random().nextLong());
                                    }
                                }
                            }
                        } while(context.random().nextInt(50) != 0);
                    } while(!this.isTouchingAir(context.level(), position.above()));

                    int torchHeight = context.random().nextInt(2) + 1;

                    for(int fence = 0; fence < torchHeight; ++fence) {
                        context.level().setBlock(position.above(1 + fence), this.getFenceState(context.level(), position.above(1 + fence)), 3);
                    }

                    context.level().setBlock(position.above(1 + torchHeight), Blocks.TORCH.defaultBlockState(), 2);
                }
            //} else {
            //    GregitasCore.LOGGER.info("ABSOLUTELY FAILED to generate a cyclops cave");
            //    return false;
            //}
        }
    }

    private static void generateShell(final FeaturePlaceContext<NoneFeatureConfiguration> context, int size, BlockPos loc) {
        int x = size + context.random().nextInt(2);
        int y = 12 + context.random().nextInt(2);
        int z = size + context.random().nextInt(2);
        float radius = (x + y + z) * 0.333F + 0.5F;

        for (BlockPos position : BlockPos.betweenClosedStream(loc.offset(-x, -y, -z), loc.offset(x, y, z)).map(BlockPos::immutable).collect(Collectors.toSet())) {
            boolean doorwayX = position.getX() >= loc.getX() - 2 + context.random().nextInt(2) && position.getX() <= loc.getX() + 2 + context.random().nextInt(2);
            boolean doorwayZ = position.getZ() >= loc.getZ() - 2 + context.random().nextInt(2) && position.getZ() <= loc.getZ() + 2 + context.random().nextInt(2);
            boolean isNotInDoorway = !doorwayX && !doorwayZ && position.getY() > loc.getY() || position.getY() > loc.getY() + y - (3 + context.random().nextInt(2));

            if (position.distSqr(loc) <= radius * radius) {
                BlockState state = context.level().getBlockState(position);

                if (!(state.getBlock() instanceof AbstractChestBlock) && state.getDestroySpeed(context.level(), position) >= 0 && isNotInDoorway) {
                    context.level().setBlock(position, TFCBlocks.PLAIN_ALABASTER.get().defaultBlockState(), Block.UPDATE_ALL);
                }
                if (position.getY() == loc.getY()) {
                    context.level().setBlock(position, TFCBlocks.PLAIN_ALABASTER_BRICKS.get().defaultBlockState(), Block.UPDATE_ALL);
                }
                if (position.getY() <= loc.getY() - 1 && !state.canOcclude()) {
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

    @Shadow
    abstract void generateSkeleton(LevelAccessor level, BlockPos position, RandomSource random, BlockPos origin, float radius);

    @Shadow
    abstract boolean isTouchingAir(LevelAccessor level, BlockPos position);

}


