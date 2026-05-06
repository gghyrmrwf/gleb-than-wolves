package com.gghyrmrwf.glebthanwolves.glm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Reduces drop rate of any of the listed items by a factor (0..1).
 * For each existing drop that matches one of the items, keeps it with probability `factor`,
 * removes it with probability `1 - factor`. Result: drop rate is multiplied by `factor`.
 *
 * Only applies if the queried loot table is in the configured `loot_tables` list.
 */
public class MultiplyItemModifier extends LootModifier {
    public static final Codec<MultiplyItemModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .and(Codec.list(ResourceLocation.CODEC).fieldOf("loot_tables").forGetter(m -> m.lootTables))
            .and(Codec.list(BuiltInRegistries.ITEM.byNameCodec()).fieldOf("items").forGetter(m -> m.items))
            .and(Codec.FLOAT.fieldOf("factor").forGetter(m -> m.factor))
            .apply(inst, MultiplyItemModifier::new));

    private final List<ResourceLocation> lootTables;
    private final List<Item> items;
    private final float factor;

    public MultiplyItemModifier(LootItemCondition[] conditions, List<ResourceLocation> lootTables,
                                List<Item> items, float factor) {
        super(conditions);
        this.lootTables = lootTables;
        this.items = items;
        this.factor = factor;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation queriedTable = context.getQueriedLootTableId();
        if (queriedTable == null || !lootTables.contains(queriedTable)) {
            return generatedLoot;
        }
        for (int i = generatedLoot.size() - 1; i >= 0; i--) {
            ItemStack stack = generatedLoot.get(i);
            if (items.contains(stack.getItem()) && context.getRandom().nextFloat() > factor) {
                generatedLoot.remove(i);
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
