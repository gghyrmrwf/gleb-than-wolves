package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Phase 1.3 — hardcore tweaks for the player and hostile mobs.
 *
 * — No sprinting in survival/adventure: every tick we cancel `isSprinting`.
 * — Raw meat / raw fish applies a short Hunger effect on top of vanilla effects.
 * — Every {@link Enemy} gets +15% max health and +10% melee attack damage when it
 *   joins a level, via permanent attribute modifiers keyed by stable UUIDs.
 *
 * Vegetables, fruit, bread and other non-meat foods are intentionally NOT covered.
 */
public class HardcoreEvents {

    private static final UUID HP_BOOST_UUID  = UUID.fromString("4e3cce71-5872-4f6d-bb29-f31ed6c9fa01");
    private static final UUID DMG_BOOST_UUID = UUID.fromString("4e3cce71-5872-4f6d-bb29-f31ed6c9fa02");

    private static final double HP_MULTIPLIER  = 0.15D;
    private static final double DMG_MULTIPLIER = 0.10D;

    private static final int RAW_FOOD_HUNGER_TICKS = 240; // 12 seconds

    private static final Set<Item> RAW_MEATS_AND_FISH = Set.of(
            Items.BEEF,
            Items.CHICKEN,
            Items.PORKCHOP,
            Items.MUTTON,
            Items.RABBIT,
            Items.COD,
            Items.SALMON,
            Items.TROPICAL_FISH
    );

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    @SubscribeEvent
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }
        Item item = event.getItem().getItem();
        if (RAW_MEATS_AND_FISH.contains(item)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.HUNGER,
                    RAW_FOOD_HUNGER_TICKS,
                    0,
                    false,
                    true));
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity living) || !(living instanceof Enemy)) {
            return;
        }
        applyAttributeBoost(living, Attributes.MAX_HEALTH, HP_BOOST_UUID, "GTW HP boost", HP_MULTIPLIER);
        // Re-fill HP to new max after boosting (otherwise the mob spawns pre-damaged).
        living.setHealth(living.getMaxHealth());

        applyAttributeBoost(living, Attributes.ATTACK_DAMAGE, DMG_BOOST_UUID, "GTW DMG boost", DMG_MULTIPLIER);
    }

    private static void applyAttributeBoost(LivingEntity entity,
                                            net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                            UUID uuid,
                                            String name,
                                            double multiplier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (instance.getModifier(uuid) != null) {
            return;
        }
        instance.addPermanentModifier(new AttributeModifier(
                uuid, name, multiplier, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
}
