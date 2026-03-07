package com.ItzChilletIgnis.horror.true_creative_mode.mixin.client;

import com.ItzChilletIgnis.horror.true_creative_mode.client.ClientState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow private ClientWorld world;
    @Shadow private int ticks;
    @Shadow @Final private static Identifier CLOUDS_TEXTURE;

    @Unique
    private static final Identifier CLEARED_CLOUDS = Identifier.of("true_creative_mode", "textures/environment/cleared_clouds.png");

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(Matrix4f matrix4f, Matrix4f matrix4f2, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        if (ClientState.isSkyCleared) {
            // 取消原版天空渲染（包括太阳、月亮、星星）
            ci.cancel();
        }
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void onRenderClouds(Matrix4f matrix4f, Matrix4f matrix4f2, float f, double d, double e, double f2, CallbackInfo ci) {
        // 这里可以通过 Redirect 替换贴图，或者在渲染前修改 Shadow 字段
        // 为了简单起见，如果需要替换贴图，通常使用 Redirect 注入到 bindTexture
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void onRenderWeather(LightmapTextureManager manager, float tickDelta, double d, double e, double f, CallbackInfo ci) {
        if (ClientState.ashfallIntensity > 0) {
            ci.cancel();
        }
    }

    @Inject(method = "tickRainSplashing", at = @At("HEAD"), cancellable = true)
    private void onTickRainSplashing(Camera camera, CallbackInfo ci) {
        if (ClientState.ashfallIntensity > 0) {
            Random random = Random.create(this.ticks * 312987231L);
            Vec3d camPos = camera.getPos();
            int density = (int) (120 * ClientState.ashfallIntensity);
            for (int i = 0; i < density; i++) {
                double offX = (random.nextDouble() - 0.5) * 32.0;
                double offY = (random.nextDouble() - 0.5) * 32.0;
                double offZ = (random.nextDouble() - 0.5) * 32.0;
                Vec3d spawnPos = camPos.add(offX, offY, offZ);
                BlockPos blockPos = BlockPos.ofFloored(spawnPos);
                if (this.world.getBlockState(blockPos).isAir()) {
                    double vx = (random.nextDouble() - 0.5) * 0.02;
                    double vy = -0.02 - random.nextDouble() * 0.03;
                    double vz = (random.nextDouble() - 0.5) * 0.02;
                    if (random.nextFloat() < 0.5f) {
                        this.world.addParticle(ParticleTypes.WHITE_ASH, spawnPos.x, spawnPos.y, spawnPos.z, vx, vy, vz);
                    } else {
                        this.world.addParticle(ParticleTypes.ASH, spawnPos.x, spawnPos.y, spawnPos.z, vx, vy, vz);
                    }
                }
            }
            ci.cancel();
        }
    }
}