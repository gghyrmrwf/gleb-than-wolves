package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 3.2 — Zombified Piglins are always hostile.
 *
 * <p>Vanilla behavior: a zombified piglin is "neutral" until provoked.
 * Provocation sources include being attacked, being attacked-by-proxy
 * (a piglin killed by the player aggros the herd), and herd-alert
 * propagation. Once provoked, vanilla sets a persistent-anger timer
 * (default 25 s ± random) tied to a specific player UUID, and the
 * piglin attacks until the timer runs out or the target dies.
 *
 * <p>This handler forces every adult zombified piglin to permanently
 * stay angry at the nearest eligible player. Every
 * {@link #CHECK_INTERVAL_TICKS} ticks we:
 *
 * <ol>
 *   <li>Locate the closest non-creative / non-spectator / alive /
 *       non-invisible player within {@link #AGGRO_RADIUS} blocks.</li>
 *   <li>Force the piglin's {@code persistentAngerTarget} (UUID) to
 *       that player.</li>
 *   <li>Refresh the persistent-anger timer to
 *       {@link #ANGER_REFRESH_TICKS} (60 s). Since we re-apply every
 *       0.5 s, the timer never runs out.</li>
 *   <li>If the piglin's current attack-target is not the player, set
 *       it directly to start chase immediately on takeoff.</li>
 * </ol>
 *
 * <p>Baby zombified piglins are skipped — in vanilla they don't
 * attack adults, only follow the herd. Zoglins ({@code Zoglin}) are
 * a different entity class and already always-hostile; not touched.
 *
 * <p>Once one zombified piglin in a herd is angered, vanilla's
 * "alert" behavior propagates anger to all nearby zombified piglins.
 * Combined with our per-entity force-anger, the entire herd becomes
 * uniformly hostile around any player.
 */
public class AlwaysHostileZombifiedPiglinsEvents {

    /**
     * Block radius for "the nearest player is too far away to bother
     * angering the piglin". 16 matches Phase 3.1 piglins; once
     * angered, vanilla follow_range (~35 blocks) takes over for the
     * actual chase distance.
     */
    private static final double AGGRO_RADIUS = 16.0;

    /**
     * Re-apply anger every 10 ticks (~0.5 s). Vanilla anger timer
     * decrements per tick; refreshing every half-second is fast
     * enough that the timer never lapses but cheap on CPU.
     */
    private static final int CHECK_INTERVAL_TICKS = 10;

    /**
     * Anger timer value pushed each refresh. 60 s, comfortably more
     * than the 0.5 s refresh interval so the piglin stays angry even
     * if a tick is skipped or the player briefly moves out of range.
     */
    private static final int ANGER_REFRESH_TICKS = 1200;

    @SubscribeEvent
    public void onZombifiedPiglinTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ZombifiedPiglin zp)) return;
        if (zp.level().isClientSide) return;
        if (zp.isBaby()) return;
        if (zp.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        Player nearest = findNearestEligiblePlayer(zp);
        if (nearest == null) return;

        // Force this player as the persistent anger target.
        zp.setPersistentAngerTarget(nearest.getUUID());
        zp.setRemainingPersistentAngerTime(ANGER_REFRESH_TICKS);

        // Set the current attack target so chase starts immediately.
        // setTarget() is a no-op if the target is already correct, so
        // this is cheap to call every refresh.
        if (zp.getTarget() != nearest) {
            zp.setTarget(nearest);
        }
    }

    /**
     * Find the closest player to {@code zp} that's a legitimate
     * attack target. Skips creative / spectator players, dead or
     * dying players, and players with the Invisibility effect
     * (vanilla zombified piglins respect invisibility).
     */
    private static Player findNearestEligiblePlayer(ZombifiedPiglin zp) {
        Player best = null;
        double bestSq = AGGRO_RADIUS * AGGRO_RADIUS;
        for (Player p : zp.level().players()) {
            if (p.isCreative() || p.isSpectator()) continue;
            if (p.isDeadOrDying()) continue;
            if (p.hasEffect(MobEffects.INVISIBILITY)) continue;
            double dsq = p.distanceToSqr(zp);
            if (dsq < bestSq) {
                bestSq = dsq;
                best = p;
            }
        }
        return best;
    }
}
