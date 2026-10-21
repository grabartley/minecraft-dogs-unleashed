package com.grahambartley.dogsunleashed.entity.delivery;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public record Delivery(
    UUID pet,
    UUID owner,
    UUID recipient,
    Identifier entityType,
    NbtCompound dog,
    DeliveryLeg leg,
    long legTicks,
    long legEndsAt) {

  public boolean isLegOver(final long gameTime) {
    return gameTime >= this.legEndsAt;
  }

  public Delivery waiting(final long gameTime, final long waitTicks) {
    return new Delivery(
        this.pet,
        this.owner,
        this.recipient,
        this.entityType,
        new NbtCompound(),
        DeliveryLeg.WAITING,
        this.legTicks,
        gameTime + waitTicks);
  }

  public Delivery homeward(final NbtCompound snapshot, final long gameTime, final long ticks) {
    return new Delivery(
        this.pet,
        this.owner,
        this.recipient,
        this.entityType,
        snapshot,
        DeliveryLeg.TO_OWNER,
        ticks,
        gameTime + ticks);
  }

  public NbtCompound toNbt() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putUuid(ModNbtKeys.DELIVERY_PET, this.pet);
    nbt.putUuid(ModNbtKeys.DELIVERY_OWNER, this.owner);
    nbt.putUuid(ModNbtKeys.DELIVERY_RECIPIENT, this.recipient);
    nbt.putString(ModNbtKeys.DELIVERY_ENTITY_TYPE, this.entityType.toString());
    nbt.put(ModNbtKeys.DELIVERY_DOG, this.dog.copy());
    nbt.putString(ModNbtKeys.DELIVERY_LEG, this.leg.id());
    nbt.putLong(ModNbtKeys.DELIVERY_LEG_TICKS, this.legTicks);
    nbt.putLong(ModNbtKeys.DELIVERY_LEG_ENDS_AT, this.legEndsAt);
    return nbt;
  }

  public static Optional<Delivery> fromNbt(final NbtCompound nbt) {
    if (!nbt.containsUuid(ModNbtKeys.DELIVERY_PET)
        || !nbt.containsUuid(ModNbtKeys.DELIVERY_OWNER)
        || !nbt.containsUuid(ModNbtKeys.DELIVERY_RECIPIENT)
        || !nbt.contains(ModNbtKeys.DELIVERY_DOG, NbtElement.COMPOUND_TYPE)) {
      return Optional.empty();
    }
    final Identifier entityType =
        Identifier.tryParse(nbt.getString(ModNbtKeys.DELIVERY_ENTITY_TYPE));
    final DeliveryLeg leg = DeliveryLeg.fromId(nbt.getString(ModNbtKeys.DELIVERY_LEG));
    if (entityType == null || leg == null) {
      return Optional.empty();
    }
    return Optional.of(
        new Delivery(
            nbt.getUuid(ModNbtKeys.DELIVERY_PET),
            nbt.getUuid(ModNbtKeys.DELIVERY_OWNER),
            nbt.getUuid(ModNbtKeys.DELIVERY_RECIPIENT),
            entityType,
            nbt.getCompound(ModNbtKeys.DELIVERY_DOG),
            leg,
            nbt.getLong(ModNbtKeys.DELIVERY_LEG_TICKS),
            nbt.getLong(ModNbtKeys.DELIVERY_LEG_ENDS_AT)));
  }
}
