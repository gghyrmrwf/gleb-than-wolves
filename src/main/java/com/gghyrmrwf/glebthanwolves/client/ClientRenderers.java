package com.gghyrmrwf.glebthanwolves.client;

import com.gghyrmrwf.glebthanwolves.GlebThanWolves;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only mod bus subscriber that overrides vanilla entity renderers.
 *
 * <p>Registers flat-billboard replacements for:
 * <ul>
 *   <li>{@link net.minecraft.world.entity.boss.enderdragon.EnderDragon}
 *       (Phase 3.15) — 16-block billboard.</li>
 *   <li>{@link net.minecraft.world.entity.monster.EnderMan}
 *       (Phase 3.17) — 3-block billboard.</li>
 *   <li>{@link net.minecraft.world.entity.monster.warden.Warden}
 *       (Phase 3.17) — 5-block billboard.</li>
 * </ul>
 * Hitboxes and server-side behaviour are unaffected for all three.
 */
@Mod.EventBusSubscriber(
        modid = GlebThanWolves.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ClientRenderers {

    private static final ResourceLocation FACE_TEXTURE = new ResourceLocation(
            GlebThanWolves.MODID, "textures/entity/flat_dragon.png");

    private ClientRenderers() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.ENDER_DRAGON, FlatEnderDragonRenderer::new);
        event.registerEntityRenderer(EntityType.ENDERMAN,
                ctx -> new FlatBillboardRenderer<>(ctx, FACE_TEXTURE, 1.5f /* half-size: 3 blocks */, 1.0f));
        event.registerEntityRenderer(EntityType.WARDEN,
                ctx -> new FlatBillboardRenderer<>(ctx, FACE_TEXTURE, 2.5f /* half-size: 5 blocks */, 1.6f));
    }
}
