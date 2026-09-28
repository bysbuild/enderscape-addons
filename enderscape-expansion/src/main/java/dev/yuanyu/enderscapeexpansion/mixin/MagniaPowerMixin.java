package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.MagniaPower;
import net.bunten.enderscape.block.MagniaBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MagniaBlock.class)
public abstract class MagniaPowerMixin extends Block {
   protected MagniaPowerMixin(Properties p) {
      super(p);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> b) {
      b.add(new Property[]{MagniaPower.POWER});
   }

   protected boolean hasAnalogOutputSignal(BlockState s) {
      return true;
   }

   protected int getAnalogOutputSignal(BlockState s, Level l, BlockPos p) {
      return (Integer)s.getValue(MagniaPower.POWER);
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      return (BlockState)this.defaultBlockState().setValue(MagniaPower.POWER, MagniaPower.strongest(this.defaultBlockState(), c.getLevel(), c.getClickedPos()));
   }

   protected void neighborChanged(BlockState s, Level l, BlockPos p, Block b, BlockPos from, boolean moving) {
      if (!l.isClientSide) {
         int power = MagniaPower.strongest(s, l, p);
         if (power != (Integer)s.getValue(MagniaPower.POWER)) {
            l.setBlockAndUpdate(p, (BlockState)s.setValue(MagniaPower.POWER, power));
         }
      }
   }
}
