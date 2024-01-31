package com.allthemods.gravitas2;

import com.allthemods.gravitas2.util.IAFEntityMap;
import net.minecraftforge.common.MinecraftForge;
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

}


