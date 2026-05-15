package com.gghyrmrwf.glebthanwolves.events;

import com.gghyrmrwf.glebthanwolves.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 3.5 — Pigs explode on death (50% chance, with custom sound).
 *
 * <p>When a regular {@link Pig} dies, roll a 50% chance to detonate
 * a creeper-equivalent explosion at the pig's center. The explosion
 * has no fuse / hiss — it fires the same tick the pig dies — but a
 * custom user-provided sound (registered as
 * {@link ModSounds#PIG_EXPLODE}) plays at the explosion position.
 *
 * <p>The chance was raised from the initial 25% (every 4th) to 50%
 * (every 2nd) at user request — "примерно каждая 5-я" in the user's
 * phrasing turned out to be a math slip; the user confirmed 50% in
 * a follow-up question.
 *
 * <p>Affected entities: {@code minecraft:pig} only. Piglins, zombified
 * piglins, piglin brutes, and hoglins are NOT pigs (separate entity
 * classes) and are not touched here. Baby pigs share the same class,
 * so they also explode if killed.
 *
 * <p>Explosion behavior matches a vanilla un-charged creeper:
 *
 * <ul>
 *   <li>Power 3.0 — same as vanilla creeper. Charged creepers use 6.0,
 *       not used here.</li>
 *   <li>Interaction = {@code MOB} — terrain damage respects the
 *       {@code mobGriefing} game rule, exactly like a vanilla creeper.
 *       Damage to entities is always applied.</li>
 *   <li>No source entity — the explosion is anonymous, attributable
 *       to no killer. This avoids weird "you were killed by pig" death
 *       messages when a player kills a pig at point-blank range.</li>
 * </ul>
 *
 * <p>Sound: {@link ModSounds#PIG_EXPLODE} is played BEFORE the
 * explosion via {@link Level#playSound}. Playing it at the pig's
 * position with the {@code HOSTILE} category and {@code volume=1.0,
 * pitch=1.0} means the vanilla explosion sound still also plays
 * (added by {@link Level#explode}), but they overlap naturally and
 * the user's sound is clearly audible.
 *
 * <p>Order with vanilla drops: {@link LivingDeathEvent} fires before
 * the vanilla loot table is rolled, so items spawn after the
 * explosion and survive it.
 *
 * <p>Lightning-struck pigs that transform into zombified piglins do
 * not go through {@link LivingDeathEvent} (vanilla calls
 * {@code discard()} on the pig, not {@code die()}). No explosion in
 * that case, which is correct — the pig wasn't really "killed".
 */
public class ExplodingPigEvents {

    /** Probability of a pig exploding on death. */
    private static final float EXPLOSION_CHANCE = 0.50f;

    /** Explosion radius — vanilla creeper is 3.0; charged is 6.0. */
    private static final float EXPLOSION_POWER = 3.0f;

    /** Custom-sound volume (1.0 = vanilla default). */
    private static final float SOUND_VOLUME = 1.0f;

    /** Custom-sound pitch (1.0 = unchanged). */
    private static final float SOUND_PITCH = 1.0f;

    @SubscribeEvent
    public void onPigDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Pig pig)) return;
        Level level = pig.level();
        if (level.isClientSide) return;

        if (pig.getRandom().nextFloat() >= EXPLOSION_CHANCE) return;

        level.playSound(
                null,
                pig.getX(),
                pig.getY(),
                pig.getZ(),
                ModSounds.PIG_EXPLODE.get(),
                SoundSource.HOSTILE,
                SOUND_VOLUME,
                SOUND_PITCH
        );

        level.explode(
                null,
                pig.getX(),
                pig.getY() + pig.getBbHeight() / 2.0,
                pig.getZ(),
                EXPLOSION_POWER,
                Level.ExplosionInteraction.MOB
        );
    }
}
