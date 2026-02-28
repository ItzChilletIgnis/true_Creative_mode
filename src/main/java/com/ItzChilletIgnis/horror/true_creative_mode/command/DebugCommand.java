package com.ItzChilletIgnis.horror.true_creative_mode.command;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DebugCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("tc_debug")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("info")
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        AbandonedToolState state = AbandonedToolState.getServerState(source.getWorld());
                        long currentTime = source.getWorld().getTime();

                        source.sendFeedback(() -> Text.literal("--- True Creative Mode Debug ---").formatted(Formatting.GOLD), false);
                        source.sendFeedback(() -> Text.literal("Tracked Tools: ").formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(state.abandonedTools.size())).formatted(Formatting.AQUA)), false);
                        source.sendFeedback(() -> Text.literal("Total Abandoned Count: ").formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(state.totalAbandonedCount)).formatted(Formatting.YELLOW)), false);
                        source.sendFeedback(() -> Text.literal("Hatred Value: ").formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(state.hatredValue)).formatted(Formatting.RED)), false);
                        source.sendFeedback(() -> Text.literal("Current Stage: ").formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(state.currentStage)).formatted(Formatting.DARK_PURPLE)), false);
                        source.sendFeedback(() -> Text.literal("Remains Dropped: ").formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(state.remainsDroppedCount)).formatted(Formatting.DARK_RED)), false);
                        source.sendFeedback(() -> Text.literal("Resolute Departure Active: ").formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(state.isResoluteDepartureActive)).formatted(state.isResoluteDepartureActive ? Formatting.GREEN : Formatting.RED)), false);

                        // 作呕与灭绝信息
                        source.sendFeedback(() -> Text.literal("--- Nausea & Extinction ---").formatted(Formatting.GOLD), false);
                        source.sendFeedback(() -> Text.literal("Recent Kills (3m): ").formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(state.recentAnimalKills.size())).formatted(Formatting.WHITE)), false);
                        source.sendFeedback(() -> Text.literal("Nausea Active: ").formatted(Formatting.GRAY)
                            .append(Text.literal(state.nauseaEndTime > currentTime ? "YES (" + (state.nauseaEndTime - currentTime) + " ticks left)" : "NO").formatted(state.nauseaEndTime > currentTime ? Formatting.GREEN : Formatting.RED)), false);
                        source.sendFeedback(() -> Text.literal("Nausea Escalated: ").formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(state.isNauseaEscalated)).formatted(state.isNauseaEscalated ? Formatting.GREEN : Formatting.RED)), false);
                        source.sendFeedback(() -> Text.literal("Escalated Kill Total: ").formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(state.escalatedKillTotal)).formatted(Formatting.DARK_RED)), false);
                        source.sendFeedback(() -> Text.literal("Extinction Active: ").formatted(Formatting.GRAY)
                            .append(Text.literal(state.extinctionEndTime > currentTime ? "YES (" + (state.extinctionEndTime - currentTime) + " ticks left)" : "NO").formatted(state.extinctionEndTime > currentTime ? Formatting.GREEN : Formatting.RED)), false);

                        if (state.abandonedTools.isEmpty()) {
                            source.sendFeedback(() -> Text.literal("Currently no tools are being tracked.").formatted(Formatting.RED, Formatting.ITALIC), false);
                        } else {
                            source.sendFeedback(() -> Text.literal("List of Tracked Tools:").formatted(Formatting.BLUE), false);
                            for (AbandonedToolState.AbandonedTool tool : state.abandonedTools) {
                                ItemStack stack = tool.stack;
                                int count = stack.hasNbt() ? stack.getNbt().getInt("abandon_count") : 0;
                                source.sendFeedback(() -> Text.literal("- ")
                                    .append(stack.getName().copy().formatted(Formatting.WHITE))
                                    .append(Text.literal(" (abandon_count: " + count + ")").formatted(Formatting.DARK_GRAY)), false);
                            }
                        }
                        return 1;
                    })
                )
                .then(CommandManager.literal("set_nausea")
                    .then(CommandManager.argument("ticks", LongArgumentType.longArg(0))
                        .executes(context -> {
                            long ticks = LongArgumentType.getLong(context, "ticks");
                            ServerCommandSource source = context.getSource();
                            AbandonedToolState state = AbandonedToolState.getServerState(source.getWorld());
                            state.nauseaEndTime = source.getWorld().getTime() + ticks;
                            state.markDirty();
                            source.sendFeedback(() -> Text.literal("Nausea set for " + ticks + " ticks."), false);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("set_extinction")
                    .then(CommandManager.argument("ticks", LongArgumentType.longArg(0))
                        .executes(context -> {
                            long ticks = LongArgumentType.getLong(context, "ticks");
                            ServerCommandSource source = context.getSource();
                            AbandonedToolState state = AbandonedToolState.getServerState(source.getWorld());
                            state.extinctionEndTime = source.getWorld().getTime() + ticks;
                            state.markDirty();
                            source.sendFeedback(() -> Text.literal("Extinction set for " + ticks + " ticks."), false);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("set_nausea_escalated")
                    .then(CommandManager.argument("active", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean active = BoolArgumentType.getBool(context, "active");
                            ServerCommandSource source = context.getSource();
                            AbandonedToolState state = AbandonedToolState.getServerState(source.getWorld());
                            state.isNauseaEscalated = active;
                            state.markDirty();
                            source.sendFeedback(() -> Text.literal("Nausea Escalated set to: " + active), false);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("reset_all")
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        AbandonedToolState state = AbandonedToolState.getServerState(source.getWorld());
                        state.abandonedTools.clear();
                        state.totalAbandonedCount = 0;
                        state.hatredValue = 0;
                        state.currentStage = 1;
                        state.remainsDroppedCount = 0;
                        state.isResoluteDepartureActive = false;
                        state.recentAnimalKills.clear();
                        state.nauseaEndTime = 0;
                        state.isNauseaEscalated = false;
                        state.escalatedKills.clear();
                        state.escalatedKillTotal = 0;
                        state.extinctionEndTime = 0;
                        state.markDirty();
                        source.sendFeedback(() -> Text.literal("Reset all True Creative Mode data."), false);
                        return 1;
                    })
                )
            );
        });
    }
}