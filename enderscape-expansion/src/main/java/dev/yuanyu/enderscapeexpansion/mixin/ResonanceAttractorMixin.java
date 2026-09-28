package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.Resonance;
import net.bunten.enderscape.item.MagniaAttractorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MagniaAttractorItem.class)
public abstract class ResonanceAttractorMixin {
   @Inject(method = "getEntitiesPulledToUseFuel", at = @At("RETURN"), cancellable = true)
   private static void expansion$threshold(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
      cir.setReturnValue(cir.getReturnValueI() + 100 * Resonance.level(stack));
   }
}
