package com.ItzChilletIgnis.horror.true_creative_mode.mixin.client;

import com.ItzChilletIgnis.horror.true_creative_mode.client.ClientState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (ClientState.ashfallIntensity > 0) {
            Vec3d originalColor = cir.getReturnValue();
            Vec3d targetColor = new Vec3d(0.24, 0.18, 0.15);
            // 使用 lerp 实现平滑的天空颜色插值
            cir.setReturnValue(originalColor.lerp(targetColor, (double) ClientState.ashfallIntensity));
        }
    }

    @Inject(method = "getCloudsColor", at = @At("RETURN"), cancellable = true)
    private void onGetCloudsColor(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (ClientState.ashfallIntensity > 0) {
            Vec3d originalColor = cir.getReturnValue();
            Vec3d targetColor = new Vec3d(0.2, 0.15, 0.12);
            // 使用 lerp 实现平滑的云层颜色插值
            cir.setReturnValue(originalColor.lerp(targetColor, (double) ClientState.ashfallIntensity));
        }
    }
}