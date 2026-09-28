package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.VoidFluid;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class VoidCameraMixin {
   @Inject(method = "getFluidInCamera", at = @At("RETURN"), cancellable = true)
   private void expansion$fluidFog(CallbackInfoReturnable<FogType> cir) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         Camera camera = (Camera)this;
         Vec3 point = camera.getPosition();
         BlockPos pos = BlockPos.containing(point);
         FluidState fluid = level.getFluidState(pos);
         if (fluid.getFluidType() == VoidFluid.TYPE.get() && point.y < pos.getY() + fluid.getHeight(level, pos)) {
            cir.setReturnValue(FogType.LAVA);
         }
      }
   }
}
