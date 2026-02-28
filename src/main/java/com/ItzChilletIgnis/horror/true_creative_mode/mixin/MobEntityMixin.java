package com.ItzChilletIgnis.horror.true_creative_mode.mixin;

import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
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
        if ((Object) this instanceof AnimalEntity) {
            PathAwareEntity pathAware = (PathAwareEntity) (Object) this;
            this.goalSelector.add(0, new CustomFleeGoal(pathAware));
            this.goalSelector.add(1, new StareAndBackGoal(pathAware));
        }
    }

    private class CustomFleeGoal extends Goal {
        private final PathAwareEntity mob;
        private PlayerEntity target;
        private Vec3d safePos;

        public CustomFleeGoal(PathAwareEntity mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (this.mob.getWorld().isClient) return false;
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.mob.getWorld());
            if (state.nauseaEndTime > this.mob.getWorld().getTime() && !state.isNauseaEscalated) {
                this.target = this.mob.getWorld().getClosestPlayer(this.mob, 16.0);
                if (this.target != null) {
                    this.safePos = NoPenaltyTargeting.findFrom(this.mob, 16, 7, this.target.getPos());
                    return this.safePos != null;
                }
            }
            return false;
        }

        @Override
        public void start() {
            if (this.safePos != null) {
                this.mob.getNavigation().startMovingTo(this.safePos.x, this.safePos.y, this.safePos.z, 1.5);
            }
        }

        @Override
        public boolean shouldContinue() {
            return !this.mob.getNavigation().isIdle();
        }
    }

    private class StareAndBackGoal extends Goal {
        private final PathAwareEntity mob;
        private PlayerEntity target;
        private int pathUpdateCountdown = 0;

        public StareAndBackGoal(PathAwareEntity mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (this.mob.getWorld().isClient) return false;
            AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) this.mob.getWorld());
            if (state.isNauseaEscalated && state.nauseaEndTime > this.mob.getWorld().getTime()) {
                this.target = this.mob.getWorld().getClosestPlayer(this.mob, 16.0);
                return this.target != null;
            }
            return false;
        }

        @Override
        public void tick() {
            if (this.target != null) {
                // 强制接管视角
                this.mob.getLookControl().lookAt(this.target, 30.0F, 30.0F);
                
                if (--this.pathUpdateCountdown <= 0) {
                    this.pathUpdateCountdown = 10; // 半秒寻路一次
                    
                    Vec3d reverseDirection = this.mob.getPos().subtract(this.target.getPos()).normalize();
                    Vec3d retreatPos = this.mob.getPos().add(reverseDirection.multiply(3.0));
                    
                    this.mob.getNavigation().startMovingTo(retreatPos.x, retreatPos.y, retreatPos.z, 1.2);
                }
            }
        }

        @Override
        public void stop() {
            this.target = null;
            this.mob.getNavigation().stop();
        }
    }
}