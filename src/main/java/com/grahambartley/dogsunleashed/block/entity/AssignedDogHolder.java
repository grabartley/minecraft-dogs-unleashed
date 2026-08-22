package com.grahambartley.dogsunleashed.block.entity;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.UUID;
import net.minecraft.world.World;

/** A block a dog can be assigned to sleep in. Implemented by the dog bed and the dog house. */
public interface AssignedDogHolder {

  boolean hasAssignedDog();

  UUID getAssignedDogUuid();

  void setAssignedDog(UnleashedDogEntity dog);

  void clearAssignedDog(World world);

  UnleashedDogEntity getAssignedDog(World world);
}
