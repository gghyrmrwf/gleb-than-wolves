package com.gghyrmrwf.glebthanwolves.client;

import com.gghyrmrwf.glebthanwolves.GlebThanWolves;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

/**
 * Phase 3.15 — flat billboard replacement for the ender dragon's 3D model.
 *
 * <p>Hitboxes (server-side EnderDragonPart sub-entities) are untouched —
 * combat, damage, dragon-breath particles, and the boss bar all behave
 * exactly as vanilla.
 */
public class FlatEnderDragonRenderer extends FlatBillboardRenderer<EnderDragon> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            GlebThanWolves.MODID, "textures/entity/flat_dragon.png");

    public FlatEnderDragonRenderer(EntityRendererProvider.Context context) {
        super(context, TEXTURE, 8.0f /* half-size: ~16 blocks */, 4.0f /* y offset */);
    }
}
