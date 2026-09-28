package dev.yuanyu.enderscapeexpansion.block;

import com.mojang.serialization.MapCodec;
import dev.yuanyu.enderscapeexpansion.ShelfBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class StorageShelf extends BaseEntityBlock implements SimpleWaterloggedBlock, SideChainPartBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<SideChainPart> PART = EnumProperty.create("side_chain", SideChainPart.class);
   public static final MapCodec<StorageShelf> CODEC = simpleCodec(StorageShelf::new);

   public StorageShelf(Properties p) {
      super(p);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(POWERED, false)).setValue(WATERLOGGED, false))
               .setValue(FACING, Direction.NORTH))
            .setValue(PART, SideChainPart.UNCONNECTED)
      );
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> b) {
      b.add(new Property[]{POWERED, WATERLOGGED, FACING, PART});
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite()))
            .setValue(POWERED, c.getLevel().hasNeighborSignal(c.getClickedPos())))
         .setValue(WATERLOGGED, c.getLevel().getFluidState(c.getClickedPos()).is(Fluids.WATER));
   }

   protected RenderShape getRenderShape(BlockState s) {
      return RenderShape.MODEL;
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return switch ((Direction)s.getValue(FACING)) {
         case NORTH -> box(0.0, 0.0, 11.0, 16.0, 16.0, 16.0);
         case SOUTH -> box(0.0, 0.0, 0.0, 16.0, 16.0, 5.0);
         case EAST -> box(0.0, 0.0, 0.0, 5.0, 16.0, 16.0);
         default -> box(11.0, 0.0, 0.0, 16.0, 16.0, 16.0);
      };
   }

   public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      return new ShelfBlockEntity(p, s);
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

   protected void onPlace(BlockState s, Level l, BlockPos p, BlockState old, boolean moving) {
      if (!l.isClientSide) {
         if ((Boolean)s.getValue(POWERED)) {
            this.updateSelfAndNeighborsOnPoweringUp(l, p, s, old);
         } else {
            this.updateNeighborsAfterPoweringDown(l, p, s);
         }
      }
   }

   protected void neighborChanged(BlockState s, Level l, BlockPos p, Block b, BlockPos from, boolean moving) {
      if (!l.isClientSide) {
         boolean power = l.hasNeighborSignal(p);
         if (power != (Boolean)s.getValue(POWERED)) {
            l.setBlockAndUpdate(p, (BlockState)((BlockState)s.setValue(POWERED, power)).setValue(PART, SideChainPart.UNCONNECTED));
         }
      }
   }

   protected void onRemove(BlockState s, Level l, BlockPos p, BlockState next, boolean moving) {
      if (!s.is(next.getBlock())) {
         this.updateNeighborsAfterPoweringDown(l, p, s);
         if (l.getBlockEntity(p) instanceof ShelfBlockEntity be) {
            Containers.dropContents(l, p, be);
         }
      }

      super.onRemove(s, l, p, next, moving);
   }

   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState s, Level l, BlockPos p, Player player, InteractionHand hand, BlockHitResult hit) {
      return hand == InteractionHand.MAIN_HAND && this.interact(s, l, p, player, hit)
         ? ItemInteractionResult.sidedSuccess(l.isClientSide)
         : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   protected InteractionResult useWithoutItem(BlockState s, Level l, BlockPos p, Player player, BlockHitResult hit) {
      return this.interact(s, l, p, player, hit) ? InteractionResult.sidedSuccess(l.isClientSide) : InteractionResult.PASS;
   }

   public boolean interact(BlockState s, Level l, BlockPos p, Player player, BlockHitResult hit) {
      if (!(hit.getDirection() == s.getValue(FACING) && l.getBlockEntity(p) instanceof ShelfBlockEntity be)) {
         return false;
      } else if (l.isClientSide) {
         return true;
      } else {
         Inventory inventory = player.getInventory();
         if ((Boolean)s.getValue(POWERED)) {
            List<BlockPos> connected = this.getAllBlocksConnectedTo(l, p);

            for (int part = 0; part < connected.size(); part++) {
               if (l.getBlockEntity(connected.get(part)) instanceof ShelfBlockEntity shelf) {
                  for (int slot = 0; slot < 3; slot++) {
                     int index = 9 - (connected.size() - part) * 3 + slot;
                     inventory.setItem(index, shelf.swap(slot, inventory.getItem(index)));
                  }

                  shelf.setChanged();
               }
            }
         } else {
            double x = hit.getLocation().x - p.getX();
            double z = hit.getLocation().z - p.getZ();

            double local = switch ((Direction)s.getValue(FACING)) {
               case NORTH -> 1.0 - x;
               case SOUTH -> x;
               default -> 1.0 - z;
               case WEST -> z;
            };
            int slot = Mth.clamp((int)(local * 3.0), 0, 2);
            ItemStack held = player.getMainHandItem();
            ItemStack old = be.swap(slot, held.copy());
            inventory.setItem(inventory.selected, player.getAbilities().instabuild && old.isEmpty() ? held : old);
            be.setChanged();
         }

         inventory.setChanged();
         l.playSound(null, p, SoundEvents.CHISELED_BOOKSHELF_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
         l.gameEvent(player, GameEvent.BLOCK_CHANGE, p);
         return true;
      }
   }

   @Override
   public SideChainPart getSideChainPart(BlockState s) {
      return (SideChainPart)s.getValue(PART);
   }

   @Override
   public BlockState setSideChainPart(BlockState s, SideChainPart part) {
      return (BlockState)s.setValue(PART, part);
   }

   @Override
   public Direction getFacing(BlockState s) {
      return (Direction)s.getValue(FACING);
   }

   @Override
   public boolean isConnectable(BlockState s) {
      return s.getBlock() instanceof StorageShelf && (Boolean)s.getValue(POWERED);
   }

   @Override
   public int getMaxChainLength() {
      return 3;
   }

   protected boolean hasAnalogOutputSignal(BlockState s) {
      return true;
   }

   protected int getAnalogOutputSignal(BlockState s, Level l, BlockPos p) {
      return l.getBlockEntity(p) instanceof ShelfBlockEntity be
         ? (be.getItem(0).isEmpty() ? 0 : 1) | (be.getItem(1).isEmpty() ? 0 : 2) | (be.getItem(2).isEmpty() ? 0 : 4)
         : 0;
   }

   protected BlockState rotate(BlockState s, Rotation r) {
      return (BlockState)s.setValue(FACING, r.rotate((Direction)s.getValue(FACING)));
   }

   protected BlockState mirror(BlockState s, Mirror m) {
      return s.rotate(m.getRotation((Direction)s.getValue(FACING)));
   }
}
