package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Phase 3.9 — Horses buck rider at low HP (30% per second below 30% HP).
 *
 * <p>When a player rides an {@link AbstractHorse} (regular horse,
 * donkey, mule, skeleton horse, zombie horse) and the horse's
 * current health drops below {@link #LOW_HP_THRESHOLD} of its
 * maximum health, the horse becomes increasingly likely to buck
 * its rider off.
 *
 * <p>Check cadence: every {@link #BUCK_CHECK_INTERVAL_TICKS} ticks
 * (1 second). Per check, {@link #BUCK_CHANCE_PER_CHECK} probability
 * of triggering. Expected: ~3-4 seconds before bucking after the
 * horse drops below threshold.
 *
 * <p>Buck effect:
 *
 * <ol>
 *   <li>Capture all passengers (the rider list).</li>
 *   <li>Eject all passengers via {@link AbstractHorse#ejectPassengers()}.</li>
 *   <li>Apply an upward velocity impulse to each ejected rider so
 *       they're physically thrown into the air, not just dismounted.</li>
 * </ol>
 *
 * <p>Why ejectPassengers + velocity rather than damage. A real horse
 * bucking doesn't necessarily injure the rider — the danger is the
 * fall after being thrown. Adding upward velocity ensures the rider
 * falls some distance, which lets fall damage handle the consequence
 * naturally based on terrain. This way buck-on-water is harmless,
 * buck-on-stone is painful — same as real life.
 *
 * <p>The horse continues living. The player can re-mount, but if the
 * horse is still below threshold it will buck again. Effectively
 * the horse refuses to be ridden while wounded — you must heal it
 * (golden apple, golden carrot, hay bale) first.
 *
 * <p>All horse subclasses inherit from {@link AbstractHorse}, so
 * this includes regular horses, donkeys, mules, skeleton horses,
 * and zombie horses. Llamas and camels are NOT AbstractHorse
 * subclasses (Llama extends AbstractChestedHorse... wait, AbstractChestedHorse
 * does extend AbstractHorse; so Llamas DO inherit). Llamas are
 * therefore also affected.
 *
 * <p>Llamas at low HP bucking spit-warriors off — feels appropriate.
 * Donkeys and mules are also affected, which is consistent.
 */
public class AngryHorseBuckEvents {

    /** HP ratio threshold below which horse may buck (0.3 = 30%). */
    private static final float LOW_HP_THRESHOLD = 0.30f;

    /** How often to check for bucking (20 ticks = 1 second). */
    private static final int BUCK_CHECK_INTERVAL_TICKS = 20;

    /** Probability per check that buck triggers (0.30 = 30%). */
    private static final float BUCK_CHANCE_PER_CHECK = 0.30f;

    /**
     * Upward velocity impulse applied to ejected rider. 0.5 = ~1.5
     * block jump-height equivalent. Combined with vanilla gravity and
     * the small forward inertia, the rider lands ~2 blocks from the
     * horse.
     */
    private static final double BUCK_UPWARD_IMPULSE = 0.5;

    @SubscribeEvent
    public void onHorseTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse horse)) return;
        if (horse.level().isClientSide) return;
        if (horse.getPassengers().isEmpty()) return;
        if (horse.tickCount % BUCK_CHECK_INTERVAL_TICKS != 0) return;

        float hpRatio = horse.getHealth() / horse.getMaxHealth();
        if (hpRatio >= LOW_HP_THRESHOLD) return;

        if (horse.getRandom().nextFloat() >= BUCK_CHANCE_PER_CHECK) return;

        // Snapshot passengers before ejecting so we can apply impulses.
        List<Entity> passengers = List.copyOf(horse.getPassengers());
        horse.ejectPassengers();

        for (Entity passenger : passengers) {
            if (passenger instanceof LivingEntity le) {
                Vec3 v = le.getDeltaMovement();
                le.setDeltaMovement(v.x, BUCK_UPWARD_IMPULSE, v.z);
                le.hurtMarked = true;
            }
        }
    }
}
