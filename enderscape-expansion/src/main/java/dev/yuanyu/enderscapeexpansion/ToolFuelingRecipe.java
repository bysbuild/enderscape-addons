package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.item.NebuliteToolItem;
import net.bunten.enderscape.registry.EnderscapeItems;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ToolFuelingRecipe extends CustomRecipe {
   private static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "enderscape");
   public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<MirrorDyeRecipe>> MIRROR_DYE = REGISTRY.register(
      "mirror_dying", () -> new SimpleCraftingRecipeSerializer(MirrorDyeRecipe::new)
   );
   public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ToolFuelingRecipe>> SERIALIZER = REGISTRY.register(
      "tool_fueling", () -> new SimpleCraftingRecipeSerializer(ToolFuelingRecipe::new)
   );

   public static void register(IEventBus bus) {
      REGISTRY.register(bus);
   }

   public ToolFuelingRecipe(CraftingBookCategory category) {
      super(category);
   }

   private ItemStack result(CraftingInput input) {
      ItemStack tool = ItemStack.EMPTY;
      int fuel = 0;

      for (ItemStack stack : input.items()) {
         if (!stack.isEmpty()) {
            if (NebuliteToolItem.is(stack)) {
               if (!tool.isEmpty()) {
                  return ItemStack.EMPTY;
               }

               tool = stack;
            } else {
               if (!stack.is((Item)EnderscapeItems.NEBULITE.get())) {
                  return ItemStack.EMPTY;
               }

               fuel++;
            }
         }
      }

      if (!tool.isEmpty() && fuel != 0 && fuel <= NebuliteToolItem.maxFuel(tool) - NebuliteToolItem.currentFuel(tool)) {
         ItemStack result = tool.copyWithCount(1);
         NebuliteToolItem.setFuel(result, NebuliteToolItem.currentFuel(result) + fuel);
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
      return (RecipeSerializer<?>)SERIALIZER.get();
   }
}
