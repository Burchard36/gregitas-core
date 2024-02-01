package com.allthemods.gravitas2.core.mixin;

import com.allthemods.gravitas2.GregitasCore;
import com.allthemods.gravitas2.util.IAFEntityMap;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.util.WorldUtil;
import com.github.alexthe666.iceandfire.world.IafWorldData;
import com.github.alexthe666.iceandfire.world.IafWorldRegistry;
import com.github.alexthe666.iceandfire.world.gen.*;
import com.mojang.serialization.Codec;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Mixin(value = WorldGenDragonRoosts.class, remap = false)
public abstract class WorldGenDragonRoostsMixin extends Feature<NoneFeatureConfiguration> implements TypedFeature {
    private static Block TFCRock = TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.HARDENED).get();
    private static Block TFCRock2 = TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.HARDENED).get();
    private List<BlockPos> previousDens = new ArrayList<>();
    public WorldGenDragonRoostsMixin(final Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /**
     * @author thevortex
     * @reason placeholder
     */
    @Overwrite
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context) {
        final EntityType<? extends EntityDragonBase> dragonType = this.getDragonType();
        final WorldGenLevel worldIn = context.level();
        final RandomSource rand = context.random();
        final BlockPos pos = context.origin();
        final ChunkDataProvider provider = ChunkDataProvider.get(worldIn);
        final ChunkData data = provider.get(worldIn, pos);
        final RockSettings rocks = data.getRockData().getRock(pos);
        TFCRock = rocks.hardened();
        TFCRock2 = rocks.hardened();
        final KoppenClimateClassification currentPositionClassification = KoppenClimateClassification.classify(data.getAverageTemp(pos), data.getRainfall(pos));
        final List<KoppenClimateClassification> entityPositionClassification = IAFEntityMap.dragonList.get(dragonType);
        if (entityPositionClassification == null) return false; // Entity didnt have any data set in the map
        if (!entityPositionClassification.contains(currentPositionClassification)) {
            return false;
        }
        if (!canGenerate(IafConfig.generateDragonRoostChance, context.level(), context.random(), context.origin(), this.getId(), true)) {

            return false;
        } else {
            boolean isMale = (new Random()).nextBoolean();
            int radius = 12 + context.random().nextInt(8);
            this.spawnDragon(context, radius, isMale);
            this.generateSurface(context, radius);
            this.generateShell(context, radius);
            radius -= 2;
            this.hollowOut(context, radius);
            radius += 15;
            this.generateDecoration(context, radius, isMale);
            this.previousDens.add(pos);
            return true;
        }
    }

    public boolean canGenerate(int configChance, WorldGenLevel level, RandomSource random, BlockPos origin, String id, boolean checkFluid) {
        return canGenerate(configChance, level, random, origin, id, IafWorldData.FeatureType.SURFACE, checkFluid);
    }

    public boolean canGenerate(int configChance, WorldGenLevel level, RandomSource random, BlockPos origin, String id, IafWorldData.FeatureType type, boolean checkFluid) {
        boolean canGenerate = random.nextInt(configChance) == 0 && IafWorldRegistry.isFarEnoughFromSpawn(level, origin);

        boolean canGenerateInRange = false;
        if (this.previousDens.size() != 0) {
            for (BlockPos pos : this.previousDens) {
                double distance = this.distanceTo(pos, origin);
                if (distance >= IafConfig.dangerousWorldGenSeparationLimit) {
                    canGenerateInRange = true;
                    //GregitasCore.LOGGER.info("canGenerateInRange was set to true Distance is: " + distance);
                    //return canGenerate && checkFluid && !level.getFluidState(origin.below()).isEmpty() ? false : canGenerate;
                } else {
                    //GregitasCore.LOGGER.info("canGenerateInRange was kept the same: " + distance);
                    return false;
                }
            }

            return canGenerate && checkFluid && !level.getFluidState(origin.below()).isEmpty() ? false : canGenerate;
        } else return canGenerate && checkFluid && !level.getFluidState(origin.below()).isEmpty() ? false : canGenerate;
    }

    public double distanceTo(BlockPos pos, BlockPos pos2) {
        return Math.sqrt(Math.pow(pos2.getX() - pos.getX(), 2) + Math.pow(pos2.getY() - pos.getY(), 2) + Math.pow(pos.getZ() - pos.getZ(), 2));
    }

    /**
     * @author thevortex
     * @reason placeholder
     */
    @Overwrite
    private void generateShell(final FeaturePlaceContext<NoneFeatureConfiguration> context, int radius) {
        int height = radius / 5;
        double circularArea = this.getCircularArea(radius, height);
        BlockPos.betweenClosedStream(context.origin().offset(-radius, -height, -radius), context.origin().offset(radius, 1, radius)).map(BlockPos::immutable).forEach((position) -> {
            if (position.distSqr(context.origin()) < circularArea) {
                context.level().setBlock(position, context.random().nextBoolean() ? TFCRock.defaultBlockState() : TFCRock2.defaultBlockState(), 2);
            } else if (position.distSqr(context.origin()) == circularArea) {
                context.level().setBlock(position, TFCRock.defaultBlockState(), 2);
            }

        });
    }

    protected double getCircularArea(final int radius, final int height) {
        final double area = (radius + height + radius) * 0.333F + 0.5F;
        return Mth.floor(area * area);
    }

    /**
     * @author thevortex
     * @reason placeholder
     */
    @Overwrite
    private void generateSurface(final FeaturePlaceContext<NoneFeatureConfiguration> context, final int radius) {
        final int height = 2;
        final double circularArea = this.getCircularArea(radius, height);
        BlockPos.betweenClosedStream(context.origin().offset(-radius, height, -radius), context.origin().offset(radius, 0, radius)).map(BlockPos::immutable).forEach((position) -> {
            int heightDifference = position.getY() - context.origin().getY();
            if (position.distSqr(context.origin()) <= circularArea && heightDifference < 2 + context.random().nextInt(height) && !context.level().isEmptyBlock(position.below())) {
                if (context.level().isEmptyBlock(position.above())) {
                    context.level().setBlock(position, TFCRock.defaultBlockState(), 2);
                } else {
                    context.level().setBlock(position, TFCRock2.defaultBlockState(), 2);
                }
            }

        });
    }



    @Shadow protected abstract void spawnDragon(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> context, int ageOffset, boolean isMale);
    @Shadow protected abstract void hollowOut(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> context, int radius);
    @Shadow protected abstract EntityType<? extends EntityDragonBase> getDragonType();

    @Shadow protected abstract void generateDecoration(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> context, int radius, boolean isMale);
}