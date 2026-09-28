package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.Resonance;
import java.util.Optional;
import net.bunten.enderscape.item.MirrorContext;
import net.bunten.enderscape.item.MirrorItem;
import net.bunten.enderscape.item.NebuliteToolContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MirrorItem.class)
public abstract class MirrorCompatibilityMixin {
   @Inject(method = "getTotalDistanceForCostIncrease", at = @At("RETURN"), cancellable = true)
   private void expansion$resonance(NebuliteToolContext context, CallbackInfoReturnable<Integer> cir) {
      cir.setReturnValue(cir.getReturnValueI() + 250 * Resonance.level(context.stack()));
   }

   @Inject(method = "getTeleportPosition", at = @At("HEAD"), cancellable = true)
   private static void expansion$standingSpace(MirrorContext context, CallbackInfoReturnable<Optional<Vec3>> cir) {
      ServerLevel level = context.linkedLevel();
      LivingEntity user = context.user();
      EntityDimensions dimensions = user.getDimensions(Pose.STANDING);
      Vec3 center = context.linkedPos().above().getBottomCenter().add(0.0, dimensions.height() / 2.0F, 0.0);
      VoxelShape shape = Shapes.create(AABB.ofSize(center, dimensions.width() + 1.0F, dimensions.height() + 1.0F, dimensions.width() + 1.0F).inflate(1.0E-6));
      Optional<Vec3> free = level.findFreePosition(user, shape, center, dimensions.width(), dimensions.height(), dimensions.width());
      if (free.isPresent()) {
         MutableBlockPos p = BlockPos.containing((Position)free.get()).mutable();

         for (int i = 0; i < 8; i++) {
            Vec3 feet = Vec3.atBottomCenterOf(p);
            AABB box = dimensions.makeBoundingBox(feet);
            if (level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP) && level.noCollision(user, box) && !level.containsAnyLiquid(box)) {
               cir.setReturnValue(Optional.of(Vec3.atLowerCornerOf(p)));
               return;
            }

            p.move(Direction.DOWN);
         }
      }

      cir.setReturnValue(Optional.empty());
   }
}
