package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.registry.EnderscapeItems;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class MirrorDyeRecipe extends CustomRecipe {
   public MirrorDyeRecipe(CraftingBookCategory category) {
      super(category);
   }

   private ItemStack result(CraftingInput input) {
      ItemStack mirror = ItemStack.EMPTY;
      DyeColor color = null;

      for (ItemStack stack : input.items()) {
         if (!stack.isEmpty()) {
            if (stack.is((Item)EnderscapeItems.MIRROR.get())) {
               if (!mirror.isEmpty()) {
                  return ItemStack.EMPTY;
               }

               mirror = stack;
            } else {
               if (!(stack.getItem() instanceof DyeItem dye)) {
                  return ItemStack.EMPTY;
               }

               if (color != null) {
                  return ItemStack.EMPTY;
               }

               color = dye.getDyeColor();
            }
         }
      }

      if (!mirror.isEmpty() && color != null && mirror.get(UnifiedShield.DYE_COLOR) != color) {
         ItemStack result = mirror.copyWithCount(1);
         result.set(UnifiedShield.DYE_COLOR, color);
         return result;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public boolean matches(CraftingInput input, Level level) {
      return !this.result(input).isEmpty();
   }

   public ItemStack assemble(CraftingInput input, Provider registries) {
      return this.result(input);
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return (RecipeSerializer<?>)ToolFuelingRecipe.MIRROR_DYE.get();
   }
}
