package com.ItzChilletIgnis.horror.true_creative_mode.state;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class AbandonedToolState extends PersistentState {
    public static class AbandonedTool {
        public ItemStack stack;

        public AbandonedTool(ItemStack stack) {
            this.stack = stack;
        }

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.put("Stack", stack.writeNbt(new NbtCompound()));
            return nbt;
        }

        public static AbandonedTool fromNbt(NbtCompound nbt) {
            ItemStack stack = ItemStack.fromNbt(nbt.getCompound("Stack"));
            return new AbandonedTool(stack);
        }
    }

    public final List<AbandonedTool> abandonedTools = new ArrayList<>();
    public int totalAbandonedCount = 0;
    public int hatredValue = 0;
    public int currentStage = 1;
    public int remainsDroppedCount = 0;
    public boolean isResoluteDepartureActive = false;

    // 作呕与灭绝相关变量
    public List<Long> recentAnimalKills = new ArrayList<>();
    public long nauseaEndTime = 0;
    public boolean isNauseaEscalated = false;
    public List<Long> escalatedKills = new ArrayList<>();
    public int escalatedKillTotal = 0;
    public long extinctionEndTime = 0;

    public void addHatred(int amount) {
        this.hatredValue += amount;
        updateStage();
        this.markDirty();
    }

    public void updateStage() {
        int previousStage = this.currentStage;
        if (this.hatredValue >= 100 && this.currentStage < 2) {
            this.currentStage = 2;
        }
        if (this.currentStage > previousStage) {
            System.out.println("[True Creative Mode] The world's hatred grows. Reached Stage " + this.currentStage);
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (AbandonedTool tool : abandonedTools) {
            list.add(tool.toNbt());
        }
        nbt.put("AbandonedTools", list);
        nbt.putInt("TotalAbandonedCount", totalAbandonedCount);
        nbt.putInt("HatredValue", hatredValue);
        nbt.putInt("CurrentStage", currentStage);
        nbt.putInt("RemainsDroppedCount", remainsDroppedCount);
        nbt.putBoolean("IsResoluteDepartureActive", isResoluteDepartureActive);

        // 序列化作呕与灭绝数据
        NbtList recentKillsNbt = new NbtList();
        for (Long time : recentAnimalKills) recentKillsNbt.add(NbtLong.of(time));
        nbt.put("RecentAnimalKills", recentKillsNbt);
        
        nbt.putLong("NauseaEndTime", nauseaEndTime);
        nbt.putBoolean("IsNauseaEscalated", isNauseaEscalated);
        
        NbtList escalatedKillsNbt = new NbtList();
        for (Long time : escalatedKills) escalatedKillsNbt.add(NbtLong.of(time));
        nbt.put("EscalatedKills", escalatedKillsNbt);
        
        nbt.putInt("EscalatedKillTotal", escalatedKillTotal);
        nbt.putLong("ExtinctionEndTime", extinctionEndTime);
        
        return nbt;
    }

    public static AbandonedToolState fromNbt(NbtCompound nbt) {
        AbandonedToolState state = new AbandonedToolState();
        NbtList list = nbt.getList("AbandonedTools", 10);
        for (int i = 0; i < list.size(); i++) {
            state.abandonedTools.add(AbandonedTool.fromNbt(list.getCompound(i)));
        }
        state.totalAbandonedCount = nbt.getInt("TotalAbandonedCount");
        state.hatredValue = nbt.getInt("HatredValue");
        state.currentStage = nbt.contains("CurrentStage") ? nbt.getInt("CurrentStage") : 1;
        state.remainsDroppedCount = nbt.getInt("RemainsDroppedCount");
        state.isResoluteDepartureActive = nbt.getBoolean("IsResoluteDepartureActive");

        // 反序列化作呕与灭绝数据
        NbtList recentKillsNbt = nbt.getList("RecentAnimalKills", 4);
        for (int i = 0; i < recentKillsNbt.size(); i++) state.recentAnimalKills.add(recentKillsNbt.getLong(i));
        
        state.nauseaEndTime = nbt.getLong("NauseaEndTime");
        state.isNauseaEscalated = nbt.getBoolean("IsNauseaEscalated");
        
        NbtList escalatedKillsNbt = nbt.getList("EscalatedKills", 4);
        for (int i = 0; i < escalatedKillsNbt.size(); i++) state.escalatedKills.add(escalatedKillsNbt.getLong(i));
        
        state.escalatedKillTotal = nbt.getInt("EscalatedKillTotal");
        state.extinctionEndTime = nbt.getLong("ExtinctionEndTime");

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