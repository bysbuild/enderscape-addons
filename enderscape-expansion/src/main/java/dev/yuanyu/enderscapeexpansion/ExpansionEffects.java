package dev.yuanyu.enderscapeexpansion;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;

public final class ExpansionEffects {
   private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "enderscape");
   private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(Keys.ATTACHMENT_TYPES, "enderscape_expansion");
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> STRUCTURE_MUSIC = ATTACHMENTS.register(
      "structure_music", () -> AttachmentType.builder(() -> "").sync(ByteBufCodecs.STRING_UTF8).build()
   );
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> ATTACK_TICKS = ATTACHMENTS.register(
      "attack_ticks", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build()
   );
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> PREPARE_DASH = ATTACHMENTS.register(
      "prepare_dash", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build()
   );
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> RUSTLE_CONVERSION = ATTACHMENTS.register(
      "rustle_conversion", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build()
   );
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> HAVEN = ATTACHMENTS.register(
      "end_haven", () -> AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).copyOnDeath().sync(ByteBufCodecs.COMPOUND_TAG).build()
   );
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<Float>> VOIDED_HEALTH = ATTACHMENTS.register(
      "voided_health", () -> AttachmentType.builder(() -> 0.0F).serialize(Codec.FLOAT).sync(ByteBufCodecs.FLOAT).build()
   );
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> VOID_TICKS = ATTACHMENTS.register(
      "void_ticks", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build()
   );
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> OUTER_VOID = ATTACHMENTS.register(
      "outer_void_ticks", () -> AttachmentType.builder(() -> 0).sync(ByteBufCodecs.VAR_INT).build()
   );
   public static final DeferredHolder<MobEffect, MobEffect> STUNNED = EFFECTS.register(
      "stunned", () -> new ExpansionEffects.Effect(MobEffectCategory.HARMFUL, 399636, 0)
   );
   public static final DeferredHolder<MobEffect, MobEffect> CORRUPTION = EFFECTS.register(
      "void_corruption", () -> new ExpansionEffects.Effect(MobEffectCategory.HARMFUL, 4664672, 1)
   );
   public static final DeferredHolder<MobEffect, MobEffect> PURIFICATION = EFFECTS.register(
      "void_purification", () -> new ExpansionEffects.Effect(MobEffectCategory.BENEFICIAL, 16766297, 2)
   );
   public static final DeferredHolder<MobEffect, MobEffect> RESISTANCE = EFFECTS.register(
      "void_resistance", () -> new ExpansionEffects.Effect(MobEffectCategory.BENEFICIAL, 4664672, 3)
   );

   public static void register(IEventBus bus) {
      EFFECTS.register(bus);
      ATTACHMENTS.register(bus);
   }

   private static final class Effect extends MobEffect {
      private final int mode;

      Effect(MobEffectCategory category, int color, int mode) {
         super(category, color);
         this.mode = mode;
      }

      public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
         return this.mode == 1 || this.mode == 2 && duration % Math.max(1, 80 >> Math.min(amplifier, 7)) == 0;
      }

      public boolean applyEffectTick(LivingEntity entity, int amplifier) {
         if (entity.level().isClientSide()) {
            return true;
         } else {
            if (this.mode == 1) {
               if (VoidSystem.tagged(entity, "healed_by_void_corruption")) {
                  if (entity.tickCount % Math.max(1, 50 >> Math.min(amplifier, 6)) == 0) {
                     entity.heal(1.0F);
                  }
               } else {
                  VoidSystem.corrupt(entity, 1 << Math.min(amplifier, 10));
               }
            } else if (this.mode == 2) {
               if (VoidSystem.tagged(entity, "hurt_by_void_purification")) {
                  entity.hurt(VoidSystem.damage(entity, "void_purification"), 4.0F);
               } else if ((Float)entity.getData(ExpansionEffects.VOIDED_HEALTH) > 0.0F) {
                  VoidSystem.purify(entity, 2.0F);
               } else if (VoidSystem.tagged(entity, "healed_by_void_purification")) {
                  entity.heal(1.0F);
               }
            }

            return true;
         }
      }
   }
}
