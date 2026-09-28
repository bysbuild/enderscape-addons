package dev.yuanyu.enderscapeexpansion.block;

import com.mojang.serialization.MapCodec;
import dev.yuanyu.enderscapeexpansion.RadioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MagniaRadio extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty PLAYING = BooleanProperty.create("is_playing");
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final MapCodec<MagniaRadio> CODEC = simpleCodec(MagniaRadio::new);

   public MagniaRadio(Properties p) {
      super(p);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(ENABLED, false)).setValue(POWERED, false))
                  .setValue(PLAYING, false))
               .setValue(WATERLOGGED, false))
            .setValue(FACING, Direction.NORTH)
      );
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> b) {
      b.add(new Property[]{ENABLED, POWERED, PLAYING, WATERLOGGED, FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      return (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(ENABLED, true))
               .setValue(FACING, c.getHorizontalDirection().getOpposite()))
            .setValue(WATERLOGGED, c.getLevel().getFluidState(c.getClickedPos()).is(Fluids.WATER)))
         .setValue(POWERED, c.getLevel().hasNeighborSignal(c.getClickedPos()));
   }

   protected RenderShape getRenderShape(BlockState s) {
      return RenderShape.MODEL;
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return ((Direction)s.getValue(FACING)).getAxis() == Axis.Z ? Block.box(0.0, 0.0, 3.0, 16.0, 12.0, 13.0) : Block.box(3.0, 0.0, 0.0, 13.0, 12.0, 16.0);
   }

   protected FluidState getFluidState(BlockState s) {
      return s.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(s);
   }

   protected BlockState updateShape(BlockState s, Direction d, BlockState other, LevelAccessor l, BlockPos p, BlockPos from) {
      if ((Boolean)s.getValue(WATERLOGGED)) {
         l.scheduleTick(p, Fluids.WATER, Fluids.WATER.getTickDelay(l));
      }

      return super.updateShape(s, d, other, l, p, from);
   }

   protected void neighborChanged(BlockState s, Level l, BlockPos p, Block b, BlockPos from, boolean moving) {
      boolean power = l.hasNeighborSignal(p);
      if (power != (Boolean)s.getValue(POWERED)) {
         l.setBlockAndUpdate(p, (BlockState)s.setValue(POWERED, power));
      }
   }

   protected InteractionResult useWithoutItem(BlockState s, Level l, BlockPos p, Player player, BlockHitResult hit) {
      if (!l.isClientSide) {
         boolean enabled = !(Boolean)s.getValue(ENABLED);
         if (!enabled && l.getBlockEntity(p) instanceof RadioBlockEntity be) {
            be.stop();
         }

         l.setBlockAndUpdate(p, (BlockState)l.getBlockState(p).setValue(ENABLED, enabled));
         MagniaSounds.play(l, p, "magnia_radio.power_" + (enabled ? "on" : "off"));
      }

      return InteractionResult.sidedSuccess(l.isClientSide);
   }

   public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      return new RadioBlockEntity(p, s);
   }

   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> type) {
      return createTickerHelper(type, (BlockEntityType)RadioBlockEntity.TYPE.get(), RadioBlockEntity::tick);
   }

   protected BlockState rotate(BlockState s, Rotation r) {
      return (BlockState)s.setValue(FACING, r.rotate((Direction)s.getValue(FACING)));
   }

   protected BlockState mirror(BlockState s, Mirror m) {
      return s.rotate(m.getRotation((Direction)s.getValue(FACING)));
   }
}
