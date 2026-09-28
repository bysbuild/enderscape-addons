package dev.yuanyu.enderscapeexpansion;

import java.util.List;
import java.util.Map;
import net.bunten.enderscape.registry.EnderscapeDataComponents;
import net.bunten.enderscape.registry.EnderscapeItems;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

public final class ExpansionItems {
   private static final Items ITEMS = DeferredRegister.createItems("enderscape");
   public static final DeferredItem<UnifiedShield> RUBBLE_SHIELD = ITEMS.register(
      "rubble_shield", () -> new UnifiedShield(new Properties().durability(336).component(UnifiedShield.VARIANT, Expansion.id("end_stone")))
   );
   public static final DeferredItem<Item> VOID_TORCH = ITEMS.register(
      "void_torch",
      () -> new StandingAndWallBlockItem(
         (Block)ExpansionBlocks.VOID_TORCH.get(), (Block)ExpansionBlocks.VOID_WALL_TORCH.get(), new Properties(), Direction.DOWN
      )
   );
   private static final DeferredRegister<ArmorMaterial> ARMOR = DeferredRegister.create(Registries.ARMOR_MATERIAL, "enderscape");
   public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SHADOLINE = ARMOR.register(
      "shadoline",
      () -> new ArmorMaterial(
         Map.of(Type.BOOTS, 2, Type.LEGGINGS, 5, Type.CHESTPLATE, 6, Type.HELMET, 2, Type.BODY, 6),
         15,
         SoundEvents.ARMOR_EQUIP_IRON,
         () -> Ingredient.of(new ItemLike[]{(ItemLike)EnderscapeItems.SHADOLINE_INGOT.get()}),
         List.of(new Layer(Expansion.id("shadoline"))),
         0.0F,
         0.0F
      )
   );
   public static final DeferredItem<Item> SHADOLINE_NUGGET = ITEMS.registerSimpleItem("shadoline_nugget");
   public static final DeferredItem<Item> RUSTLE_SILK = ITEMS.register("rustle_silk", () -> new RustleSilkItem(new Properties()));
   public static final DeferredItem<BlockItem> PURUBERRY = ITEMS.register(
      "puruberry",
      () -> new BlockItem(
         (Block)ExpansionBlocks.PURUBERRY_VINE.get(),
         new Properties()
            .food(
               new Builder()
                  .nutrition(6)
                  .saturationModifier(1.0F)
                  .alwaysEdible()
                  .effect(() -> new MobEffectInstance(ExpansionEffects.PURIFICATION, 300), 1.0F)
                  .build()
            )
      ) {
         public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            ItemStack result = super.finishUsingItem(stack, level, entity);
            if (entity instanceof Player player) {
               player.getCooldowns().addCooldown(this, 60);
            }

            return result;
         }
      }
   );
   public static final DeferredItem<ArmorItem> SHADOLINE_HELMET = armor("shadoline_helmet", Type.HELMET);
   public static final DeferredItem<ArmorItem> SHADOLINE_CHESTPLATE = armor("shadoline_chestplate", Type.CHESTPLATE);
   public static final DeferredItem<ArmorItem> SHADOLINE_LEGGINGS = armor("shadoline_leggings", Type.LEGGINGS);
   public static final DeferredItem<ArmorItem> SHADOLINE_BOOTS = armor("shadoline_boots", Type.BOOTS);
   public static final DeferredItem<DaggerItem> DAGGER = ITEMS.register(
      "dagger",
      () -> new DaggerItem(
         new Properties()
            .component(EnderscapeDataComponents.MAXIMUM_NEBULITE_FUEL, 5)
            .component(EnderscapeDataComponents.NEBULITE_FUEL_PER_USE, 1)
            .attributes(
               ItemAttributeModifiers.builder()
                  .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 3.0, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                  .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -1.5, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                  .build()
            )
      )
   );

   private static DeferredItem<ArmorItem> armor(String name, Type type) {
      return ITEMS.register(name, () -> new ArmorItem(SHADOLINE, type, new Properties().durability(type.getDurability(25))));
   }

   public static void register(IEventBus bus) {
      ARMOR.register(bus);
      ITEMS.register(bus);
   }
}
