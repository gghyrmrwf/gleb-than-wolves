package com.gghyrmrwf.glebthanwolves.client;

import com.gghyrmrwf.glebthanwolves.GlebThanWolves;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Phase 3.15 — replace the ender dragon's 3D body with a single flat
 * camera-facing image (billboard quad).
 *
 * <p>Server-side hitbox sub-entities (head, neck, body, tail segments, wings)
 * are untouched: combat, damage, and movement work exactly as vanilla.
 * Only the visual model is replaced; from any angle the dragon now looks
 * like a flat PNG hovering at the body's center.
 */
public class FlatEnderDragonRenderer extends EntityRenderer<EnderDragon> {

    /** Texture used for the billboard. */
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            GlebThanWolves.MODID, "textures/entity/flat_dragon.png");

    /** Half-edge of the billboard quad in world units (blocks). */
    private static final float HALF_SIZE = 8.0f;

    /** Vertical lift above the entity's base position to roughly centre on body. */
    private static final float Y_OFFSET = 4.0f;

    public FlatEnderDragonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f; // no shadow under the flat image
    }

    @Override
    public ResourceLocation getTextureLocation(EnderDragon entity) {
        return TEXTURE;
    }

    @Override
    public void render(EnderDragon dragon, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Lift to body centre.
        poseStack.translate(0.0, Y_OFFSET, 0.0);

        // Always face the camera (billboard).
        Quaternionf cameraOrientation = this.entityRenderDispatcher.cameraOrientation();
        poseStack.mulPose(cameraOrientation);

        // Flip horizontally so the image isn't mirrored.
        poseStack.scale(-1f, 1f, 1f);

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

        addVertex(vc, matrix, normal, -HALF_SIZE, -HALF_SIZE, 0f, 1f, packedLight);
        addVertex(vc, matrix, normal,  HALF_SIZE, -HALF_SIZE, 1f, 1f, packedLight);
        addVertex(vc, matrix, normal,  HALF_SIZE,  HALF_SIZE, 1f, 0f, packedLight);
        addVertex(vc, matrix, normal, -HALF_SIZE,  HALF_SIZE, 0f, 0f, packedLight);

        poseStack.popPose();

        super.render(dragon, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void addVertex(VertexConsumer vc, Matrix4f matrix, Matrix3f normal,
                                  float x, float y, float u, float v, int packedLight) {
        vc.vertex(matrix, x, y, 0f)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0f, 0f, 1f)
                .endVertex();
    }
}
