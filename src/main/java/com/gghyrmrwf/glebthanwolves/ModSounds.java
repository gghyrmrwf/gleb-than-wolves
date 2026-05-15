package com.gghyrmrwf.glebthanwolves;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Custom sound registrations for the Gleb Than Wolves mod.
 *
 * <p>Each {@link SoundEvent} here corresponds to an entry in
 * {@code src/main/resources/assets/glebthanwolves/sounds.json} which
 * in turn points at one or more {@code .ogg} files under
 * {@code src/main/resources/assets/glebthanwolves/sounds/}.
 *
 * <p>Minecraft only supports OGG Vorbis sound files; MP3 and other
 * formats must be converted before bundling.
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GlebThanWolves.MODID);

    /**
     * Phase 3.5 — sound played when a pig explodes on death.
     *
     * <p>Linked to {@code pig_explode.ogg} via {@code sounds.json}.
     */
    public static final RegistryObject<SoundEvent> PIG_EXPLODE = SOUND_EVENTS.register(
            "pig_explode",
            () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(GlebThanWolves.MODID, "pig_explode")
            )
    );
}
