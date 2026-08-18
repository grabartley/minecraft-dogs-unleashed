package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.DogGraveBlock;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

public final class DogGraveSpawner {

  private static final int GRAVE_SEARCH_RADIUS = 3;
  private static final int GRAVE_SEARCH_MIN_Y = -2;
  private static final int GRAVE_SEARCH_MAX_Y = 2;
  private static final int HORIZONTAL_DIRECTION_COUNT = 4;

  private static final List<Vec3i> SEARCH_OFFSETS = buildSearchOffsets();

  private DogGraveSpawner() {}

  public static void spawnGrave(
      final ServerWorld world,
      final UnleashedDogEntity dog,
      final @Nullable BlockPos bedPosToAvoid) {
    final BlockPos gravePos = findValidGravePosition(world, dog.getBlockPos(), bedPosToAvoid);
    if (gravePos == null) {
      return;
    }

    final Direction facing =
        Direction.Type.HORIZONTAL.stream()
            .toList()
            .get(world.getRandom().nextInt(HORIZONTAL_DIRECTION_COUNT));
    world.setBlockState(
        gravePos, ModBlocks.DOG_GRAVE.getDefaultState().with(DogGraveBlock.FACING, facing));

    if (world.getBlockEntity(gravePos) instanceof DogGraveBlockEntity graveEntity) {
      graveEntity.setDogUuid(dog.getUuid());
      graveEntity.setFlowerColor(dog.getCollarColor());

      final PetManager petManager = PetManager.get(world.getServer());
      final PetData petData = petManager.getPetByEntityId(dog.getUuid());
      final String dogName = petData != null ? petData.getName() : dog.getName().getString();

      graveEntity.setDogName(dogName);
    }
  }

  static @Nullable BlockPos findValidGravePosition(
      final ServerWorld world, final BlockPos center, final @Nullable BlockPos bedPosToAvoid) {
    // Two passes so an air block anywhere in range always beats a merely replaceable one nearer
    // the death position.
    for (final Vec3i offset : SEARCH_OFFSETS) {
      final BlockPos testPos = center.add(offset);
      if (testPos.equals(bedPosToAvoid)) {
        continue;
      }
      if (world.getBlockState(testPos).isAir() && isValidGravePosition(world, testPos)) {
        return testPos;
      }
    }

    for (final Vec3i offset : SEARCH_OFFSETS) {
      final BlockPos testPos = center.add(offset);
      if (testPos.equals(bedPosToAvoid)) {
        continue;
      }
      if (isValidGravePosition(world, testPos)) {
        return testPos;
      }
    }

    return null;
  }

  static boolean isValidGravePosition(final ServerWorld world, final BlockPos pos) {
    final BlockState stateAtPos = world.getBlockState(pos);
    final BlockState stateBelow = world.getBlockState(pos.down());
    final BlockState stateAbove = world.getBlockState(pos.up());

    final boolean hasValidGround =
        !stateBelow.isAir() && stateBelow.isSolidBlock(world, pos.down());
    final boolean canReplace = stateAtPos.isReplaceable() || stateAtPos.isAir();
    final boolean hasAirAbove = stateAbove.isAir() || stateAbove.isReplaceable();
    final boolean notInFluid = !stateAtPos.isOf(Blocks.WATER) && !stateAtPos.isOf(Blocks.LAVA);

    return hasValidGround && canReplace && hasAirAbove && notInFluid;
  }

  public static List<Vec3i> searchOffsets() {
    return SEARCH_OFFSETS;
  }

  private static List<Vec3i> buildSearchOffsets() {
    final List<Vec3i> offsets = new ArrayList<>();
    for (int radius = 0; radius <= GRAVE_SEARCH_RADIUS; radius++) {
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
            continue;
          }
          for (int dy = GRAVE_SEARCH_MIN_Y; dy <= GRAVE_SEARCH_MAX_Y; dy++) {
            offsets.add(new Vec3i(dx, dy, dz));
          }
        }
      }
    }
    return List.copyOf(offsets);
  }
}
