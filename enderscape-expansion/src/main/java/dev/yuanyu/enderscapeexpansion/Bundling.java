package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.item.MagniaAttractorItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.BundleContents.Mutable;

public final class Bundling {
   public static boolean deposit(Inventory inventory, ItemStack incoming) {
      ItemStack magnet = MagniaAttractorItem.getValidAttractor(inventory);
      if (!magnet.isEmpty()
         && MagniaAttractorItem.isEnabled(magnet)
         && !magnet.getEnchantments().entrySet().stream().noneMatch(e -> ((Holder)e.getKey()).is(Expansion.id("bundling")) && e.getIntValue() > 0)) {
         for (ItemStack bundle : inventory.items) {
            BundleContents contents = (BundleContents)bundle.get(DataComponents.BUNDLE_CONTENTS);
            if (bundle.getItem() instanceof BundleItem && contents != null) {
               for (ItemStack existing : contents.items()) {
                  if (ItemStack.isSameItemSameComponents(existing, incoming)) {
                     Mutable mutable = new Mutable(contents);
                     if (mutable.tryInsert(incoming) > 0) {
                        bundle.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                        inventory.setChanged();
                        return true;
                     }
                     break;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
