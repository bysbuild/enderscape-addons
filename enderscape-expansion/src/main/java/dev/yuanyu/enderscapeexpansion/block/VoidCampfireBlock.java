package dev.yuanyu.enderscapeexpansion.block;

import dev.yuanyu.enderscapeexpansion.VoidSystem;
import net.bunten.enderscape.registry.EnderscapeParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public final class VoidCampfireBlock extends CampfireBlock {
   public VoidCampfireBlock(Properties properties) {
      super(false, 0, properties);
   }

   protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if (!level.isClientSide && (Boolean)state.getValue(LIT) && entity instanceof LivingEntity living) {
         living.hurt(VoidSystem.damage(living, "void_campfire"), 1.0F);
      }
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if ((Boolean)state.getValue(LIT)) {
         if (random.nextInt(10) == 0) {
            level.playLocalSound(
               pos.getX() + 0.5,
               pos.getY() + 0.5,
               pos.getZ() + 0.5,
               SoundEvents.CAMPFIRE_CRACKLE,
               SoundSource.BLOCKS,
               0.5F + random.nextFloat(),
               random.nextFloat() * 0.7F + 0.6F,
               false
            );
         }

         for (int i = 0; i < 2; i++) {
            level.addParticle(
               (ParticleOptions)EnderscapeParticles.VOID_STARS.get(),
               pos.getX() + 0.2 + random.nextDouble() * 0.6,
               pos.getY() + 0.35 + random.nextDouble() * 0.3,
               pos.getZ() + 0.2 + random.nextDouble() * 0.6,
               0.0,
               0.04 + random.nextDouble() * 0.02,
               0.0
            );
         }
      }
   }

   protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
   }

   protected ItemInteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
   ) {
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return null;
   }
}
