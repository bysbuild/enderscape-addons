package dev.yuanyu.enderscapeexpansion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public record ConversionRecipe(Ingredient input, ItemStack result, float chance, float minimumVoided, float experience, boolean rustle, RustleEffects effects)
   implements Recipe<SingleRecipeInput> {
   private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "enderscape");
   private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "enderscape");
   public static final DeferredHolder<RecipeType<?>, RecipeType<ConversionRecipe>> VOID_TYPE = TYPES.register(
      "void_lachryma", () -> new RecipeType<ConversionRecipe>() {}
   );
   public static final DeferredHolder<RecipeType<?>, RecipeType<ConversionRecipe>> RUSTLE_TYPE = TYPES.register(
      "rustle", () -> new RecipeType<ConversionRecipe>() {}
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ConversionRecipe>> VOID_SERIALIZER = SERIALIZERS.register(
      "void_lachryma", () -> new ConversionRecipe.Serializer(false)
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ConversionRecipe>> RUSTLE_SERIALIZER = SERIALIZERS.register(
      "rustle", () -> new ConversionRecipe.Serializer(true)
   );

   public boolean matches(SingleRecipeInput input, Level level) {
      return this.input.test(input.item());
   }

   public ItemStack assemble(SingleRecipeInput input, Provider registries) {
      return this.result.copy();
   }

   public boolean canCraftInDimensions(int width, int height) {
      return true;
   }

   public ItemStack getResultItem(Provider registries) {
      return this.result;
   }

   public RecipeSerializer<?> getSerializer() {
      return this.rustle ? (RecipeSerializer)RUSTLE_SERIALIZER.get() : (RecipeSerializer)VOID_SERIALIZER.get();
   }

   public RecipeType<?> getType() {
      return this.rustle ? (RecipeType)RUSTLE_TYPE.get() : (RecipeType)VOID_TYPE.get();
   }

   public boolean isSpecial() {
      return true;
   }

   public NonNullList<Ingredient> getIngredients() {
      return NonNullList.of(Ingredient.EMPTY, new Ingredient[]{this.input});
   }

   public static void register(IEventBus bus) {
      TYPES.register(bus);
      SERIALIZERS.register(bus);
   }

   private static final class Serializer implements RecipeSerializer<ConversionRecipe> {
      private final MapCodec<ConversionRecipe> codec;
      private final StreamCodec<RegistryFriendlyByteBuf, ConversionRecipe> stream;

      Serializer(boolean rustle) {
         this.codec = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                  Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(ConversionRecipe::input),
                  ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ConversionRecipe::result),
                  Codec.floatRange(0.0F, 1.0F).optionalFieldOf("average_chance_per_second", 1.0F).forGetter(ConversionRecipe::chance),
                  Codec.floatRange(0.0F, 1.0F).optionalFieldOf("minimum_voided_percentage", 1.0F).forGetter(ConversionRecipe::minimumVoided),
                  Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(ConversionRecipe::experience),
                  RustleEffects.CODEC.optionalFieldOf("effects", RustleEffects.DEFAULT).forGetter(ConversionRecipe::effects)
               )
               .apply(instance, (input, result, chance, minimum, xp, effects) -> new ConversionRecipe(input, result, chance, minimum, xp, rustle, effects))
         );
         this.stream = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            ConversionRecipe::input,
            ItemStack.STREAM_CODEC,
            ConversionRecipe::result,
            ByteBufCodecs.FLOAT,
            ConversionRecipe::chance,
            ByteBufCodecs.FLOAT,
            ConversionRecipe::minimumVoided,
            ByteBufCodecs.FLOAT,
            ConversionRecipe::experience,
            RustleEffects.STREAM,
            ConversionRecipe::effects,
            (input, result, chance, minimum, xp, effects) -> new ConversionRecipe(input, result, chance, minimum, xp, rustle, effects)
         );
      }

      public MapCodec<ConversionRecipe> codec() {
         return this.codec;
      }

      public StreamCodec<RegistryFriendlyByteBuf, ConversionRecipe> streamCodec() {
         return this.stream;
      }
   }
}
