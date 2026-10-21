package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogPersonality;
import com.grahambartley.dogsunleashed.entity.DogPointedOrders;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import java.util.Comparator;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

public class BringToyGoal extends Goal {

  private final UnleashedDogEntity dog;
  private final BooleanSupplier looks;
  private @Nullable ItemEntity toy;

  public BringToyGoal(final UnleashedDogEntity dog) {
    this(dog, () -> dog.getRandom().nextInt(BringToyRules.LOOK_ONE_IN) == 0);
  }

  public BringToyGoal(final UnleashedDogEntity dog, final BooleanSupplier looks) {
    this.dog = dog;
    this.looks = looks;
  }

  @Override
  public boolean canStart() {
    final LivingEntity owner = this.dog.getOwner();
    if (!this.dog.isTamed()
        || owner == null
        || owner.getWorld() != this.dog.getWorld()
        || !this.dog.getComponents().get(DogPersonality.class).traits().bringsToys()
        || !BringToyRules.mayLook(
            this.dog.getCommand(),
            this.dog.isInSittingPose(),
            this.dog.getFetchCarry().isCarrying(),
            this.orders().fetchItem() != null)
        || !this.looks.getAsBoolean()) {
      return false;
    }
    this.toy = this.nearestLooseToy(owner);
    return this.toy != null;
  }

  @Override
  public boolean shouldContinue() {
    return false;
  }

  @Override
  public void start() {
    if (this.toy != null) {
      this.orders().fetch(this.toy);
      this.toy = null;
    }
  }

  private @Nullable ItemEntity nearestLooseToy(final LivingEntity owner) {
    return this.dog
        .getWorld()
        .getEntitiesByClass(
            ItemEntity.class,
            this.dog.getBoundingBox().expand(BringToyRules.SEARCH_RANGE),
            item ->
                item.isAlive()
                    && !item.cannotPickup()
                    && FetchTypes.forItem(item.getStack().getItem()) != null
                    && BringToyRules.worthBringing(item.squaredDistanceTo(owner)))
        .stream()
        .min(Comparator.comparingDouble(this.dog::squaredDistanceTo))
        .orElse(null);
  }

  private DogPointedOrders orders() {
    return this.dog.getComponents().get(DogPointedOrders.class);
  }
}
