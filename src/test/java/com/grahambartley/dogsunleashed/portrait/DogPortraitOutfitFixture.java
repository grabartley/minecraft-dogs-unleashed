package com.grahambartley.dogsunleashed.portrait;

import static com.grahambartley.dogsunleashed.TestRegistries.REGISTRIES;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public final class DogPortraitOutfitFixture {

  private DogPortraitOutfitFixture() {}

  public static DogPortraitOutfit dressed() {
    return new DogPortraitOutfit(
        Map.of(
            DogEquipmentSlot.ARMOUR,
            new ItemStack(Items.WOLF_ARMOR),
            DogEquipmentSlot.COSMETIC,
            new ItemStack(Items.CARVED_PUMPKIN),
            DogEquipmentSlot.SADDLEBAGS,
            new ItemStack(ModItems.SADDLEBAGS)),
        Optional.of(DyeColor.LIME),
        ItemStack.EMPTY,
        Optional.empty(),
        true);
  }

  public static DogPortraitOutfit collarOnly(final DyeColor collar) {
    return new DogPortraitOutfit(
        Map.of(), Optional.of(collar), ItemStack.EMPTY, Optional.empty(), false);
  }

  public static DogPortraitOutfit wearing(final Map<DogEquipmentSlot, ItemStack> gear) {
    return new DogPortraitOutfit(gear, Optional.empty(), ItemStack.EMPTY, Optional.empty(), false);
  }

  public static DogPortraitOutfit carrying(
      final ItemStack carried, final Optional<Identifier> fetchType) {
    return new DogPortraitOutfit(Map.of(), Optional.empty(), carried, fetchType, false);
  }

  public static Stream<DogPortraitOutfit> samples() {
    return Stream.of(DogPortraitOutfit.NONE, dressed());
  }

  public static NbtElement save(final DogPortraitOutfit outfit) {
    return DogPortraitOutfit.CODEC
        .encodeStart(REGISTRIES.getOps(NbtOps.INSTANCE), outfit)
        .getOrThrow();
  }
}
