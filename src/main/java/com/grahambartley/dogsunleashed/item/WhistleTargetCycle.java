package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.pet.PetData;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Target selection for the Dog Whistle, kept free of world and server types so the ordering
 * contract can be tested directly.
 */
public final class WhistleTargetCycle {

  private WhistleTargetCycle() {}

  /**
   * Alive pets ordered by pet id. Pack order has to be stable across sessions or repeated
   * sneak-clicks would land on a different dog each time the list came back in a new order.
   */
  public static List<PetData> orderedAlivePets(final List<PetData> pets) {
    return pets.stream()
        .filter(PetData::isAlive)
        .sorted(Comparator.comparing(pet -> pet.getPetId().toString()))
        .toList();
  }

  /** The pet after {@code current}, wrapping around, or null when the owner has no alive pets. */
  public static @Nullable UUID nextTarget(final List<PetData> pets, final @Nullable UUID current) {
    final List<PetData> ordered = orderedAlivePets(pets);
    if (ordered.isEmpty()) {
      return null;
    }
    final int index = indexOf(ordered, current);
    return ordered.get((index + 1) % ordered.size()).getPetId();
  }

  /**
   * The pet a blow should recall: the stack's stored target while it is still alive, otherwise the
   * alive pet whose last known position is closest to the player, so a whistle that has never been
   * pointed at anything still does the obvious thing.
   */
  public static @Nullable UUID resolveTarget(
      final List<PetData> pets,
      final @Nullable UUID current,
      final String dimension,
      final BlockPos origin) {
    final List<PetData> ordered = orderedAlivePets(pets);
    if (ordered.isEmpty()) {
      return null;
    }
    if (current != null && ordered.stream().anyMatch(pet -> current.equals(pet.getPetId()))) {
      return current;
    }
    return ordered.stream()
        .min(Comparator.comparingDouble(pet -> distanceRank(pet, dimension, origin)))
        .map(PetData::getPetId)
        .orElse(null);
  }

  /**
   * Pets in another dimension, or with no recorded position, sort behind every pet in this one
   * rather than being dropped: an unreachable target still beats no target at all.
   */
  private static double distanceRank(
      final PetData pet, final String dimension, final BlockPos origin) {
    final BlockPos position = pet.getLastKnownPosition();
    if (position == null || !Objects.equals(dimension, pet.getDimension())) {
      return Double.MAX_VALUE;
    }
    return position.getSquaredDistance(origin.getX(), origin.getY(), origin.getZ());
  }

  private static int indexOf(final List<PetData> ordered, final @Nullable UUID current) {
    for (int index = 0; index < ordered.size(); index++) {
      if (ordered.get(index).getPetId().equals(current)) {
        return index;
      }
    }
    return -1;
  }
}
