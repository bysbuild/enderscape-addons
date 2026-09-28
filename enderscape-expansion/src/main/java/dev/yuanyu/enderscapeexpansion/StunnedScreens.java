package dev.yuanyu.enderscapeexpansion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent.Opening;

@EventBusSubscriber(modid = "enderscape_expansion", value = Dist.CLIENT)
public final class StunnedScreens {
   @SubscribeEvent
   public static void opening(Opening e) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && player.hasEffect(ExpansionEffects.STUNNED) && e.getNewScreen() instanceof AbstractContainerScreen) {
         e.setCanceled(true);
      }
   }
}
