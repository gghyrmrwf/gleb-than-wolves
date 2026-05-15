package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 3.10 — Endermen in crimson/warped forest aggro on proximity.
 *
 * <p>Vanilla endermen only become hostile when a player either looks
 * directly at their head or attacks them. In Nether's crimson_forest
 * and warped_forest biomes (where endermen spawn most frequently),
 * this handler force-aggros every adult enderman onto the nearest
 * eligible player within 12 blocks, without needing the look
 * trigger.
 *
 * <p>Biome restriction. Only the two Nether forest biomes are
 * affected:
 *
 * <ul>
 *   <li>{@code minecraft:crimson_forest} — red-toned forest.</li>
 *   <li>{@code minecraft:warped_forest} — blue-toned forest, main
 *       enderman habitat in Nether.</li>
 * </ul>
 *
 * <p>Other Nether biomes (nether_wastes, soul_sand_valley,
 * basalt_deltas) and Overworld/End endermen are NOT affected — they
 * keep vanilla look-aggro behavior.
 *
 * <p>Carved pumpkin still defends. Vanilla endermen ignore players
 * wearing a carved pumpkin helmet (the "enderman defense"). Logically
 * this is a look-only defense, but in the game's lore players who
 * learned to wear pumpkins for enderman safety should keep the
 * benefit — removing it would feel like an unfair surprise. So the
 * carved_pumpkin helmet check is preserved here too.
 *
 * <p>Implementation pattern mirrors Phase 3.1
 * (AlwaysHostilePiglinsEvents) and Phase 3.2
 * (AlwaysHostileZombifiedPiglinsEvents): per-tick check every 10
 * ticks, find a nearby eligible player, force {@code setTarget} if
 * the enderman has no existing target. Endermen use traditional
 * Goal-based AI (not the brain memory system that piglins use), so
 * {@code setTarget} is the correct API.
 *
 * <p>Once aggro'd, vanilla teleport / attack AI handles the chase
 * naturally. Water (rain) doesn't apply — Nether has no weather.
 */
public class EndermanProximityAggroEvents {

    /** Maximum distance at which an enderman aggros on a player. */
    private static final double AGGRO_RADIUS = 12.0;

    /** How often the per-enderman check runs. */
    private static final int CHECK_INTERVAL_TICKS = 10;

    @SubscribeEvent
    public void onEndermanTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EnderMan enderman)) return;
        if (enderman.level().isClientSide) return;
        if (enderman.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        Holder<Biome> biomeHolder = enderman.level().getBiome(enderman.blockPosition());
        if (!biomeHolder.is(Biomes.CRIMSON_FOREST) && !biomeHolder.is(Biomes.WARPED_FOREST)) {
            return;
        }

        // Don't override an existing valid target.
        if (enderman.getTarget() != null && enderman.getTarget().isAlive()) return;

        Player nearest = findNearestEligible(enderman);
        if (nearest != null) {
            enderman.setTarget(nearest);
        }
    }

    private static Player findNearestEligible(EnderMan enderman) {
        Player best = null;
        double bestSq = AGGRO_RADIUS * AGGRO_RADIUS;
        for (Player p : enderman.level().players()) {
            if (p.isCreative() || p.isSpectator()) continue;
            if (p.isDeadOrDying()) continue;
            if (p.hasEffect(MobEffects.INVISIBILITY)) continue;

            // Carved pumpkin defense (preserved from vanilla).
            ItemStack helmet = p.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.is(Items.CARVED_PUMPKIN)) continue;

            double dsq = p.distanceToSqr(enderman);
            if (dsq < bestSq) {
                bestSq = dsq;
                best = p;
            }
        }
        return best;
    }
}
