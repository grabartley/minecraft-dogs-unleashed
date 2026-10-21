package com.grahambartley.dogsunleashed.entity.personality;

import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;

public record DogTraits(List<DogTrait> traits) {

  public static final int MAX_TRAITS = 2;
  public static final DogTraits NONE = new DogTraits(List.of());

  public DogTraits {
    traits = compatible(traits);
  }

  public static DogTraits of(final DogTrait... traits) {
    return new DogTraits(List.of(traits));
  }

  private static List<DogTrait> compatible(final List<DogTrait> candidates) {
    final List<DogTrait> kept = new ArrayList<>(MAX_TRAITS);
    for (final DogTrait candidate : candidates) {
      if (kept.size() < MAX_TRAITS
          && !kept.contains(candidate)
          && kept.stream().noneMatch(candidate::conflictsWith)) {
        kept.add(candidate);
      }
    }
    return List.copyOf(kept);
  }

  public boolean has(final DogTrait trait) {
    return this.traits.contains(trait);
  }

  public boolean isEmpty() {
    return this.traits.isEmpty();
  }

  public boolean holdsGround() {
    return this.any(DogTrait::holdsGround);
  }

  public double fleeHealthFraction() {
    return this.traits.stream().mapToDouble(DogTrait::fleeHealthFraction).max().orElse(0.0);
  }

  public double stanceOffset() {
    return this.traits.stream()
        .mapToDouble(DogTrait::stanceOffset)
        .filter(offset -> offset != 0.0)
        .findFirst()
        .orElse(0.0);
  }

  public boolean growlsAtThreats() {
    return this.any(DogTrait::growlsAtThreats);
  }

  public boolean cowersAtThreats() {
    return this.any(DogTrait::cowersAtThreats);
  }

  public boolean joinsOwnerFights() {
    return this.traits.stream().allMatch(DogTrait::joinsOwnerFights);
  }

  public boolean snacks() {
    return this.any(DogTrait::snacks);
  }

  public boolean napsOnGround() {
    return this.any(DogTrait::napsOnGround);
  }

  public double napScale() {
    return this.product(DogTrait::napScale);
  }

  public double idleSpeedScale() {
    return this.product(DogTrait::idleSpeedScale);
  }

  public double followDistanceScale() {
    return this.product(DogTrait::followDistanceScale);
  }

  public boolean whinesWhenLonely() {
    return this.any(DogTrait::whinesWhenLonely);
  }

  public boolean greetsWithHearts() {
    return this.any(DogTrait::greetsWithHearts);
  }

  public boolean bringsToys() {
    return this.any(DogTrait::bringsToys);
  }

  private boolean any(final Predicate<DogTrait> effect) {
    return this.traits.stream().anyMatch(effect);
  }

  private double product(final ToDoubleFunction<DogTrait> scale) {
    return this.traits.stream().mapToDouble(scale).reduce(1.0, (a, b) -> a * b);
  }

  public NbtList toNbt() {
    final NbtList list = new NbtList();
    this.traits.forEach(trait -> list.add(NbtString.of(trait.serializedId())));
    return list;
  }

  public static DogTraits fromNbt(final NbtList list) {
    final List<DogTrait> traits = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).getType() == NbtElement.STRING_TYPE) {
        final DogTrait trait = DogTrait.fromSerializedIdOrNull(list.getString(i));
        if (trait != null) {
          traits.add(trait);
        }
      }
    }
    return new DogTraits(traits);
  }

  public void write(final PacketByteBuf buf) {
    buf.writeVarInt(this.traits.size());
    this.traits.forEach(trait -> buf.writeVarInt(trait.ordinal()));
  }

  public static DogTraits read(final PacketByteBuf buf) {
    final int count = buf.readVarInt();
    if (count < 0 || count > MAX_TRAITS) {
      throw new DecoderException("Too many dog traits: " + count);
    }
    final List<DogTrait> traits = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      final int ordinal = buf.readVarInt();
      if (ordinal >= 0 && ordinal < DogTrait.values().length) {
        traits.add(DogTrait.values()[ordinal]);
      }
    }
    return new DogTraits(traits);
  }
}
