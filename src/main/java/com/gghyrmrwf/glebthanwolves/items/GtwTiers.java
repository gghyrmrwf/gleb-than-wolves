package com.gghyrmrwf.glebthanwolves.items;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Tier definitions for Gleb Than Wolves tools.
 *
 * <p>Pattern: every GTW tier mirrors a vanilla tier in mining-level and
 * attack-damage bonus, but ships with ~50% durability and ~75% mining
 * speed. The repair ingredient is the closest single vanilla item.
 *
 * <ul>
 *   <li>{@link #GTW_WOODEN} — Phase 2.2.x. Mirrors {@code Tiers.WOOD}
 *       (level 0, +0 dmg). Durability 30 (vs 59), speed 1.5 (vs 2.0),
 *       enchantability 15 (same), repair = oak_planks.</li>
 *   <li>{@link #GTW_STONE} — Phase 2.2. Mirrors {@code Tiers.STONE}
 *       (level 1, +1.0 dmg). Durability 70 (vs 131), speed 3.0 (vs 4.0),
 *       enchantability 5 (same), repair = cobblestone.</li>
 *   <li>{@link #GTW_IRON} — Phase 2.2.x. Mirrors {@code Tiers.IRON}
 *       (level 2, +2.0 dmg). Durability 130 (vs 250), speed 4.5 (vs 6.0),
 *       enchantability 14 (same), repair = iron_ingot.</li>
 * </ul>
 */
public final class GtwTiers {

    public static final Tier GTW_WOODEN = new Tier() {
        @Override
        public int getUses() { return 30; }

        @Override
        public float getSpeed() { return 1.5F; }

        @Override
        public float getAttackDamageBonus() { return 0.0F; }

        @Override
        public int getLevel() { return 0; }

        @Override
        public int getEnchantmentValue() { return 15; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.of(Items.OAK_PLANKS); }
    };

    public static final Tier GTW_STONE = new Tier() {
        @Override
        public int getUses() { return 70; }

        @Override
        public float getSpeed() { return 3.0F; }

        @Override
        public float getAttackDamageBonus() { return 1.0F; }

        @Override
        public int getLevel() { return 1; }

        @Override
        public int getEnchantmentValue() { return 5; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.of(Items.COBBLESTONE); }
    };

    public static final Tier GTW_IRON = new Tier() {
        @Override
        public int getUses() { return 130; }

        @Override
        public float getSpeed() { return 4.5F; }

        @Override
        public float getAttackDamageBonus() { return 2.0F; }

        @Override
        public int getLevel() { return 2; }

        @Override
        public int getEnchantmentValue() { return 14; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.of(Items.IRON_INGOT); }
    };

    private GtwTiers() {}
}
