package dev.yuanyu.enderscapeexpansion.block;

import net.bunten.enderscape.block.properties.StateProperties;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractVineBlock extends Block {
   public static final BooleanProperty ATTACHED = StateProperties.ATTACHED;
   public static final IntegerProperty AGE = StateProperties.AGE_15;
   public static final int MAX_AGE = 15;

   public AbstractVineBlock(Properties settings) {
      super(settings);
   }

   abstract TagKey<Block> getPreferredBlocks();

   protected static boolean getAttached(BlockState state) {
      return (Boolean)state.getValue(ATTACHED);
   }

   protected static boolean isBody(BlockState state) {
      return getAttached(state);
   }

   protected static int getAge(BlockState state) {
      return (Integer)state.getValue(AGE);
   }

   protected static int getPlacementAge(RandomSource random) {
      return Mth.nextInt(random, 1, 9);
   }

   protected void growVine(ServerLevel world, RandomSource random, BlockPos pos, BlockState state, float cycleChance) {
      BlockState down = (BlockState)state.setValue(ATTACHED, false);
      BlockState vine = (BlockState)state.setValue(ATTACHED, true);
      if (random.nextFloat() <= cycleChance && getAge(state) < 15) {
         down = (BlockState)down.cycle(AGE);
         vine = (BlockState)vine.cycle(AGE);
      }

      world.setBlockAndUpdate(pos.below(), down);
      world.setBlockAndUpdate(pos, vine);
   }

   protected BlockState state(BlockState state, boolean attached, int age) {
      return (BlockState)((BlockState)state.setValue(ATTACHED, attached)).setValue(AGE, age);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = context.getLevel().getBlockState(context.getClickedPos().below());
      return state.is(this) ? this.defaultBlockState() : (BlockState)this.defaultBlockState().setValue(AGE, getPlacementAge(context.getLevel().getRandom()));
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
      return getAttached(state) ? box(2.5, 0.0, 2.5, 13.5, 16.0, 13.5) : box(2.5, 2.5, 2.5, 13.5, 16.0, 13.5);
   }

   protected BlockState updateShape(BlockState state, Direction direction, BlockState state2, LevelAccessor world, BlockPos pos, BlockPos pos2) {
      if (!state.canSurvive(world, pos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         BlockState down = world.getBlockState(pos.below());
         return down.is(this) ? this.state(state, true, (Integer)down.getValue(AGE)) : this.state(state, false, getAge(state));
      }
   }

   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
      BlockState above = world.getBlockState(pos.above());
      return world.getFluidState(pos).isEmpty() && (above.is(this) || above.isFaceSturdy(world, pos, Direction.DOWN) && above.is(this.getPreferredBlocks()));
   }

   public boolean isPathfindable(BlockState state, PathComputationType type) {
      return type == PathComputationType.AIR && !this.hasCollision || super.isPathfindable(state, type);
   }

   protected ItemInteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result
   ) {
      if (stack.getItem() instanceof ShearsItem && getAge(state) < 15 && !getAttached(state)) {
         player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
         if (player instanceof ServerPlayer server) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(server, pos, stack);
         }

         level.playSound(player, pos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
         level.setBlockAndUpdate(pos, this.state(state, false, 15));
         stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
         return ItemInteractionResult.SUCCESS;
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }
}
