package dev.yuanyu.enderscapeexpansion.block;

import net.bunten.enderscape.block.properties.MagniaType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class PolarizedMagnia extends Block {
   public static final EnumProperty<MagniaType> POLARITY = EnumProperty.create("polarity", MagniaType.class);
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   public PolarizedMagnia(Properties p) {
      super(p);
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(POLARITY, MagniaType.ALLURING)).setValue(POWERED, false));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> b) {
      b.add(new Property[]{POLARITY, POWERED});
   }

   protected void onPlace(BlockState s, Level l, BlockPos p, BlockState old, boolean moving) {
      if (!old.is(this)) {
         this.update(s, l, p);
      }
   }

   protected void neighborChanged(BlockState s, Level l, BlockPos p, Block b, BlockPos from, boolean moving) {
      this.update(s, l, p);
   }

   private void update(BlockState s, Level l, BlockPos p) {
      if (!l.isClientSide) {
         boolean powered = l.hasNeighborSignal(p);
         if (powered != (Boolean)s.getValue(POWERED)) {
            if (powered) {
               s = (BlockState)s.cycle(POLARITY);
               MagniaSounds.play(l, p, "polarized_magnia.swap_polarity");
            }

            l.setBlockAndUpdate(p, (BlockState)s.setValue(POWERED, powered));
         }
      }
   }
}
