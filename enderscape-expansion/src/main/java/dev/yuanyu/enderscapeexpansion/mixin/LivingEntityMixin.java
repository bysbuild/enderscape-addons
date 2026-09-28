package dev.yuanyu.enderscapeexpansion.mixin;

import dev.yuanyu.enderscapeexpansion.Expansion;
import dev.yuanyu.enderscapeexpansion.ExpansionEffects;
import dev.yuanyu.enderscapeexpansion.ExpansionItems;
import dev.yuanyu.enderscapeexpansion.VoidFluid;
import dev.yuanyu.enderscapeexpansion.VoidSystem;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   @Inject(method = "canStandOnFluid", at = @At("RETURN"), cancellable = true)
   private void expansion$walkVoid(FluidState fluid, CallbackInfoReturnable<Boolean> ci) {
      if (fluid.getFluidType() == VoidFluid.TYPE.get() && VoidSystem.tagged((LivingEntity)this, "void_lachryma_walkable_mobs")) {
         ci.setReturnValue(true);
      }
   }

   @Inject(method = "canBeAffected", at = @At("RETURN"), cancellable = true)
   private void expansion$unsupported(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
      LivingEntity self = (LivingEntity)this;
      if (self.hasData(ExpansionEffects.VOIDED_HEALTH)
         && (Float)self.getData(ExpansionEffects.VOIDED_HEALTH) > 0.0F
         && effect.getEffect().is(TagKey.create(Registries.MOB_EFFECT, Expansion.id("unsupported_with_voided_health")))) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
   private void expansion$stealth(Entity lookingEntity, CallbackInfoReturnable<Double> cir) {
      LivingEntity self = (LivingEntity)this;
      double reduction = 0.0;
      if (self.getItemBySlot(EquipmentSlot.HEAD).is((Item)ExpansionItems.SHADOLINE_HELMET.get())) {
         reduction += 0.15;
      }

      if (self.getItemBySlot(EquipmentSlot.CHEST).is((Item)ExpansionItems.SHADOLINE_CHESTPLATE.get())) {
         reduction += 0.2;
      }

      if (self.getItemBySlot(EquipmentSlot.LEGS).is((Item)ExpansionItems.SHADOLINE_LEGGINGS.get())) {
         reduction += 0.2;
      }

      if (self.getItemBySlot(EquipmentSlot.FEET).is((Item)ExpansionItems.SHADOLINE_BOOTS.get())) {
         reduction += 0.15;
      }

      if (reduction > 0.0) {
         cir.setReturnValue(cir.getReturnValueD() * (1.0 - reduction));
      }
   }

   @ModifyVariable(method = "setHealth", at = @At("HEAD"), argsOnly = true)
   private float expansion$healthCap(float health) {
      if (!ExpansionEffects.VOIDED_HEALTH.isBound()) {
         return health;
      } else {
         LivingEntity self = (LivingEntity)this;
         if (!self.hasData(ExpansionEffects.VOIDED_HEALTH)) {
            return health;
         } else {
            return self.getData(ExpansionEffects.VOIDED_HEALTH) > 0.0F ? Math.min(health, VoidSystem.healthLimit(self)) : health;
         }
      }
   }

   @Inject(
      method = "checkTotemDeathProtection",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V", shift = Shift.BEFORE)
   )
   private void expansion$totem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
      LivingEntity self = (LivingEntity)this;
      self.setData(
         ExpansionEffects.VOIDED_HEALTH,
         Math.min((Float)self.getData(ExpansionEffects.VOIDED_HEALTH), Math.min(10.0F, Math.max(0.0F, self.getMaxHealth() - 1.0F)))
      );
      self.getPersistentData().putInt("enderscape_expansion.void_immunity", 10);
   }

   @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
   private void expansion$stunnedTravel(Vec3 input, CallbackInfo ci) {
      LivingEntity self = (LivingEntity)this;
      if (self.hasEffect(ExpansionEffects.STUNNED)) {
         self.setDeltaMovement(Vec3.ZERO);
         ci.cancel();
      }
   }
}
