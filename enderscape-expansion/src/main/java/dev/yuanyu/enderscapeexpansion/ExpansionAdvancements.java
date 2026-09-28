package dev.yuanyu.enderscapeexpansion;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class ExpansionAdvancements {
   public static void award(Player player, String path) {
      if (player instanceof ServerPlayer server) {
         AdvancementHolder advancement = server.server.getAdvancements().get(Expansion.id(path));
         if (advancement != null) {
            server.getAdvancements().award(advancement, path);
         }
      }
   }
}
