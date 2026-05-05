package com.gghyrmrwf.glebthanwolves.client;

import com.gghyrmrwf.glebthanwolves.GlebThanWolves;
import com.gghyrmrwf.glebthanwolves.events.DarkNightEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import net.minecraft.world.level.Level;

@Mod.EventBusSubscriber(modid = GlebThanWolves.MODID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class DarkNightClientEvents {

    private static final float DARK_NIGHT_FOG_NEAR = 4.0F;
    private static final float DARK_NIGHT_FOG_FAR = 26.0F;
    private static final float DARK_NIGHT_FOG_RED = 0.006F;
    private static final float DARK_NIGHT_FOG_GREEN = 0.007F;
    private static final float DARK_NIGHT_FOG_BLUE = 0.018F;
    private static final float DARK_NIGHT_SKY_LIGHT_SCALE = 0.06F;
    private static final float DARK_NIGHT_BLOCK_LIGHT_SCALE = 0.32F;
    private static final float DARK_NIGHT_MIN_LIGHT = 0.012F;

    private DarkNightClientEvents() {
    }

    @Mod.EventBusSubscriber(
            modid = GlebThanWolves.MODID,
            value = net.minecraftforge.api.distmarker.Dist.CLIENT,
            bus = Mod.EventBusSubscriber.Bus.MOD
    )
    public static final class ModBusEvents {

        private ModBusEvents() {
        }

        @SubscribeEvent
        public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
            event.register(BuiltinDimensionTypes.OVERWORLD_EFFECTS, new DarkNightOverworldEffects());
        }
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!isClientDarkNight()) {
            return;
        }
        event.setRed(DARK_NIGHT_FOG_RED);
        event.setGreen(DARK_NIGHT_FOG_GREEN);
        event.setBlue(DARK_NIGHT_FOG_BLUE);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!isClientDarkNight()) {
            return;
        }
        event.setNearPlaneDistance(DARK_NIGHT_FOG_NEAR);
        event.setFarPlaneDistance(DARK_NIGHT_FOG_FAR);
    }

    public static void adjustLightmap(ClientLevel level, int skyLight, int blockLight, Vector3f colors) {
        if (!isClientDarkNight(level)) {
            return;
        }
        float skyFactor = 1.0F - (skyLight / (float) LightTexture.FULL_SKY) * (1.0F - DARK_NIGHT_SKY_LIGHT_SCALE);
        float blockFactor = 1.0F - (blockLight / (float) LightTexture.FULL_BLOCK) * (1.0F - DARK_NIGHT_BLOCK_LIGHT_SCALE);
        float factor = Math.max(DARK_NIGHT_MIN_LIGHT, Math.min(skyFactor, blockFactor));
        colors.mul(factor);
    }

    private static boolean isClientDarkNight() {
        Minecraft minecraft = Minecraft.getInstance();
        return isClientDarkNight(minecraft.level);
    }

    private static boolean isClientDarkNight(ClientLevel level) {
        return level != null && level.dimension() == Level.OVERWORLD && DarkNightEvents.isDarkNight(level);
    }

    private static class DarkNightOverworldEffects extends DimensionSpecialEffects.OverworldEffects {

        @Override
        public void adjustLightmapColors(
                ClientLevel level,
                float partialTicks,
                float skyDarken,
                float blockLightRedFlicker,
                float skyLight,
                int packedLight,
                int skyLightLevel,
                Vector3f colors
        ) {
            DarkNightClientEvents.adjustLightmap(
                    level,
                    LightTexture.sky(packedLight),
                    LightTexture.block(packedLight),
                    colors
            );
        }
    }
}
