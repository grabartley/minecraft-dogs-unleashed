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
        true,
        true,
        false);
  }

  public static DogPortraitOutfit collarOnly(final DyeColor collar) {
    return new DogPortraitOutfit(
        Map.of(), Optional.of(collar), ItemStack.EMPTY, Optional.empty(), false, false, false);
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
