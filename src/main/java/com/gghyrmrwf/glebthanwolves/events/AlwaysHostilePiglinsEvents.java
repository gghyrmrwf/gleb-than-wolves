package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 3.1 — Piglins are always hostile.
 *
 * <p>Vanilla behavior: a piglin is "neutral" toward a player who wears
 * any piece of gold armor (head/chest/legs/feet). Piglins also enter an
 * "admire" sub-state when they pick up a gold item, during which they
 * stop attacking for ~5 seconds and may trade.
 *
 * <p>This event handler bypasses both pacifying behaviors:
 *
 * <ol>
 *   <li>Every {@link #CHECK_INTERVAL_TICKS} ticks, scan each adult
 *       piglin. Find the nearest non-creative / non-spectator,
 *       non-invisible, alive player within {@link #AGGRO_RADIUS}
 *       blocks. Inject that player into the piglin's
 *       {@code NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD} brain memory
 *       slot. Vanilla AI normally clears this slot when the player
 *       wears gold armor; by force-setting it we make the piglin
 *       treat the player as a valid target regardless of armor.</li>
 *   <li>Erase the piglin's {@code ADMIRING_ITEM} memory. This prevents
 *       the piglin from entering its admire-and-pacify state when it
 *       picks up a thrown gold item. The classic "throw gold ingot to
 *       distract piglins" exploit no longer works.</li>
 * </ol>
 *
 * <p>Baby piglins are skipped — in vanilla they never attack, only
 * panic-flee. Piglin brutes (which extend {@link
 * net.minecraft.world.entity.monster.piglin.PiglinBrute}, not
 * {@link Piglin}) are already always-hostile in vanilla and need no
 * handling. Zoglins (zombified piglins) are also already always-hostile.
 *
 * <p>Dimension-agnostic: applies wherever piglins exist (Nether by
 * spawn, Overworld/End only via portal teleport before zombification).
 */
public class AlwaysHostilePiglinsEvents {

    /**
     * Maximum distance at which a piglin will lock onto a player.
     * 16 blocks matches the vanilla "without gold armor" detection
     * range, so the mechanic just removes the gold-armor exception
     * without buffing detection range.
     */
    private static final double AGGRO_RADIUS = 16.0;

    /**
     * Re-apply the target every {@code CHECK_INTERVAL_TICKS} ticks.
     * Vanilla brain logic may clear the memory between checks; 10 ticks
     * (0.5 s) is fast enough that any neutral window is imperceptible
     * but slow enough that the per-tick CPU cost is negligible.
     */
    private static final int CHECK_INTERVAL_TICKS = 10;

    @SubscribeEvent
    public void onPiglinTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Piglin piglin)) return;
        if (piglin.level().isClientSide) return;
        if (piglin.isBaby()) return;
        if (piglin.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        Player nearest = findNearestEligiblePlayer(piglin);
        if (nearest == null) return;

        // Force this player into the "targetable, no gold armor" slot.
        // Vanilla AI will then route them to the attack-target memory
        // through the regular piglin behavior tree, so they get the
        // proper hostile animations, weapon raise, charge, etc.
        piglin.getBrain().setMemory(
                MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD,
                nearest
        );

        // Bypass admiration: a piglin that just picked up gold won't
        // freeze to admire it; it will keep attacking.
        piglin.getBrain().eraseMemory(MemoryModuleType.ADMIRING_ITEM);
        piglin.getBrain().eraseMemory(MemoryModuleType.ADMIRING_DISABLED);
    }

    /**
     * Find the closest player to {@code piglin} that's a legitimate
     * attack target. Skips creative / spectator players, dead or dying
     * players, and players with the Invisibility effect (vanilla
     * piglins respect invisibility, we preserve that).
     */
    private static Player findNearestEligiblePlayer(Piglin piglin) {
        Player best = null;
        double bestSq = AGGRO_RADIUS * AGGRO_RADIUS;
        for (Player p : piglin.level().players()) {
            if (p.isCreative() || p.isSpectator()) continue;
            if (p.isDeadOrDying()) continue;
            if (p.hasEffect(MobEffects.INVISIBILITY)) continue;
            double dsq = p.distanceToSqr(piglin);
            if (dsq < bestSq) {
                bestSq = dsq;
                best = p;
            }
        }
        return best;
    }
}
