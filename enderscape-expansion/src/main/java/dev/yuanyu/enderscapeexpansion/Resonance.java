package dev.yuanyu.enderscapeexpansion;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class Resonance {
   public static int level(ItemStack stack) {
      for (Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet()) {
         if (((Holder)entry.getKey()).is(Expansion.id("resonance"))) {
            return entry.getIntValue();
         }
      }

      return 0;
   }
}
