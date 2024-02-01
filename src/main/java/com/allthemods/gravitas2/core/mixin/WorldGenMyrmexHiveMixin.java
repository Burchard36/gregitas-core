package com.allthemods.gravitas2.core.mixin;

import com.allthemods.gravitas2.GregitasCore;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.world.IafWorldRegistry;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import com.github.alexthe666.iceandfire.world.gen.TypedFeature;
import com.github.alexthe666.iceandfire.world.gen.WorldGenMyrmexHive;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = WorldGenMyrmexHive.class, remap = false)
public abstract class WorldGenMyrmexHiveMixin extends Feature<NoneFeatureConfiguration> implements TypedFeature {

    @Shadow
    private boolean small;
    @Shadow
    private boolean hasFoodRoom;
    @Shadow
    private boolean hasNursery;
    @Shadow
    private int totalRooms;
    @Shadow
    private int entrances = 0;
    @Shadow
    private BlockPos centerOfHive;

    public WorldGenMyrmexHiveMixin(Codec<NoneFeatureConfiguration> codex) {
        super(codex);
    }


    @Overwrite
    private void decorateCircle(LevelAccessor world, RandomSource rand, BlockPos position, int size, int height, Direction direction) {
        int radius = size + 2;
        {
            for (float i = 0; i < radius; i += 0.5) {
                for (float j = 0; j < 2 * Math.PI * i; j += 0.5) {
                    int x = (int) Math.floor(Mth.sin(j) * i);
                    int z = (int) Math.floor(Mth.cos(j) * i);
                    if (direction == Direction.WEST || direction == Direction.EAST) {
                        if (world.isEmptyBlock(position.offset(0, x, z))) {
                            decorate(world, position.offset(0, x, z), position, size, rand, WorldGenMyrmexHive.RoomType.TUNNEL);
                        }
                        if (world.isEmptyBlock(position.offset(0, x, (z - 1)))) {
                            decorateTubers(world, position.offset(0, x, z), rand, WorldGenMyrmexHive.RoomType.TUNNEL);
                        }
                    } else {
                        if (world.isEmptyBlock(position.offset(x, z, 0))) {
                            decorate(world, position.offset(x, z, 0), position, size, rand, WorldGenMyrmexHive.RoomType.TUNNEL);
                        }
                        if (world.isEmptyBlock(position.offset(0, x, (z - 1)))) {
                            decorateTubers(world, position.offset(0, x, z), rand, WorldGenMyrmexHive.RoomType.TUNNEL);
                        }
                    }
                }
            }
        }
    }

    @Overwrite
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldIn = context.level();
        RandomSource rand = context.random();
        BlockPos pos = context.origin();
        if (!this.small) {
            int random = rand.nextInt(IafConfig.myrmexColonyGenChance);
            if (random != 0 || !IafWorldRegistry.isFarEnoughFromSpawn(worldIn, pos)) {
                GregitasCore.LOGGER.info("Could not plate myrmex colony 1 Genration chance: " + random + " out of: " + IafConfig.myrmexColonyGenChance);
                return false;
            }

            if (MyrmexWorldData.get(worldIn.getLevel()) != null /*&& MyrmexWorldData.get(worldIn.getLevel()).getNearestHive(pos, 200) != null*/) {
                GregitasCore.LOGGER.info("Could not plate myrmex colony 2");
                return false;
            }
        }

        if (!this.small && !worldIn.getFluidState(pos.below()).isEmpty()) {
            GregitasCore.LOGGER.info("Could not plate myrmex colony 3");
            return false;
        } else {
            this.hasFoodRoom = false;
            this.hasNursery = false;
            this.totalRooms = 0;
            int down = Math.max(15, pos.getY() - 20 + rand.nextInt(10));
            BlockPos undergroundPos = new BlockPos(pos.getX(), down, pos.getZ());
            this.entrances = 0;
            this.centerOfHive = undergroundPos;
            this.generateMainRoom(worldIn, rand, undergroundPos);
            this.small = false;
            return true;
        }
    }

    @Shadow
    abstract void decorate(LevelAccessor world, BlockPos blockpos, BlockPos center, int size, RandomSource random, WorldGenMyrmexHive.RoomType roomType);

    @Shadow
    abstract void decorateTubers(LevelAccessor world, BlockPos blockpos, RandomSource random, WorldGenMyrmexHive.RoomType roomType);

    @Shadow
    abstract void generateMainRoom(ServerLevelAccessor world, RandomSource rand, BlockPos position);

}
