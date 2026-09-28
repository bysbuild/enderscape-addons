package dev.yuanyu.enderscapeexpansion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ExpansionSounds {
   private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "enderscape");

   public static void register(IEventBus bus) {
      try (InputStream input = ExpansionSounds.class.getResourceAsStream("/expansion-sounds.txt")) {
         if (input == null) {
            throw new IllegalStateException("Missing generated sound manifest");
         }

         for (String name : new String(input.readAllBytes(), StandardCharsets.UTF_8).lines().toList()) {
            if (!name.isBlank()) {
               SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Expansion.id(name)));
            }
         }
      } catch (IOException var6) {
         throw new IllegalStateException(var6);
      }

      SOUNDS.register(bus);
   }
}
