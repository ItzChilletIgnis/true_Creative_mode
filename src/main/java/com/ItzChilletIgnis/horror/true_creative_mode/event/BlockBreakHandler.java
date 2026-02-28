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

import java.util.Random;

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
        if (toolState.abandonedTools.isEmpty()) return;

        if (RANDOM.nextFloat() < 0.05f) {
            int index = RANDOM.nextInt(toolState.abandonedTools.size());
            AbandonedToolState.AbandonedTool abandonedTool = toolState.abandonedTools.get(index);
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
                toolState.abandonedTools.remove(index);
            } else {
                // 掉落老友
                String msg = TOOL_MESSAGES[RANDOM.nextInt(TOOL_MESSAGES.length)];
                stack.setCustomName(Text.literal(msg).formatted(Formatting.GRAY, Formatting.ITALIC));
                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
                
                // 如果是容器中的，需要标记移除（这里简单处理，直接从列表移除，后续逻辑在容器交互时处理）
                toolState.abandonedTools.remove(index);
            }
            toolState.markDirty();
        }
    }
}