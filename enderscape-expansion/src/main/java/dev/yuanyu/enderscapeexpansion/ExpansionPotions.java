package dev.yuanyu.enderscapeexpansion;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionBrewing.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class ExpansionPotions {
   private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, "enderscape");
   public static final List<DeferredHolder<Potion, Potion>> ADDED = new ArrayList<>();
   public static final DeferredHolder<Potion, Potion> PURIFICATION = add("void_purification", ExpansionEffects.PURIFICATION, 1800, 0);
   public static final DeferredHolder<Potion, Potion> LONG_PURIFICATION = add("long_void_purification", ExpansionEffects.PURIFICATION, 4800, 0);
   public static final DeferredHolder<Potion, Potion> STRONG_PURIFICATION = add("strong_void_purification", ExpansionEffects.PURIFICATION, 600, 1);
   public static final DeferredHolder<Potion, Potion> CORRUPTION = add("void_corruption", ExpansionEffects.CORRUPTION, 800, 0);
   public static final DeferredHolder<Potion, Potion> LONG_CORRUPTION = add("long_void_corruption", ExpansionEffects.CORRUPTION, 1600, 0);
   public static final DeferredHolder<Potion, Potion> STRONG_CORRUPTION = add("strong_void_corruption", ExpansionEffects.CORRUPTION, 400, 1);
   public static final DeferredHolder<Potion, Potion> RESISTANCE = add("void_resistance", ExpansionEffects.RESISTANCE, 1200, 0);
   public static final DeferredHolder<Potion, Potion> LONG_RESISTANCE = add("long_void_resistance", ExpansionEffects.RESISTANCE, 3600, 0);

   private static DeferredHolder<Potion, Potion> add(String name, Holder<MobEffect> effect, int duration, int amplifier) {
      DeferredHolder<Potion, Potion> holder = POTIONS.register(
         name,
         () -> new Potion(
            "enderscape_" + name.replace("long_", "").replace("strong_", ""), new MobEffectInstance[]{new MobEffectInstance(effect, duration, amplifier)}
         )
      );
      ADDED.add(holder);
      return holder;
   }

   public static void register(IEventBus bus) {
      POTIONS.register(bus);
   }

   @SubscribeEvent
   public static void brewing(RegisterBrewingRecipesEvent e) {
      Builder b = e.getBuilder();
      b.addMix(Potions.AWKWARD, (Item)ExpansionItems.PURUBERRY.get(), PURIFICATION);
      b.addMix(Potions.AWKWARD, (Item)VoidFluid.BUCKET.get(), CORRUPTION);
      b.addMix(PURIFICATION, Items.ENDER_PEARL, RESISTANCE);
      b.addMix(PURIFICATION, Items.REDSTONE, LONG_PURIFICATION);
      b.addMix(PURIFICATION, Items.GLOWSTONE_DUST, STRONG_PURIFICATION);
      b.addMix(CORRUPTION, Items.REDSTONE, LONG_CORRUPTION);
      b.addMix(CORRUPTION, Items.GLOWSTONE_DUST, STRONG_CORRUPTION);
      b.addMix(RESISTANCE, Items.REDSTONE, LONG_RESISTANCE);
   }
}
