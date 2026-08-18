package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.DogBedBlock;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import com.grahambartley.dogsunleashed.item.DogWhistleItem;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetRegistrar;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves what a right-click on a dog does. Each step returns {@code null} to mean "not mine, keep
 * going", so {@link #interact} reads as the interaction precedence order it enforces.
 */
public final class DogInteractions {

  private static final int TAME_SUCCESS_CHANCE = 3;
  private static final float BREEDING_ITEM_HEAL_AMOUNT = 2.0F;

  private final UnleashedDogEntity dog;

  DogInteractions(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static boolean shouldConsumeOnClient(
      final boolean owner, final boolean tamed, final boolean sneaking) {
    return owner || !tamed || sneaking;
  }

  ActionResult interact(final PlayerEntity player, final Hand hand) {
    final ItemStack itemStack = player.getStackInHand(hand);

    if (this.dog.getWorld().isClient) {
      return shouldConsumeOnClient(
              this.dog.isOwner(player), this.dog.isTamed(), player.isSneaking())
          ? ActionResult.CONSUME
          : ActionResult.PASS;
    }

    final ActionResult untamedResult =
        firstHandled(
            List.of(
                () -> this.tryOpenInspect(player),
                () -> this.tryTogglePlayMode(player, itemStack)));
    if (untamedResult != null) {
      return untamedResult;
    }

    if (this.dog.isTamed()) {
      final ActionResult tamedResult = this.interactWithTamedDog(player, hand, itemStack);
      if (tamedResult != null) {
        return tamedResult;
      }
    } else if (this.dog.isTamingItem(itemStack)) {
      return this.tryTame(player, itemStack);
    }

    return this.dog.vanillaInteract(player, hand);
  }

  /**
   * Applies ownership and the initial Sit command, then registers the pet so the owner gets the
   * naming screen. Also used for puppies, which inherit their parents' owner rather than being
   * tamed by hand.
   */
  public void tame(final @Nullable PlayerEntity player) {
    if (player == null) {
      return;
    }
    this.dog.setOwner(player);
    this.dog.getCommandController().apply(DogCommand.SIT);
    this.dog
        .getWorld()
        .sendEntityStatus(this.dog, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);

    final PetData petData = PetRegistrar.registerPetFor(this.dog, player.getUuid());
    if (petData != null && player instanceof ServerPlayerEntity serverPlayer) {
      ModNetworking.sendOpenNamingScreen(
          serverPlayer, this.dog.getUuid(), this.dog.getBreed(), petData.getName());
    }
  }

  private @Nullable ActionResult interactWithTamedDog(
      final PlayerEntity player, final Hand hand, final ItemStack itemStack) {
    final ActionResult handled =
        firstHandled(
            List.of(
                () -> this.tryBindWhistle(player, itemStack),
                () -> this.tryCure(player, itemStack),
                () -> this.tryFeedTreat(player, itemStack),
                () -> this.tryHeal(player, itemStack),
                () -> this.tryDyeCollar(player, itemStack),
                () -> this.tryDirectEquip(player, hand, itemStack),
                () -> this.tryAssignBedOrOpenWheel(player, itemStack)));
    if (handled != null) {
      return handled;
    }

    final ActionResult vanillaResult = this.dog.vanillaInteract(player, hand);
    if (vanillaResult.isAccepted() || this.dog.isBreedingItem(itemStack)) {
      return vanillaResult;
    }
    return null;
  }

  private @Nullable ActionResult tryOpenInspect(final PlayerEntity player) {
    if (this.dog.isOwner(player) || !player.isSneaking()) {
      return null;
    }
    if (player instanceof ServerPlayerEntity serverPlayer) {
      ModNetworking.sendOpenDogInspect(serverPlayer, this.dog);
    }
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryTogglePlayMode(
      final PlayerEntity player, final ItemStack itemStack) {
    final FetchItemType fetchItemType = FetchTypes.forItem(itemStack.getItem());
    if (!this.dog.isTamed()
        || !this.dog.isOwner(player)
        || !player.isSneaking()
        || fetchItemType == null) {
      return null;
    }

    final DogPlaySession playSession = this.dog.getPlaySession();
    if (playSession.isInPlayMode()) {
      playSession.endPlayMode();
      this.sendFeedback(player, "message.dogs-unleashed.play_end");
      return ActionResult.SUCCESS;
    }

    if (this.dog.isLeashed() && DogsUnleashed.SERVER_CONFIG.dropLeashOnPlayMode()) {
      this.dog.detachLeash();
    }
    playSession.endOtherNearbyPlayModes(player);
    playSession.startPlayMode(player, fetchItemType);
    this.sendFeedback(player, "message.dogs-unleashed.play_start");
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryBindWhistle(
      final PlayerEntity player, final ItemStack itemStack) {
    if (!this.isDirectOwnerInteraction(player) || !itemStack.isOf(ModItems.DOG_WHISTLE)) {
      return null;
    }
    DogWhistleItem.bind(itemStack, this.dog.getUuid(), this.dog.getTamedName());
    this.sendFeedback(player, "message.dogs-unleashed.whistle.bound");
    return ActionResult.SUCCESS;
  }

  /** Weakness plus a Golden Apple starts the conversion back to a living dog. */
  private @Nullable ActionResult tryCure(final PlayerEntity player, final ItemStack itemStack) {
    if (!this.dog.getCuring().canStart(itemStack)) {
      return null;
    }
    itemStack.decrementUnlessCreative(1, player);
    this.dog.getCuring().start(player);
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryFeedTreat(
      final PlayerEntity player, final ItemStack itemStack) {
    if (!this.isDirectOwnerInteraction(player) || !itemStack.isOf(ModItems.DOG_TREAT)) {
      return null;
    }
    itemStack.decrementUnlessCreative(1, player);
    this.dog.getTreatBuffState().apply();
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryHeal(final PlayerEntity player, final ItemStack itemStack) {
    if (!this.dog.isBreedingItem(itemStack) || this.dog.getHealth() >= this.dog.getMaxHealth()) {
      return null;
    }
    itemStack.decrementUnlessCreative(1, player);
    this.dog.heal(BREEDING_ITEM_HEAL_AMOUNT);
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryDyeCollar(
      final PlayerEntity player, final ItemStack itemStack) {
    if (!(itemStack.getItem() instanceof DyeItem dyeItem)) {
      return null;
    }
    this.dog.setCollarColor(dyeItem.getColor());
    itemStack.decrementUnlessCreative(1, player);
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryDirectEquip(
      final PlayerEntity player, final Hand hand, final ItemStack itemStack) {
    if (!this.isDirectOwnerInteraction(player)) {
      return null;
    }
    return this.dog.getEquipmentHolder().tryDirectEquip(player, hand, itemStack);
  }

  private @Nullable ActionResult tryAssignBedOrOpenWheel(
      final PlayerEntity player, final ItemStack itemStack) {
    if (!this.dog.isOwner(player) || this.dog.isTamingItem(itemStack)) {
      return null;
    }
    if (player.isSneaking()) {
      DogBedBlock.setPendingAssignment(player.getUuid(), this.dog.getUuid());
      this.sendFeedback(player, "message.dogs-unleashed.pending_bed_assignment");
      return ActionResult.SUCCESS;
    }
    if (player instanceof ServerPlayerEntity serverPlayer) {
      ModNetworking.sendOpenCommandWheel(serverPlayer, this.dog);
    }
    return ActionResult.SUCCESS;
  }

  private ActionResult tryTame(final PlayerEntity player, final ItemStack itemStack) {
    itemStack.decrementUnlessCreative(1, player);
    if (this.dog.getRandom().nextInt(TAME_SUCCESS_CHANCE) == 0) {
      this.tame(player);
    } else {
      this.dog
          .getWorld()
          .sendEntityStatus(this.dog, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
    }
    return ActionResult.SUCCESS;
  }

  private boolean isDirectOwnerInteraction(final PlayerEntity player) {
    return this.dog.isOwner(player) && !player.isSneaking();
  }

  private void sendFeedback(final PlayerEntity player, final String translationKey) {
    player.sendMessage(Text.translatable(translationKey, this.dog.getTamedName()), true);
  }

  private static @Nullable ActionResult firstHandled(final List<Supplier<ActionResult>> steps) {
    for (final Supplier<ActionResult> step : steps) {
      final ActionResult result = step.get();
      if (result != null) {
        return result;
      }
    }
    return null;
  }
}
