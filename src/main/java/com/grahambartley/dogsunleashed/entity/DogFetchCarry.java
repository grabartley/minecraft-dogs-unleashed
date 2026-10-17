package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class DogFetchCarry {

  private final UnleashedDogEntity dog;

  DogFetchCarry(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  String activeTypeId() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.ACTIVE_FETCH_TYPE_ID);
  }

  void setActiveTypeId(final String activeFetchTypeId) {
    this.dog.getDataTracker().set(UnleashedDogEntity.ACTIVE_FETCH_TYPE_ID, activeFetchTypeId);
  }

  public @Nullable FetchItemType activeType() {
    return DogPlaySession.fetchTypeFromId(this.activeTypeId());
  }

  public void setActiveType(final @Nullable FetchItemType activeFetchType) {
    this.setActiveTypeId(DogPlaySession.fetchTypeIdOf(activeFetchType));
  }

  public boolean isCarrying() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.IS_CARRYING_FETCH_ITEM);
  }

  public void setCarrying(final boolean carrying) {
    this.dog.getDataTracker().set(UnleashedDogEntity.IS_CARRYING_FETCH_ITEM, carrying);
    if (!carrying) {
      this.dog.getDataTracker().set(UnleashedDogEntity.CARRIED_FETCH_ITEM_STACK, ItemStack.EMPTY);
    }
  }

  public ItemStack carriedStack() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.CARRIED_FETCH_ITEM_STACK);
  }

  public void setCarriedStack(final ItemStack stack) {
    this.dog.getDataTracker().set(UnleashedDogEntity.CARRIED_FETCH_ITEM_STACK, stack);
  }
}
