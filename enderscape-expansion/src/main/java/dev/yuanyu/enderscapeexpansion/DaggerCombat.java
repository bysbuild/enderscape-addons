package dev.yuanyu.enderscapeexpansion;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class DaggerCombat {
   public static boolean backstab(LivingEntity attacker, LivingEntity victim) {
      if (victim.getType() != EntityType.ARMOR_STAND && victim.getType() != EntityType.SHULKER) {
         Vec3 delta = attacker.position().subtract(victim.position()).multiply(1.0, 0.0, 1.0).normalize();
         double yaw = Math.toRadians(victim.getYHeadRot());
         Vec3 facing = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
         return delta.dot(facing) < Math.cos(Math.toRadians(110.0));
      } else {
         return false;
      }
   }

   @SubscribeEvent
   public static void incoming(LivingIncomingDamageEvent event) {
      if (event.getSource().getDirectEntity() instanceof LivingEntity attacker
         && attacker.getMainHandItem().is((Item)ExpansionItems.DAGGER.get())
         && event.getSource().getEntity() == attacker
         && backstab(attacker, event.getEntity())) {
         event.setAmount(event.getAmount() + 3.0F);
         SoundEvent sound = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id("item.dagger.backstab"));
         if (sound != null) {
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), sound, attacker.getSoundSource(), 1.0F, 1.0F);
         }
      }
   }
}
