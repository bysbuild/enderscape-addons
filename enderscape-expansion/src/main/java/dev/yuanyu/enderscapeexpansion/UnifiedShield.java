package dev.yuanyu.enderscapeexpansion;

import net.bunten.enderscape.item.RubbleShieldItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UnifiedShield extends RubbleShieldItem {
   private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "enderscape");
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<DyeColor>> DYE_COLOR = COMPONENTS.register(
      "dye_color",
      () -> DataComponentType.builder().persistent(DyeColor.CODEC).networkSynchronized(ByteBufCodecs.VAR_INT.map(DyeColor::byId, DyeColor::getId)).build()
   );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> VARIANT = COMPONENTS.register(
      "rubble_shield/variant", () -> DataComponentType.builder().persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).build()
   );

   public UnifiedShield(Properties properties) {
      super(properties);
   }

   public Component getName(ItemStack stack) {
      return Component.translatable(
         "item.enderscape." + ((ResourceLocation)stack.getOrDefault(VARIANT, Expansion.id("end_stone"))).getPath() + "_rubble_shield"
      );
   }

   public static void register(IEventBus bus) {
      COMPONENTS.register(bus);
   }
}
