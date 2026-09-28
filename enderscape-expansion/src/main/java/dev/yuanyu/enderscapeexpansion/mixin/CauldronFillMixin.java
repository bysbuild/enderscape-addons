package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.Expansion;
import dev.yuanyu.enderscapeexpansion.ExpansionBlocks;
import dev.yuanyu.enderscapeexpansion.VoidFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCauldronBlock.class)
public abstract class CauldronFillMixin {
   @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
   private void expansion$fill(
      ItemStack stack,
      BlockState s,
      Level l,
      BlockPos p,
      Player player,
      InteractionHand hand,
      BlockHitResult hit,
      CallbackInfoReturnable<ItemInteractionResult> cir
   ) {
      if (stack.is((Item)VoidFluid.BUCKET.get())) {
         if (!l.isClientSide) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            l.setBlockAndUpdate(p, ((Block)ExpansionBlocks.VOID_CAULDRON.get()).defaultBlockState());
            player.awardStat(Stats.FILL_CAULDRON);
            l.playSound(null, p, (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id("item.void_lachryma_bucket.empty")), SoundSource.BLOCKS, 1.0F, 1.0F);
            l.gameEvent(player, GameEvent.FLUID_PLACE, p);
         }

         cir.setReturnValue(ItemInteractionResult.sidedSuccess(l.isClientSide));
      }
   }
}
