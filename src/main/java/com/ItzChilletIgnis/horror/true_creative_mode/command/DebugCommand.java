package com.ItzChilletIgnis.horror.true_creative_mode.command;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
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
                            
                            source.sendFeedback(() -> Text.literal("Hatred set to: ").formatted(Formatting.GRAY)
                                .append(Text.literal(String.valueOf(newHatred)).formatted(Formatting.RED))
                                .append(Text.literal(", Stage: ").formatted(Formatting.GRAY))
                                .append(Text.literal(String.valueOf(state.currentStage)).formatted(Formatting.DARK_PURPLE)), false);
                            return 1;
                        })
                    )
                )
            );
        });
    }
}