package com.gghyrmrwf.glebthanwolves;

import com.gghyrmrwf.glebthanwolves.glm.AddItemModifier;
import com.gghyrmrwf.glebthanwolves.glm.MultiplyItemModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, GlebThanWolves.MODID);

    public static final RegistryObject<Codec<AddItemModifier>> ADD_ITEM =
            GLM_SERIALIZERS.register("add_item", () -> AddItemModifier.CODEC);

    public static final RegistryObject<Codec<MultiplyItemModifier>> MULTIPLY_ITEM =
            GLM_SERIALIZERS.register("multiply_item", () -> MultiplyItemModifier.CODEC);

    private ModLootModifiers() {}
}
