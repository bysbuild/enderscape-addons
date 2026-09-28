package dev.yuanyu.enderscapeexpansion.block;

import com.mojang.serialization.MapCodec;
import dev.yuanyu.enderscapeexpansion.EndHaven;
import dev.yuanyu.enderscapeexpansion.ExpansionAdvancements;
import dev.yuanyu.enderscapeexpansion.HavenBlockEntity;
import java.util.Locale;
import java.util.Optional;
import net.bunten.enderscape.registry.EnderscapeItems;
import net.bunten.enderscape.registry.EnderscapeParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

public final class EndHavenCore extends BaseEntityBlock {
   public static final EnumProperty<EndHavenCore.State> STATE = EnumProperty.create("state", EndHavenCore.State.class);

   public EndHavenCore(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(STATE, EndHavenCore.State.INACTIVE));
   }

   protected MapCodec<EndHavenCore> codec() {
      return simpleCodec(EndHavenCore::new);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{STATE});
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new HavenBlockEntity(pos, state);
   }

   protected ItemInteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
   ) {
      if (state.getValue(STATE) == EndHavenCore.State.INACTIVE && stack.is((Item)EnderscapeItems.NEBULITE.get())) {
         if (!level.isClientSide) {
            stack.consume(1, player);
            level.setBlock(pos, (BlockState)state.setValue(STATE, EndHavenCore.State.ACTIVE), 3);
            HavenBlockEntity.sound(level, pos, "activate");
            ExpansionAdvancements.award(player, "activate_end_haven");
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, final BlockPos pos, Player player, BlockHitResult hit) {
      if (state.getValue(STATE) != EndHavenCore.State.ACTIVE) {
         return InteractionResult.PASS;
      } else {
         if (!level.isClientSide) {
            if (level.dimension() != Level.END) {
               level.removeBlock(pos, false);
               final boolean water = Plane.HORIZONTAL.stream().<BlockPos>map(pos::relative).anyMatch(p -> {
                  FluidState fluid = level.getFluidState(p);
                  return fluid.is(FluidTags.WATER) && (fluid.isSource() || fluid.getAmount() >= 2 && !level.getFluidState(p.below()).is(FluidTags.WATER));
               }) || level.getFluidState(pos.above()).is(FluidTags.WATER);
               var calculator = new ExplosionDamageCalculator() {
                  public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter getter, BlockPos test, BlockState block, FluidState fluid) {
                     return water && test.equals(pos)
                        ? Optional.of(Blocks.WATER.getExplosionResistance())
                        : super.getBlockExplosionResistance(explosion, getter, test, block, fluid);
                  }
               };
               level.explode(
                  null,
                  level.damageSources().badRespawnPointExplosion(pos.getCenter()),
                  calculator,
                  pos.getX() + 0.5,
                  pos.getY() + 0.5,
                  pos.getZ() + 0.5,
                  5.0F,
                  true,
                  ExplosionInteraction.BLOCK
               );
            } else if (player instanceof ServerPlayer server) {
               EndHaven.bind(server, pos);
               server.sendSystemMessage(Component.translatable("block.enderscape.end_haven_core.set_respawn_point"));
               HavenBlockEntity.sound(level, pos, "set_spawn");
            }
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (state.getValue(STATE) == EndHavenCore.State.ACTIVE) {
         for (int i = 0; i < 1 + random.nextInt(4); i++) {
            double x = pos.getX() - 0.5 + random.nextDouble() * 2.0;
            double z = pos.getZ() - 0.5 + random.nextDouble() * 2.0;
            level.addParticle(
               (ParticleOptions)EnderscapeParticles.END_PORTAL_STARS.get(),
               x,
               pos.getY() + 0.2 + random.nextDouble() * 0.6,
               z,
               (pos.getX() + 0.5 - x) * 0.02,
               0.0,
               (pos.getZ() + 0.5 - z) * 0.02
            );
         }
      }
   }

   public static enum State implements StringRepresentable {
      INACTIVE,
      ACTIVE;

      public String getSerializedName() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }
}
