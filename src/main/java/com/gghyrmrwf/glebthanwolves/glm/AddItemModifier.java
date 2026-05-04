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
 * Adds an item to a loot pool with a chance and an optional count range.
 *
 * Only applies when the queried loot table is in the configured `loot_tables` list.
 *
 * JSON fields:
 * - loot_tables: list of loot table ids this modifier applies to
 * - item: registry id of the item to add
 * - chance: float in [0,1], probability of adding any items at all
 * - count_min: optional, default 1, minimum stack size when added
 * - count_max: optional, default 1, maximum stack size when added (rolled uniformly with count_min)
 */
public class AddItemModifier extends LootModifier {
    public static final Codec<AddItemModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .and(Codec.list(ResourceLocation.CODEC).fieldOf("loot_tables").forGetter(m -> m.lootTables))
            .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item))
            .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
            .and(Codec.INT.optionalFieldOf("count_min", 1).forGetter(m -> m.countMin))
            .and(Codec.INT.optionalFieldOf("count_max", 1).forGetter(m -> m.countMax))
            .apply(inst, AddItemModifier::new));

    private final List<ResourceLocation> lootTables;
    private final Item item;
    private final float chance;
    private final int countMin;
    private final int countMax;

    public AddItemModifier(LootItemCondition[] conditions, List<ResourceLocation> lootTables,
                           Item item, float chance, int countMin, int countMax) {
        super(conditions);
        this.lootTables = lootTables;
        this.item = item;
        this.chance = chance;
        this.countMin = countMin;
        this.countMax = countMax;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation queriedTable = context.getQueriedLootTableId();
        if (queriedTable == null || !lootTables.contains(queriedTable)) {
            return generatedLoot;
        }
        if (context.getRandom().nextFloat() < chance) {
            int span = Math.max(0, countMax - countMin);
            int count = countMin + (span > 0 ? context.getRandom().nextInt(span + 1) : 0);
            if (count > 0) {
                generatedLoot.add(new ItemStack(item, count));
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
