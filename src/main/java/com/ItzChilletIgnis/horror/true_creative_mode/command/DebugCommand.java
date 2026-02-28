package com.ItzChilletIgnis.horror.true_creative_mode.command;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
                .then(CommandManager.literal("set_hatred")
                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            int newHatred = IntegerArgumentType.getInteger(context, "value");
                            ServerCommandSource source = context.getSource();
                            AbandonedToolState state = AbandonedToolState.getServerState(source.getWorld());
                            state.hatredValue = newHatred;
                            state.updateStage();
                            state.markDirty();
                            source.sendFeedback(() -> Text.literal("Hatred set to: " + newHatred + ", Stage: " + state.currentStage), false);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("set_remains_count")
                    .then(CommandManager.argument("count", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            int count = IntegerArgumentType.getInteger(context, "count");
                            ServerCommandSource source = context.getSource();
                            AbandonedToolState state = AbandonedToolState.getServerState(source.getWorld());
                            state.remainsDroppedCount = count;
                            state.markDirty();
                            source.sendFeedback(() -> Text.literal("Remains dropped count set to: " + count), false);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("set_resolute_departure")
                    .then(CommandManager.argument("active", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean active = BoolArgumentType.getBool(context, "active");
                            ServerCommandSource source = context.getSource();
                            AbandonedToolState state = AbandonedToolState.getServerState(source.getWorld());
                            state.isResoluteDepartureActive = active;
                            state.markDirty();
                            source.sendFeedback(() -> Text.literal("Resolute Departure active: " + active), false);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("clear_tracked")
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        AbandonedToolState state = AbandonedToolState.getServerState(source.getWorld());
                        state.abandonedTools.clear();
                        state.markDirty();
                        source.sendFeedback(() -> Text.literal("Cleared all tracked tools."), false);
                        return 1;
                    })
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
                        state.markDirty();
                        source.sendFeedback(() -> Text.literal("Reset all True Creative Mode data."), false);
                        return 1;
                    })
                )
            );
        });
    }
}