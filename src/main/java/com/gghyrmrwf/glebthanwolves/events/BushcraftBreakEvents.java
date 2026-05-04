package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 1.1 — primitive bushcraft.
 *
 * Logs cannot be broken without a tool that can perform AXE_DIG. This means:
 * — empty hand → cannot break
 * — sword / shovel / pickaxe → cannot break
 * — Primitive Axe / wooden+stone+iron+diamond+netherite axe → can break (vanilla speeds)
 *
 * Creative players bypass this restriction.
 */
public class BushcraftBreakEvents {

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.isCreative()) {
            return;
        }
        BlockState state = event.getState();
        if (!state.is(BlockTags.LOGS)) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        if (!tool.canPerformAction(ToolActions.AXE_DIG)) {
            event.setCanceled(true);
        }
    }
}
