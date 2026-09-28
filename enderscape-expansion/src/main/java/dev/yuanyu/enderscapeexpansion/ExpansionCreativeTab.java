package dev.yuanyu.enderscapeexpansion;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.bunten.enderscape.item.RubbleShieldItem;
import net.bunten.enderscape.registry.EnderscapeCreativeModeTab;
import net.bunten.enderscape.registry.EnderscapeItems;
import net.bunten.enderscape.registry.EnderscapePotions;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

@EventBusSubscriber(modid = "enderscape_expansion")
public final class ExpansionCreativeTab {
   @SubscribeEvent
   public static void append(BuildCreativeModeTabContentsEvent event) {
      if (event.getTabKey().location().equals(Expansion.id("enderscape"))) {
         for (ItemStack stack : List.copyOf(event.getParentEntries())) {
            if (stack.getItem() instanceof RubbleShieldItem || stack.is(((Block)ExpansionBlocks.WILDFLOWERS.get()).asItem())) {
               event.remove(stack, TabVisibility.PARENT_AND_SEARCH_TABS);
            }
         }

         for (ItemStack stackx : List.copyOf(event.getParentEntries())) {
            if (stackx.is(Items.ENCHANTED_BOOK)
               && ((ItemEnchantments)stackx.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY))
                  .entrySet()
                  .stream()
                  .anyMatch(e -> ((Holder)e.getKey()).unwrapKey().map(k -> k.location().getNamespace().equals("enderscape")).orElse(false))) {
               event.remove(stackx, TabVisibility.PARENT_AND_SEARCH_TABS);
            }
         }

         RegistryLookup<Enchantment> enchantments = event.getParameters().holders().lookupOrThrow(Registries.ENCHANTMENT);

         for (String name : List.of("bundling", "rebound", "resonance", "stun_burst", "transdimensional")) {
            Reference<Enchantment> enchantment = enchantments.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Expansion.id(name)));
            event.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, ((Enchantment)enchantment.value()).getMaxLevel())));
         }

         LinkedHashSet<Item> items = new LinkedHashSet<>();

         for (DeferredBlock<? extends Block> block : ExpansionBlocks.ADDED.values()) {
            items.add(((Block)block.get()).asItem());
         }

         items.add((Item)ExpansionItems.PURUBERRY.get());
         items.add((Item)ExpansionItems.RUSTLE_SILK.get());
         items.add((Item)ExpansionItems.SHADOLINE_NUGGET.get());
         items.add((Item)ExpansionItems.SHADOLINE_HELMET.get());
         items.add((Item)ExpansionItems.SHADOLINE_CHESTPLATE.get());
         items.add((Item)ExpansionItems.SHADOLINE_LEGGINGS.get());
         items.add((Item)ExpansionItems.SHADOLINE_BOOTS.get());
         items.add((Item)ExpansionItems.DAGGER.get());
         items.add((Item)VoidFluid.BUCKET.get());
         items.remove(Items.AIR);
         items.remove(((Block)ExpansionBlocks.WILDFLOWERS.get()).asItem());

         for (Item item : items) {
            event.accept(item);
         }

         for (ItemStack stackxx : List.copyOf(event.getParentEntries())) {
            PotionContents contents = (PotionContents)stackxx.get(DataComponents.POTION_CONTENTS);
            if (contents != null
               && contents.potion().<ResourceKey>flatMap(Holder::unwrapKey).map(k -> k.location().getNamespace().equals("enderscape")).orElse(false)) {
               event.remove(stackxx, TabVisibility.PARENT_AND_SEARCH_TABS);
            }
         }

         ArrayList<Holder<Potion>> potions = new ArrayList<>();
         potions.add(EnderscapePotions.LOW_GRAVITY);
         potions.add(EnderscapePotions.LONG_LOW_GRAVITY);
         potions.addAll(ExpansionPotions.ADDED);

         for (Item container : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
            for (Holder<Potion> potion : potions) {
               event.accept(PotionContents.createItemStack(container, potion));
            }
         }

         for (DyeColor color : DyeColor.values()) {
            ItemStack mirror = new ItemStack((ItemLike)EnderscapeItems.MIRROR.get());
            mirror.set(UnifiedShield.DYE_COLOR, color);
            event.accept(mirror);
         }

         event.accept(
            EnderscapeCreativeModeTab.getPainting(event.getParameters(), ResourceKey.create(Registries.PAINTING_VARIANT, Expansion.id("fallen_star")))
         );

         for (String variant : List.of("end_stone", "mirestone", "veradite", "kurodite")) {
            ItemStack stackxxx = new ItemStack((ItemLike)ExpansionItems.RUBBLE_SHIELD.get());
            stackxxx.set(UnifiedShield.VARIANT, Expansion.id(variant));
            event.accept(stackxxx);
         }
      }
   }
}
