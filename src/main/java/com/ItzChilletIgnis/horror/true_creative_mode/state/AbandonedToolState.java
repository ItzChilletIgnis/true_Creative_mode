package com.ItzChilletIgnis.horror.true_creative_mode.state;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AbandonedToolState extends PersistentState {
    public static class AbandonedTool {
        public ItemStack stack;
        public BlockPos containerPos;
        public int slot;
        public long timestamp;
        public boolean inContainer;
        public UUID ownerUuid;

        public AbandonedTool(ItemStack stack, BlockPos pos, int slot, long timestamp, boolean inContainer, UUID ownerUuid) {
            this.stack = stack;
            this.containerPos = pos;
            this.slot = slot;
            this.timestamp = timestamp;
            this.inContainer = inContainer;
            this.ownerUuid = ownerUuid;
        }

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.put("Stack", stack.writeNbt(new NbtCompound()));
            if (containerPos != null) {
                nbt.putLong("Pos", containerPos.asLong());
            }
            nbt.putInt("Slot", slot);
            nbt.putLong("Timestamp", timestamp);
            nbt.putBoolean("InContainer", inContainer);
            if (ownerUuid != null) {
                nbt.putUuid("Owner", ownerUuid);
            }
            return nbt;
        }

        public static AbandonedTool fromNbt(NbtCompound nbt) {
            ItemStack stack = ItemStack.fromNbt(nbt.getCompound("Stack"));
            BlockPos pos = nbt.contains("Pos") ? BlockPos.fromLong(nbt.getLong("Pos")) : null;
            int slot = nbt.getInt("Slot");
            long timestamp = nbt.getLong("Timestamp");
            boolean inContainer = nbt.getBoolean("InContainer");
            UUID ownerUuid = nbt.contains("Owner") ? nbt.getUuid("Owner") : null;
            return new AbandonedTool(stack, pos, slot, timestamp, inContainer, ownerUuid);
        }
    }

    public final List<AbandonedTool> abandonedTools = new ArrayList<>();
    public int totalAbandonedCount = 0;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (AbandonedTool tool : abandonedTools) {
            list.add(tool.toNbt());
        }
        nbt.put("AbandonedTools", list);
        nbt.putInt("TotalAbandonedCount", totalAbandonedCount);
        return nbt;
    }

    public static AbandonedToolState fromNbt(NbtCompound nbt) {
        AbandonedToolState state = new AbandonedToolState();
        NbtList list = nbt.getList("AbandonedTools", 10);
        for (int i = 0; i < list.size(); i++) {
            state.abandonedTools.add(AbandonedTool.fromNbt(list.getCompound(i)));
        }
        state.totalAbandonedCount = nbt.getInt("TotalAbandonedCount");
        return state;
    }

    public static AbandonedToolState getServerState(ServerWorld world) {
        return world.getServer().getOverworld().getPersistentStateManager().getOrCreate(
                AbandonedToolState::fromNbt,
                AbandonedToolState::new,
                "true_creative_abandoned_tools"
        );
    }
}