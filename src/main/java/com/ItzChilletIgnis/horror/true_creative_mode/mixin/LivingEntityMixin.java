package com.ItzChilletIgnis.horror.true_creative_mode.mixin;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"))
    private void onDropStack(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        if (!this.getWorld().isClient && (Object)this instanceof AnimalEntity) {
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.getWorld());
            if (state.nauseaEndTime > this.getWorld().getTime()) {
                if (stack.getItem().isFood()) {
                    NbtCompound nbt = stack.getOrCreateNbt();
                    nbt.putBoolean("true_creative_tainted", true);
                }
            }
        }
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void onEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClient && (Object)this instanceof PlayerEntity player) {
            if (stack.hasNbt() && stack.getNbt().getBoolean("true_creative_tainted")) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0));
                if (new Random().nextFloat() < 0.3f) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0));
                }
            }
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.getWorld().isClient && (Object)this instanceof AnimalEntity) {
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.getWorld());
            if (state.extinctionEndTime > this.getWorld().getTime()) {
                this.discard();
                cir.setReturnValue(false);
            }
        }
    }
}