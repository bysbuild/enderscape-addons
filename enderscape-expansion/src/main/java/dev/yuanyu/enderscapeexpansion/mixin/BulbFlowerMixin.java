package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.PurifyingPlants;
import java.util.List;
import net.bunten.enderscape.block.BulbFlowerBlock;
import net.bunten.enderscape.block.DirectionalPlantBlock;
import net.bunten.enderscape.block.properties.DirectionProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.SuspiciousStewEffects.Entry;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BulbFlowerBlock.class)
public abstract class BulbFlowerMixin extends DirectionalPlantBlock implements SuspiciousEffectHolder {
   @ModifyArg(
      method = "<init>",
      at = @At(
         value = "INVOKE",
         target = "Lnet/bunten/enderscape/block/DirectionalPlantBlock;<init>(Lnet/bunten/enderscape/block/properties/DirectionProperties;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V"
      ),
      index = 1
   )
   private static Properties expansion$properties(Properties p) {
      return p.randomTicks().lightLevel(s -> {
         return switch ((PurifyingPlants.Phase)s.getValue(PurifyingPlants.PHASE)) {
            case ACTIVE -> 11;
            case CHARGING -> 9;
            default -> 7;
         };
      });
   }

   protected BulbFlowerMixin(DirectionProperties directions, Properties properties) {
      super(directions, properties);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{PurifyingPlants.PHASE});
   }

   @Inject(method = "<init>", at = @At("RETURN"))
   private void expansion$default(Properties p, CallbackInfo ci) {
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(PurifyingPlants.PHASE, PurifyingPlants.Phase.POWERLESS));
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = super.getStateForPlacement(context);
      return state == null
         ? null
         : (BlockState)state.setValue(PurifyingPlants.PHASE, PurifyingPlants.dormant(context.getLevel(), context.getClickedPos(), state));
   }

   protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      BlockState result = super.updateShape(state, direction, neighbor, level, pos, neighborPos);
      return !(result.is(this) && level instanceof Level world)
            || result.getValue(PurifyingPlants.PHASE) != PurifyingPlants.Phase.POWERLESS
               && result.getValue(PurifyingPlants.PHASE) != PurifyingPlants.Phase.INACTIVE
         ? result
         : (BlockState)result.setValue(PurifyingPlants.PHASE, PurifyingPlants.dormant(world, pos, result));
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

   public SuspiciousStewEffects getSuspiciousEffects() {
      return new SuspiciousStewEffects(List.of(new Entry(MobEffects.GLOWING, 140)));
   }
}
