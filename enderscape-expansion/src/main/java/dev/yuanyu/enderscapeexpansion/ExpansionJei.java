package dev.yuanyu.enderscapeexpansion;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.bunten.enderscape.item.RubbleShieldItem;
import net.bunten.enderscape.registry.EnderscapeItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

@JeiPlugin
public final class ExpansionJei implements IModPlugin {
   public static final RecipeType<ConversionRecipe> VOID = new RecipeType(Expansion.id("void_lachryma"), ConversionRecipe.class);
   public static final RecipeType<ConversionRecipe> RUSTLE = new RecipeType(Expansion.id("rustle"), ConversionRecipe.class);

   public ResourceLocation getPluginUid() {
      return ResourceLocation.fromNamespaceAndPath("enderscape_expansion", "conversions");
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
      registration.addRecipeCategories(new IRecipeCategory[]{new ExpansionJei.Category(gui, VOID, false), new ExpansionJei.Category(gui, RUSTLE, true)});
   }

   public void registerRecipes(IRecipeRegistration registration) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         registration.addRecipes(
            VOID,
            level.getRecipeManager()
               .getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)ConversionRecipe.VOID_TYPE.get())
               .stream()
               .map(RecipeHolder::value)
               .toList()
         );
         registration.addRecipes(
            RUSTLE,
            level.getRecipeManager()
               .getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)ConversionRecipe.RUSTLE_TYPE.get())
               .stream()
               .map(RecipeHolder::value)
               .toList()
         );
         ItemStack mirror = new ItemStack((ItemLike)EnderscapeItems.MIRROR.get());
         ArrayList<ItemStack> mirrors = new ArrayList<>();
         mirrors.add(mirror);

         for (DyeColor color : DyeColor.values()) {
            ItemStack dyed = mirror.copy();
            dyed.set(UnifiedShield.DYE_COLOR, color);
            mirrors.add(dyed);
         }

         ArrayList<IJeiAnvilRecipe> anvils = new ArrayList<>();
         IVanillaRecipeFactory factory = registration.getJeiHelpers().getVanillaRecipeFactory();

         for (Reference<Enchantment> holder : level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().toList()) {
            if (((Enchantment)holder.value()).canEnchant(mirror)) {
               for (int rank = 1; rank <= ((Enchantment)holder.value()).getMaxLevel(); rank++) {
                  ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, rank));
                  ArrayList<ItemStack> outputs = new ArrayList<>();

                  for (ItemStack input : mirrors) {
                     ItemStack output = input.copy();
                     output.enchant(holder, rank);
                     outputs.add(output);
                  }

                  ResourceLocation id = holder.key().location();
                  anvils.add(
                     factory.createAnvilRecipe(
                        mirrors,
                        List.of(book),
                        outputs,
                        ResourceLocation.fromNamespaceAndPath(
                           "enderscape_expansion", "mirror_enchanting/" + id.getNamespace() + "/" + id.getPath() + "/" + rank
                        )
                     )
                  );
               }
            }
         }

         registration.addRecipes(RecipeTypes.ANVIL, anvils);
      }
   }

   public void onRuntimeAvailable(IJeiRuntime runtime) {
      IIngredientManager ingredients = runtime.getIngredientManager();
      List<ItemStack> hidden = ingredients.getAllIngredients(VanillaTypes.ITEM_STACK)
         .stream()
         .filter(
            stack -> stack.is(((Block)ExpansionBlocks.WILDFLOWERS.get()).asItem())
               || stack.getItem() instanceof RubbleShieldItem && !stack.is((Item)ExpansionItems.RUBBLE_SHIELD.get())
         )
         .toList();
      ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hidden);
      if (Boolean.getBoolean("enderscape_expansion.verifyAssets")) {
         Minecraft.getInstance().tell(() -> verifyRuntime(runtime));
      }
   }

   private static void verifyRuntime(IJeiRuntime runtime) {
      IRecipeManager manager = runtime.getRecipeManager();
      IFocus<ItemStack> mirrorFocus = runtime.getJeiHelpers()
         .getFocusFactory()
         .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, new ItemStack((ItemLike)EnderscapeItems.MIRROR.get()));
      List<IJeiAnvilRecipe> mirrorRecipes = manager.createRecipeLookup(RecipeTypes.ANVIL)
         .limitFocus(List.of(mirrorFocus))
         .get()
         .filter(r -> r.getUid() != null && r.getUid().getNamespace().equals("enderscape_expansion"))
         .toList();
      if (mirrorRecipes.isEmpty()) {
         throw new IllegalStateException("Mirror R-key output focus has no anvil recipes");
      } else {
         for (IJeiAnvilRecipe recipe : mirrorRecipes) {
            if (manager.createRecipeLayoutDrawable(
                  manager.getRecipeCategory(RecipeTypes.ANVIL), recipe, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()
               )
               .isEmpty()) {
               throw new IllegalStateException("Mirror anvil layout failed");
            }
         }

         LogUtils.getLogger().info("MIRROR JEI VERIFICATION PASS: {} anvil recipes found by output focus", mirrorRecipes.size());

         for (RecipeType<ConversionRecipe> type : List.of(VOID, RUSTLE)) {
            List<ConversionRecipe> recipes = manager.createRecipeLookup(type).get().toList();
            if (recipes.isEmpty()) {
               throw new IllegalStateException("Missing JEI conversions " + type);
            }

            for (ConversionRecipe recipex : recipes) {
               if (manager.createRecipeLayoutDrawable(manager.getRecipeCategory(type), recipex, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup())
                  .isEmpty()) {
                  throw new IllegalStateException("Invalid JEI layout " + type);
               }
            }

            LogUtils.getLogger().info("JEI CONVERSION VERIFICATION PASS: {} recipes for {}", recipes.size(), type);
         }
      }
   }

   private static final class Category implements IRecipeCategory<ConversionRecipe> {
      private final RecipeType<ConversionRecipe> type;
      private final boolean rustle;
      private final IDrawable background;
      private final IDrawable icon;

      Category(IGuiHelper gui, RecipeType<ConversionRecipe> type, boolean rustle) {
         this.type = type;
         this.rustle = rustle;
         this.background = gui.createBlankDrawable(150, 60);
         this.icon = gui.createDrawableItemStack(new ItemStack(rustle ? (ItemLike)ExpansionItems.RUSTLE_SILK.get() : (ItemLike)VoidFluid.BUCKET.get()));
      }

      public RecipeType<ConversionRecipe> getRecipeType() {
         return this.type;
      }

      public Component getTitle() {
         return Component.translatable(this.rustle ? "jei.enderscape.category.rustle" : "jei.enderscape.category.void_lachryma");
      }

      public IDrawable getBackground() {
         return this.background;
      }

      public IDrawable getIcon() {
         return this.icon;
      }

      public void setRecipe(IRecipeLayoutBuilder builder, ConversionRecipe recipe, IFocusGroup focus) {
         builder.addSlot(RecipeIngredientRole.INPUT, 18, 32).addIngredients(recipe.input());
         builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 32).addItemStack(recipe.result());
      }

      public void draw(ConversionRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
         Font font = Minecraft.getInstance().font;
         graphics.drawString(font, "→", 70, 35, 5592405, false);
         if (!this.rustle) {
            graphics.drawString(font, Component.literal(String.format(Locale.ROOT, "每刻 %.3f%%", recipe.chance() / 20.0F * 100.0F)), 20, 7, 5592405, false);
         }
      }
   }
}
