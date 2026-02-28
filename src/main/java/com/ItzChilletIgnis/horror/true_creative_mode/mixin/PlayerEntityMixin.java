package com.ItzChilletIgnis.horror.true_creative_mode.mixin;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void onDropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (!this.getWorld().isClient()) {
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.getWorld());
            if (state.isResoluteDepartureActive) {
                if (stack.getItem() instanceof ToolItem || stack.getItem() instanceof ArmorItem) {
                    // 播放损坏音效
                    this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    
                    // 造成 1 点魔法伤害
                    this.damage(this.getDamageSources().magic(), 1.0f);
                    
                    // 物品消失
                    stack.setCount(0);
                    
                    // 取消掉落
                    cir.setReturnValue(null);
                }
            }
        }
    }
}