package dev.yuanyu.enderscapeexpansion;

public final class EndAmbientLight {
   private EndAmbientLight() {
   }

   public static int brighten(int color) {
      int red = Math.max(color & 0xFF, 140);
      int green = Math.max(color >>> 8 & 0xFF, 145);
      int blue = Math.max(color >>> 16 & 0xFF, 140);
      return color & 0xFF000000 | blue << 16 | green << 8 | red;
   }
}
