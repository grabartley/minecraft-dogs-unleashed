package com.grahambartley.dogsunleashed.entity.expedition;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public record Expedition(
    UUID pet,
    UUID owner,
    Identifier entityType,
    NbtCompound dog,
    Identifier originDimension,
    int findX,
    int findZ,
    long returnTimeOfDay,
    long departedGameTime) {

  public boolean isDue(final long timeOfDay, final long gameTime) {
    return ExpeditionSchedule.isDue(
        this.returnTimeOfDay, this.departedGameTime, timeOfDay, gameTime);
  }

  public NbtCompound toNbt() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putUuid(ModNbtKeys.EXPEDITION_PET, this.pet);
    nbt.putUuid(ModNbtKeys.EXPEDITION_OWNER, this.owner);
    nbt.putString(ModNbtKeys.EXPEDITION_ENTITY_TYPE, this.entityType.toString());
    nbt.put(ModNbtKeys.EXPEDITION_DOG, this.dog.copy());
    nbt.putString(ModNbtKeys.EXPEDITION_ORIGIN_DIMENSION, this.originDimension.toString());
    nbt.putInt(ModNbtKeys.EXPEDITION_FIND_X, this.findX);
    nbt.putInt(ModNbtKeys.EXPEDITION_FIND_Z, this.findZ);
    nbt.putLong(ModNbtKeys.EXPEDITION_RETURN_TIME, this.returnTimeOfDay);
    nbt.putLong(ModNbtKeys.EXPEDITION_DEPARTED_AT, this.departedGameTime);
    return nbt;
  }

  public static Optional<Expedition> fromNbt(final NbtCompound nbt) {
    if (!nbt.containsUuid(ModNbtKeys.EXPEDITION_PET)
        || !nbt.containsUuid(ModNbtKeys.EXPEDITION_OWNER)
        || !nbt.contains(ModNbtKeys.EXPEDITION_DOG, NbtElement.COMPOUND_TYPE)) {
      return Optional.empty();
    }
    final Identifier entityType =
        Identifier.tryParse(nbt.getString(ModNbtKeys.EXPEDITION_ENTITY_TYPE));
    final Identifier origin =
        Identifier.tryParse(nbt.getString(ModNbtKeys.EXPEDITION_ORIGIN_DIMENSION));
    if (entityType == null || origin == null) {
      return Optional.empty();
    }
    return Optional.of(
        new Expedition(
            nbt.getUuid(ModNbtKeys.EXPEDITION_PET),
            nbt.getUuid(ModNbtKeys.EXPEDITION_OWNER),
            entityType,
            nbt.getCompound(ModNbtKeys.EXPEDITION_DOG),
            origin,
            nbt.getInt(ModNbtKeys.EXPEDITION_FIND_X),
            nbt.getInt(ModNbtKeys.EXPEDITION_FIND_Z),
            nbt.getLong(ModNbtKeys.EXPEDITION_RETURN_TIME),
            nbt.getLong(ModNbtKeys.EXPEDITION_DEPARTED_AT)));
  }
}
