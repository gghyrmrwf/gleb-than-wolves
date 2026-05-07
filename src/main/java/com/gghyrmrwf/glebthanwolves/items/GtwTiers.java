package com.gghyrmrwf.glebthanwolves.items;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Tier definitions for Gleb Than Wolves tools.
 *
 * <p>Phase 2.2 — "Crude" stone tier. Same mining level (1) and damage bonus
 * (+1.0) as vanilla {@code Tiers.STONE}, but lower durability (70 vs 131) and
 * lower mining speed (3.0 vs 4.0). Repaired with cobblestone.
 */
public final class GtwTiers {

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

    private GtwTiers() {}
}
