package com.grahambartley.dogsunleashed.portrait;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public record DogPortraitOutfit(
    Map<DogEquipmentSlot, ItemStack> gear,
    Optional<DyeColor> collar,
    ItemStack carried,
    Optional<Identifier> fetchType,
    boolean muddy) {

  public static final DogPortraitOutfit NONE =
      new DogPortraitOutfit(Map.of(), Optional.empty(), ItemStack.EMPTY, Optional.empty(), false);

  private static final Codec<DogEquipmentSlot> SLOT_CODEC =
      Codec.STRING.comapFlatMap(
          id ->
              Optional.ofNullable(DogEquipmentSlot.fromId(id))
                  .map(DataResult::success)
                  .orElseGet(() -> DataResult.error(() -> "Unknown equipment slot " + id)),
          DogEquipmentSlot::id);

  public static final Codec<DogPortraitOutfit> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.unboundedMap(SLOT_CODEC, PortraitGear.LENIENT_CODEC)
                          .lenientOptionalFieldOf("gear", Map.of())
                          .forGetter(DogPortraitOutfit::gear),
                      DyeColor.CODEC
                          .lenientOptionalFieldOf("collar")
                          .forGetter(DogPortraitOutfit::collar),
                      PortraitGear.LENIENT_CODEC
                          .optionalFieldOf("carried", ItemStack.EMPTY)
                          .forGetter(DogPortraitOutfit::carried),
                      Identifier.CODEC
                          .lenientOptionalFieldOf("fetch_type")
                          .forGetter(DogPortraitOutfit::fetchType),
                      Codec.BOOL
                          .optionalFieldOf("muddy", false)
                          .forGetter(DogPortraitOutfit::muddy))
                  .apply(instance, DogPortraitOutfit::new));

  public static final PacketCodec<RegistryByteBuf, DogPortraitOutfit> PACKET_CODEC =
      PacketCodecs.registryCodec(CODEC);

  public DogPortraitOutfit {
    final Map<DogEquipmentSlot, ItemStack> worn = new EnumMap<>(DogEquipmentSlot.class);
    gear.forEach(
        (slot, stack) -> {
          final ItemStack shown = PortraitGear.shownPart(stack);
          if (!shown.isEmpty()) {
            worn.put(slot, shown);
          }
        });
    gear = Map.copyOf(worn);
    carried = PortraitGear.shownPart(carried);
  }

  @Override
  public Map<DogEquipmentSlot, ItemStack> gear() {
    final Map<DogEquipmentSlot, ItemStack> copies = new EnumMap<>(DogEquipmentSlot.class);
    this.gear.forEach((slot, stack) -> copies.put(slot, stack.copy()));
    return copies;
  }

  @Override
  public ItemStack carried() {
    return this.carried.copy();
  }

  public ItemStack worn(final DogEquipmentSlot slot) {
    return this.gear.getOrDefault(slot, ItemStack.EMPTY).copy();
  }

  public boolean carrying() {
    return !this.carried.isEmpty() || this.fetchType.isPresent();
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof DogPortraitOutfit that)) {
      return false;
    }
    return this.muddy == that.muddy
        && this.collar.equals(that.collar)
        && this.fetchType.equals(that.fetchType)
        && ItemStack.areEqual(this.carried, that.carried)
        && this.gear.keySet().equals(that.gear.keySet())
        && this.gear.entrySet().stream()
            .allMatch(entry -> ItemStack.areEqual(entry.getValue(), that.gear.get(entry.getKey())));
  }

  @Override
  public int hashCode() {
    int gearHash = 0;
    for (final Map.Entry<DogEquipmentSlot, ItemStack> entry : this.gear.entrySet()) {
      gearHash += entry.getKey().hashCode() ^ ItemStack.hashCode(entry.getValue());
    }
    return Objects.hash(
        gearHash, this.collar, ItemStack.hashCode(this.carried), this.fetchType, this.muddy);
  }
}
