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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow private ClientWorld world;
    @Shadow private int ticks;

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(Matrix4f matrix4f, Matrix4f matrix4f2, float f, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        if (ClientState.isSkyCleared) {
            // 取消原版天空渲染（包括太阳、月亮、星星）
            ci.cancel();
        }
    }

    @ModifyArg(method = "renderClouds", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"), index = 1)
    private Identifier changeCloudTexture(Identifier original) {
        if (ClientState.isSkyCleared) {
            return Identifier.of("true_creative_mode", "textures/environment/cleared_clouds.png");
        }
        return original;
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