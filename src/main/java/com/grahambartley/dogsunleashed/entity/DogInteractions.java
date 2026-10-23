package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModPortraits;
import com.grahambartley.dogsunleashed.advancement.DogMilestone;
import com.grahambartley.dogsunleashed.advancement.DogMilestoneCriterion;
import com.grahambartley.dogsunleashed.advancement.DogTamedCriterion;
import com.grahambartley.dogsunleashed.block.DogBedBlock;
import com.grahambartley.dogsunleashed.entity.delivery.DogDeliveries;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import com.grahambartley.dogsunleashed.entity.trick.TrickLearningRules;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import com.grahambartley.dogsunleashed.item.BlankCanvasItem;
import com.grahambartley.dogsunleashed.item.DogWhistleItem;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetNames;
import com.grahambartley.dogsunleashed.pet.PetRegistrar;
import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public final class DogInteractions {

  private final UnleashedDogEntity dog;
  private final DogScentHandoff scentHandoff;

  DogInteractions(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.scentHandoff = new DogScentHandoff(dog);
  }

  public static boolean shouldConsumeOnClient(
      final boolean owner, final boolean tamed, final boolean sneaking) {
    return owner || !tamed || sneaking;
  }

  public static boolean mayFeedBreedingItem(final boolean owner, final boolean baby) {
    return owner || baby;
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
                () -> this.dog.getComponents().get(DogTug.class).interact(player, itemStack),
                () -> this.tryOpenDeliveredBags(player),
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
    } else if (DogFoods.isTamingItem(itemStack)) {
      return this.tryTame(player, itemStack);
    }

    return this.dog.vanillaInteract(player, hand);
  }

  public void tame(final @Nullable PlayerEntity player) {
    this.becomeOwnedBy(player, false);
    if (player instanceof ServerPlayerEntity serverPlayer) {
      final UnleashedDogCoat coat = this.dog.getCoatVariant();
      DogTamedCriterion.INSTANCE.trigger(
          serverPlayer,
          this.dog.getBreed().serializedId(),
          coat == null ? "" : coat.getTexturePrefix());
    }
  }

  public void adoptNewborn(final @Nullable PlayerEntity player) {
    this.becomeOwnedBy(player, true);
  }

  private void becomeOwnedBy(final @Nullable PlayerEntity player, final boolean bornPuppy) {
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
      ModNetworking.sendOpenNamingScreen(serverPlayer, this.dog, bornPuppy, petData.getName());
    }
  }

  private @Nullable ActionResult interactWithTamedDog(
      final PlayerEntity player, final Hand hand, final ItemStack itemStack) {
    final ActionResult handled =
        firstHandled(
            List.of(
                () -> this.tryBindWhistle(player, itemStack),
                () -> this.tryPaintPortrait(player, itemStack),
                () -> this.tryEvolve(player, itemStack),
                () -> this.tryCure(player, itemStack),
                () -> this.tryFeedTreat(player, itemStack),
                () -> this.tryHeal(player, itemStack),
                () -> this.tryRefuseStrangerBreedingFood(player, itemStack),
                () -> this.tryDyeCollar(player, itemStack),
                () -> this.tryRepairArmour(player, itemStack),
                () -> this.tryDirectEquip(player, itemStack),
                () -> this.tryTrimFur(player, hand, itemStack),
                () -> this.tryGiveScent(player, itemStack),
                () -> this.tryGiveBone(player, itemStack),
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

  private @Nullable ActionResult tryOpenDeliveredBags(final PlayerEntity player) {
    if (player.isSneaking() || !DogDeliveries.isWaitingFor(this.dog, player)) {
      return null;
    }
    player.openHandledScreen(this.dog);
    return ActionResult.SUCCESS;
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

    if (this.dog.isLeashed() && DogsUnleashed.SERVER_CONFIG.gameplay().dropLeashOnPlayMode()) {
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
    DogWhistleItem.bind(itemStack, this.dog.getUuid(), PetNames.tamedNameOf(this.dog));
    this.sendFeedback(player, "message.dogs-unleashed.whistle.bound");
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryPaintPortrait(
      final PlayerEntity player, final ItemStack itemStack) {
    if (!this.isDirectOwnerInteraction(player) || !itemStack.isOf(ModPortraits.BLANK_CANVAS)) {
      return null;
    }
    final DogPortrait portrait = BlankCanvasItem.paint(itemStack, player, this.dog);
    player.sendMessage(
        Text.translatable("message.dogs-unleashed.portrait.painted", portrait.name()), true);
    return ActionResult.SUCCESS;
  }

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
    final boolean learnedTrick =
        this.dog.getComponents().get(DogTricks.class).rewardOffered(player);
    this.dog
        .getComponents()
        .get(DogTreatBuffState.class)
        .apply(TrickLearningRules.barkPitchBoost(learnedTrick));
    this.dog.getComponents().get(DogBond.class).reward(DogBondRules.Source.TREAT);
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryHeal(final PlayerEntity player, final ItemStack itemStack) {
    if (!this.dog.isBreedingItem(itemStack) || this.dog.getHealth() >= this.dog.getMaxHealth()) {
      return null;
    }
    itemStack.decrementUnlessCreative(1, player);
    this.dog.heal(DogFoods.HEAL_AMOUNT);
    if (this.dog.isOwner(player)) {
      this.dog.getComponents().get(DogBond.class).reward(DogBondRules.Source.TREAT);
    }
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryRefuseStrangerBreedingFood(
      final PlayerEntity player, final ItemStack itemStack) {
    if (!this.dog.isBreedingItem(itemStack)
        || mayFeedBreedingItem(this.dog.isOwner(player), this.dog.isBaby())) {
      return null;
    }
    return ActionResult.PASS;
  }

  private @Nullable ActionResult tryDyeCollar(
      final PlayerEntity player, final ItemStack itemStack) {
    if (!this.dog.isOwner(player) || !(itemStack.getItem() instanceof DyeItem dyeItem)) {
      return null;
    }
    this.dog.getComponents().get(DogCollar.class).setColor(dyeItem.getColor());
    DogMilestoneCriterion.INSTANCE.trigger(player, DogMilestone.COLLAR_DYED);
    itemStack.decrementUnlessCreative(1, player);
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryRepairArmour(
      final PlayerEntity player, final ItemStack itemStack) {
    if (!this.isDirectOwnerInteraction(player)) {
      return null;
    }
    return this.dog.getArmour().tryRepair(player, itemStack);
  }

  private @Nullable ActionResult tryDirectEquip(
      final PlayerEntity player, final ItemStack itemStack) {
    if (!this.isDirectOwnerInteraction(player)) {
      return null;
    }
    return this.dog.getEquipmentHolder().tryDirectEquip(player, itemStack);
  }

  private @Nullable ActionResult tryEvolve(final PlayerEntity player, final ItemStack itemStack) {
    return this.isDirectOwnerInteraction(player)
        ? this.dog.getGrowth().tryEvolve(player, itemStack)
        : null;
  }

  private @Nullable ActionResult tryTrimFur(
      final PlayerEntity player, final Hand hand, final ItemStack itemStack) {
    if (!this.isDirectOwnerInteraction(player)) {
      return null;
    }
    final DogFurStage before = this.dog.getFur().stage();
    final ActionResult result = this.dog.getFur().tryTrim(player, hand, itemStack);
    if (this.dog.getFur().stage() != before) {
      this.dog.getComponents().get(DogBond.class).reward(DogBondRules.Source.GROOM);
    }
    return result;
  }

  private @Nullable ActionResult tryGiveScent(
      final PlayerEntity player, final ItemStack itemStack) {
    return this.isDirectOwnerInteraction(player)
        ? this.scentHandoff.tryGive(player, itemStack)
        : null;
  }

  private @Nullable ActionResult tryGiveBone(final PlayerEntity player, final ItemStack itemStack) {
    final DogBoneBurial burial = this.dog.getComponents().get(DogBoneBurial.class);
    if (!this.isDirectOwnerInteraction(player)
        || !itemStack.isOf(Items.BONE)
        || !burial.canTakeBone()) {
      return null;
    }
    burial.takeBone(player, itemStack);
    this.sendFeedback(player, "message.dogs-unleashed.bone.bury");
    return ActionResult.SUCCESS;
  }

  private @Nullable ActionResult tryAssignBedOrOpenWheel(
      final PlayerEntity player, final ItemStack itemStack) {
    if (!this.dog.isOwner(player) || DogFoods.isTamingItem(itemStack)) {
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
    if (this.dog.getTrust().applies()) {
      return this.dog.getTrust().feed(player, itemStack);
    }
    itemStack.decrementUnlessCreative(1, player);
    if (this.dog.getRandom().nextInt(this.dog.getBreed().nature().tameSuccessChance()) == 0) {
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
    tell(this.dog, player, translationKey);
  }

  static void tell(
      final UnleashedDogEntity dog, final PlayerEntity player, final String translationKey) {
    player.sendMessage(Text.translatable(translationKey, PetNames.tamedNameOf(dog)), true);
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
