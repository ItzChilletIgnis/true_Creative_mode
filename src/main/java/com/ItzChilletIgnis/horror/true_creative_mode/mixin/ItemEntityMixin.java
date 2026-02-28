package com.ItzChilletIgnis.horror.true_creative_mode.mixin;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow public abstract ItemStack getStack();

    @Unique
    private boolean isRecorded = false;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.getWorld().isClient && !this.isRecorded && (source.isOf(DamageTypes.LAVA) || source.isOf(DamageTypes.IN_FIRE) || source.isOf(DamageTypes.CACTUS))) {
            ItemStack stack = this.getStack();
            if (stack.getItem() instanceof ToolItem && stack.getDamage() < stack.getMaxDamage()) {
                AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.getWorld());
                state.abandonedTools.add(new AbandonedToolState.AbandonedTool(stack.copy(), null, -1, this.getWorld().getTime(), false, null));
                state.totalAbandonedCount++;
                state.markDirty();
                this.isRecorded = true;
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!this.getWorld().isClient && !this.isRecorded) {
            ItemEntity entity = (ItemEntity) (Object) this;
            if (entity.getAge() >= 5990) { // 接近 5 分钟
                ItemStack stack = this.getStack();
                if (stack.getItem() instanceof ToolItem && stack.getDamage() < stack.getMaxDamage()) {
                    AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.getWorld());
                    state.abandonedTools.add(new AbandonedToolState.AbandonedTool(stack.copy(), null, -1, this.getWorld().getTime(), false, null));
                    state.totalAbandonedCount++;
                    state.markDirty();
                    this.isRecorded = true;
                }
            }
        }
    }
}