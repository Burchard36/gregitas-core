package com.allthemods.gravitas2;

import com.allthemods.gravitas2.util.IAFEntityMap;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GregitasCore.MOD_ID)
public class GregitasCore {
    public static final String MOD_ID = "gregitas_core";
    public static final Logger LOGGER = LogManager.getLogger();

    public GregitasCore() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void initSpawnData(ServerAboutToStartEvent event){
        IAFEntityMap.init();
    }
    @SubscribeEvent
    public void spawnCheck(MobSpawnEvent.FinalizeSpawn event) {
        if(event.getEntity() instanceof Sheep){ event.getEntity().discard(); event.setSpawnCancelled(true); event.setCanceled(true); }
        if (!IAFEntityMap.spawnList.containsKey(event.getEntity().getType())) return;
        if (!(event.getLevel().getLevel().dimension() == Level.OVERWORLD)) return;
        var start = Util.getNanos();
        if (event.getLevel() instanceof WorldGenLevel wgl){
            BlockPos pos = new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ());
            ChunkDataProvider provider = ChunkDataProvider.get(wgl);
            ChunkData data = provider.get(wgl, pos);
            float rainfall = data.getRainfall(pos);
            float avgAnnualTemperature = data.getAverageTemp(pos);
            EntityType<?> entityType = event.getEntity().getType();
            var climateTest = IAFEntityMap.spawnList.get(entityType);
            var tempAndRainfall = new float[]{avgAnnualTemperature, rainfall};
            if (!climateTest.test(tempAndRainfall)) {
                event.setSpawnCancelled(true);
                event.setCanceled(true);

            } else {
            }

        }
    }
}


