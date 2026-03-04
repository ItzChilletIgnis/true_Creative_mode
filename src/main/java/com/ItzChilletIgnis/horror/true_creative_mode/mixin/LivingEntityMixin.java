package com.ItzChilletIgnis.horror.true_creative_mode.mixin;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    private boolean isRawMeat(Item item) {
        return item == Items.BEEF || item == Items.PORKCHOP || item == Items.CHICKEN || 
               item == Items.MUTTON || item == Items.RABBIT;
    }

    @Unique
    private boolean isCookedMeat(Item item) {
        return item == Items.COOKED_BEEF || item == Items.COOKED_PORKCHOP || item == Items.COOKED_CHICKEN || 
               item == Items.COOKED_MUTTON || item == Items.COOKED_RABBIT;
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void onEatFood(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClient && (Object)this instanceof PlayerEntity player) {
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) world);
            long currentTime = world.getTime();
            
            // 检查是否处于“作呕”或“灭绝”阶段
            if (state.nauseaEndTime > currentTime || state.extinctionEndTime > currentTime) {
                Item item = stack.getItem();
                boolean raw = isRawMeat(item);
                boolean cooked = isCookedMeat(item);

                if (raw || cooked) {
                    if (raw) {
                        // 生肉：反胃 14s (280 ticks)，饥饿 10s (200 ticks)
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 280, 0));
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 0));
                    } else {
                        // 熟肉：反胃 7s (140 ticks)
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 140, 0));
                    }

                    // 播放呕吐音效 (打嗝降调)
                    world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1.0F, 0.5F);
                }
            }
        }
    }
}