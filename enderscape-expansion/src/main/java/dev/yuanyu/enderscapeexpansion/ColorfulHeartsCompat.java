package dev.yuanyu.enderscapeexpansion;

import java.util.Map;
import java.util.TreeMap;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import terrails.colorfulhearts.api.neoforge.event.NeoHeartSingleRenderEvent;
import terrails.colorfulhearts.api.neoforge.event.NeoHeartRenderEvent.Post;
import terrails.colorfulhearts.api.neoforge.event.NeoHeartRenderEvent.Pre;

public final class ColorfulHeartsCompat {
   private static final Map<Integer, int[]> positions = new TreeMap<>();

   public static void register() {
      NeoForge.EVENT_BUS.addListener(ColorfulHeartsCompat::begin);
      NeoForge.EVENT_BUS.addListener(ColorfulHeartsCompat::heart);
      NeoForge.EVENT_BUS.addListener(ColorfulHeartsCompat::end);
   }

   private static void begin(Pre event) {
      positions.clear();
   }

   private static void heart(NeoHeartSingleRenderEvent event) {
      positions.put(event.getIndex(), new int[]{event.getX(), event.getY()});
   }

   private static void end(Post wrapper) {
      terrails.colorfulhearts.api.event.HeartRenderEvent.Post event = (terrails.colorfulhearts.api.event.HeartRenderEvent.Post)wrapper.getEvent();
      Player player = event.getPlayer();
      float voided = (Float)player.getData(ExpansionEffects.VOIDED_HEALTH);
      int count = positions.size();
      if (count != 0) {
         int hearts = (int)Math.ceil(voided / 2.0F);
         int skip = Math.max(0, count - hearts);
         int index = 0;

         for (int[] pos : positions.values()) {
            if (index++ >= skip) {
               String suffix = index == skip + 1 && voided % 2.0F > 0.0F ? "half" : "full";
               event.getGuiGraphics().blitSprite(Expansion.id("hud/heart/voided/" + (event.isHardcore() ? "hardcore/" : "") + suffix), pos[0], pos[1], 9, 9);
               if (player.hasEffect(ExpansionEffects.PURIFICATION)) {
                  event.getGuiGraphics().blitSprite(Expansion.id("hud/heart/void_purification_outline/" + suffix), pos[0], pos[1], 9, 9);
               }
            }
         }

         if ((Integer)player.getData(ExpansionEffects.OUTER_VOID) > 0 && player.tickCount % 10 < 5) {
            for (int[] posx : positions.values()) {
               event.getGuiGraphics().blitSprite(Expansion.id("hud/heart/outer_void_warning/full"), posx[0], posx[1], 9, 9);
            }
         }
      }
   }
}
