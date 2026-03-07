package com.ItzChilletIgnis.horror.true_creative_mode.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
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
                        clearedSkyActive.add(uuid);
                    }
                } else {
                    if (clearedSkyActive.contains(uuid)) {
                        player.sendMessage(Text.literal("You welcome")
                                .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
                        clearedSkyActive.remove(uuid);
                        undergroundTicks.put(uuid, 0);
                    } else {
                        int ticks = undergroundTicks.getOrDefault(uuid, 0);
                        if (ticks > 0) {
                            undergroundTicks.put(uuid, ticks - 1);
                        }
                    }
                }
            }
        });
    }
}