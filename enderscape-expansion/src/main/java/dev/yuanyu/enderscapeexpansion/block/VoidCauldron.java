package dev.yuanyu.enderscapeexpansion.block;

import com.mojang.serialization.MapCodec;
import dev.yuanyu.enderscapeexpansion.Expansion;
import dev.yuanyu.enderscapeexpansion.FluidInteractions;
import dev.yuanyu.enderscapeexpansion.VoidFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public final class VoidCauldron extends AbstractCauldronBlock {
   public static final MapCodec<VoidCauldron> CODEC = simpleCodec(VoidCauldron::new);

   public VoidCauldron(Properties p) {
      super(p, CauldronInteraction.EMPTY);
   }

   public MapCodec<VoidCauldron> codec() {
      return CODEC;
   }

   public boolean isFull(BlockState s) {
      return true;
   }

   protected double getContentHeight(BlockState s) {
      return 0.9375;
   }

   protected int getAnalogOutputSignal(BlockState s, Level l, BlockPos p) {
      return 3;
   }

   protected void entityInside(BlockState s, Level l, BlockPos p, Entity e) {
      if (this.isEntityInsideContent(s, p, e)) {
         FluidInteractions.expose(e);
      }
   }

   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState s, Level l, BlockPos p, Player player, InteractionHand hand, BlockHitResult hit) {
      if (stack.is(Items.BUCKET)) {
         if (!l.isClientSide) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack((ItemLike)VoidFluid.BUCKET.get())));
            l.setBlockAndUpdate(p, Blocks.CAULDRON.defaultBlockState());
            player.awardStat(Stats.USE_CAULDRON);
            l.playSound(null, p, (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id("item.void_lachryma_bucket.fill")), SoundSource.BLOCKS, 1.0F, 1.0F);
            l.gameEvent(player, GameEvent.FLUID_PICKUP, p);
         }

         return ItemInteractionResult.sidedSuccess(l.isClientSide);
      } else {
         return super.useItemOn(stack, s, l, p, player, hand, hit);
      }
   }
}
