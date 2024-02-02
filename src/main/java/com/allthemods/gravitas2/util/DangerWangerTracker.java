package com.allthemods.gravitas2.util;

import com.allthemods.gravitas2.GregitasCore;
import com.github.alexthe666.iceandfire.IafConfig;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class DangerWangerTracker {

    public static List<BlockPos> dangerousLocations = new ArrayList<>();

    public static void addDanger(final BlockPos pos) {
        dangerousLocations.add(pos);
    }

    public static boolean isNearDanger(final BlockPos position) {
        for (final BlockPos pos : dangerousLocations) {
            final double distance = distanceTo(pos, position);
            if (distance < IafConfig.dangerousWorldGenSeparationLimit) {
                //GregitasCore.LOGGER.info("Danger check passed with a equation %s < %s".formatted(distance, IafConfig.dangerousWorldGenSeparationLimit));
                return true;
            }
        }
        return false;
    }

    public static  double distanceTo(BlockPos pos, BlockPos pos2) {
        return Math.sqrt(Math.pow(pos2.getX() - pos.getX(), 2) + Math.pow(pos2.getY() - pos.getY(), 2) + Math.pow(pos.getZ() - pos.getZ(), 2));
    }

}
