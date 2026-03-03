package com.ItzChilletIgnis.horror.true_creative_mode.event;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class AnimalKillHandler {
    private static final Random RANDOM = new Random();

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
                        
                        // 九头蛇机制：在玩家背后生成一只同类型动物
                        spawnBehindPlayer(world, player, animal.getType());

                        // 灭绝判定
                        if (state.escalatedKillTotal >= 10) {
                            state.nauseaEndTime = 0;
                            state.extinctionEndTime = currentTime + 72000;
                            // 叙事更新：世界以一种扭曲的顺从姿态回应玩家的杀戮
                            player.sendMessage(Text.literal("As you wish.").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
                            
                            // 播放洞穴音效增强恐怖感
                            world.playSound(null, player.getBlockPos(), SoundEvents.AMBIENT_CAVE.value(), SoundCategory.AMBIENT, 1.0F, 1.0F);
                            
                            // 遍历世界清理动物 (使用虚空伤害触发死亡动画)
                            world.iterateEntities().forEach(entity -> {
                                if (entity instanceof AnimalEntity targetAnimal) {
                                    targetAnimal.damage(world.getDamageSources().outOfWorld(), Float.MAX_VALUE);
                                }
                            });
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

    private static void spawnBehindPlayer(ServerWorld world, PlayerEntity player, EntityType<?> type) {
        // 计算玩家背后的随机角度
        float baseAngle = player.getYaw() + 180;
        float randomOffset = (RANDOM.nextFloat() - 0.5f) * 90; // +/- 45 度
        double angleRad = Math.toRadians(baseAngle + randomOffset);

        // 随机距离 6-8 格
        double distance = 6 + RANDOM.nextDouble() * 2;

        // 计算目标 X 和 Z
        double targetX = player.getX() + Math.sin(-angleRad) * distance;
        double targetZ = player.getZ() + Math.cos(-angleRad) * distance;

        int playerY = player.getBlockPos().getY();
        BlockPos bestPos = null;
        int minDy = Integer.MAX_VALUE;

        // Y轴智能适配：从 -5 到 +5 搜索合法位置
        for (int dy = -5; dy <= 5; dy++) {
            BlockPos pos = new BlockPos((int)targetX, playerY + dy, (int)targetZ);
            
            // 合法性检查：目标是空气，上方是空气，下方是固体
            if (world.getBlockState(pos).isAir() && 
                world.getBlockState(pos.up()).isAir() && 
                world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
                
                int absDy = Math.abs(dy);
                if (absDy < minDy) {
                    minDy = absDy;
                    bestPos = pos;
                }
            }
        }

        // 如果找到了合法位置，生成实体
        if (bestPos != null) {
            type.spawn(world, bestPos, SpawnReason.EVENT);
        }
    }
}