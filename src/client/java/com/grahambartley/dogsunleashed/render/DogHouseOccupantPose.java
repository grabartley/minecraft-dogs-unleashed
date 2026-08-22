package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;

/**
 * Where a sleeping dog is drawn inside a dog house.
 *
 * <p>The shared sleep animation rolls the dog ninety degrees onto its side, which carries its body
 * off to one side of the point it actually occupies. On an open bed nobody notices; framed by a
 * doorway it reads as a dog shoved up against the door post, so the model is nudged back over its
 * own position.
 *
 * <p>This is deliberately a drawing offset and not a change to where the dog stands. Moving the
 * entity would take its hitbox off centre with it, and the hitbox is what has to stay honest
 * against the walls it is sitting between.
 */
public final class DogHouseOccupantPose {

  /**
   * Sideways travel of the rolled body, in model units. The offset is applied while the pose stack
   * is still in model space, so the dog's own scale is already in the matrix and one value covers
   * every breed. Measured from the rendered result: the roll comes out of the animation's pivot
   * rather than anything the rig declares.
   */
  static final float ROLL_OFFSET_UNITS = -0.38f;

  private DogHouseOccupantPose() {}

  /**
   * The assigned bed position is server-side state, so occupancy is read from the block the dog is
   * standing in. A sleeping dog lies at the middle of its house's footprint, which is inside it.
   */
  public static boolean isInDogHouse(final UnleashedDogEntity dog) {
    return dog.isSleepingInBed()
        && dog.getWorld().getBlockState(dog.getBlockPos()).isOf(ModBlocks.DOG_HOUSE);
  }
}
