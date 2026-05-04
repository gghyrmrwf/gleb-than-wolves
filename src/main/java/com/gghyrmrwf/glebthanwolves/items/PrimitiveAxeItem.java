package com.gghyrmrwf.glebthanwolves.items;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class PrimitiveAxeItem extends AxeItem {

    public static final Tier PRIMITIVE_TIER = new Tier() {
        @Override
        public int getUses() { return 8; }

        @Override
        public float getSpeed() { return 1.5F; }

        @Override
        public float getAttackDamageBonus() { return 1.0F; }

        @Override
        public int getLevel() { return 0; }

        @Override
        public int getEnchantmentValue() { return 0; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public PrimitiveAxeItem(Tier tier, float damage, float speed, Item.Properties props) {
        super(tier, damage, speed, props);
    }
}
