package com.ItzChilletIgnis.horror.true_creative_mode.mixin;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends BlockEntity {
    public LootableContainerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, net.minecraft.block.BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "onOpen", at = @At("HEAD"))
    private void onOpen(PlayerEntity player, CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            LootableContainerBlockEntity container = (LootableContainerBlockEntity) (Object) this;
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.getWorld());
            
            // 检查是否有被标记为“老友”的物品在容器中
            for (int i = 0; i < container.size(); i++) {
                ItemStack stack = container.getStack(i);
                if (stack.getItem() instanceof ToolItem) {
                    // 逻辑：如果该物品已被“重逢”掉落，则清空原件
                    // 这里需要更精细的匹配逻辑，目前先实现基础的记录
                }
            }
        }
    }
}