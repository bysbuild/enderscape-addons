package dev.yuanyu.enderscapeexpansion.block;

import dev.yuanyu.enderscapeexpansion.MagniaPower;
import java.util.Locale;
import net.bunten.enderscape.block.MagniaBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class BlisteredMagnia extends Block {
   public static final EnumProperty<BlisteredMagnia.Polarity> POLARITY = EnumProperty.create("polarity", BlisteredMagnia.Polarity.class);

   public BlisteredMagnia(Properties p) {
      super(p);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(POLARITY, BlisteredMagnia.Polarity.NONE));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> b) {
      b.add(new Property[]{POLARITY});
   }

   public static BlisteredMagnia.Polarity select(BlockGetter l, BlockPos p) {
      int a = 0;
      int r = 0;

      for (Direction d : Direction.values()) {
         BlockState s = l.getBlockState(p.relative(d));
         if (s.getBlock() instanceof MagniaBlock) {
            if (MagniaPower.polarity(s).equals("alluring")) {
               a++;
            } else {
               r++;
            }
         }
      }

      return a + r == 0 ? BlisteredMagnia.Polarity.NONE : (a >= r ? BlisteredMagnia.Polarity.ALLURING : BlisteredMagnia.Polarity.REPULSIVE);
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      return (BlockState)this.defaultBlockState().setValue(POLARITY, select(c.getLevel(), c.getClickedPos()));
   }

   protected BlockState updateShape(BlockState s, Direction d, BlockState other, LevelAccessor l, BlockPos p, BlockPos from) {
      if (select(l, p) != s.getValue(POLARITY)) {
         l.scheduleTick(p, this, 5);
      }

      return s;
   }

   protected void tick(BlockState s, ServerLevel l, BlockPos p, RandomSource random) {
      BlisteredMagnia.Polarity polarity = select(l, p);
      if (polarity != s.getValue(POLARITY)) {
         l.setBlockAndUpdate(p, (BlockState)s.setValue(POLARITY, polarity));
         MagniaSounds.play(l, p, "blistered_magnia." + (polarity == BlisteredMagnia.Polarity.NONE ? "power_off" : "power_on"));
      }

      l.updateNeighborsAt(p, this);
   }

   public static enum Polarity implements StringRepresentable {
      NONE,
      ALLURING,
      REPULSIVE;

      public String getSerializedName() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }
}
