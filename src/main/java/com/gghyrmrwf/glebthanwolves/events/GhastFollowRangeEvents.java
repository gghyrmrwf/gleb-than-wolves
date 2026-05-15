package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 3.3 — Ghasts hear at 100 blocks.
 *
 * <p>Vanilla {@link Ghast} entity has a default {@code FOLLOW_RANGE}
 * attribute of 64 blocks. That value is used by the ghast's
 * {@code NearestAttackableTargetGoal<Player>} target selector to decide
 * whether a player is "close enough" to attack. We bump the base value
 * to {@link #GHAST_FOLLOW_RANGE} blocks at spawn, so ghasts notice and
 * start shooting at players from much further away.
 *
 * <p>The vanilla ±4 vertical filter on the target predicate is
 * preserved (we don't touch the goal): a ghast still ignores any
 * player whose Y coordinate differs from its own by more than 4
 * blocks. That filter is what keeps ghasts from firing through whole
 * Nether biomes at players they can't see; lifting it would create
 * blind sniping. Bumping FOLLOW_RANGE only affects the horizontal
 * detection.
 *
 * <p>We use {@code setBaseValue}, so any other modifier that adds
 * (e.g. a difficulty-scaling mod) still stacks on top. Server-side
 * only; runs once per ghast at spawn.
 */
public class GhastFollowRangeEvents {

    /** New FOLLOW_RANGE base value for ghasts (vanilla is 64). */
    private static final double GHAST_FOLLOW_RANGE = 100.0;

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Ghast ghast)) return;
        if (ghast.level().isClientSide) return;

        AttributeInstance attr = ghast.getAttribute(Attributes.FOLLOW_RANGE);
        if (attr != null && attr.getBaseValue() < GHAST_FOLLOW_RANGE) {
            attr.setBaseValue(GHAST_FOLLOW_RANGE);
        }
    }
}
