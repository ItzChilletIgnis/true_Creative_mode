package com.ItzChilletIgnis.horror.true_creative_mode.event;

import com.ItzChilletIgnis.horror.true_creative_mode.item.ModItems;
import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BlockBreakHandler {
    private static final Random RANDOM = new Random();
    private static final String[] TOOL_MESSAGES = {
            "I want to stay with you",
            "Don't abandon me",
            "Take me with you"
    };
    private static final String[] REMAINS_MESSAGES = {
            "The dead",
            "Once had life",
            "You forgot it"
    };

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(BlockBreakHandler::handle);
    }

    private static void handle(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (world.isClient) return;

        AbandonedToolState toolState = AbandonedToolState.getServerState((ServerWorld) world);
        
        // 筛选符合条件的工具：非容器内的，或者在容器内关了超过 3 分钟的，且尚未重逢的
        List<AbandonedToolState.AbandonedTool> eligibleTools = toolState.abandonedTools.stream()
                .filter(tool -> !tool.reunited)
                .filter(tool -> !tool.inContainer || (world.getTime() - tool.timestamp >= 3600))
                .collect(Collectors.toList());

        if (eligibleTools.isEmpty()) return;

        if (RANDOM.nextFloat() < 0.05f) {
            AbandonedToolState.AbandonedTool abandonedTool = eligibleTools.get(RANDOM.nextInt(eligibleTools.size()));
            ItemStack stack = abandonedTool.stack;

            NbtCompound nbt = stack.getOrCreateNbt();
            int abandonCount = nbt.getInt("abandon_count") + 1;
            nbt.putInt("abandon_count", abandonCount);

            if (abandonCount > 3 || toolState.totalAbandonedCount > 10) {
                // 异化为残骸
                ItemStack remains = new ItemStack(ModItems.REMAINS);
                String msg = REMAINS_MESSAGES[RANDOM.nextInt(REMAINS_MESSAGES.length)];
                remains.setCustomName(Text.literal(msg).formatted(Formatting.DARK_RED));
                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, remains));
                
                // 彻底移除记录
                toolState.abandonedTools.remove(abandonedTool);
            } else {
                // 掉落老友
                String msg = TOOL_MESSAGES[RANDOM.nextInt(TOOL_MESSAGES.length)];
                stack.setCustomName(Text.literal(msg).formatted(Formatting.GRAY, Formatting.ITALIC));
                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
                
                // 标记为已重逢，等待开箱抹除（如果是容器内的）或后续处理
                if (abandonedTool.inContainer) {
                    abandonedTool.reunited = true;
                } else {
                    toolState.abandonedTools.remove(abandonedTool);
                }
            }
            toolState.markDirty();
        }
    }
}