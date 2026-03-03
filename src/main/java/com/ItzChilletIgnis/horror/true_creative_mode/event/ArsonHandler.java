package com.ItzChilletIgnis.horror.true_creative_mode.event;

import com.ItzChilletIgnis.horror.true_creative_mode.True_creative_mode;
import com.ItzChilletIgnis.horror.true_creative_mode.item.ModItems;
import com.ItzChilletIgnis.horror.true_creative_mode.state.AbandonedToolState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArsonHandler {
    private static final Random RANDOM = new Random();

    public static void register() {
        // 种植赎罪 (放置树苗)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient) {
                ItemStack stack = player.getStackInHand(hand);
                if (stack.getItem().toString().contains("sapling")) {
                     AbandonedToolState state = AbandonedToolState.getServerState((ServerWorld) world);
                     state.saplingPlantTimestamps.add(world.getTime());
                     state.markDirty();
                }
            }
            return ActionResult.PASS;
        });

        // 砍伐与触发
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient) return true;
            
            if (isLog(state)) {
                AbandonedToolState toolState = AbandonedToolState.getServerState((ServerWorld) world);
                long currentTime = world.getTime();

                toolState.logBreakTimestamps.removeIf(t -> t < currentTime - 24000);
                toolState.saplingPlantTimestamps.removeIf(t -> t < currentTime - 24000);

                toolState.logBreakTimestamps.add(currentTime);

                boolean isEmber = toolState.emberEndTime > currentTime;
                boolean isFire = toolState.fireEndTime > currentTime;
                boolean isAshfall = toolState.ashfallEndTime > currentTime;

                if (!isEmber && !isFire && !isAshfall) {
                    if (toolState.logBreakTimestamps.size() > 44 && toolState.saplingPlantTimestamps.size() < 4) {
                        toolState.emberEndTime = currentTime + 6000;
                        toolState.addHatred(3);
                        player.sendMessage(Text.literal("Take a deep breath").formatted(Formatting.WHITE), false);
                        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        isEmber = true;
                    }
                }
                else if (isEmber && !isFire) {
                    toolState.logsChoppedInEmber++;
                    if (toolState.logsChoppedInEmber >= 16) {
                        toolState.fireEndTime = currentTime + 6000;
                        toolState.emberEndTime = 0;
                        toolState.logsChoppedInEmber = 0;
                        toolState.addHatred(8);
                        isFire = true;
                        isEmber = false;

                        if (!toolState.hasDroppedEvidence) {
                            player.sendMessage(Text.literal("Do you remember it?").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
                            ItemStack evidence = new ItemStack(Items.FLINT_AND_STEEL);
                            evidence.setDamage(32);
                            evidence.setCustomName(Text.literal("Evidence").formatted(Formatting.GRAY, Formatting.ITALIC));
                            world.spawnEntity(new ItemEntity(world, player.getX(), player.getY(), player.getZ(), evidence));
                            toolState.hasDroppedEvidence = true;
                        }
                    }
                }
                else if (isFire && !isAshfall) {
                    toolState.logsChoppedInFire++;
                    if (toolState.logsChoppedInFire >= 4) {
                        toolState.ashfallEndTime = currentTime + 6000;
                        toolState.fireEndTime = 0;
                        toolState.logsChoppedInFire = 0;
                        toolState.addHatred(14);
                        isAshfall = true;
                        isFire = false;
                        
                        // 触发 Ashfall 时的文本
                        player.sendMessage(Text.literal("In the way, right?").formatted(Formatting.DARK_RED, Formatting.BOLD), false);

                        // 播放凋灵出生音效
                        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.AMBIENT, 1.0f, 1.0f);

                        // 资本清零与塞满凶器 (仅针对主物品栏)
                        List<ItemStack> mainInv = player.getInventory().main;
                        for (int i = 0; i < mainInv.size(); i++) {
                            ItemStack invStack = mainInv.get(i);
                            // 置换逻辑
                            if (invStack.isIn(ItemTags.LOGS) || invStack.isIn(ItemTags.PLANKS)) {
                                int count = invStack.getCount();
                                mainInv.set(i, new ItemStack(ModItems.ASHES, count));
                            }
                        }
                        // 填充逻辑 (再次遍历主物品栏)
                        for (int i = 0; i < mainInv.size(); i++) {
                            if (mainInv.get(i).isEmpty()) {
                                ItemStack weapon = new ItemStack(Items.FLINT_AND_STEEL);
                                weapon.setDamage(61); // 仅剩 3 点耐久
                                weapon.setCustomName(Text.literal("Gift").formatted(Formatting.GRAY, Formatting.ITALIC)); // 命名为 Gift
                                mainInv.set(i, weapon);
                            }
                        }
                        // 检查副手
                        if (player.getOffHandStack().isEmpty()) {
                            ItemStack weapon = new ItemStack(Items.FLINT_AND_STEEL);
                            weapon.setDamage(61);
                            weapon.setCustomName(Text.literal("Gift").formatted(Formatting.GRAY, Formatting.ITALIC));
                            player.setStackInHand(net.minecraft.util.Hand.OFF_HAND, weapon);
                        }

                        syncAshfallState((ServerWorld) world, true);
                    }
                }

                toolState.markDirty();

                if (isEmber || isFire || isAshfall) {
                    if (RANDOM.nextFloat() < 0.25f) {
                        world.breakBlock(pos, false);
                        world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.ASHES)));
                        return false; 
                    }
                }
            }
            return true;
        });

        // Tick 监听逻辑
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (world.getTime() % 20 == 0) { // 每秒执行一次
                AbandonedToolState state = AbandonedToolState.getServerState(world);
                long currentTime = world.getTime();
                boolean isFire = state.fireEndTime > currentTime;
                boolean isAshfall = state.ashfallEndTime > currentTime;
                
                syncAshfallState(world, isAshfall);

                if (isFire || isAshfall) {
                    for (PlayerEntity player : world.getPlayers()) {
                        BlockPos playerPos = player.getBlockPos();
                        int radius = isAshfall ? 8 : 5;
                        List<BlockPos> flammableBlocks = new ArrayList<>();

                        for (int x = -radius; x <= radius; x++) {
                            for (int y = -radius; y <= radius; y++) {
                                for (int z = -radius; z <= radius; z++) {
                                    BlockPos pos = playerPos.add(x, y, z);
                                    BlockState blockState = world.getBlockState(pos);
                                    if (isLog(blockState) || blockState.isIn(BlockTags.LEAVES)) {
                                        flammableBlocks.add(pos);
                                    }
                                }
                            }
                        }

                        if (isFire && !isAshfall) {
                            for (int i = 0; i < 3 && !flammableBlocks.isEmpty(); i++) {
                                int index = RANDOM.nextInt(flammableBlocks.size());
                                BlockPos targetPos = flammableBlocks.remove(index);
                                igniteAround(world, targetPos);
                            }
                        }
                        else if (isAshfall) {
                            for (BlockPos targetPos : flammableBlocks) {
                                igniteAround(world, targetPos);
                            }

                            if (!player.isOnFire()) {
                                player.setOnFireFor(10);
                            }

                            // Tick 期间的置换也仅针对主物品栏
                            List<ItemStack> mainInv = player.getInventory().main;
                            for (int i = 0; i < mainInv.size(); i++) {
                                ItemStack stack = mainInv.get(i);
                                if (stack.isIn(ItemTags.LOGS) || stack.isIn(ItemTags.PLANKS)) {
                                    int count = stack.getCount();
                                    mainInv.set(i, new ItemStack(ModItems.ASHES, count));
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private static void igniteAround(ServerWorld world, BlockPos targetPos) {
        int firesPlaced = 0;
        for (Direction direction : Direction.values()) {
            if (firesPlaced >= 2) break;
            
            BlockPos firePos = targetPos.offset(direction);
            if (world.isAir(firePos)) {
                BlockState fireState = AbstractFireBlock.getState(world, firePos);
                world.setBlockState(firePos, fireState);
                firesPlaced++;
            }
        }
    }

    private static void syncAshfallState(ServerWorld world, boolean isAshfall) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isAshfall);
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, True_creative_mode.PACKET_SYNC_ASHFALL_STATE, buf);
        }
    }

    private static boolean isLog(BlockState state) {
        return state.isIn(BlockTags.LOGS);
    }
}