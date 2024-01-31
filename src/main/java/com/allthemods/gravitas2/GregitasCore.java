package com.allthemods.gravitas2;

import com.allthemods.gravitas2.util.IAFEntityMap;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(GregitasCore.MOD_ID)
public class GregitasCore {
    public static final String MOD_ID = "gregitas_core";
    public static final Logger LOGGER = LogManager.getLogger();

    public GregitasCore() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public final void initSpawnData(final ServerAboutToStartEvent event){
        IAFEntityMap.init();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) // We have FINAL SAY SO over mob spawns! Can be important in case IAF/TFC overwrites
    public final void spawnCheck(final MobSpawnEvent.FinalizeSpawn event) {
        if (event.getEntity() instanceof Sheep) {
            event.getEntity().discard();
            event.setSpawnCancelled(true);
            event.setCanceled(true);
        }
        if (!IAFEntityMap.spawnList.containsKey(event.getEntity().getType())) return;

        if (!(event.getLevel().getLevel().dimension() == Level.OVERWORLD)) return;

        if (event.getLevel() instanceof WorldGenLevel wgl) {
            final BlockPos pos = new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ());
            final ChunkDataProvider provider = ChunkDataProvider.get(wgl);
            final ChunkData data = provider.get(wgl, pos);
            final EntityType<?> entityType = event.getEntity().getType();
            final List<KoppenClimateClassification> entityPositionClassification = IAFEntityMap.spawnList.get(entityType);

            if (entityPositionClassification == null) return; // Had no classification for this spawn

            final KoppenClimateClassification spawnPositionClassification = KoppenClimateClassification.classify(data.getAverageTemp(pos), data.getRainfall(pos));
            if (!entityPositionClassification.contains(spawnPositionClassification)) {
                event.setSpawnCancelled(true);
                event.setCanceled(true);

            }
        }
    }
}


