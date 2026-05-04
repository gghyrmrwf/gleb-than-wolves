package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Phase 1.3 — world-level hardcore tweaks.
 *
 * — Daytime advances at 1.5× speed (every other server tick we add +1 to the day
 *   time). Night progresses at vanilla speed. Net effect: night arrives ~1.5×
 *   faster than vanilla.
 * — At night we trigger one extra hostile-mob spawn attempt every 60 ticks per
 *   online player with 50% probability. The mob is placed at a random surface
 *   block 24–48 blocks from the player. Standard spawn rules (light, terrain,
 *   collision) still apply, so failed attempts produce nothing.
 *
 * Both behaviours respect the `doDaylightCycle` and `doMobSpawning` game rules.
 */
public class WorldEvents {

    private static final List<EntityType<? extends Mob>> NIGHT_HOSTILES = List.of(
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.CREEPER
    );

    private static final int EXTRA_SPAWN_INTERVAL_TICKS = 60;
    private static final float EXTRA_SPAWN_CHANCE = 0.5F;

    private static final int MIN_SPAWN_DISTANCE = 24;
    private static final int MAX_SPAWN_DISTANCE = 48;

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.level instanceof ServerLevel level)) {
            return;
        }
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        long timeOfDay = level.getDayTime() % 24000L;
        boolean isDay = timeOfDay < 12000L;

        // 1. Daytime ×1.5 — add an extra +1 to dayTime every other tick.
        if (isDay
                && level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)
                && (level.getGameTime() & 1L) == 0L) {
            level.setDayTime(level.getDayTime() + 1L);
        }

        // 2. Extra hostile spawns at night.
        if (!isDay
                && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                && level.getGameTime() % EXTRA_SPAWN_INTERVAL_TICKS == 0L) {
            for (ServerPlayer player : level.players()) {
                if (level.random.nextFloat() < EXTRA_SPAWN_CHANCE) {
                    trySpawnExtraMonster(level, player);
                }
            }
        }
    }

    private static void trySpawnExtraMonster(ServerLevel level, ServerPlayer player) {
        RandomSource rand = level.random;

        double angle = rand.nextDouble() * Math.PI * 2D;
        int distance = MIN_SPAWN_DISTANCE + rand.nextInt(MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE + 1);
        int x = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * distance);
        int z = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * distance);

        BlockPos columnPos = new BlockPos(x, 0, z);
        if (!level.hasChunkAt(columnPos)) {
            return;
        }

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos spawnPos = new BlockPos(x, y, z);

        if (!level.getBlockState(spawnPos).isAir()
                || !level.getBlockState(spawnPos.above()).isAir()) {
            return;
        }
        BlockPos belowPos = spawnPos.below();
        if (!level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP)) {
            return;
        }
        if (level.getBrightness(LightLayer.BLOCK, spawnPos) > 7) {
            return;
        }

        EntityType<? extends Mob> type = NIGHT_HOSTILES.get(rand.nextInt(NIGHT_HOSTILES.size()));
        Mob mob = type.create(level);
        if (mob == null) {
            return;
        }
        mob.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                rand.nextFloat() * 360F, 0F);

        if (!mob.checkSpawnRules(level, MobSpawnType.NATURAL) || !mob.checkSpawnObstruction(level)) {
            mob.discard();
            return;
        }

        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.NATURAL, null, null);
        level.addFreshEntity(mob);
    }
}
