package com.ItzChilletIgnis.horror.true_creative_mode.client;

import com.ItzChilletIgnis.horror.true_creative_mode.True_creative_mode;
import com.ItzChilletIgnis.horror.true_creative_mode.network.SyncAshfallStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class True_creative_modeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncAshfallStatePayload.ID, (payload, context) -> {
            boolean active = payload.isAshfall();
            context.client().execute(() -> {
                ClientState.isAshfallActive = active;
            });
        });

        // 环境强度过渡逻辑
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ClientState.isAshfallActive) {
                // 约 3 秒完成过渡 (1.0 / (20 * 3) ≈ 0.0167)
                ClientState.ashfallIntensity = Math.min(1f, ClientState.ashfallIntensity + 0.0167f);
            } else {
                ClientState.ashfallIntensity = Math.max(0f, ClientState.ashfallIntensity - 0.0167f);
            }
        });
    }
}