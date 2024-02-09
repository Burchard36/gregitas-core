package com.allthemods.gravitas2.core.mixin;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.entity.util.MyrmexHive;
import com.github.alexthe666.iceandfire.world.IafWorldRegistry;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import com.github.alexthe666.iceandfire.world.gen.TypedFeature;
import com.github.alexthe666.iceandfire.world.gen.WorldGenMyrmexHive;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
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
    @Shadow
    public MyrmexHive hive;
    @Shadow
    private boolean jungle;

    @Shadow
    private static BlockState DESERT_RESIN;
    @Shadow
    private static BlockState STICKY_DESERT_RESIN;
    @Shadow
    private static BlockState JUNGLE_RESIN;
    @Shadow
    private static BlockState STICKY_JUNGLE_RESIN;

    public WorldGenMyrmexHiveMixin(Codec<NoneFeatureConfiguration> codex) {
        super(codex);
    }


    @Overwrite
    private void decorateCircle(LevelAccessor world, RandomSource rand, BlockPos position, int size, int height, Direction direction) {
        int radius = size + 2;

        for (float i = 0; i < radius; i += 0.5) {
            for (float j = 0; j < 2 * Math.PI * i; j += 0.5) {
                int x = (int) Math.floor(Mth.sin(j) * i);
                int z = (int) Math.floor(Mth.cos(j) * i);
                BlockPos offsetPos;

                if (direction == Direction.WEST || direction == Direction.EAST) {
                    offsetPos = position.offset(0, x, z);
                } else {
                    offsetPos = position.offset(x, z, 0);
                }

                if (world.isAreaLoaded(offsetPos, 1)) {
                    if (world.isEmptyBlock(offsetPos)) {
                        decorate(world, offsetPos, position, size, rand, WorldGenMyrmexHive.RoomType.TUNNEL);
                    }
                }

                BlockPos tuberPos = position.offset(0, x, (z - 1));

                // TODO: This may not even work, check for crashes related to myrmex!
                if (world.isAreaLoaded(tuberPos, 1) && world.isEmptyBlock(tuberPos)) {
                    decorateTubers(world, tuberPos, rand, WorldGenMyrmexHive.RoomType.TUNNEL);
                }
            }
        }
    }


    @Overwrite
    private void generateMainRoom(ServerLevelAccessor world, RandomSource rand, BlockPos position) {
        this.hive = new MyrmexHive(world.getLevel(), position, 100);
        MyrmexWorldData.addHive(world.getLevel(), this.hive);
        BlockState resin = this.jungle ? JUNGLE_RESIN : DESERT_RESIN;
        BlockState sticky_resin = this.jungle ? STICKY_JUNGLE_RESIN : STICKY_DESERT_RESIN;
        this.generateSphere(world, rand, position, 14, 7, resin, sticky_resin);
        this.generateSphere(world, rand, position, 12, 5, Blocks.AIR.defaultBlockState());
        this.decorateSphere(world, rand, position, 12, 5, WorldGenMyrmexHive.RoomType.QUEEN);
        this.generatePath(world, rand, position.relative(Direction.NORTH, 7).below(), 10 + rand.nextInt(8), Direction.NORTH, 100);
        this.generatePath(world, rand, position.relative(Direction.SOUTH, 7).below(), 10 + rand.nextInt(8), Direction.SOUTH, 100);
        this.generatePath(world, rand, position.relative(Direction.WEST, 7).below(), 10 + rand.nextInt(8), Direction.WEST, 100);
        this.generatePath(world, rand, position.relative(Direction.EAST, 7).below(), 10 + rand.nextInt(8), Direction.EAST, 100);
        if (!this.small) {
            EntityMyrmexQueen queen = new EntityMyrmexQueen(IafEntityRegistry.MYRMEX_QUEEN.get(), world.getLevel());
            BlockPos ground = MyrmexHive.getGroundedPos(world, position);
            queen.finalizeSpawn(world, world.getCurrentDifficultyAt(ground), MobSpawnType.CHUNK_GENERATION, null, null);
            queen.setHive(this.hive);
            queen.setJungleVariant(this.jungle);
            queen.absMoveTo((double)ground.getX() + 0.5, (double)ground.getY() + 1.0, (double)ground.getZ() + 0.5, 0.0F, 0.0F);
            world.addFreshEntity(queen);

            int i;
            for(i = 0; i < 4 + rand.nextInt(3); ++i) {
                EntityMyrmexBase myrmex = new EntityMyrmexWorker(IafEntityRegistry.MYRMEX_WORKER.get(), world.getLevel());
                myrmex.finalizeSpawn(world, world.getCurrentDifficultyAt(ground), MobSpawnType.CHUNK_GENERATION, null, null);
                myrmex.setHive(this.hive);
                myrmex.absMoveTo((double)ground.getX() + 0.5, (double)ground.getY() + 1.0, (double)ground.getZ() + 0.5, 0.0F, 0.0F);
                myrmex.setJungleVariant(this.jungle);
                world.addFreshEntity(myrmex);
            }

            for(i = 0; i < 2 + rand.nextInt(2); ++i) {
                EntityMyrmexBase myrmex = new EntityMyrmexSoldier(IafEntityRegistry.MYRMEX_SOLDIER.get(), world.getLevel());
                myrmex.finalizeSpawn(world, world.getCurrentDifficultyAt(ground), MobSpawnType.CHUNK_GENERATION, null, null);
                myrmex.setHive(this.hive);
                myrmex.absMoveTo((double)ground.getX() + 0.5, (double)ground.getY() + 1.0, (double)ground.getZ() + 0.5, 0.0F, 0.0F);
                myrmex.setJungleVariant(this.jungle);
                world.addFreshEntity(myrmex);
            }

            for(i = 0; i < rand.nextInt(2); ++i) {
                EntityMyrmexBase myrmex = new EntityMyrmexSentinel(IafEntityRegistry.MYRMEX_SENTINEL.get(), world.getLevel());
                myrmex.finalizeSpawn(world, world.getCurrentDifficultyAt(ground), MobSpawnType.CHUNK_GENERATION, null, null);
                myrmex.setHive(this.hive);
                myrmex.absMoveTo((double)ground.getX() + 0.5, (double)ground.getY() + 1.0, (double)ground.getZ() + 0.5, 0.0F, 0.0F);
                myrmex.setJungleVariant(this.jungle);
                world.addFreshEntity(myrmex);
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
                return false;
            }

            if (MyrmexWorldData.get(worldIn.getLevel()) == null /*&& MyrmexWorldData.get(worldIn.getLevel()).getNearestHive(pos, 200) != null*/) {
                return false;
            }
        }

        if (!this.small && !worldIn.getFluidState(pos.below()).isEmpty()) {
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
        //return false;
    }

    @Overwrite
    private void generateEntrance(LevelAccessor world, RandomSource rand, BlockPos position, int size, int height, Direction direction) {
        BlockPos up = position.above();
        this.hive.getEntranceBottoms().put(up, direction);

        while(up.getY() < world.getHeightmapPos(this.small ? Heightmap.Types.MOTION_BLOCKING_NO_LEAVES : Heightmap.Types.WORLD_SURFACE_WG, up).getY() && !world.getBlockState(up).is(BlockTags.LOGS)) {
            this.generateCircleRespectSky(world, rand, up, size, height, direction);
            up = up.above().relative(direction);
        }

        BlockState resin = this.jungle ? JUNGLE_RESIN : DESERT_RESIN;
        BlockState sticky_resin = this.jungle ? STICKY_JUNGLE_RESIN : STICKY_DESERT_RESIN;
        this.generateSphereRespectAir(world, rand, up, size + 4, height + 2, resin, sticky_resin);
        this.generateSphere(world, rand, up.above(), size, height, Blocks.AIR.defaultBlockState());
        this.decorateSphere(world, rand, up.above(), size, height - 1, WorldGenMyrmexHive.RoomType.ENTERANCE);
        this.hive.getEntrances().put(up, direction);
        ++this.entrances;
    }


    @Overwrite
    private void generateCircleRespectSky(LevelAccessor world, RandomSource rand, BlockPos position, int size, int height, Direction direction) {
        BlockState resin = this.jungle ? JUNGLE_RESIN : DESERT_RESIN;
        BlockState sticky_resin = this.jungle ? STICKY_JUNGLE_RESIN : STICKY_DESERT_RESIN;
        int radius = size + 2;

        float i;
        float j;
        int x;
        int z;
        for (i = 0.0F; i < (float) radius; i = (float) ((double) i + 0.5)) {
            for (j = 0.0F; (double) j < 9.583185307179586 * (double) i; j = (float) ((double) j + 0.5)) {
                x = (int) Math.floor((double) (Mth.sin(j) * i));
                z = (int) Math.floor((double) (Mth.cos(j) * i));
                if (direction != Direction.WEST && direction != Direction.EAST) {
                    if (!world.canSeeSkyFromBelowWater(position.offset(x, z, 0))) {
                        world.setBlock(position.offset(x, z, 0), rand.nextInt(3) == 0 ? sticky_resin : resin, 3);
                    }
                } else if (!world.canSeeSkyFromBelowWater(position.offset(0, x, z))) {
                    world.setBlock(position.offset(0, x, z), rand.nextInt(3) == 0 ? sticky_resin : resin, 3);
                }
            }
        }
    }

    @Shadow
    abstract void generateSphereRespectAir(LevelAccessor world, RandomSource rand, BlockPos position, int size, int height, BlockState fill, BlockState fill2);

    @Shadow
    abstract void decorate(LevelAccessor world, BlockPos blockpos, BlockPos center, int size, RandomSource random, WorldGenMyrmexHive.RoomType roomType);

    @Shadow
    abstract void decorateTubers(LevelAccessor world, BlockPos blockpos, RandomSource random, WorldGenMyrmexHive.RoomType roomType);

    @Shadow
    abstract void generatePath(LevelAccessor world, RandomSource rand, BlockPos offset, int length, Direction direction, int roomChance);

    @Shadow
    abstract void generateSphere(LevelAccessor world, RandomSource rand, BlockPos position, int size, int height, BlockState fill);

    @Shadow
    abstract void generateSphere(LevelAccessor world, RandomSource rand, BlockPos position, int size, int height, BlockState fill, BlockState fill2);

    @Shadow
    abstract void decorateSphere(LevelAccessor world, RandomSource rand, BlockPos position, int size, int height, WorldGenMyrmexHive.RoomType roomType);

}
