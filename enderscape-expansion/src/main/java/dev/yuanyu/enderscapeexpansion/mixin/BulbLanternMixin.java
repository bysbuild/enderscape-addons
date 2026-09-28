package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.PurifyingPlants;
import net.bunten.enderscape.block.BulbLanternBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BulbLanternBlock.class)
public abstract class BulbLanternMixin extends LanternBlock {
   @ModifyArg(
      method = "<init>",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/block/LanternBlock;<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V"
      ),
      index = 0
   )
   private static Properties expansion$properties(Properties p) {
      return p.randomTicks();
   }

   protected BulbLanternMixin(Properties p) {
      super(p);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{PurifyingPlants.PHASE});
   }

   @Inject(method = "<init>", at = @At("RETURN"))
   private void expansion$default(Properties p, CallbackInfo ci) {
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(PurifyingPlants.PHASE, PurifyingPlants.Phase.INACTIVE));
   }

   public boolean isRandomlyTicking(BlockState state) {
      return state.getValue(PurifyingPlants.PHASE) == PurifyingPlants.Phase.INACTIVE;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      PurifyingPlants.tick(level, pos, state, true);
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      PurifyingPlants.tick(level, pos, state, false);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      PurifyingPlants.animate(state, level, pos, random);
   }
}
