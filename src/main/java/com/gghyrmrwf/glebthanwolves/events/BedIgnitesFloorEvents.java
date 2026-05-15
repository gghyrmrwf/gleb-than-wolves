package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 3.11 — Bed in Nether: 50% chance to ignite floor instead of exploding.
 *
 * <p>Vanilla bed behavior in Nether: right-clicking a bed in any
 * dimension where {@code bedWorks() == false} (Nether, End) triggers
 * a powerful explosion (power 5.0, ignores block protection, ~25 HP
 * damage at point-blank to the player who clicked).
 *
 * <p>This handler intercepts right-click-bed events in the Nether
 * specifically and, with 50% probability, replaces the vanilla
 * explosion with a "floor ignition" effect: the bed blocks are
 * removed and fire blocks are spawned on top of solid blocks in a
 * 3-block radius around the bed. The player is not directly damaged
 * (no explosion) but stands in a burning area.
 *
 * <p>On netherrack — which infinite-burns by vanilla design — the
 * fires last forever, creating a long-term hazard. On other
 * surfaces (e.g. stone, gold blocks) the fire will burn out
 * normally over a few minutes.
 *
 * <p>Dimension restriction: ONLY {@code Level.NETHER}. End beds
 * keep their vanilla explosion (the user did not request changes
 * there). Overworld beds work normally (and {@code bedWorks() == true}
 * there anyway).
 *
 * <p>Why probabilistic. The user specifically wanted a chance for
 * the floor-ignition outcome rather than always — preserving the
 * vanilla "bed explosion" surprise as one possibility. Going to bed
 * in the Nether becomes a gamble: 50% you blow up, 50% you set
 * everything on fire and have to escape the flames.
 *
 * <p>Implementation: {@link PlayerInteractEvent.RightClickBlock}
 * fires BEFORE {@link BedBlock#use} runs the vanilla explosion. We
 * roll 50%, and on the ignition outcome we:
 *
 * <ol>
 *   <li>Cancel the event so vanilla never explodes.</li>
 *   <li>Identify head and foot blocks of the bed and remove both
 *       (matching vanilla destruction behavior).</li>
 *   <li>Scan a 3-block radius for air spaces above solid floors and
 *       spawn fire blocks at ~30% density.</li>
 * </ol>
 */
public class BedIgnitesFloorEvents {

    /** Probability of choosing the ignition outcome over vanilla explosion. */
    private static final float IGNITE_CHANCE = 0.5f;

    /** Horizontal radius for fire spread around the bed (blocks). */
    private static final int FIRE_RADIUS = 3;

    /** Vertical extent for fire spawning (0 = same Y, 1 = +1 Y). */
    private static final int FIRE_HEIGHT_EXTENT = 1;

    /** Per-eligible-cell probability of spawning a fire block. */
    private static final float FIRE_DENSITY = 0.30f;

    @SubscribeEvent
    public void onBedRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;
        if (level.dimension() != Level.NETHER) return;

        BlockState state = level.getBlockState(event.getPos());
        if (!(state.getBlock() instanceof BedBlock)) return;

        Player player = event.getEntity();
        if (player.getRandom().nextFloat() >= IGNITE_CHANCE) return;

        // Cancel vanilla bed interaction — no explosion, no sleep attempt.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setResult(Event.Result.DENY);

        // Identify both halves of the bed.
        BlockPos clickedPos = event.getPos();
        Direction facing = state.getValue(BedBlock.FACING);
        BlockPos otherPos;
        if (state.getValue(BedBlock.PART) == BedPart.HEAD) {
            otherPos = clickedPos.relative(facing.getOpposite());
        } else {
            otherPos = clickedPos.relative(facing);
        }

        // Remove both bed blocks silently (no drops, no explosion).
        level.removeBlock(clickedPos, false);
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.getBlock() instanceof BedBlock) {
            level.removeBlock(otherPos, false);
        }

        igniteAround(level, clickedPos);
    }

    private void igniteAround(Level level, BlockPos center) {
        for (int dx = -FIRE_RADIUS; dx <= FIRE_RADIUS; dx++) {
            for (int dz = -FIRE_RADIUS; dz <= FIRE_RADIUS; dz++) {
                for (int dy = 0; dy <= FIRE_HEIGHT_EXTENT; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.getBlockState(pos).isAir()) continue;

                    BlockPos belowPos = pos.below();
                    BlockState below = level.getBlockState(belowPos);
                    if (!below.isFaceSturdy(level, belowPos, Direction.UP)) continue;

                    if (level.getRandom().nextFloat() < FIRE_DENSITY) {
                        level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
