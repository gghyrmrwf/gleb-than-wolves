package com.gghyrmrwf.glebthanwolves.events;

import com.gghyrmrwf.glebthanwolves.GlebThanWolves;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 2.3a — mining gate.
 *
 * <p>Default-deny: by default no block is breakable by anything (including
 * empty hand). Each tier has a small whitelist of blocks it is allowed to
 * break, expressed as a datapack block tag:
 *
 * <ul>
 *   <li>{@code glebthanwolves:breakable_by/hand} — tier 0 (no tool)</li>
 *   <li>{@code glebthanwolves:breakable_by/primitive} — tier 1 (wood-equivalent: primitive_axe, vanilla wooden_*)</li>
 *   <li>{@code glebthanwolves:breakable_by/stone} — tier 2 (GTW stone_* and vanilla stone_*)</li>
 *   <li>{@code glebthanwolves:breakable_by/iron} — tier 3</li>
 *   <li>{@code glebthanwolves:breakable_by/diamond} — tier 4</li>
 *   <li>{@code glebthanwolves:breakable_by/netherite} — tier 5</li>
 * </ul>
 *
 * <p>Higher-tier tags include the lower-tier tags via {@code "#tag"} entries
 * in JSON, so anything breakable by hand is automatically breakable by every
 * tool above it.
 *
 * <p>If the held item is not a {@link TieredItem}, the player is treated as
 * having tier 0 (hand). This means bows, fishing rods, plant_fiber, etc. all
 * count as bare hands for mining purposes.
 *
 * <p>Vanilla {@code correctToolForDrops} is left untouched, so even if a tool
 * can <em>break</em> a block under this gate, the block may still drop nothing
 * if the held tool is the wrong <em>type</em> (e.g. an iron shovel breaks an
 * iron_ore block at iron tier but drops nothing — same as vanilla).
 *
 * <p>Creative players bypass the gate entirely.
 */
public class MiningGate {

    private static final TagKey<Block> HAND = blockTag("breakable_by/hand");
    private static final TagKey<Block> PRIMITIVE = blockTag("breakable_by/primitive");
    private static final TagKey<Block> STONE = blockTag("breakable_by/stone");
    private static final TagKey<Block> IRON = blockTag("breakable_by/iron");
    private static final TagKey<Block> DIAMOND = blockTag("breakable_by/diamond");
    private static final TagKey<Block> NETHERITE = blockTag("breakable_by/netherite");

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK,
                new ResourceLocation(GlebThanWolves.MODID, path));
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.isCreative()) {
            return;
        }
        BlockState state = event.getState();
        if (state.isAir()) {
            return;
        }
        int tier = toolTier(player.getMainHandItem());
        if (!isBreakable(state, tier)) {
            event.setNewSpeed(0.0F);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative()) {
            return;
        }
        BlockState state = event.getState();
        if (state.isAir()) {
            return;
        }
        int tier = toolTier(player.getMainHandItem());
        if (!isBreakable(state, tier)) {
            event.setCanceled(true);
        }
    }

    private static int toolTier(ItemStack stack) {
        if (stack.getItem() instanceof TieredItem tiered) {
            // Vanilla tier levels: WOOD=0, STONE=1, IRON=2, DIAMOND=3, NETHERITE=4.
            // We shift by +1 so that hand=0, primitive/wood=1, stone=2, ...
            return tiered.getTier().getLevel() + 1;
        }
        return 0;
    }

    private static boolean isBreakable(BlockState state, int tier) {
        return switch (tier) {
            case 5 -> state.is(NETHERITE);
            case 4 -> state.is(DIAMOND);
            case 3 -> state.is(IRON);
            case 2 -> state.is(STONE);
            case 1 -> state.is(PRIMITIVE);
            default -> state.is(HAND);
        };
    }
}
