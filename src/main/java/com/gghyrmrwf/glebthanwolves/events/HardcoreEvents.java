package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Phase 1.3 + 1.4 — hardcore tweaks for the player and hostile mobs.
 *
 * Player:
 *  — Sprint is gated by accelerated hunger drain. While {@code foodLevel > 6}
 *    we add {@link #EXTRA_EXHAUSTION_PER_TICK} exhaustion every tick, draining
 *    food roughly 6× faster than vanilla. Once foodLevel reaches 6 the vanilla
 *    client-side check (`foodLevel > 6`) stops the player from sprinting, and
 *    we leave drain at vanilla rate from there on. Regeneration keeps using
 *    vanilla rules.
 *  — Max health is reduced to {@link #PLAYER_MAX_HEALTH} HP (5 hearts) via a
 *    permanent attribute modifier on {@link Attributes#MAX_HEALTH}, applied on
 *    every level-join (login, respawn, dimension change).
 *  — Eating raw beef / chicken / porkchop / mutton / rabbit / cod / salmon /
 *    tropical_fish applies Hunger I for {@link #RAW_FOOD_HUNGER_TICKS} ticks
 *    AND deals {@link #RAW_FOOD_DAMAGE} HP of generic (armor-bypassing) damage
 *    per swallow.
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

    // Accelerated hunger drain to gate sprint (Phase 1.4).
    // Vanilla client refuses to start sprinting while foodLevel <= 6, so we just
    // make food drain quickly until that threshold and let vanilla take over.
    private static final int FAST_DRAIN_THRESHOLD = 6;
    // 0.05 exhaustion per tick = 1.0/sec → 1 food unit per ~4 sec, ~6× vanilla casual rate.
    private static final float EXTRA_EXHAUSTION_PER_TICK = 0.05F;

    // Raw food hunger debuff (Phase 1.3) and direct HP damage (Phase 1.5).
    private static final int RAW_FOOD_HUNGER_TICKS = 240;
    private static final float RAW_FOOD_DAMAGE = 1.0F; // half a heart

    // Sleep tweaks (Phase 1.4).
    private static final float SLEEP_FAIL_CHANCE = 0.20F;
    private static final int PHANTOM_SPAWN_THRESHOLD = 72001; // PhantomSpawner triggers at > 72000.

    // Phase 1.6 environmental / combat tweaks.
    private static final float FALL_DAMAGE_MULTIPLIER = 1.5F;
    private static final int   RAIN_DAMAGE_INTERVAL_TICKS = 200;  // every 10 sec
    private static final float RAIN_DAMAGE = 1.0F;                // 0.5 hearts
    private static final int   COLD_DAMAGE_INTERVAL_TICKS = 600;  // every 30 sec
    private static final float COLD_DAMAGE = 1.0F;                // 0.5 hearts
    private static final int   COLD_BLOCK_LIGHT_THRESHOLD = 7;
    private static final float ZOMBIE_GRAB_CHANCE = 0.30F;
    private static final int   ZOMBIE_GRAB_DURATION_TICKS = 60;   // 3 sec
    private static final int   ZOMBIE_GRAB_AMPLIFIER = 1;         // Slowness II
    private static final float SKELETON_ARROW_MULTIPLIER = 1.5F;

    // Iron-golem hostility (Phase 1.7).
    private static final double GOLEM_AGGRO_RANGE = 32.0D;
    private static final int    GOLEM_RETARGET_INTERVAL_TICKS = 20;

    // Wolf hostility (Phase 1.8).
    private static final double WOLF_AGGRO_RANGE = 16.0D;
    private static final int    WOLF_RETARGET_INTERVAL_TICKS = 20;

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

        // Accelerated hunger drain — drain ~6× faster while above the sprint threshold.
        // Once foodLevel <= 6, vanilla blocks sprint and we stop adding extra exhaustion.
        if (player.getFoodData().getFoodLevel() > FAST_DRAIN_THRESHOLD) {
            player.causeFoodExhaustion(EXTRA_EXHAUSTION_PER_TICK);
        }

        Level level = player.level();
        BlockPos pos = player.blockPosition();

        // Rain damage — half a heart every 10 sec while exposed to actual precipitation.
        if (player.tickCount % RAIN_DAMAGE_INTERVAL_TICKS == 0
                && level.isRainingAt(pos.above())) {
            player.hurt(player.damageSources().generic(), RAIN_DAMAGE);
        }

        // Cold damage at night — half a heart every 30 sec when no nearby block-light heat source.
        if (player.tickCount % COLD_DAMAGE_INTERVAL_TICKS == 0
                && isNightTime(level)
                && level.getBrightness(LightLayer.BLOCK, pos) <= COLD_BLOCK_LIGHT_THRESHOLD) {
            player.hurt(player.damageSources().generic(), COLD_DAMAGE);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        LivingEntity victim = event.getEntity();

        // Skeleton arrows hit harder, even against armor (already armor-piercing in vanilla
        // for projectile, but we just bump base damage).
        if (source.getDirectEntity() instanceof AbstractArrow
                && source.getEntity() instanceof AbstractSkeleton) {
            event.setAmount(event.getAmount() * SKELETON_ARROW_MULTIPLIER);
        }

        // Player-only effects below.
        if (!(victim instanceof Player player)) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        // Heavier fall damage.
        if (source.is(DamageTypes.FALL)) {
            event.setAmount(event.getAmount() * FALL_DAMAGE_MULTIPLIER);
        }

        // Zombie grab — chance to slow the player on a zombie hit.
        if (source.getEntity() instanceof Zombie
                && player.level().random.nextFloat() < ZOMBIE_GRAB_CHANCE) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    ZOMBIE_GRAB_DURATION_TICKS,
                    ZOMBIE_GRAB_AMPLIFIER,
                    false,
                    true));
        }
    }

    private static boolean isNightTime(Level level) {
        long t = level.getDayTime() % 24000L;
        return t >= 13000L && t <= 23000L;
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
            player.hurt(player.damageSources().generic(), RAW_FOOD_DAMAGE);
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

        // Iron golems lose any "player-built" allegiance so canAttack(player) is true
        // (their actual targeting is forced from the LivingTickEvent below).
        if (living instanceof IronGolem golem) {
            golem.setPlayerCreated(false);
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        // Iron golems hunt the nearest player on sight (Phase 1.7).
        if (entity instanceof IronGolem golem) {
            if (golem.tickCount % GOLEM_RETARGET_INTERVAL_TICKS != 0) {
                return;
            }
            LivingEntity current = golem.getTarget();
            if (current instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator()) {
                return;
            }
            Player nearest = golem.level().getNearestPlayer(golem, GOLEM_AGGRO_RANGE);
            if (nearest != null && !nearest.isCreative() && !nearest.isSpectator() && nearest.isAlive()) {
                golem.setTarget(nearest);
            }
        }

        // Wild wolves hunt the nearest player on sight (Phase 1.8).
        if (entity instanceof Wolf wolf && !wolf.isTame()) {
            if (wolf.tickCount % WOLF_RETARGET_INTERVAL_TICKS != 0) {
                return;
            }
            LivingEntity current = wolf.getTarget();
            if (current instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator()) {
                return;
            }
            Player nearest = wolf.level().getNearestPlayer(wolf, WOLF_AGGRO_RANGE);
            if (nearest != null && !nearest.isCreative() && !nearest.isSpectator() && nearest.isAlive()) {
                wolf.setTarget(nearest);
                wolf.setIsInterested(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        // Block trade GUI for villagers and wandering traders (Phase 1.7).
        if (event.getTarget() instanceof AbstractVillager) {
            event.setCanceled(true);
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
