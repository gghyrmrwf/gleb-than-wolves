package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Phase 3.6 — Chickens lay TNT instead of eggs (10%).
 *
 * <p>Vanilla chicken behavior: every 6000-12000 ticks (5-10 min), an
 * adult non-jockey chicken calls {@code spawnAtLocation(Items.EGG)},
 * which spawns an {@link ItemEntity} containing an egg at the
 * chicken's current position.
 *
 * <p>This handler hooks {@link EntityJoinLevelEvent}, filters for
 * {@link ItemEntity}s containing eggs, checks whether an adult
 * {@link Chicken} is within {@link #CHICKEN_PROXIMITY_RADIUS} blocks
 * (to distinguish naturally-laid eggs from player-dropped eggs), and
 * with {@link #TNT_CHANCE} probability cancels the egg spawn and
 * replaces it with a {@link PrimedTnt} entity instead.
 *
 * <p>Why proximity check vs. player-thrown eggs: when a player
 * "throws" an egg, vanilla spawns a {@code ThrownEgg} projectile,
 * NOT an {@code ItemEntity}. So an ItemEntity with {@code Items.EGG}
 * comes from one of:
 *
 * <ul>
 *   <li>Chicken laying it (most common — we want to catch this).</li>
 *   <li>Player dropping an egg item from inventory (rare).</li>
 *   <li>Loot tables / dispensers (very rare).</li>
 * </ul>
 *
 * <p>Checking for a nearby Chicken filters cases 2 and 3 out for the
 * most part. Chickens don't move during the same tick they lay (lay
 * fires in {@code Chicken#aiStep}, position is current), so the egg
 * spawns at ~0 distance from the chicken.
 *
 * <p>TNT properties: fuse 80 ticks (4 seconds — default vanilla
 * primed TNT). Sufficient warning for nearby entities to retreat but
 * still a meaningful threat. Owner is null (anonymous explosion).
 *
 * <p>Chicken jockeys (a chicken being ridden by a baby zombie) do
 * NOT lay eggs in vanilla, so this handler never fires for them.
 */
public class ChickenLaysTntEvents {

    /** Probability of an egg being replaced with TNT. */
    private static final float TNT_CHANCE = 0.10f;

    /**
     * Maximum block distance between the egg ItemEntity and an adult
     * Chicken for the egg to be considered "laid". Player-dropped
     * eggs typically aren't this close to a chicken unless the player
     * deliberately dropped one next to one — acceptable edge case.
     */
    private static final double CHICKEN_PROXIMITY_RADIUS = 1.5;

    /** TNT fuse in ticks (4 seconds, vanilla default). */
    private static final int TNT_FUSE_TICKS = 80;

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ItemEntity ie)) return;
        if (!ie.getItem().is(Items.EGG)) return;

        Level level = (Level) event.getLevel();

        // Must be near a Chicken (i.e. it's a laid egg, not dropped).
        AABB box = ie.getBoundingBox().inflate(CHICKEN_PROXIMITY_RADIUS);
        List<Chicken> nearby = level.getEntitiesOfClass(Chicken.class, box);
        if (nearby.isEmpty()) return;

        if (level.getRandom().nextFloat() >= TNT_CHANCE) return;

        // Cancel the egg ItemEntity spawn and replace with primed TNT.
        event.setCanceled(true);

        PrimedTnt tnt = new PrimedTnt(level, ie.getX(), ie.getY(), ie.getZ(), null);
        tnt.setFuse(TNT_FUSE_TICKS);
        level.addFreshEntity(tnt);
    }
}
