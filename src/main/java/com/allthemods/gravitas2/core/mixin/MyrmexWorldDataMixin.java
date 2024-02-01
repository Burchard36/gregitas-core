package com.allthemods.gravitas2.core.mixin;

import dev.architectury.patchedmixin.staticmixin.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.level.saveddata.SavedData;

@Mixin(value = MyrmexWorldDataMixin.class, remap = false)
public abstract class MyrmexWorldDataMixin extends SavedData {



    public MyrmexWorldDataMixin() {

    }


}
