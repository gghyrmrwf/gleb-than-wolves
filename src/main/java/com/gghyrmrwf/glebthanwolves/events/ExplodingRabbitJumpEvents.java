package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 3.8 — Rabbits sometimes explode from jumping (2%).
 *
 * <p>Vanilla rabbits jump frequently as part of their movement AI
 * (every ~1-3 seconds). Each jump triggers Forge's
 * {@link LivingEvent.LivingJumpEvent}. We roll a 2% chance per jump
 * to detonate a smaller-than-creeper explosion at the rabbit's
 * position.
 *
 * <p>At ~30 jumps per minute and 2% per jump, expect roughly 1
 * explosion per minute of rabbit activity. Quietly chaotic.
 *
 * <p>Explosion details:
 *
 * <ul>
 *   <li>Power 1.5 — half of vanilla creeper. Rabbits are smaller
 *       than creepers, so a smaller blast feels right and avoids
 *       leveling player bases when wild rabbits roam nearby.</li>
 *   <li>Interaction MOB — terrain damage respects {@code mobGriefing}
 *       game rule.</li>
 *   <li>Null source entity (anonymous explosion).</li>
 * </ul>
 *
 * <p>The explosion kills the rabbit. Vanilla rabbit drops (raw rabbit,
 * rabbit hide, rabbit foot) trigger via {@link net.minecraftforge.event.entity.living.LivingDeathEvent}
 * after the explosion damage pass, so drops survive — similar to
 * Phase 3.5 pig explosions.
 *
 * <p>Killer Bunny (vanilla rare aggressive rabbit variant) is also
 * a {@link Rabbit} instance and is included.
 *
 * <p>Baby rabbits also jump and are included — they're still
 * {@link Rabbit} class instances.
 */
public class ExplodingRabbitJumpEvents {

    /** Probability of a rabbit exploding per jump. */
    private static final float EXPLODE_CHANCE = 0.02f;

    /** Explosion power. 1.5 = small (creeper is 3.0). */
    private static final float EXPLODE_POWER = 1.5f;

    @SubscribeEvent
    public void onRabbitJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Rabbit rabbit)) return;
        Level level = rabbit.level();
        if (level.isClientSide) return;

        if (rabbit.getRandom().nextFloat() >= EXPLODE_CHANCE) return;

        level.explode(
                null,
                rabbit.getX(),
                rabbit.getY() + rabbit.getBbHeight() / 2.0,
                rabbit.getZ(),
                EXPLODE_POWER,
                Level.ExplosionInteraction.MOB
        );
    }
}
