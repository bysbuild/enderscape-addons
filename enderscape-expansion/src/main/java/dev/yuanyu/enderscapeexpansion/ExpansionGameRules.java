package dev.yuanyu.enderscapeexpansion;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Category;
import net.minecraft.world.level.GameRules.Key;

public final class ExpansionGameRules {
   public static final Key<BooleanValue> VOID_HEALTH = GameRules.register("enderscapeVoidDamageOverridesHealth", Category.PLAYER, BooleanValue.create(true));
   public static final Key<BooleanValue> VOID_SOURCES = GameRules.register(
      "enderscapeVoidLachrymaSourceConversion", Category.UPDATES, BooleanValue.create(false)
   );

   public static void init() {
   }
}
