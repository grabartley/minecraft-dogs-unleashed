package com.grahambartley.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.world.World;

public class DalmatianEntity extends WolfEntity {

  public DalmatianEntity(EntityType<? extends WolfEntity> entityType, World world) {
    super(entityType, world);
  }

  // Add custom behavior here if needed
  // For now, inherits all vanilla wolf behavior
}
