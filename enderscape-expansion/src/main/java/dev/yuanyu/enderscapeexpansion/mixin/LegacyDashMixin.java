package dev.yuanyu.enderscapeexpansion.mixin;

import net.bunten.enderscape.item.RubbleShieldItem;
import net.bunten.enderscape.item.component.DashJump;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DashJump.class)
public abstract class LegacyDashMixin {
   @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
   private void expansion$chargeRequired(ServerLevel level, ServerPlayer player, ItemStack stack, DashJump jump, CallbackInfoReturnable<Boolean> cir) {
      if (stack.getItem() instanceof RubbleShieldItem) {
         cir.setReturnValue(false);
      }
   }
}
