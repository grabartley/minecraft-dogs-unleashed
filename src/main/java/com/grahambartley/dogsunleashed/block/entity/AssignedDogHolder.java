package com.grahambartley.dogsunleashed.block.entity;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.UUID;
import net.minecraft.world.World;

public interface AssignedDogHolder {

  boolean hasAssignedDog();

  UUID getAssignedDogUuid();

  void setAssignedDog(UnleashedDogEntity dog);

  void clearAssignedDog(World world);

  UnleashedDogEntity getAssignedDog(World world);
}
