package com.gghyrmrwf.glebthanwolves.events;

import com.gghyrmrwf.glebthanwolves.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 3.13 — play a custom user-provided sound whenever a creeper detonates.
 *
 * <p>Hooks {@link ExplosionEvent.Start}, which fires once per explosion just
 * before the affected blocks/entities are resolved. We inspect
 * {@link Explosion#getDirectSourceEntity()} and only act when the explosion
 * was started by a {@link Creeper} — TNT, ghast fireballs, end crystals,
 * beds, and respawn anchors are NOT touched.
 *
 * <p>The vanilla creeper-fuse hiss and the post-explosion thump still play
 * as normal; the custom sound layers on top.
 */
public class CreeperExplodeSoundEvents {

    /** Custom-sound volume (1.0 = vanilla default). */
    private static final float SOUND_VOLUME = 1.0f;

    /** Custom-sound pitch (1.0 = unchanged). */
    private static final float SOUND_PITCH = 1.0f;

    @SubscribeEvent
    public void onExplosionStart(ExplosionEvent.Start event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        Explosion explosion = event.getExplosion();
        if (!(explosion.getDirectSourceEntity() instanceof Creeper creeper)) return;

        level.playSound(
                null,
                creeper.getX(),
                creeper.getY(),
                creeper.getZ(),
                ModSounds.CREEPER_EXPLODE.get(),
                SoundSource.HOSTILE,
                SOUND_VOLUME,
                SOUND_PITCH
        );
    }
}
