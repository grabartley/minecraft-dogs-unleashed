package com.grahambartley.dogsunleashed.entity;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class DogPointedOrders extends DogComponent {

  private final UnleashedDogEntity dog;

  private @Nullable LivingEntity attackTarget;
  private @Nullable ItemEntity fetchItem;

  DogPointedOrders(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public void attack(final LivingEntity target) {
    this.prepareForOrder();
    this.attackTarget = target;
  }

  public void fetch(final ItemEntity item) {
    this.prepareForOrder();
    this.fetchItem = item;
  }

  public @Nullable LivingEntity attackTarget() {
    return this.attackTarget;
  }

  public @Nullable ItemEntity fetchItem() {
    return this.fetchItem;
  }

  public void clearAttack() {
    this.attackTarget = null;
  }

  public void clearFetch() {
    this.fetchItem = null;
  }

  void clear() {
    this.attackTarget = null;
    this.fetchItem = null;
  }

  private void prepareForOrder() {
    this.clear();
    this.dog.getComponents().get(DogScent.class).stop();
    this.dog.setTarget(null);
    this.dog.getNavigation().stop();
  }
}
