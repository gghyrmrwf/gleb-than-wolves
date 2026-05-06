package com.gghyrmrwf.glebthanwolves;

import com.gghyrmrwf.glebthanwolves.events.BushcraftBreakEvents;
import com.gghyrmrwf.glebthanwolves.events.HardcoreEvents;
import com.gghyrmrwf.glebthanwolves.events.WorldEvents;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(GlebThanWolves.MODID)
public class GlebThanWolves {
    public static final String MODID = "glebthanwolves";

    private static final Logger LOGGER = LogUtils.getLogger();

    public GlebThanWolves() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModLootModifiers.GLM_SERIALIZERS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new BushcraftBreakEvents());
        MinecraftForge.EVENT_BUS.register(new HardcoreEvents());
        MinecraftForge.EVENT_BUS.register(new WorldEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[Gleb Than Wolves] common setup complete — phases 1.1–1.3 loaded");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[Gleb Than Wolves] server starting");
    }
}
