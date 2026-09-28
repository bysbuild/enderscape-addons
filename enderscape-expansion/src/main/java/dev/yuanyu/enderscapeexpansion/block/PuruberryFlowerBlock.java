package dev.yuanyu.enderscapeexpansion.block;

import com.mojang.serialization.MapCodec;
import dev.yuanyu.enderscapeexpansion.ExpansionBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PuruberryFlowerBlock extends Block implements BonemealableBlock {
   public static final MapCodec<PuruberryFlowerBlock> CODEC = simpleCodec(PuruberryFlowerBlock::new);

   public PuruberryFlowerBlock(Properties settings) {
      super(settings);
   }

   public MapCodec<PuruberryFlowerBlock> codec() {
      return CODEC;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return box(2.0, 10.0, 2.0, 14.0, 16.0, 14.0);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState state2, LevelAccessor world, BlockPos pos, BlockPos pos2) {
      return !this.canSurvive(state, world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, state2, world, pos, pos2);
   }

   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
      return Block.canSupportCenter(world, pos.above(), Direction.DOWN) && !world.isWaterAt(pos) || this.attachedToVine(world, pos);
   }

   private boolean attachedToVine(LevelReader world, BlockPos pos) {
      return world.getBlockState(pos.above()).is((Block)ExpansionBlocks.PURUBERRY_VINE.get());
   }

   public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
      if (this.isBonemealSuccess(world, random, pos, state)) {
         this.performBonemeal(world, random, pos, state);
      }
   }

   public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
      return this.attachedToVine(world, pos);
   }

   public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
      return this.attachedToVine(world, pos) && random.nextFloat() > 0.8F;
   }

   public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
      BlockState unripe = ((Block)ExpansionBlocks.UNRIPE_PURUBERRY_BLOCK.get()).defaultBlockState();
      world.setBlock(pos, unripe, 3);
      SoundType group = unripe.getSoundType();
      world.playSound(null, pos, group.getPlaceSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 2.0F, group.getPitch() * 0.8F);
      world.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return 2;
   }
}
