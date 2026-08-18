package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

/**
 * The resurrection ritual end to end: a Totem of Undying installed on a grave, a lightning rod
 * above it, and a bolt that raises the pet as its undead self.
 *
 * <p>Resurrection is deferred one tick off the bolt's own entity-load event, so every assertion
 * runs well after the strike tick.
 */
public final class ResurrectionRitualGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  // Relative y1 is the template floor, so the ritual site is built one layer up.
  private static final BlockPos REL_GRAVE = new BlockPos(3, 2, 3);
  private static final BlockPos REL_ROD = new BlockPos(3, 3, 3);
  private static final BlockPos REL_DOG = new BlockPos(1, 2, 1);
  private static final DyeColor COLLAR = DyeColor.LIME;
  private static final int STRIKE_TICK = 5;
  private static final int ASSERT_TICK = 20;
  private static final int TICK_LIMIT = 100;

  private static final DogGenome GENOME =
      new DogGenome(
          List.of(
              new BreedShare(UnleashedDogBreed.HUSKY, 0.6f),
              new BreedShare(UnleashedDogBreed.BEAGLE, 0.4f)),
          18.0,
          0.29,
          4.0,
          UnleashedDogBreed.BEAGLE);

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void totemFromThePlayersHandInstallsOnTheGrave(final TestContext context) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    final DogGraveBlockEntity grave = placeGrave(context, UUID.randomUUID());
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.TOTEM_OF_UNDYING, 2));

    context.useBlock(REL_GRAVE, player);

    context.assertTrue(grave.hasTotem(), "The grave should hold the totem after a right-click");
    context.assertTrue(
        player.getStackInHand(Hand.MAIN_HAND).getCount() == 1,
        "Installing a totem should consume exactly one from the stack");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void usingAGraveThatHoldsATotemGivesItBack(final TestContext context) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    final DogGraveBlockEntity grave = placeGrave(context, UUID.randomUUID());
    grave.installTotem(player.getUuid());

    context.useBlock(REL_GRAVE, player);

    context.assertTrue(!grave.hasTotem(), "Using a grave that holds a totem should take it back");
    context.assertTrue(
        player.getInventory().contains(new ItemStack(Items.TOTEM_OF_UNDYING)),
        "The retrieved totem should land back in the player's inventory");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void breakingAGraveDropsItsInstalledTotem(final TestContext context) {
    final DogGraveBlockEntity grave = placeGrave(context, UUID.randomUUID());
    grave.installTotem(UUID.randomUUID());

    context.removeBlock(REL_GRAVE);

    context.expectEntityAround(EntityType.ITEM, REL_GRAVE, 3.0);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void lightningOnTheRodRaisesTheDeceasedPetAsUndead(final TestContext context) {
    final PetData pet = deceasedPetWithGrave(context, "Balto", true);

    context.runAtTick(STRIKE_TICK, () -> strikeRod(context));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          context.assertTrue(
              pet.getLifeState() == PetLifeState.UNDEAD,
              "The pet record should read UNDEAD, but was " + pet.getLifeState());
          context.assertTrue(
              context.getWorld().getBlockState(context.getAbsolutePos(REL_GRAVE)).isAir(),
              "The ritual should consume the grave");
          context.assertTrue(
              context.getWorld().getBlockState(context.getAbsolutePos(REL_ROD)).isAir(),
              "The ritual should consume the rod rather than leave it hanging over nothing");
          context.assertTrue(
              raisedDog(context, pet.getPetId()) != null,
              "An undead dog should stand where the grave was");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void aRaisedPetKeepsItsIdentity(final TestContext context) {
    final PetData pet = deceasedPetWithGrave(context, "Pixel", true);

    context.runAtTick(STRIKE_TICK, () -> strikeRod(context));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          final UnleashedDogEntity dog = raisedDog(context, pet.getPetId());
          context.assertTrue(dog != null, "The pet should have been raised");
          context.assertTrue(dog.isUndead(), "The raised dog must be undead");
          context.assertTrue(
              dog.getBreed() == pet.getBreed(),
              "Breed should survive the ritual, but was " + dog.getBreed());
          context.assertTrue(
              dog.getCollarColor() == COLLAR,
              "Collar colour should survive the ritual, but was " + dog.getCollarColor());
          context.assertTrue(
              GENOME.equals(dog.getGenome()),
              "Genome should survive the ritual, but was " + dog.getGenome());
          context.assertTrue(
              pet.getOwnerId().equals(dog.getOwnerUuid()),
              "The raised dog should still belong to its owner");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void aRaisedPetIsWeakerThanItWasAlive(final TestContext context) {
    final PetData pet = deceasedPetWithGrave(context, "Wraith", true);

    context.runAtTick(STRIKE_TICK, () -> strikeRod(context));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          final UnleashedDogEntity dog = raisedDog(context, pet.getPetId());
          context.assertTrue(dog != null, "The pet should have been raised");
          context.assertTrue(
              dog.getMaxHealth() < GENOME.maxHealth(),
              "An undead pet should have less max health than its genome gave it in life, but had "
                  + dog.getMaxHealth());
          context.assertTrue(
              dog.getHealth() == dog.getMaxHealth(),
              "A freshly raised pet should come back on full undead health");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void theRitualDoesNotSetFireToWhatItRaises(final TestContext context) {
    final PetData pet = deceasedPetWithGrave(context, "Ember", true);

    context.runAtTick(STRIKE_TICK, () -> strikeRod(context));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          final UnleashedDogEntity dog = raisedDog(context, pet.getPetId());
          context.assertTrue(dog != null, "The pet should have been raised");
          context.assertTrue(
              !dog.isOnFire(),
              "The bolt that raised the pet must not also set it alight, but it was burning");
          context.assertTrue(
              !context
                  .getWorld()
                  .getBlockState(context.getAbsolutePos(REL_GRAVE))
                  .isOf(Blocks.FIRE),
              "The ritual must not leave the grave site burning");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void lightningOnARodOverAGraveWithNoTotemDoesNothing(final TestContext context) {
    final PetData pet = deceasedPetWithGrave(context, "Ghost", false);

    context.runAtTick(STRIKE_TICK, () -> strikeRod(context));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          context.assertTrue(
              pet.getLifeState() == PetLifeState.DECEASED,
              "A totem-less ritual must leave the pet deceased, but it was " + pet.getLifeState());
          context.expectBlock(ModBlocks.DOG_GRAVE, REL_GRAVE);
          context.assertTrue(
              raisedDog(context, pet.getPetId()) == null,
              "A totem-less ritual must not spawn a dog");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void lightningRefusesAPetThatDiedWhileUndead(final TestContext context) {
    final PetData pet = deceasedPetWithGrave(context, "Requiem", true);
    pet.setLifeState(PetLifeState.LOST);

    context.runAtTick(STRIKE_TICK, () -> strikeRod(context));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          final DogGraveBlockEntity grave = context.getBlockEntity(REL_GRAVE);
          context.assertTrue(
              pet.getLifeState() == PetLifeState.LOST,
              "A pet lost while undead must stay lost, but it was " + pet.getLifeState());
          context.assertTrue(grave.hasTotem(), "A refused ritual must not consume the totem");
          context.expectBlock(ModBlocks.DOG_GRAVE, REL_GRAVE);
          context.assertTrue(
              raisedDog(context, pet.getPetId()) == null, "A refused ritual must not spawn a dog");
          context.complete();
        });
  }

  private static void strikeRod(final TestContext context) {
    context.spawnEntity(EntityType.LIGHTNING_BOLT, REL_ROD);
  }

  private static DogGraveBlockEntity placeGrave(final TestContext context, final UUID dogUuid) {
    context.setBlockState(REL_GRAVE, ModBlocks.DOG_GRAVE.getDefaultState());
    final DogGraveBlockEntity grave = context.getBlockEntity(REL_GRAVE);
    grave.setDogUuid(dogUuid);
    return grave;
  }

  /**
   * Builds the whole ritual site: a rod, a grave, and a deceased pet record whose appearance was
   * captured off a real dog through the production {@code syncAppearanceFrom} path.
   */
  private static PetData deceasedPetWithGrave(
      final TestContext context, final String name, final boolean withTotem) {
    final ServerWorld world = context.getWorld();
    final UUID ownerId = UUID.randomUUID();

    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.CROSS_BREED, REL_DOG);
    dog.setAiDisabled(true);
    dog.applyGenome(GENOME);
    dog.setCollarColor(COLLAR);
    dog.setOwnerUuid(ownerId);
    dog.setTamed(true, true);

    final PetData pet =
        new PetData(
            dog.getUuid(),
            ownerId,
            dog.getBreed(),
            name,
            0.0f,
            dog.getMaxHealth(),
            context.getAbsolutePos(REL_GRAVE),
            world.getRegistryKey().getValue().toString(),
            PetLifeState.DECEASED);
    pet.syncAppearanceFrom(dog);
    PetManager.get(world.getServer()).registerPet(pet);
    dog.discard();

    final DogGraveBlockEntity grave = placeGrave(context, pet.getPetId());
    if (withTotem) {
      grave.installTotem(ownerId);
    }
    context.setBlockState(REL_ROD, Blocks.LIGHTNING_ROD.getDefaultState());
    return pet;
  }

  private static @Nullable UnleashedDogEntity raisedDog(
      final TestContext context, final UUID petId) {
    return context.getWorld().getEntity(petId) instanceof UnleashedDogEntity dog ? dog : null;
  }
}
