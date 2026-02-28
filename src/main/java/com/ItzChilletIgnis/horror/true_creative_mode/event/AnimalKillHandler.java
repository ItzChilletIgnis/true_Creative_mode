package com.ItzChilletIgnis.horror.true_creative_mode.event;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;

public class AnimalKillHandler {
    public static void register() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, killer, killed) -> {
            if (killer instanceof PlayerEntity player && killed instanceof AnimalEntity animal) {
                AbandonedToolState state = AbandonedToolState.getServerState(world);
                long currentTime = world.getTime();

                // 清理过期数据
                state.recentAnimalKills.removeIf(time -> time < currentTime - 3600);
                state.escalatedKills.removeIf(time -> time < currentTime - 2400);

                // 逻辑判定树
                if (state.extinctionEndTime > currentTime) return;

                if (state.nauseaEndTime > currentTime) {
                    if (!state.isNauseaEscalated) {
                        state.escalatedKills.add(currentTime);
                        if (state.escalatedKills.size() >= 4) {
                            state.isNauseaEscalated = true;
                            state.escalatedKills.clear();
                        }
                    } else {
                        state.escalatedKillTotal++;
                        
                        // 九头蛇机制：在玩家背后生成两只同类型动物
                        double yaw = Math.toRadians(player.getYaw());
                        Vec3d offset = new Vec3d(Math.sin(yaw) * 6, 0, -Math.cos(yaw) * 6);
                        Vec3d spawnPos = player.getPos().add(offset);
                        
                        EntityType<?> type = animal.getType();
                        for (int i = 0; i < 2; i++) {
                            type.spawn(world, null, null, null, player.getBlockPos().add((int)offset.x, 2, (int)offset.z), SpawnReason.EVENT, true, false);
                        }

                        // 灭绝判定
                        if (state.escalatedKillTotal >= 10) {
                            state.nauseaEndTime = 0;
                            state.extinctionEndTime = currentTime + 72000;
                            player.sendMessage(Text.literal("Satisfied?").formatted(Formatting.RED, Formatting.BOLD), false);
                            
                            // 遍历世界清理动物
                            for (Entity entity : world.iterateEntities()) {
                                if (entity instanceof AnimalEntity) entity.discard();
                            }
                        }
                    }
                } else {
                    state.recentAnimalKills.add(currentTime);
                    if (state.recentAnimalKills.size() >= 8) {
                        state.nauseaEndTime = currentTime + 6000;
                        state.isNauseaEscalated = false;
                        state.escalatedKillTotal = 0;
                        state.escalatedKills.clear();
                        player.sendMessage(Text.literal("What you get is more than what you need").formatted(Formatting.WHITE), false);
                    }
                }
                state.markDirty();
            }
        });
    }
}