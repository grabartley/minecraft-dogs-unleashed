package com.grahambartley.dogsunleashed.render.armour;

import com.grahambartley.dogsunleashed.ModArmorItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.passive.Cracks;
import net.minecraft.item.ItemStack;

public enum ArmourLook {
  LEATHER(
      Pattern.STITCHED,
      new Palette(0xFF8A8A8A, 0xFFB4B4B4, 0xFFCDCDCD, 0xFFE4E4E4, 0xFFF2F2F2, 0xFFE9DBB4),
      true),
  IRON(
      Pattern.PLATES,
      new Palette(0xFF5A5A5A, 0xFF8F8F8F, 0xFFC6C6C6, 0xFFDCDCDC, 0xFFFFFFFF, 0xFFF0F0F0),
      false),
  GOLD(
      Pattern.PLATES,
      new Palette(0xFF8C5A11, 0xFFC98A16, 0xFFF2C83A, 0xFFFCE57E, 0xFFFFFAD0, 0xFFFFF3B0),
      false),
  DIAMOND(
      Pattern.PLATES,
      new Palette(0xFF1B6E6A, 0xFF2BB3A8, 0xFF4AEDD9, 0xFFA1FBE8, 0xFFE2FFF7, 0xFFD8FFF5),
      false),
  NETHERITE(
      Pattern.PLATES,
      new Palette(0xFF241D20, 0xFF3A3135, 0xFF4D4347, 0xFF645A5E, 0xFF857A7E, 0xFFB08D57),
      false),
  WOLF(
      Pattern.SCUTES,
      new Palette(0xFF5C2D30, 0xFF713E3E, 0xFF965954, 0xFFB67B76, 0xFFC68884, 0xFFC68884),
      false);

  public static final int UNDYED_LEATHER = 0xFFA06540;
  static final int STRAP = 0xFF4A2E1A;
  static final int STRAP_LIGHT = 0xFF6B4528;
  static final int BUCKLE = 0xFFD8B048;

  public enum Pattern {
    PLATES,
    STITCHED,
    SCUTES
  }

  public record Palette(int edge, int shade, int base, int light, int highlight, int accent) {}

  private final Pattern pattern;
  private final Palette palette;
  private final boolean fillIsDyed;

  ArmourLook(final Pattern pattern, final Palette palette, final boolean fillIsDyed) {
    this.pattern = pattern;
    this.palette = palette;
    this.fillIsDyed = fillIsDyed;
  }

  public Pattern pattern() {
    return this.pattern;
  }

  public Palette palette() {
    return this.palette;
  }

  public boolean fillIsDyed() {
    return this.fillIsDyed;
  }

  public static ArmourLook of(final ItemStack stack) {
    return ModArmorItems.tierOf(stack).map(tier -> valueOf(tier.name())).orElse(WOLF);
  }

  public static int crackLevel(final ItemStack worn) {
    return switch (Cracks.WOLF_ARMOR.getCrackLevel(worn)) {
      case NONE -> 0;
      case LOW -> 1;
      case MEDIUM -> 2;
      case HIGH -> 3;
    };
  }

  public static boolean trimIsDyed(final ArmourLook look, final ItemStack stack) {
    return look == WOLF && stack.contains(DataComponentTypes.DYED_COLOR);
  }
}
