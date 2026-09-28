package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.RustleProcessing;
import net.bunten.enderscape.entity.rustle.Rustle;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Rustle.class)
public abstract class RustleProcessingMixin {
   @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
   private void expansion$feed(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
      Rustle mob = (Rustle)this;
      ItemStack stack = player.getItemInHand(hand);
      if (!mob.level().isClientSide && stack.is(Items.BUCKET)) {
         RustleProcessing.interrupt(mob);
      }

      if (RustleProcessing.begin(mob, player, stack)) {
         cir.setReturnValue(InteractionResult.sidedSuccess(mob.level().isClientSide));
      }
   }

   @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
   private void expansion$pauseNavigation(CallbackInfo ci) {
      Rustle mob = (Rustle)this;
      if (RustleProcessing.busy(mob)) {
         mob.getNavigation().stop();
         ci.cancel();
      }
   }
}
