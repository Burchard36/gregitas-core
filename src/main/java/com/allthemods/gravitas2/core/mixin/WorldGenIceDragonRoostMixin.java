package com.allthemods.gravitas2.core.mixin;

import com.github.alexthe666.iceandfire.world.gen.WorldGenFireDragonRoosts;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = WorldGenFireDragonRoosts.class,remap = false)
public abstract class WorldGenIceDragonRoostMixin {

    /**
     * @author thevortex
     * @reason placeholder
     */
    @Overwrite(remap = false)
    protected BlockState transform(BlockState state) {
        if (state.getBlock() == Blocks.COBBLESTONE) { return Blocks.AIR.defaultBlockState(); }
        return state.getBlock() == Blocks.AIR ? Blocks.AIR.defaultBlockState() : state;
    }
}
