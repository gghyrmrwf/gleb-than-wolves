package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

    // Boats and oxygen (Phase 1.9).
    // Vanilla boat: accel 0.04 / tick, friction 0.9 ⇒ steady-state v_max ≈ 0.4.
    // With per-tick scale x: v_max = 0.04*x / (1 - 0.9*x). x=0.91 gives v_max ≈ 0.20,
    // i.e. half of vanilla's max speed.
    private static final double BOAT_VELOCITY_SCALE = 0.91D;
    private static final int    EXTRA_AIR_DRAIN_PER_TICK = 1;

    // Movement complications (Phase 1.10).
    // Sneak speed: -50% via MULTIPLY_TOTAL on MOVEMENT_SPEED while crouching.
    private static final UUID   SNEAK_SPEED_UUID = UUID.fromString("4e3cce71-5872-4f6d-bb29-f31ed6c9fa10");
    private static final double SNEAK_SPEED_DELTA = -0.5D;
    // Snow / powder snow slow: -25% via MULTIPLY_TOTAL on MOVEMENT_SPEED.
    private static final UUID   SNOW_SPEED_UUID = UUID.fromString("4e3cce71-5872-4f6d-bb29-f31ed6c9fa11");
    private static final double SNOW_SPEED_DELTA = -0.25D;
    // Encumbrance: ≥ this many filled inventory slots → Slowness I + Mining Fatigue I.
    private static final int    ENCUMBRANCE_FILL_THRESHOLD = 27;
    private static final int    ENCUMBRANCE_REFRESH_INTERVAL_TICKS = 40;
    private static final int    ENCUMBRANCE_EFFECT_DURATION_TICKS = 60;
    // Swim slow: scale horizontal velocity in water each tick. 0.85 → ~ -30% sustained.
    private static final double SWIM_VELOCITY_SCALE = 0.85D;
    // Climb (ladders/vines/scaffolding): scale vertical velocity by 0.7 each tick.
    private static final double CLIMB_VELOCITY_SCALE = 0.70D;
    // Ice slipperiness: small per-tick momentum boost while sliding on ice w/o input.
    private static final double ICE_SLIDE_SCALE = 1.02D;
    private static final double ICE_SLIDE_MIN_SPEED = 0.05D;
    private static final double ICE_SLIDE_MAX_SPEED = 0.50D;

    // Predators (Phase 1.11).
    // Endermen aggro players in 16-block radius without requiring eye contact.
    private static final double ENDERMAN_AGGRO_RANGE = 16.0D;
    private static final int    ENDERMAN_RETARGET_INTERVAL_TICKS = 20;
    // Silent creepers: chance a creeper spawns muted (no fuse hiss / step / hurt sounds).
    private static final float  SILENT_CREEPER_CHANCE = 0.15F;
    // Husk replaces zombie 10% of the time (any biome).
    private static final float  HUSK_REPLACE_CHANCE = 0.10F;
    // Ghast extra fireball: every N ticks, with chance, fire an additional fireball.
    private static final int    GHAST_EXTRA_FIRE_INTERVAL_TICKS = 60;
    private static final float  GHAST_EXTRA_FIRE_CHANCE = 0.60F;
    // Headless creeper: chance to spawn with bumped explosion radius (3 -> 5).
    private static final float  HEADLESS_CREEPER_CHANCE = 0.20F;
    private static final int    HEADLESS_CREEPER_RADIUS = 5;
    private static final java.lang.reflect.Field CREEPER_EXPLOSION_RADIUS;
    static {
        java.lang.reflect.Field f;
        try {
            f = Creeper.class.getDeclaredField("explosionRadius");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            f = null;
        }
        CREEPER_EXPLOSION_RADIUS = f;
    }

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

        // Boats are roughly 2× slower (Phase 1.9).
        if (player.getVehicle() instanceof Boat boat) {
            Vec3 dm = boat.getDeltaMovement();
            boat.setDeltaMovement(dm.x * BOAT_VELOCITY_SCALE, dm.y, dm.z * BOAT_VELOCITY_SCALE);
        }

        // Oxygen drains 2× faster underwater (Phase 1.9).
        if (player.isEyeInFluid(FluidTags.WATER)
                && !player.canBreatheUnderwater()
                && !MobEffectUtil.hasWaterBreathing(player)) {
            int air = player.getAirSupply();
            if (air > -20) {
                player.setAirSupply(air - EXTRA_AIR_DRAIN_PER_TICK);
            }
        }

        // Phase 1.10 — movement complications.

        // Sneak ×0.5 via dynamic MOVEMENT_SPEED modifier.
        toggleSpeedModifier(player, SNEAK_SPEED_UUID, "GTW sneak slow",
                SNEAK_SPEED_DELTA, player.isCrouching());

        // Snow / powder snow slow via dynamic MOVEMENT_SPEED modifier.
        BlockState atFeet  = level.getBlockState(pos);
        BlockState belowFeet = level.getBlockState(pos.below());
        boolean inSnow = atFeet.is(Blocks.SNOW)
                || atFeet.is(Blocks.POWDER_SNOW)
                || belowFeet.is(Blocks.SNOW)
                || belowFeet.is(Blocks.POWDER_SNOW);
        toggleSpeedModifier(player, SNOW_SPEED_UUID, "GTW snow slow",
                SNOW_SPEED_DELTA, inSnow);

        // Ice slipperiness — give a tiny per-tick momentum boost while sliding.
        if (!player.isCrouching() && !player.isInWater()) {
            BlockState below = level.getBlockState(pos.below());
            boolean onIce = below.is(Blocks.ICE)
                    || below.is(Blocks.PACKED_ICE)
                    || below.is(Blocks.BLUE_ICE)
                    || below.is(Blocks.FROSTED_ICE);
            if (onIce) {
                Vec3 dm = player.getDeltaMovement();
                double mag = Math.sqrt(dm.x * dm.x + dm.z * dm.z);
                if (mag > ICE_SLIDE_MIN_SPEED && mag < ICE_SLIDE_MAX_SPEED) {
                    player.setDeltaMovement(dm.x * ICE_SLIDE_SCALE, dm.y, dm.z * ICE_SLIDE_SCALE);
                }
            }
        }

        // Swim slow — scale horizontal velocity in water each tick.
        if (player.isInWater() && player.getVehicle() == null) {
            Vec3 dm = player.getDeltaMovement();
            player.setDeltaMovement(dm.x * SWIM_VELOCITY_SCALE, dm.y, dm.z * SWIM_VELOCITY_SCALE);
        }

        // Climbing slower — scale vertical velocity on ladders/vines/scaffolding.
        if (player.onClimbable() && !player.onGround()) {
            Vec3 dm = player.getDeltaMovement();
            if (Math.abs(dm.y) > 0.01D) {
                player.setDeltaMovement(dm.x, dm.y * CLIMB_VELOCITY_SCALE, dm.z);
            }
        }

        // Encumbrance — heavy inventory imposes Slowness I + Mining Fatigue I.
        if (player.tickCount % ENCUMBRANCE_REFRESH_INTERVAL_TICKS == 0) {
            int filled = 0;
            for (ItemStack s : player.getInventory().items) {
                if (!s.isEmpty()) {
                    filled++;
                }
            }
            if (filled >= ENCUMBRANCE_FILL_THRESHOLD) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        ENCUMBRANCE_EFFECT_DURATION_TICKS, 0,
                        false, false, true));
                player.addEffect(new MobEffectInstance(
                        MobEffects.DIG_SLOWDOWN,
                        ENCUMBRANCE_EFFECT_DURATION_TICKS, 0,
                        false, false, true));
            }
        }
    }

    private static void toggleSpeedModifier(Player player, UUID uuid, String name, double delta, boolean active) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        AttributeModifier existing = speed.getModifier(uuid);
        if (active) {
            if (existing == null) {
                speed.addTransientModifier(new AttributeModifier(
                        uuid, name, delta, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        } else if (existing != null) {
            speed.removeModifier(uuid);
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

        // Phase 1.11 — predators.

        // Husk replaces vanilla zombie 10% of the time (any biome).
        // Only convert plain Zombie (not Husk/Drowned/ZombieVillager subclasses).
        if (living.getClass() == Zombie.class) {
            Zombie z = (Zombie) living;
            if (event.getLevel().getRandom().nextFloat() < HUSK_REPLACE_CHANCE) {
                event.setCanceled(true);
                Husk husk = EntityType.HUSK.create(z.level());
                if (husk != null) {
                    husk.moveTo(z.getX(), z.getY(), z.getZ(), z.getYRot(), z.getXRot());
                    z.level().addFreshEntity(husk);
                }
                return;
            }
        }

        // Silent creeper: 15% chance to spawn fully muted (no fuse hiss either).
        // Headless creeper: 20% chance to spawn with bumped explosion radius.
        if (living instanceof Creeper creeper) {
            if (creeper.level().getRandom().nextFloat() < SILENT_CREEPER_CHANCE) {
                creeper.setSilent(true);
            }
            if (CREEPER_EXPLOSION_RADIUS != null
                    && creeper.level().getRandom().nextFloat() < HEADLESS_CREEPER_CHANCE) {
                try {
                    CREEPER_EXPLOSION_RADIUS.setInt(creeper, HEADLESS_CREEPER_RADIUS);
                } catch (IllegalAccessException ignored) {
                }
            }
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

        // Endermen target the nearest player without requiring eye-contact (Phase 1.11).
        if (entity instanceof EnderMan enderman) {
            if (enderman.tickCount % ENDERMAN_RETARGET_INTERVAL_TICKS != 0) {
                return;
            }
            LivingEntity current = enderman.getTarget();
            if (current instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator()) {
                return;
            }
            Player nearest = enderman.level().getNearestPlayer(enderman, ENDERMAN_AGGRO_RANGE);
            if (nearest != null && !nearest.isCreative() && !nearest.isSpectator() && nearest.isAlive()) {
                enderman.setTarget(nearest);
            }
        }

        // Ghasts shoot extra fireballs (Phase 1.11).
        if (entity instanceof Ghast ghast) {
            if (ghast.tickCount % GHAST_EXTRA_FIRE_INTERVAL_TICKS != 0) {
                return;
            }
            LivingEntity target = ghast.getTarget();
            if (target == null || !target.isAlive()) {
                return;
            }
            if (ghast.level().getRandom().nextFloat() >= GHAST_EXTRA_FIRE_CHANCE) {
                return;
            }
            Vec3 view = ghast.getViewVector(1.0F);
            double sx = ghast.getX() + view.x * 4.0D;
            double sy = ghast.getY(0.5D) + 0.5D;
            double sz = ghast.getZ() + view.z * 4.0D;
            double dx = target.getX() - sx;
            double dy = target.getY(0.5D) - sy;
            double dz = target.getZ() - sz;
            LargeFireball fireball = new LargeFireball(ghast.level(), ghast, dx, dy, dz, ghast.getExplosionPower());
            fireball.setPos(sx, sy, sz);
            ghast.level().addFreshEntity(fireball);
            if (!ghast.isSilent()) {
                ghast.level().levelEvent(null, 1016, ghast.blockPosition(), 0);
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
