package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogCrossBreedingGameTest implements FabricGameTest {

  private static final double SHARE_DELTA = 1e-4;
  private static final int LOVE_TICKS = 200;

  private UnleashedDogEntity breedMixedPair(final TestContext context) {
    final UnleashedDogEntity husky =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(0, 1, 0));
    final UnleashedDogEntity beagle =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, new BlockPos(1, 1, 0));
    return (UnleashedDogEntity) husky.createChild(context.getWorld(), beagle);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void mixedPairInLoveCanBreed(TestContext context) {
    final UnleashedDogEntity husky =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(0, 1, 0));
    final UnleashedDogEntity beagle =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, new BlockPos(1, 1, 0));
    husky.setLoveTicks(LOVE_TICKS);
    beagle.setLoveTicks(LOVE_TICKS);

    context.assertTrue(
        husky.canBreedWith(beagle), "Two tamed dogs of different breeds in love should breed");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void mixedPairChildCompositionIsHalfEachBreedModuloThrowback(TestContext context) {
    final UnleashedDogEntity baby = breedMixedPair(context);

    context.assertTrue(baby != null, "Mixed pair should produce a puppy");
    final DogGenome genome = baby.getGenome();
    context.assertTrue(genome != null, "Cross-breed puppy should carry a genome");
    final Set<UnleashedDogBreed> breeds =
        genome.composition().stream().map(BreedShare::breed).collect(Collectors.toSet());
    context.assertTrue(
        breeds.equals(Set.of(UnleashedDogBreed.HUSKY, UnleashedDogBreed.BEAGLE)),
        "Composition should contain exactly the two parent breeds, was " + breeds);
    final double huskyShare = shareOf(genome, UnleashedDogBreed.HUSKY);
    context.assertTrue(
        isOneOf(huskyShare, 0.5, 0.5 * 0.85, 0.5 * 0.85 + 0.15),
        "Husky share should be the plain average or a throwback-nudged value, was " + huskyShare);
    final double sum = genome.composition().stream().mapToDouble(BreedShare::share).sum();
    context.assertTrue(Math.abs(sum - 1.0) < SHARE_DELTA, "Shares should sum to 1, was " + sum);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void crossBreedChildAppliesGenomeAttributes(TestContext context) {
    final UnleashedDogEntity baby = breedMixedPair(context);

    context.assertTrue(baby != null, "Mixed pair should produce a puppy");
    final DogGenome genome = baby.getGenome();
    context.assertTrue(
        baby.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) == genome.maxHealth(),
        "Max health attribute should match the genome stat gene");
    context.assertTrue(
        baby.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) == genome.movementSpeed(),
        "Movement speed attribute should match the genome stat gene");
    context.assertTrue(
        baby.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) == genome.attackDamage(),
        "Attack damage attribute should match the genome stat gene");
    context.assertTrue(
        baby.getHealth() == baby.getMaxHealth(), "Newborn should spawn at full genome health");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void crossBreedChildInheritsVoiceFromAParent(TestContext context) {
    final UnleashedDogEntity baby = breedMixedPair(context);

    context.assertTrue(baby != null, "Mixed pair should produce a puppy");
    final UnleashedDogBreed voice = baby.getVoiceBreed();
    context.assertTrue(
        voice == UnleashedDogBreed.HUSKY || voice == UnleashedDogBreed.BEAGLE,
        "Voice gene should come from a parent, was " + voice);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void crossBreedChildRendersAsDominantCompositionBreed(TestContext context) {
    final UnleashedDogEntity baby = breedMixedPair(context);

    context.assertTrue(baby != null, "Mixed pair should produce a puppy");
    context.assertTrue(
        baby.getRigSourceBreed() == baby.getGenome().dominantBreed(),
        "Rig source breed should be the dominant composition breed");
    context.assertTrue(
        baby.getRigSourceBreed() != UnleashedDogBreed.CROSS_BREED,
        "Rig source breed should be a founding breed");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void crossBreedPairBreedsRecursivelyFromStoredGenomes(TestContext context) {
    final UnleashedDogEntity crossParent = breedMixedPair(context);
    context.assertTrue(crossParent != null, "Mixed pair should produce a puppy");
    final UnleashedDogEntity shiba =
        DogTestHelper.spawnTamedDog(context, DogTestData.SHIBA_INU, new BlockPos(1, 1, 0));

    final UnleashedDogEntity child =
        (UnleashedDogEntity) crossParent.createChild(context.getWorld(), shiba);

    context.assertTrue(child != null, "A cross-breed and a pure breed should produce a puppy");
    context.assertTrue(
        child.getBreed() == UnleashedDogBreed.CROSS_BREED,
        "A pair involving a cross-breed should produce a cross-breed puppy");
    final DogGenome genome = child.getGenome();
    final Set<UnleashedDogBreed> breeds =
        genome.composition().stream().map(BreedShare::breed).collect(Collectors.toSet());
    context.assertTrue(
        breeds.equals(
            Set.of(UnleashedDogBreed.HUSKY, UnleashedDogBreed.BEAGLE, UnleashedDogBreed.SHIBA_INU)),
        "Composition should merge the stored genome with the pure parent, was " + breeds);
    final double shibaShare = shareOf(genome, UnleashedDogBreed.SHIBA_INU);
    context.assertTrue(
        shibaShare > 0.4 && shibaShare < 0.6,
        "The pure parent should contribute about half, was " + shibaShare);
    final double sum = genome.composition().stream().mapToDouble(BreedShare::share).sum();
    context.assertTrue(Math.abs(sum - 1.0) < SHARE_DELTA, "Shares should sum to 1, was " + sum);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void crossBreedGenomeSurvivesNbtRoundTrip(TestContext context) {
    final UnleashedDogEntity baby = breedMixedPair(context);
    context.assertTrue(baby != null, "Mixed pair should produce a puppy");

    final NbtCompound nbt = new NbtCompound();
    baby.writeCustomDataToNbt(nbt);
    final UnleashedDogEntity reloaded = ModEntities.CROSS_BREED.create(context.getWorld());
    reloaded.readCustomDataFromNbt(nbt);

    context.assertTrue(
        baby.getGenome().equals(reloaded.getGenome()),
        "The genome should survive an NBT round trip");
    context.assertTrue(
        reloaded.getRigSourceBreed() == baby.getRigSourceBreed(),
        "The rig source breed should survive an NBT round trip");
    context.assertTrue(
        reloaded.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH)
            == baby.getGenome().maxHealth(),
        "Genome attributes should be reapplied on NBT read");
    reloaded.discard();
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void sameBreedPairStillProducesPureChild(TestContext context) {
    final UnleashedDogEntity husky =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(0, 1, 0));
    final UnleashedDogEntity otherHusky =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(1, 1, 0));

    final UnleashedDogEntity baby =
        (UnleashedDogEntity) husky.createChild(context.getWorld(), otherHusky);

    context.assertTrue(baby != null, "A same-breed pair should produce a puppy");
    context.assertTrue(
        baby.getBreed() == UnleashedDogBreed.HUSKY, "A same-breed pair should produce a pure baby");
    context.assertTrue(baby.getGenome() == null, "A pure baby should carry no genome");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void crossBreedPuppyRecordsBothParents(TestContext context) {
    final UnleashedDogEntity husky =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(0, 1, 0));
    final UnleashedDogEntity beagle =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, new BlockPos(1, 1, 0));

    final UnleashedDogEntity baby =
        (UnleashedDogEntity) husky.createChild(context.getWorld(), beagle);

    context.assertTrue(baby != null, "Mixed pair should produce a puppy");
    context.assertTrue(
        husky.getUuid().equals(baby.getLineage().getParentDogUuid()),
        "First parent UUID should be recorded");
    context.assertTrue(
        beagle.getUuid().equals(baby.getLineage().getSecondParentDogUuid()),
        "Second parent UUID should be recorded");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void crossBreedPuppyPetRecordStoresItsGenomeData(TestContext context) {
    final UUID ownerUuid = UUID.randomUUID();
    final UnleashedDogEntity husky =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(0, 1, 0), ownerUuid);
    final UnleashedDogEntity beagle =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, new BlockPos(1, 1, 0), ownerUuid);

    final UnleashedDogEntity baby =
        (UnleashedDogEntity) husky.createChild(context.getWorld(), beagle);

    context.assertTrue(baby != null, "Mixed pair should produce a puppy");
    final PetData petData =
        PetManager.get(context.getWorld().getServer()).getPetByEntityId(baby.getUuid());
    context.assertTrue(petData != null, "The cross-breed puppy should be registered as a pet");
    context.assertTrue(
        petData.getBreed() == UnleashedDogBreed.CROSS_BREED,
        "The pet record should carry the cross-breed");
    context.assertTrue(
        petData.getComposition().equals(baby.getGenome().composition()),
        "The pet record should store the genome composition");
    context.assertTrue(
        petData.getMovementSpeed()
            == (float) baby.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED),
        "The pet record should store the genome movement speed");
    context.assertTrue(
        petData.getAttackDamage()
            == (float) baby.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
        "The pet record should store the genome attack damage");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void summonedCrossBreedGeneratesARandomFounderMix(TestContext context) {
    final UnleashedDogEntity dog =
        context.spawnEntity(ModEntities.CROSS_BREED, new BlockPos(0, 1, 0));
    dog.initialize(
        context.getWorld(),
        context.getWorld().getLocalDifficulty(dog.getBlockPos()),
        SpawnReason.COMMAND,
        null);

    final DogGenome genome = dog.getGenome();
    context.assertTrue(genome != null, "A summoned cross-breed should generate a genome");
    context.assertTrue(
        genome.composition().size() >= 2, "A generated mix should span at least two breeds");
    context.assertTrue(
        genome.composition().stream()
            .map(BreedShare::breed)
            .allMatch(UnleashedDogBreed::isNaturallySpawning),
        "A generated mix should only contain founding breeds");
    context.complete();
  }

  private static double shareOf(final DogGenome genome, final UnleashedDogBreed breed) {
    return genome.composition().stream()
        .filter(share -> share.breed() == breed)
        .mapToDouble(BreedShare::share)
        .sum();
  }

  private static boolean isOneOf(final double actual, final double... candidates) {
    for (final double candidate : candidates) {
      if (Math.abs(actual - candidate) <= SHARE_DELTA) {
        return true;
      }
    }
    return false;
  }
}
