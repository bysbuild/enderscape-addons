package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.entity.ai.EnderscapeMemory;
import net.bunten.enderscape.entity.rubblemite.Rubblemite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Post;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class RubblemiteBehavior {
   public static void prepare(Rubblemite mob) {
      if (!mob.hasEffect(ExpansionEffects.STUNNED)) {
         mob.setData(ExpansionEffects.PREPARE_DASH, 5);
         mob.getBrain().setMemoryWithExpiry((MemoryModuleType)EnderscapeMemory.RUBBLEMITE_DASH_ON_COOLDOWN.get(), true, 45L);
         mob.playSound((SoundEvent)BuiltInRegistries.SOUND_EVENT.get(Expansion.id("entity.rubblemite.prepare_dash")), 1.0F, 1.0F);
         mob.gameEvent(GameEvent.ENTITY_ACTION);
      }
   }

   @SubscribeEvent
   public static void tick(Post event) {
      if (event.getEntity() instanceof Rubblemite mob && !mob.level().isClientSide) {
         int ticks = (Integer)mob.getData(ExpansionEffects.PREPARE_DASH);
         if (mob.hasEffect(ExpansionEffects.STUNNED)) {
            mob.setFlags(0);
            mob.getBrain().eraseMemory((MemoryModuleType)EnderscapeMemory.RUBBLEMITE_HIDING_DURATION.get());
            if (ticks > 0) {
               mob.setData(ExpansionEffects.PREPARE_DASH, 0);
            }
         } else {
            if (ticks > 0) {
               mob.getNavigation().stop();
               mob.setData(ExpansionEffects.PREPARE_DASH, ticks - 1);
               if (ticks == 1 && mob.isAlive() && !mob.isInsideShell() && !mob.isInWaterOrRain()) {
                  mob.dash();
               }
            }
         }
      }
   }
}
