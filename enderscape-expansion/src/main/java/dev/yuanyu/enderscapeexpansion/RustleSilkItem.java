package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.registry.EnderscapeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class RustleSilkItem extends Item {
   public RustleSilkItem(Properties properties) {
      super(properties);
   }

   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState state = level.getBlockState(pos);
      if ((state.is(Blocks.END_STONE) || state.is((Block)EnderscapeBlocks.VEILED_END_STONE.get())) && level.isEmptyBlock(pos.above())) {
         if (level instanceof ServerLevel server) {
            for (BlockPos target : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
               if (target.distSqr(pos) <= 4.0 && level.getBlockState(target).is(Blocks.END_STONE) && level.isEmptyBlock(target.above())) {
                  level.setBlockAndUpdate(target, ((Block)EnderscapeBlocks.VEILED_END_STONE.get()).defaultBlockState());
               }
            }

            level.setBlockAndUpdate(pos, ((Block)EnderscapeBlocks.VEILED_END_STONE.get()).defaultBlockState());
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 4, 0.5, 0.1, 0.5, 0.1);
            server.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
               context.getItemInHand().shrink(1);
            }

            if (context.getPlayer() != null) {
               context.getPlayer().awardStat(Stats.ITEM_USED.get(this));
            }
         }

         return InteractionResult.sidedSuccess(level.isClientSide());
      } else {
         return InteractionResult.PASS;
      }
   }
}
