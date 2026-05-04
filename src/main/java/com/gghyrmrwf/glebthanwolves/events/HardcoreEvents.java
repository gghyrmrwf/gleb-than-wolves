package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Phase 1.3 + 1.4 — hardcore tweaks for the player and hostile mobs.
 *
 * Player:
 *  — Food level is hard-capped at {@link #FOOD_CAP} every tick. Vanilla's client-side
 *    sprint check requires {@code foodLevel > 6}, so the player can never sprint.
 *    Saturation is also wiped to 0 to keep the cap stable.
 *  — Because the food cap blocks vanilla regen ({@code foodLevel >= 18} required),
 *    we run our own slow regeneration: 1 HP every 80 ticks while hurt and not on
 *    Peaceful difficulty.
 *  — Max health is reduced to {@link #PLAYER_MAX_HEALTH} HP (5 hearts) via a
 *    permanent attribute modifier on {@link Attributes#MAX_HEALTH}, applied on
 *    every level-join (login, respawn, dimension change).
 *  — Eating raw beef / chicken / porkchop / mutton / rabbit / cod / salmon /
 *    tropical_fish applies Hunger I for {@link #RAW_FOOD_HUNGER_TICKS} ticks.
 *  — Eating golden apple / enchanted golden apple immediately strips the powerful
 *    effects (regeneration, absorption, resistance, fire resistance). Hunger /
 *    saturation gain stays.
 *  — On {@link PlayerSleepInBedEvent}, with probability {@link #SLEEP_FAIL_CHANCE}
 *    the sleep is rejected with {@link Player.BedSleepingProblem#OTHER_PROBLEM}
 *    ("You can't sleep right now").
 *  — On {@link PlayerWakeUpEvent}, after vanilla resets {@code TIME_SINCE_REST}
 *    to zero, we schedule a server task that pushes it back above the phantom
 *    spawn threshold, so phantoms keep spawning even after sleep.
 *
 * Mobs:
 *  — Every {@link Enemy} that joins a level gets +15% max health and +10% melee
 *    damage via permanent attribute modifiers keyed by stable UUIDs. The mob is
 *    healed to its new max so it doesn't spawn pre-damaged.
 *  — Every zombie additionally gets +20% movement speed.
 */
public class HardcoreEvents {

    // Mob buffs (Phase 1.3).
    private static final UUID HP_BOOST_UUID  = UUID.fromString("4e3cce71-5872-4f6d-bb29-f31ed6c9fa01");
    private static final UUID DMG_BOOST_UUID = UUID.fromString("4e3cce71-5872-4f6d-bb29-f31ed6c9fa02");
    private static final double HP_MULTIPLIER  = 0.15D;
    private static final double DMG_MULTIPLIER = 0.10D;

    // Player HP cap (Phase 1.4).
    private static final UUID PLAYER_HP_CAP_UUID = UUID.fromString("4e3cce71-5872-4f6d-bb29-f31ed6c9fa03");
    private static final double PLAYER_MAX_HEALTH = 10.0D;
    private static final double PLAYER_HP_CAP_DELTA = PLAYER_MAX_HEALTH - 20.0D; // -10

    // Zombie speed (Phase 1.4).
    private static final UUID ZOMBIE_SPEED_UUID = UUID.fromString("4e3cce71-5872-4f6d-bb29-f31ed6c9fa04");
    private static final double ZOMBIE_SPEED_MULTIPLIER = 0.20D;

    // Sprint blocker via permanent hunger (Phase 1.4).
    private static final int FOOD_CAP = 6;

    // Manual regen interval (Phase 1.4).
    private static final int REGEN_INTERVAL_TICKS = 80;

    // Raw food hunger debuff (Phase 1.3).
    private static final int RAW_FOOD_HUNGER_TICKS = 240;

    // Sleep tweaks (Phase 1.4).
    private static final float SLEEP_FAIL_CHANCE = 0.20F;
    private static final int PHANTOM_SPAWN_THRESHOLD = 72001; // PhantomSpawner triggers at > 72000.

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

        // Permanent-hunger sprint blocker. Client refuses to sprint when foodLevel <= 6.
        FoodData food = player.getFoodData();
        if (food.getFoodLevel() != FOOD_CAP) {
            food.setFoodLevel(FOOD_CAP);
        }
        if (food.getSaturationLevel() > 0F) {
            food.setSaturation(0F);
        }

        // Manual regen — vanilla regen needs foodLevel >= 18, which the cap forbids.
        if (player.level().getDifficulty() != Difficulty.PEACEFUL
                && player.tickCount % REGEN_INTERVAL_TICKS == 0
                && player.isHurt()) {
            player.heal(1.0F);
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

        // Strip the powerful effects from golden apples right after vanilla applies them.
        if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
            player.removeEffect(MobEffects.REGENERATION);
            player.removeEffect(MobEffects.ABSORPTION);
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            player.removeEffect(MobEffects.FIRE_RESISTANCE);
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }

        if (living instanceof Player player) {
            applyPlayerHpCap(player);
            return;
        }

        if (!(living instanceof Enemy)) {
            return;
        }

        applyAttributeBoost(living, Attributes.MAX_HEALTH, HP_BOOST_UUID, "GTW HP boost", HP_MULTIPLIER);
        // Re-fill HP to new max after boosting (otherwise the mob spawns pre-damaged).
        living.setHealth(living.getMaxHealth());

        applyAttributeBoost(living, Attributes.ATTACK_DAMAGE, DMG_BOOST_UUID, "GTW DMG boost", DMG_MULTIPLIER);

        if (living instanceof Zombie) {
            applyAttributeBoost(living, Attributes.MOVEMENT_SPEED, ZOMBIE_SPEED_UUID,
                    "GTW zombie speed", ZOMBIE_SPEED_MULTIPLIER);
        }
    }

    @SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        if (player.level().random.nextFloat() < SLEEP_FAIL_CHANCE) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        // Run after vanilla's stopSleepInBed bookkeeping so our value sticks.
        server.execute(() -> {
            Stat<ResourceLocation> stat = Stats.CUSTOM.get(Stats.TIME_SINCE_REST);
            player.getStats().setValue(player, stat, PHANTOM_SPAWN_THRESHOLD);
        });
    }

    private static void applyPlayerHpCap(Player player) {
        AttributeInstance hp = player.getAttribute(Attributes.MAX_HEALTH);
        if (hp == null) {
            return;
        }
        if (hp.getModifier(PLAYER_HP_CAP_UUID) == null) {
            hp.addPermanentModifier(new AttributeModifier(
                    PLAYER_HP_CAP_UUID,
                    "GTW player HP cap",
                    PLAYER_HP_CAP_DELTA,
                    AttributeModifier.Operation.ADDITION));
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyAttributeBoost(LivingEntity entity,
                                            Attribute attribute,
                                            UUID uuid,
                                            String name,
                                            double value) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (instance.getModifier(uuid) != null) {
            return;
        }
        instance.addPermanentModifier(new AttributeModifier(
                uuid, name, value, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
}
