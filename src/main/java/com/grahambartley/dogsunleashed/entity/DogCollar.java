package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.DyeColor;

public final class DogCollar extends DogComponent {

  public static final DyeColor DEFAULT_COLOR = DyeColor.RED;

  private final UnleashedDogEntity dog;

  DogCollar(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public DyeColor getColor() {
    return DyeColor.byId(this.dog.getDataTracker().get(UnleashedDogEntity.COLLAR_COLOR));
  }

  public void setColor(final DyeColor color) {
    this.dog.getDataTracker().set(UnleashedDogEntity.COLLAR_COLOR, color.getId());
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    nbt.putInt(ModNbtKeys.COLLAR_COLOR, this.getColor().getId());
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    this.setColor(colorFromSave(nbt, this.getColor()));
  }

  static DyeColor colorFromSave(final NbtCompound nbt, final DyeColor current) {
    return nbt.contains(ModNbtKeys.COLLAR_COLOR, NbtElement.NUMBER_TYPE)
        ? DyeColor.byId(nbt.getInt(ModNbtKeys.COLLAR_COLOR))
        : current;
  }
}
