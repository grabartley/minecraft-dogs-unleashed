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
import net.minecraft.util.DyeColor;

public record DogPortraitOutfit(
    Map<DogEquipmentSlot, ItemStack> gear,
    Optional<DyeColor> collar,
    ItemStack carried,
    boolean muddy,
    boolean senior,
    boolean undead) {

  public static final DogPortraitOutfit NONE =
      new DogPortraitOutfit(Map.of(), Optional.empty(), ItemStack.EMPTY, false, false, false);

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
                      Codec.unboundedMap(SLOT_CODEC, ItemStack.CODEC)
                          .optionalFieldOf("gear", Map.of())
                          .forGetter(DogPortraitOutfit::gear),
                      DyeColor.CODEC.optionalFieldOf("collar").forGetter(DogPortraitOutfit::collar),
                      ItemStack.OPTIONAL_CODEC
                          .optionalFieldOf("carried", ItemStack.EMPTY)
                          .forGetter(DogPortraitOutfit::carried),
                      Codec.BOOL
                          .optionalFieldOf("muddy", false)
                          .forGetter(DogPortraitOutfit::muddy),
                      Codec.BOOL
                          .optionalFieldOf("senior", false)
                          .forGetter(DogPortraitOutfit::senior),
                      Codec.BOOL
                          .optionalFieldOf("undead", false)
                          .forGetter(DogPortraitOutfit::undead))
                  .apply(instance, DogPortraitOutfit::new));

  public DogPortraitOutfit {
    final Map<DogEquipmentSlot, ItemStack> worn = new EnumMap<>(DogEquipmentSlot.class);
    gear.forEach(
        (slot, stack) -> {
          if (!stack.isEmpty()) {
            worn.put(slot, stack.copy());
          }
        });
    gear = Map.copyOf(worn);
    carried = carried.copy();
  }

  public ItemStack worn(final DogEquipmentSlot slot) {
    return this.gear.getOrDefault(slot, ItemStack.EMPTY).copy();
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof DogPortraitOutfit that)) {
      return false;
    }
    return this.muddy == that.muddy
        && this.senior == that.senior
        && this.undead == that.undead
        && this.collar.equals(that.collar)
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
        gearHash,
        this.collar,
        ItemStack.hashCode(this.carried),
        this.muddy,
        this.senior,
        this.undead);
  }
}
