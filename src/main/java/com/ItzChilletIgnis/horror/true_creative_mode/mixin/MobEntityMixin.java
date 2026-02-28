package com.ItzChilletIgnis.horror.true_creative_mode.mixin;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    @Shadow @Final protected GoalSelector goalSelector;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void onInitGoals(CallbackInfo ci) {
        if ((Object) this instanceof AnimalEntity animal) {
            this.goalSelector.add(0, new CustomFleeGoal(animal));
            this.goalSelector.add(0, new StareAndBackGoal(animal));
        }
    }

    private class CustomFleeGoal extends Goal {
        private final MobEntity mob;
        private PlayerEntity target;

        public CustomFleeGoal(MobEntity mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (this.mob.getWorld().isClient) return false;
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.mob.getWorld());
            if (state.nauseaEndTime > this.mob.getWorld().getTime() && !state.isNauseaEscalated) {
                this.target = this.mob.getWorld().getClosestPlayer(this.mob, 16.0);
                return this.target != null;
            }
            return false;
        }

        @Override
        public void tick() {
            if (this.target != null && this.mob.squaredDistanceTo(this.target) < 256.0) {
                if (this.mob.age % 5 == 0) {
                    Vec3d away = this.mob.getPos().subtract(this.target.getPos()).normalize().multiply(8.0);
                    this.mob.getNavigation().startMovingTo(this.mob.getX() + away.x, this.mob.getY(), this.mob.getZ() + away.z, 1.5);
                }
            }
        }
    }

    private class StareAndBackGoal extends Goal {
        private final AnimalEntity animal;
        private PlayerEntity target;

        public StareAndBackGoal(AnimalEntity animal) {
            this.animal = animal;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (this.animal.getWorld().isClient) return false;
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.animal.getWorld());
            if (state.isNauseaEscalated && state.nauseaEndTime > this.animal.getWorld().getTime()) {
                this.target = this.animal.getWorld().getClosestPlayer(this.animal, 16.0);
                return this.target != null;
            }
            return false;
        }

        @Override
        public void tick() {
            if (this.target != null) {
                this.animal.getLookControl().lookAt(this.target, 30.0F, 30.0F);
                if (this.animal.squaredDistanceTo(this.target) < 36.0) {
                    if (this.animal.age % 10 == 0) {
                        Vec3d away = this.animal.getPos().subtract(this.target.getPos()).normalize().multiply(4.0);
                        this.animal.getNavigation().startMovingTo(this.animal.getX() + away.x, this.animal.getY(), this.animal.getZ() + away.z, 1.2);
                    }
                } else {
                    this.animal.getNavigation().stop();
                }
            }
        }
    }
}