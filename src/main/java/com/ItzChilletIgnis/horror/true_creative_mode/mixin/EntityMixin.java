package com.ItzChilletIgnis.horror.true_creative_mode.mixin;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"))
    private void onDropStack(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        Entity entity = (Entity) (Object) this;
        if (!entity.getWorld().isClient && entity instanceof AnimalEntity) {
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) entity.getWorld());
            if (state.nauseaEndTime > entity.getWorld().getTime()) {
                if (stack.getItem().isFood()) {
                    NbtCompound nbt = stack.getOrCreateNbt();
                    nbt.putBoolean("true_creative_tainted", true);
                }
            }
        }
    }
}