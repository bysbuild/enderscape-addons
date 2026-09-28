package dev.yuanyu.enderscapeexpansion.block;

import com.mojang.serialization.MapCodec;
import dev.yuanyu.enderscapeexpansion.ExpansionBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public class RipePuruberryBlock extends Block {
   public static final MapCodec<RipePuruberryBlock> CODEC = simpleCodec(RipePuruberryBlock::new);

   public RipePuruberryBlock(Properties settings) {
      super(settings);
   }

   public MapCodec<RipePuruberryBlock> codec() {
      return CODEC;
   }

   protected int getFallDelay() {
      return 2;
   }

   protected boolean canFall(Level world, BlockPos pos) {
      boolean bl = FallingBlock.isFree(world.getBlockState(pos.below())) && world.getBlockState(pos.above()).getBlock() != ExpansionBlocks.PURUBERRY_VINE.get();
      if (pos.getY() < world.getMinBuildHeight()) {
         bl = false;
      }

      return bl;
   }

   public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
      world.scheduleTick(pos, this, this.getFallDelay());
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState state2, LevelAccessor world, BlockPos pos, BlockPos pos2) {
      world.scheduleTick(pos, this, this.getFallDelay());
      return super.updateShape(state, direction, state2, world, pos, pos2);
   }

   public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
      if (this.canFall(world, pos)) {
         FallingBlockEntity.fall(world, pos, state);
      }
   }

   public void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
      if (projectile.getType().is(EntityTypeTags.IMPACT_PROJECTILES)) {
         world.destroyBlock(hit.getBlockPos(), true, projectile);
      }
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return 6;
   }
}
