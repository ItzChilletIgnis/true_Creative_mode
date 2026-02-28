package com.ItzChilletIgnis.horror.true_creative_mode.mixin;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LockableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends BlockEntity implements Inventory {
    public LootableContainerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, net.minecraft.block.BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "onOpen", at = @At("HEAD"))
    private void onOpen(PlayerEntity player, CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.getWorld());
            for (int i = 0; i < this.size(); i++) {
                ItemStack stack = this.getStack(i);
                if (stack.hasNbt() && stack.getNbt().contains("true_creative_id")) {
                    UUID uuid = stack.getNbt().getUuid("true_creative_id");
                    AbandonedToolState.AbandonedTool tool = state.getToolByUUID(uuid);
                    if (tool != null && tool.reunited) {
                        this.setStack(i, ItemStack.EMPTY);
                        state.abandonedTools.remove(tool);
                        state.markDirty();
                    }
                }
            }
        }
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void onClose(PlayerEntity player, CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.getWorld());
            for (int i = 0; i < this.size(); i++) {
                ItemStack stack = this.getStack(i);
                if (stack.getItem() instanceof ToolItem) {
                    NbtCompound nbt = stack.getOrCreateNbt();
                    UUID uuid;
                    if (!nbt.contains("true_creative_id")) {
                        uuid = UUID.randomUUID();
                        nbt.putUuid("true_creative_id", uuid);
                    } else {
                        uuid = nbt.getUuid("true_creative_id");
                    }

                    AbandonedToolState.AbandonedTool tool = state.getToolByUUID(uuid);
                    if (tool == null) {
                        tool = new AbandonedToolState.AbandonedTool(stack.copy(), this.getPos(), i, this.getWorld().getTime(), true, player.getUuid());
                        tool.uuid = uuid;
                        state.abandonedTools.add(tool);
                    } else {
                        tool.containerPos = this.getPos();
                        tool.slot = i;
                        tool.timestamp = this.getWorld().getTime();
                        tool.inContainer = true;
                    }
                    state.markDirty();
                }
            }
        }
    }
}