package dev.yuanyu.enderscapeexpansion.block;

import com.mojang.serialization.MapCodec;
import dev.yuanyu.enderscapeexpansion.ExpansionBlocks;
import dev.yuanyu.enderscapeexpansion.ExpansionTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class PuruberryVine extends AbstractVineBlock implements BonemealableBlock {
   public static final MapCodec<PuruberryVine> CODEC = simpleCodec(PuruberryVine::new);

   public PuruberryVine(Properties settings) {
      super(settings);
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(ATTACHED, false)).setValue(AGE, 0));
   }

   public MapCodec<PuruberryVine> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ATTACHED, AGE});
   }

   @Override
   public TagKey<Block> getPreferredBlocks() {
      return ExpansionTags.SUPPORTS_PURUBERRY_VINE;
   }

   @Override
   public BlockState updateShape(BlockState state, Direction direction, BlockState state2, LevelAccessor world, BlockPos pos, BlockPos pos2) {
      BlockState down = world.getBlockState(pos.below());
      return state.canSurvive(world, pos) && down.is(ExpansionTags.PURUBERRY_VINE_SUPPORTS)
         ? this.state(state, true, getAge(state))
         : super.updateShape(state, direction, state2, world, pos, pos2);
   }

   @Override
   public boolean isPathfindable(BlockState state, PathComputationType type) {
      return type == PathComputationType.AIR && !this.hasCollision || super.isPathfindable(state, type);
   }

   public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
      if (random.nextInt(12) == 0 && this.isBonemealSuccess(world, random, pos, state)) {
         this.performBonemeal(world, random, pos, state);
      }
   }

   public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
      for (BlockPos var4 = isBody(state) ? pos.below() : pos; var4.getY() > world.getMinBuildHeight(); var4 = var4.below()) {
         if (world.getBlockState(var4).is(this) && world.getBlockState(var4.below()).isAir()) {
            return true;
         }
      }

      return false;
   }

   public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
      return true;
   }

   public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
      for (BlockPos var9 = isBody(state) ? pos.below() : pos; var9.getY() > world.getMinBuildHeight(); var9 = var9.below()) {
         BlockState state2 = world.getBlockState(var9);
         if (state2.is(this)) {
            BlockState down = world.getBlockState(var9.below());
            if ((Integer)state2.getValue(AGE) == 15) {
               if (down.isAir()) {
                  world.setBlockAndUpdate(var9, this.state(state2, true, 15));
                  BlockState cycleStage = ((Block)ExpansionBlocks.PURUBERRY_FLOWER.get()).defaultBlockState();
                  world.setBlock(var9.below(), cycleStage, 3);
                  SoundType group = cycleStage.getSoundType();
                  world.playSound(null, var9, group.getPlaceSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 2.0F, group.getPitch() * 0.8F);
                  break;
               }
            } else if (down.isAir()) {
               this.growVine(world, random, var9, state2, 1.0F);
               break;
            }
         }
      }
   }
}
