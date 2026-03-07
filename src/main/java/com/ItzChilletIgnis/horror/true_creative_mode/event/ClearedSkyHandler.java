package com.ItzChilletIgnis.horror.true_creative_mode.event;

import com.ItzChilletIgnis.horror.true_creative_mode.network.ClearedSkyPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClearedSkyHandler {
    private static final Map<UUID, Integer> undergroundTicks = new HashMap<>();
    private static final Set<UUID> clearedSkyActive = new HashSet<>();

    public static void register() {
        // 监听玩家 Tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.isSpectator()) continue;

                World world = player.getWorld();
                UUID uuid = player.getUuid();
                boolean isUnderground = !world.isSkyVisible(player.getBlockPos()) && player.getY() < 60;

                if (isUnderground) {
                    int ticks = undergroundTicks.getOrDefault(uuid, 0) + 1;
                    undergroundTicks.put(uuid, ticks);

                    if (ticks == 3600) {
                        player.sendMessage(Text.literal("(...I can mine all day, and all night...)")
                                .formatted(Formatting.GRAY, Formatting.ITALIC), true);
                    } else if (ticks == 7200) {
                        if (!clearedSkyActive.contains(uuid)) {
                            clearedSkyActive.add(uuid);
                            // 通知客户端触发黑天
                            ServerPlayNetworking.send(player, new ClearedSkyPayload(true));
                        }
                    }
                } else {
                    if (clearedSkyActive.contains(uuid)) {
                        player.sendMessage(Text.literal("You welcome")
                                .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
                        clearedSkyActive.remove(uuid);
                        undergroundTicks.put(uuid, 0);
                        // 回到地表自动恢复（逻辑重置）
                        ServerPlayNetworking.send(player, new ClearedSkyPayload(false));
                    } else {
                        int ticks = undergroundTicks.getOrDefault(uuid, 0);
                        if (ticks > 0) {
                            undergroundTicks.put(uuid, ticks - 1);
                        }
                    }
                }
            }
        });

        // 监听聊天消息（恢复天空的“咒语”）
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String content = message.getContent().getString();
            if ("I said, let there be light".equals(content)) {
                UUID uuid = sender.getUuid();
                if (clearedSkyActive.contains(uuid)) {
                    // 1. 重置状态
                    clearedSkyActive.remove(uuid);
                    undergroundTicks.put(uuid, 0);

                    // 2. 通知客户端恢复天空
                    ServerPlayNetworking.send(sender, new ClearedSkyPayload(false));

                    // 3. 播放重置音效
                    sender.getWorld().playSound(null, sender.getBlockPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                    sender.getWorld().playSound(null, sender.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.AMBIENT, 1.0f, 0.5f);

                    // 4. 取消消息显示
                    return false;
                }
            }
            return true;
        });
    }
}