package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 3.7 — Sheep can inflict Wither when sheared (10%).
 *
 * <p>When a player right-clicks a non-sheared adult sheep with
 * shears, there is a 10% chance the act of shearing inflicts Wither I
 * on the player for 5 seconds. The shear itself still succeeds —
 * the wither is a bonus penalty.
 *
 * <p>Wither I deals ~1 damage every 2 seconds, so 5 seconds = ~2-3
 * HP penalty. Modest but noticeable.
 *
 * <p>How it works: subscribe to {@link PlayerInteractEvent.EntityInteract}
 * which fires when a player right-clicks any entity. Filter to:
 *
 * <ul>
 *   <li>Target is a {@link Sheep}.</li>
 *   <li>Sheep is not already sheared (otherwise nothing happens
 *       and we shouldn't punish the player).</li>
 *   <li>Sheep is an adult (babies have no wool in vanilla).</li>
 *   <li>Held item is {@code Items.SHEARS}.</li>
 * </ul>
 *
 * <p>If all checks pass, roll the 10% chance. We do NOT cancel the
 * event — the shear proceeds normally. The wither is just applied
 * to the player.
 *
 * <p>This event fires BEFORE vanilla's shear logic. If the player's
 * shears break mid-shear (durability == 1), the event still fired
 * and Wither still applies. That's intentional: the shears broke
 * because the act of shearing happened.
 */
public class SheepShearedWitherEvents {

    /** Probability of Wither being applied per successful shear. */
    private static final float WITHER_CHANCE = 0.10f;

    /** Wither duration in ticks (100 ticks = 5 seconds). */
    private static final int WITHER_DURATION_TICKS = 100;

    /** Wither amplifier (0 = level I, 1 = level II). */
    private static final int WITHER_AMPLIFIER = 0;

    @SubscribeEvent
    public void onSheepInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getTarget() instanceof Sheep sheep)) return;
        if (sheep.isSheared()) return;
        if (sheep.isBaby()) return;

        ItemStack item = event.getItemStack();
        if (!item.is(Items.SHEARS)) return;

        if (sheep.getRandom().nextFloat() >= WITHER_CHANCE) return;

        Player player = event.getEntity();
        player.addEffect(new MobEffectInstance(
                MobEffects.WITHER,
                WITHER_DURATION_TICKS,
                WITHER_AMPLIFIER,
                false,
                true
        ));
    }
}
