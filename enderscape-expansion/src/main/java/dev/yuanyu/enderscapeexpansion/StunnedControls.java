package dev.yuanyu.enderscapeexpansion;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class StunnedControls {
   @SubscribeEvent
   public static void block(RightClickBlock e) {
      if (e.getEntity().hasEffect(ExpansionEffects.STUNNED)) {
         e.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void item(RightClickItem e) {
      if (e.getEntity().hasEffect(ExpansionEffects.STUNNED)) {
         e.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void entity(EntityInteract e) {
      if (e.getEntity().hasEffect(ExpansionEffects.STUNNED)) {
         e.setCanceled(true);
      }
   }
}
