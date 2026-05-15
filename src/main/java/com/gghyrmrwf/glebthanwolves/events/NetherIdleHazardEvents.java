package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 3.0 — Nether idle hazard.
 *
 * While in the Nether, a player who stays on the ground without performing
 * a "real" jump (defined below) accumulates an idle timer. After
 * {@link #IGNITE_THRESHOLD_TICKS} ticks (5 seconds), the player is set on
 * fire and the fire is refreshed each subsequent tick until they jump.
 *
 * <p>A "real" jump is detected by the player remaining airborne for at
 * least {@link #REAL_JUMP_AIRBORNE_TICKS} ticks (~250 ms). This blocks the
 * trivial exploit of placing a solid block one above the head and
 * spamming the jump key: head-bump jumps only last 1–2 ticks before the
 * player is back on the ground, so they never reach the threshold.
 * Vanilla jumps in open air span ~12 airborne ticks.
 *
 * <p>The idle timer is reset (and any fire we set is cleared) the moment
 * a real jump is registered. Climbing ladders / vines, riding elytra, or
 * being knocked into the air also counts as "airborne" and resets the
 * timer if it lasts long enough — those are active state changes, not
 * idleness.
 *
 * <p>Creative / spectator players are exempt. The mechanic only fires
 * server-side and is dimension-gated to {@link Level#NETHER}.
 */
public class NetherIdleHazardEvents {

    /** 5 seconds at 20 TPS. */
    private static final int IGNITE_THRESHOLD_TICKS = 100;

    /**
     * Minimum airborne duration that qualifies as a "real" jump.
     * Head-bump jumps under a 2-block-tall ceiling typically last 1–2
     * ticks before vertical velocity is zeroed and the player lands
     * again. A free vanilla jump arcs for ~12 ticks. 5 ticks (~250 ms)
     * is safely above the exploit zone and well inside any genuine
     * jump.
     */
    private static final int REAL_JUMP_AIRBORNE_TICKS = 5;

    /**
     * Fire duration refreshed each tick while standing past the
     * threshold. Short enough that any natural fire source (lava
     * contact, fire block) is restored by vanilla logic on the next
     * tick after we stop refreshing.
     */
    private static final int FIRE_REFRESH_SECONDS = 1;

    /** Ticks the player has been standing on the ground continuously. */
    private static final Map<UUID, Integer> STANDING_TICKS = new ConcurrentHashMap<>();

    /** Consecutive ticks the player has been airborne (resets on landing). */
    private static final Map<UUID, Integer> AIRBORNE_TICKS = new ConcurrentHashMap<>();

    /**
     * Whether the most recent fire on the player was caused by this
     * mechanic. Used to decide whether {@link Player#clearFire()} is
     * safe on jump (we only clear fire we set ourselves, never fire
     * from lava / fire blocks / etc.).
     */
    private static final Map<UUID, Boolean> OUR_FIRE_ACTIVE = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        UUID uuid = player.getUUID();

        // Dimension gate — only Nether triggers the mechanic.
        if (player.level().dimension() != Level.NETHER) {
            resetState(uuid);
            return;
        }

        // Creative / spectator are exempt.
        if (player.isCreative() || player.isSpectator()) {
            resetState(uuid);
            return;
        }

        // Dead or sleeping players don't count.
        if (!player.isAlive() || player.isSleeping()) {
            resetState(uuid);
            return;
        }

        boolean onGround = player.onGround();

        if (!onGround) {
            // Airborne — count consecutive airborne ticks.
            int airborne = AIRBORNE_TICKS.getOrDefault(uuid, 0) + 1;
            AIRBORNE_TICKS.put(uuid, airborne);

            // Once we cross the "real jump" threshold, reset the idle
            // timer and clear our fire. We only do this once per
            // takeoff (when airborne == threshold), not every airborne
            // tick, to avoid spamming clearFire over lava sources.
            if (airborne == REAL_JUMP_AIRBORNE_TICKS) {
                STANDING_TICKS.put(uuid, 0);
                if (OUR_FIRE_ACTIVE.getOrDefault(uuid, false)) {
                    player.clearFire();
                    OUR_FIRE_ACTIVE.put(uuid, false);
                }
            }
        } else {
            // On the ground — accumulate idle ticks.
            AIRBORNE_TICKS.put(uuid, 0);
            int standing = STANDING_TICKS.getOrDefault(uuid, 0) + 1;
            STANDING_TICKS.put(uuid, standing);

            if (standing >= IGNITE_THRESHOLD_TICKS) {
                // Refresh fire each tick while still standing past the
                // threshold. Vanilla setSecondsOnFire only extends if
                // the new duration exceeds the remaining duration, so
                // this caps the burn at FIRE_REFRESH_SECONDS after the
                // mechanic stops refreshing (i.e. when the player
                // jumps or leaves the Nether).
                player.setSecondsOnFire(FIRE_REFRESH_SECONDS);
                OUR_FIRE_ACTIVE.put(uuid, true);
            }
        }
    }

    /**
     * Clean up per-player state when they disconnect. Prevents the
     * map from growing unbounded across player logins.
     */
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        STANDING_TICKS.remove(uuid);
        AIRBORNE_TICKS.remove(uuid);
        OUR_FIRE_ACTIVE.remove(uuid);
    }

    /**
     * Reset both timers and our-fire flag for a player. Called when
     * the player leaves the Nether or enters a non-eligible state
     * (creative, dead, sleeping). Leaves vanilla fire alone — only
     * clears local bookkeeping.
     */
    private static void resetState(UUID uuid) {
        STANDING_TICKS.put(uuid, 0);
        AIRBORNE_TICKS.put(uuid, 0);
        OUR_FIRE_ACTIVE.put(uuid, false);
    }
}
