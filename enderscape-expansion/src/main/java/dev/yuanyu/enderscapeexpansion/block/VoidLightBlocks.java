package dev.yuanyu.enderscapeexpansion.block;

import net.bunten.enderscape.registry.EnderscapeParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public final class VoidLightBlocks {
   private static void stars(Level level, RandomSource random, double x, double y, double z) {
      if (random.nextBoolean()) {
         level.addParticle(
            (ParticleOptions)EnderscapeParticles.VOID_STARS.get(),
            x + (random.nextDouble() - 0.5) * 0.4,
            y,
            z + (random.nextDouble() - 0.5) * 0.4,
            0.0,
            0.015,
            0.0
         );
      }
   }

   public static final class Standing extends TorchBlock {
      public Standing(Properties properties) {
         super(ParticleTypes.SOUL_FIRE_FLAME, properties);
      }

      public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
         VoidLightBlocks.stars(level, random, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5);
      }
   }

   public static final class Wall extends WallTorchBlock {
      public Wall(Properties properties) {
         super(ParticleTypes.SOUL_FIRE_FLAME, properties);
      }

      public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
         Direction back = ((Direction)state.getValue(FACING)).getOpposite();
         VoidLightBlocks.stars(level, random, pos.getX() + 0.5 + 0.27 * back.getStepX(), pos.getY() + 0.92, pos.getZ() + 0.5 + 0.27 * back.getStepZ());
      }
   }
}
