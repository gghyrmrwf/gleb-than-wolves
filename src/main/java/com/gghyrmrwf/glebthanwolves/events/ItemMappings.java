package com.gghyrmrwf.glebthanwolves.events;

import com.gghyrmrwf.glebthanwolves.GlebThanWolves;
import com.gghyrmrwf.glebthanwolves.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles registry-id renames so that pre-Phase-2.3b saves don't lose stacks
 * of the old items.
 *
 * <ul>
 *   <li>{@code glebthanwolves:plant_fiber}  &rarr;
 *       {@code glebthanwolves:organic_fiber}</li>
 *   <li>{@code glebthanwolves:wood_chunk}   &rarr;
 *       {@code glebthanwolves:wood_chip}</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = GlebThanWolves.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ItemMappings {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemMappings.class);

    /**
     * Old registry name &rarr; supplier of the new item.
     */
    private static final Map<ResourceLocation, java.util.function.Supplier<Item>> RENAMES = new HashMap<>();

    static {
        RENAMES.put(new ResourceLocation(GlebThanWolves.MODID, "plant_fiber"),
                () -> ModItems.ORGANIC_FIBER.get());
        RENAMES.put(new ResourceLocation(GlebThanWolves.MODID, "wood_chunk"),
                () -> ModItems.WOOD_CHIP.get());
    }

    @SubscribeEvent
    public static void onMissingItemMappings(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Item> mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, GlebThanWolves.MODID)) {
            java.util.function.Supplier<Item> supplier = RENAMES.get(mapping.getKey());
            if (supplier != null) {
                Item replacement = supplier.get();
                mapping.remap(replacement);
                LOGGER.info("[Gleb Than Wolves] Remapped legacy item {} -> {}",
                        mapping.getKey(), ForgeRegistries.ITEMS.getKey(replacement));
            }
        }
    }

    private ItemMappings() {}
}
