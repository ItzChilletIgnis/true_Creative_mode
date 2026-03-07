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
        if (ClientState.isSkyCleared) {
            // 天空被清除时，强制返回纯黑色
            cir.setReturnValue(Vec3d.ZERO);
        } else if (ClientState.ashfallIntensity > 0) {
            Vec3d originalColor = cir.getReturnValue();
            Vec3d targetColor = new Vec3d(0.24, 0.18, 0.15);
            cir.setReturnValue(originalColor.lerp(targetColor, (double) ClientState.ashfallIntensity));
        }
    }

    @Inject(method = "getCloudsColor", at = @At("RETURN"), cancellable = true)
    private void onGetCloudsColor(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (ClientState.isSkyCleared) {
            // 云层同步变黑
            cir.setReturnValue(Vec3d.ZERO);
        } else if (ClientState.ashfallIntensity > 0) {
            Vec3d originalColor = cir.getReturnValue();
            Vec3d targetColor = new Vec3d(0.2, 0.15, 0.12);
            cir.setReturnValue(originalColor.lerp(targetColor, (double) ClientState.ashfallIntensity));
        }
    }
}