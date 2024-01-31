package com.allthemods.gravitas2.util;

import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.ibm.icu.impl.CollectionSet;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.channels.AsynchronousByteChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class IAFEntityMap {

    public static final Map<EntityType<?>, List<KoppenClimateClassification>> spawnList = new ConcurrentHashMap<>();
    public static final Map<EntityType<?>, List<KoppenClimateClassification>> dragonList = new ConcurrentHashMap<>();

    public static void init() {

        spawnList.put(IafEntityRegistry.AMPHITHERE.get(), List.of(
                KoppenClimateClassification.TEMPERATE,
                KoppenClimateClassification.HOT_DESERT,
                KoppenClimateClassification.TROPICAL_RAINFOREST
        ));

        spawnList.put(IafEntityRegistry.COCKATRICE.get(), List.of(
                KoppenClimateClassification.HUMID_SUBARCTIC,
                KoppenClimateClassification.HOT_DESERT,
                KoppenClimateClassification.HUMID_SUBTROPICAL,
                KoppenClimateClassification.TROPICAL_SAVANNA
        ));

        spawnList.put(IafEntityRegistry.DEATH_WORM.get(), List.of(
                KoppenClimateClassification.COLD_DESERT,
                KoppenClimateClassification.HOT_DESERT
        ));

        spawnList.put(IafEntityRegistry.SIREN.get(), List.of(

                KoppenClimateClassification.HUMID_OCEANIC,
                KoppenClimateClassification.HUMID_SUBTROPICAL,
                KoppenClimateClassification.HUMID_SUBARCTIC
        ));

        spawnList.put(IafEntityRegistry.SEA_SERPENT.get(), List.of(

                KoppenClimateClassification.HUMID_OCEANIC,
                KoppenClimateClassification.HUMID_SUBTROPICAL,
                KoppenClimateClassification.HUMID_SUBARCTIC

        ));


        dragonList.put(IafEntityRegistry.ICE_DRAGON.get(), List.of(
                KoppenClimateClassification.ARCTIC,
                KoppenClimateClassification.COLD_DESERT,
                KoppenClimateClassification.SUBARCTIC,
                KoppenClimateClassification.TUNDRA)
        );

        dragonList.put(IafEntityRegistry.FIRE_DRAGON.get(), List.of(
                KoppenClimateClassification.HOT_DESERT,
                KoppenClimateClassification.HUMID_OCEANIC,
                KoppenClimateClassification.TROPICAL_SAVANNA,
                KoppenClimateClassification.HUMID_SUBTROPICAL
        ));

        dragonList.put(IafEntityRegistry.LIGHTNING_DRAGON.get(), List.of(
                KoppenClimateClassification.TEMPERATE,
                KoppenClimateClassification.SUBTROPICAL,
                KoppenClimateClassification.TROPICAL_RAINFOREST
        ));
    }
}
