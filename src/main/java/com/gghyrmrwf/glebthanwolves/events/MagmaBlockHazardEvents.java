package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 3.4 — Magma blocks burn harder.
 *
 * <p>Vanilla magma block deals 1 damage when an entity steps onto it
 * via {@code MagmaBlock#stepOn}. Because of the entity invulnerability
 * window (~20 ticks), in practice this is ~1 damage per second to a
 * player standing on the block.
 *
 * <p>This handler augments vanilla magma damage in two ways:
 *
 * <ol>
 *   <li><b>Slowness I</b> — refreshed every tick while standing on a
 *       magma block. Capped at 2 seconds remaining so it expires
 *       quickly after stepping off, but is always active while on the
 *       block.</li>
 *   <li><b>Continuous burning</b> — entity is set on fire for 2 seconds
 *       every tick. Fire damage in vanilla ticks once per second for
 *       1 damage, so this adds approximately +1 damage per second on
 *       top of the magma's step-on damage. Net: ~2 DPS while on the
 *       block (roughly ×2 of vanilla).</li>
 * </ol>
 *
 * <p>The exemptions match vanilla magma behavior plus a few extras:
 *
 * <ul>
 *   <li><b>Crouching</b> ({@link LivingEntity#isSteppingCarefully()}) —
 *       vanilla magma also skips damage when crouching. We respect that.</li>
 *   <li><b>Frost Walker enchantment on boots</b> — vanilla magma skips
 *       damage in this case. We respect that.</li>
 *   <li><b>Fire-immune entities</b> (blazes, striders, skeleton horses)
 *       — they don't take fire damage at all in vanilla, so the fire
 *       bonus is moot. We skip them for cleanliness.</li>
 *   <li><b>Airborne entities</b> — only entities physically on the
 *       block are affected. Jumping above the block is safe.</li>
 *   <li><b>Creative / spectator players</b> — exempt.</li>
 * </ul>
 *
 * <p>The mechanic is dimension-agnostic: magma blocks in the Overworld
 * (e.g. underwater ravines) are also affected. The user lumped this
 * with Nether-difficulty changes, but the mechanic itself is per-block
 * and applying it everywhere keeps the game consistent.
 */
public class MagmaBlockHazardEvents {

    /** Slowness duration refreshed each tick (40 ticks = 2 s). */
    private static final int SLOWNESS_DURATION_TICKS = 40;

    /** Slowness amplifier (0 = level I, 1 = level II). */
    private static final int SLOWNESS_AMPLIFIER = 0;

    /**
     * Fire duration refreshed each tick. 2 seconds is long enough that
     * vanilla fire damage (1 per ~20 ticks) tick once per second
     * effectively, contributing ~1 DPS on top of vanilla magma damage.
     */
    private static final int FIRE_REFRESH_SECONDS = 2;

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (entity instanceof Player p && (p.isCreative() || p.isSpectator())) return;
        if (!entity.onGround()) return;
        if (entity.isSteppingCarefully()) return;
        if (entity.fireImmune()) return;

        // Frost Walker boots exemption (matches vanilla magma behavior).
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FROST_WALKER, boots) > 0) return;

        // Check the block the entity is standing on.
        BlockState stateOn = entity.level().getBlockState(entity.getOnPos());
        if (!stateOn.is(Blocks.MAGMA_BLOCK)) return;

        // Slowness I, refreshed every tick. Hidden particles + no icon
        // would be ideal but the visible-icon API is server-side
        // friendly; leave default visible so player knows why they're
        // slow.
        entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                SLOWNESS_DURATION_TICKS,
                SLOWNESS_AMPLIFIER,
                false,
                false
        ));

        // Bonus burning. setSecondsOnFire only extends if the new
        // duration exceeds remaining duration, so this caps the burn
        // at ~2 seconds after the entity steps off the block.
        entity.setSecondsOnFire(FIRE_REFRESH_SECONDS);
    }
}
