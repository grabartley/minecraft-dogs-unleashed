package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.block.DogStatueSourceRules.Candidate;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class DogStatueSources {

  private static final int REACH = (int) Math.ceil(DogStatueSourceRules.REACH_BLOCKS);

  private DogStatueSources() {}

  public static Optional<DogStatue> nearest(
      final ServerWorld world, final BlockPos base, final UUID ownerId) {
    final Vec3d centre = Vec3d.ofCenter(base);
    final PetManager pets = PetManager.get(world.getServer());
    final List<Candidate<DogStatue>> dogs =
        world
            .getEntitiesByClass(
                UnleashedDogEntity.class,
                new Box(base).expand(DogStatueSourceRules.REACH_BLOCKS),
                dog -> dog.isAlive() && ownerId.equals(dog.getOwnerUuid()))
            .stream()
            .map(dog -> new Candidate<>(carvingOf(pets, dog), dog.squaredDistanceTo(centre)))
            .toList();
    return DogStatueSourceRules.pick(dogs, () -> graves(world, pets, base, ownerId));
  }

  private static DogStatue carvingOf(final PetManager pets, final UnleashedDogEntity dog) {
    if (dog.hasCustomName()) {
      return DogStatue.carved(DogStatueLook.of(dog), dog.getCustomName().getString());
    }
    final PetData pet = pets.getPetByEntityId(dog.getUuid());
    return DogStatue.carved(DogStatueLook.of(dog), pet == null ? "" : pet.getName());
  }

  private static List<Candidate<DogStatue>> graves(
      final ServerWorld world, final PetManager pets, final BlockPos base, final UUID ownerId) {
    final List<Candidate<DogStatue>> graves = new ArrayList<>();
    for (final BlockPos pos :
        BlockPos.iterate(base.add(-REACH, -REACH, -REACH), base.add(REACH, REACH, REACH))) {
      if (!(world.getBlockEntity(pos) instanceof DogGraveBlockEntity grave)
          || grave.getDogUuid() == null) {
        continue;
      }
      final PetData pet = pets.getPet(ownerId, grave.getDogUuid());
      if (pet != null) {
        graves.add(
            new Candidate<>(
                DogStatue.carved(
                    pet.getLook(),
                    grave.getDogName().isEmpty() ? pet.getName() : grave.getDogName()),
                pos.getSquaredDistance(base)));
      }
    }
    return graves;
  }
}
