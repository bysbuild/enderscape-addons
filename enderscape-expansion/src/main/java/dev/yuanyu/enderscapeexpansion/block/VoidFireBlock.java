package dev.yuanyu.enderscapeexpansion.block;

import com.mojang.serialization.MapCodec;
import dev.yuanyu.enderscapeexpansion.Expansion;
import dev.yuanyu.enderscapeexpansion.VoidSystem;
import net.bunten.enderscape.registry.EnderscapeParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public final class VoidFireBlock extends BaseFireBlock {
   public static final MapCodec<VoidFireBlock> CODEC = simpleCodec(VoidFireBlock::new);

   public VoidFireBlock(Properties properties) {
      super(properties, 1.0F);
   }

   public MapCodec<VoidFireBlock> codec() {
      return CODEC;
   }

   public static boolean supports(BlockState state) {
      return state.is(TagKey.create(Registries.BLOCK, Expansion.id("void_fire_base_blocks")));
   }

   protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos below = pos.below();
      return supports(level.getBlockState(below)) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
   }

   protected BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      return this.canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
   }

   protected boolean canBurn(BlockState state) {
      return false;
   }

   protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      entity.clearFire();
      if (!level.isClientSide && entity instanceof LivingEntity living) {
         VoidSystem.corrupt(living, 90);
         living.hurt(VoidSystem.damage(living, "in_void_fire"), 1.0F);
      }
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      for (int i = 0; i < 3; i++) {
         level.addParticle(
            (ParticleOptions)EnderscapeParticles.VOID_STARS.get(),
            pos.getX() + random.nextDouble(),
            pos.getY() + 0.35 + random.nextDouble() * 0.3,
            pos.getZ() + random.nextDouble(),
            0.0,
            0.04 + random.nextDouble() * 0.02,
            0.0
         );
      }
   }
}
