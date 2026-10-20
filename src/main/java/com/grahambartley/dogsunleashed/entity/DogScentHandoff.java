package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModItemTags;
import com.grahambartley.dogsunleashed.entity.scent.MapScentMarks;
import com.grahambartley.dogsunleashed.entity.scent.ScentLandmark;
import com.grahambartley.dogsunleashed.entity.scent.ScentRule;
import com.grahambartley.dogsunleashed.entity.scent.ScentRules;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

final class DogScentHandoff {

  private final UnleashedDogEntity dog;

  DogScentHandoff(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  @Nullable
  ActionResult tryGive(final PlayerEntity player, final ItemStack itemStack) {
    final boolean map = itemStack.isOf(Items.FILLED_MAP);
    if ((!map && !itemStack.isIn(ModItemTags.SCENT_ITEMS)) || DogFoods.isTamingItem(itemStack)) {
      return null;
    }
    if (map) {
      return this.tryFollowMap(player, itemStack);
    }
    if (this.scent().isTrackingItem(itemStack)) {
      return this.callOff(player);
    }
    return this.tryFollowScent(player, itemStack);
  }

  private @Nullable ActionResult tryFollowMap(
      final PlayerEntity player, final ItemStack itemStack) {
    final Optional<ScentLandmark> mark =
        ScentLandmark.nearest(MapScentMarks.of(itemStack), this.dog.getX(), this.dog.getZ());
    if (mark.isEmpty()) {
      return null;
    }
    if (this.scent().isTrackingItem(itemStack) && mark.get().equals(this.scent().landmark())) {
      return this.callOff(player);
    }
    this.readyToLead();
    this.scent().followMark(mark.get(), itemStack.getItem());
    this.scent().cues().sniff();
    DogInteractions.tell(this.dog, player, "message.dogs-unleashed.scent.map");
    return ActionResult.SUCCESS;
  }

  private ActionResult tryFollowScent(final PlayerEntity player, final ItemStack itemStack) {
    final Optional<RegistryEntry.Reference<ScentRule>> rule =
        ScentRules.forItem(this.dog.getWorld(), itemStack);
    if (rule.isEmpty()) {
      this.scent().cues().whine();
      return ActionResult.SUCCESS;
    }
    this.readyToLead();
    this.scent().start(rule.get().registryKey().getValue(), itemStack.getItem());
    this.scent().cues().sniff();
    DogInteractions.tell(this.dog, player, "message.dogs-unleashed.scent.start");
    return ActionResult.SUCCESS;
  }

  private ActionResult callOff(final PlayerEntity player) {
    this.scent().stop();
    DogInteractions.tell(this.dog, player, "message.dogs-unleashed.scent.stop");
    return ActionResult.SUCCESS;
  }

  private void readyToLead() {
    if (this.dog.getCommand() == DogCommand.SIT) {
      this.dog.getCommandController().apply(DogCommand.FOLLOW);
    }
    this.dog.getComponents().get(DogPointedOrders.class).clear();
    this.dog.getNavigation().stop();
  }

  private DogScent scent() {
    return this.dog.getComponents().get(DogScent.class);
  }
}
