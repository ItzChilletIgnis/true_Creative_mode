package com.ItzChilletIgnis.horror.true_creative_mode.mixin.client;

import com.ItzChilletIgnis.horror.true_creative_mode.client.ClientState;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {
    @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
    private void onGetRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
        // 使用 ashfallIntensity 实现平滑的亮度过渡
        if (ClientState.ashfallIntensity > 0) {
            cir.setReturnValue(ClientState.ashfallIntensity);
        }
    }
}