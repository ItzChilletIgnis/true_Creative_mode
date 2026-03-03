package com.ItzChilletIgnis.horror.true_creative_mode.event;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.world.ServerWorld;

public class SpawningHandler {
    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            // world 参数在 ENTITY_LOAD 中已经是 ServerWorld 类型
            if (entity instanceof AnimalEntity && entity.age == 0) {
                AbandonedToolState state = AbandonedToolState.getServerState(world);
                if (state.extinctionEndTime > world.getTime()) {
                    // 灭绝期：直接抹杀新生成的动物
                    entity.discard();
                }
            }
        });
    }
}