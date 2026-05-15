package com.gghyrmrwf.glebthanwolves.client;

import com.gghyrmrwf.glebthanwolves.GlebThanWolves;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only mod bus subscriber that overrides vanilla entity renderers.
 *
 * <p>Currently registers {@link FlatEnderDragonRenderer} in place of the
 * vanilla ender-dragon renderer. Hitboxes and server-side behaviour are
 * unaffected.
 */
@Mod.EventBusSubscriber(
        modid = GlebThanWolves.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ClientRenderers {

    private ClientRenderers() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.ENDER_DRAGON, FlatEnderDragonRenderer::new);
    }
}
