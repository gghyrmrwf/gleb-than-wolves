package com.gghyrmrwf.glebthanwolves.glm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

/**
 * Phase 2.3b — block-shard loot modifier.
 *
 * <p>The core mechanic of the shard system: when a player breaks a block in
 * the configured {@code block_tag}, this modifier replaces (or extends) the
 * vanilla drops with the configured {@code shard_item} stack.
 *
 * <p>JSON fields:
 * <ul>
 *   <li>{@code block_tag}: id of the block tag whose blocks this modifier
 *       applies to (e.g. {@code glebthanwolves:has_stone_fragment_shard}).</li>
 *   <li>{@code shard_item}: registry id of the shard item to drop.</li>
 *   <li>{@code count}: integer; how many shards to drop (default 2).</li>
 *   <li>{@code mode}: {@code "replace"} or {@code "additive"}.
 *     <ul>
 *       <li>{@code replace} — vanilla drops are discarded; only shards drop.</li>
 *       <li>{@code additive} — vanilla drops are kept; shards are appended.</li>
 *     </ul>
 *   </li>
 *   <li>{@code tool_action}: optional Forge tool-action id (e.g.
 *       {@code "axe_dig"}, {@code "pickaxe_dig"}, {@code "shovel_dig"},
 *       {@code "shears_dig"}). If set, the held tool must be able to perform
 *       this action for any drop to occur. Wrong tool type ⇒ no shards. In
 *       {@code replace} mode, wrong tool also clears any vanilla drops the
 *       earlier loot rolls produced (i.e. the block silently gives nothing).
 *       Omit this field to mean "any tool / hand works" (default for
 *       organic / dirt / sand / gravel / snow / wool categories).</li>
 * </ul>
 *
 * <p>Silk-touch always bypasses this modifier: the vanilla loot is returned
 * unchanged. This is the only way to obtain the original block as an item.
 */
public class BlockShardModifier extends LootModifier {

    public enum Mode {
        REPLACE,
        ADDITIVE
    }

    public static final Codec<BlockShardModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .and(ResourceLocation.CODEC.fieldOf("block_tag").forGetter(m -> m.blockTagId))
            .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("shard_item").forGetter(m -> m.shardItem))
            .and(Codec.INT.optionalFieldOf("count", 2).forGetter(m -> m.count))
            .and(Codec.STRING.fieldOf("mode").forGetter(m -> m.mode.name().toLowerCase(Locale.ROOT)))
            .and(Codec.STRING.optionalFieldOf("tool_action").forGetter(m -> Optional.ofNullable(m.toolActionName)))
            .apply(inst, BlockShardModifier::new));

    private final ResourceLocation blockTagId;
    private final TagKey<Block> blockTag;
    private final Item shardItem;
    private final int count;
    private final Mode mode;
    private final String toolActionName;
    private final ToolAction toolAction;

    public BlockShardModifier(LootItemCondition[] conditions,
                              ResourceLocation blockTagId,
                              Item shardItem,
                              int count,
                              String modeName,
                              Optional<String> toolActionName) {
        super(conditions);
        this.blockTagId = blockTagId;
        this.blockTag = TagKey.create(Registries.BLOCK, blockTagId);
        this.shardItem = shardItem;
        this.count = count;
        this.mode = Mode.valueOf(modeName.toUpperCase(Locale.ROOT));
        this.toolActionName = toolActionName.orElse(null);
        this.toolAction = (this.toolActionName != null) ? ToolAction.get(this.toolActionName) : null;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null || !state.is(blockTag)) {
            return generatedLoot;
        }

        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);

        // Silk-touch: bypass entirely so the player can recover the original block.
        if (tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
            return generatedLoot;
        }

        boolean toolMatches = (toolAction == null)
                || (tool != null && tool.canPerformAction(toolAction));

        if (mode == Mode.REPLACE) {
            if (!toolMatches) {
                // Wrong tool type: clear all drops. Block was broken (gate let it
                // through on tier), but no item drops out.
                return new ObjectArrayList<>();
            }
            ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
            result.add(new ItemStack(shardItem, count));
            return result;
        }

        // ADDITIVE: vanilla drops always kept; shards added only if the tool matches.
        if (toolMatches) {
            generatedLoot.add(new ItemStack(shardItem, count));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
