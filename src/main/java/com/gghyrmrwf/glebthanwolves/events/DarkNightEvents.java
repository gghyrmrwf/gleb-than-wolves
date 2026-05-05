package com.gghyrmrwf.glebthanwolves.events;

import com.gghyrmrwf.glebthanwolves.GlebThanWolves;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DarkNightEvents {

    private static final int DARK_NIGHT_INTERVAL_DAYS = 5;
    private static final int DARK_NIGHT_START_TICK = 13000;
    private static final int DARK_NIGHT_END_TICK = 23000;
    private static final int DARKNESS_KILL_TICKS = 6000;
    private static final int DARKNESS_ACTIONBAR_INTERVAL_TICKS = 100;
    private static final int DARKNESS_SAFE_BLOCK_LIGHT = 1;

    private static final String DARKNESS_TIMER_TAG = "GTWDarkNightDarknessTicks";

    private static final ResourceKey<DamageType> THE_DARKNESS_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(GlebThanWolves.MODID, "the_darkness")
    );

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            clearDarknessTimer(player);
            return;
        }
        if (!(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            clearDarknessTimer(player);
            return;
        }
        if (!isDarkNight(level)) {
            clearDarknessTimer(player);
            return;
        }
        if (!isStandingInDarkness(level, player.blockPosition())) {
            clearDarknessTimer(player);
            return;
        }

        long darknessTicks = player.getPersistentData().getLong(DARKNESS_TIMER_TAG) + 1L;
        player.getPersistentData().putLong(DARKNESS_TIMER_TAG, darknessTicks);

        if (darknessTicks % DARKNESS_ACTIONBAR_INTERVAL_TICKS == 0L) {
            long secondsLeft = Math.max(0L, (DARKNESS_KILL_TICKS - darknessTicks) / 20L);
            player.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.translatable("message.glebthanwolves.darkness_warning", secondsLeft)
            ));
        }

        if (darknessTicks >= DARKNESS_KILL_TICKS) {
            killByDarkness(level, player);
        }
    }

    @SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        if (player.level() instanceof ServerLevel level && isDarkNight(level)) {
            player.displayClientMessage(Component.translatable("message.glebthanwolves.dark_night_no_sleep"), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }

    public static boolean isDarkNight(Level level) {
        if (level.dimension() != Level.OVERWORLD) {
            return false;
        }
        long dayTime = level.getDayTime();
        long day = Math.floorDiv(dayTime, 24000L) + 1L;
        long timeOfDay = Math.floorMod(dayTime, 24000L);
        return day % DARK_NIGHT_INTERVAL_DAYS == 0
                && timeOfDay >= DARK_NIGHT_START_TICK
                && timeOfDay <= DARK_NIGHT_END_TICK;
    }

    private static boolean isStandingInDarkness(ServerLevel level, BlockPos pos) {
        return level.isLoaded(pos)
                && level.getBrightness(LightLayer.BLOCK, pos) <= DARKNESS_SAFE_BLOCK_LIGHT
                && !level.canSeeSky(pos);
    }

    private static void clearDarknessTimer(ServerPlayer player) {
        player.getPersistentData().remove(DARKNESS_TIMER_TAG);
    }

    private static void killByDarkness(ServerLevel level, ServerPlayer player) {
        DamageSource source = new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(THE_DARKNESS_DAMAGE)
        );
        player.hurt(source, Float.MAX_VALUE);
        clearDarknessTimer(player);
    }
}
