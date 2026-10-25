package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newRegistryBuf;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.samplePet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.config.AgingSettings;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.personality.DogTrait;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetAge;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PetSyncDataTest {

  private static final DynamicRegistryManager REGISTRIES =
      DynamicRegistryManager.of(Registries.REGISTRIES);

  @Test
  @DisplayName("codec round-trips a cross-breed pet with genome fields")
  void codecRoundTripsCrossBreedGenomeFields() {
    final PetSyncData pet =
        samplePet(
            UnleashedDogBreed.CROSS_BREED,
            "Pixel",
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)));
    final RegistryByteBuf buf = newRegistryBuf();
    PetSyncData.CODEC.encode(buf, pet);
    assertEquals(pet, PetSyncData.CODEC.decode(buf));
  }

  @ParameterizedTest(name = "codec round-trips a {0} pet")
  @EnumSource(PetLifeState.class)
  @DisplayName("codec round-trips every lifecycle state so the client can render undead pets")
  void codecRoundTripsEveryLifeState(final PetLifeState lifeState) {
    final PetSyncData pet =
        new PetSyncData(
            samplePet(UnleashedDogBreed.HUSKY, "Balto").petId(),
            UnleashedDogBreed.HUSKY,
            "Balto",
            9.0f,
            18.0f,
            1,
            2,
            3,
            "minecraft:overworld",
            lifeState,
            false,
            1,
            0,
            0,
            0,
            0.3f,
            5.0f,
            List.of(),
            RidingView.NONE,
            PetAwayStatus.HOME,
            false,
            DogBondStep.STRANGER,
            DogTraits.NONE,
            0,
            DogPortraitOutfit.NONE);
    final RegistryByteBuf buf = newRegistryBuf();
    PetSyncData.CODEC.encode(buf, pet);

    final PetSyncData decoded = PetSyncData.CODEC.decode(buf);
    assertEquals(pet, decoded);
    assertEquals(lifeState.isAlive(), decoded.alive(), "alive");
    assertEquals(lifeState == PetLifeState.UNDEAD, decoded.undead(), "undead");
  }

  @ParameterizedTest(name = "baby {0}, senior {1} round-trips as {2}")
  @CsvSource({"false, false, ADULT", "false, true, SENIOR", "true, false, PUPPY"})
  @DisplayName("codec round-trips the senior flag so the client can label seniors")
  void codecRoundTripsSenior(final boolean baby, final boolean senior, final PetAge age) {
    final PetSyncData pet =
        new PetSyncData(
            samplePet(UnleashedDogBreed.BEAGLE, "Old Tom").petId(),
            UnleashedDogBreed.BEAGLE,
            "Old Tom",
            9.0f,
            18.0f,
            1,
            2,
            3,
            "minecraft:overworld",
            PetLifeState.LIVING,
            baby,
            1,
            0,
            0,
            0,
            0.27f,
            5.0f,
            List.of(),
            RidingView.NONE,
            PetAwayStatus.HOME,
            senior,
            DogBondStep.STRANGER,
            DogTraits.NONE,
            0,
            DogPortraitOutfit.NONE);
    final RegistryByteBuf buf = newRegistryBuf();
    PetSyncData.CODEC.encode(buf, pet);
    final PetSyncData decoded = PetSyncData.CODEC.decode(buf);
    assertEquals(pet, decoded);
    assertEquals(age, decoded.age());
  }

  @ParameterizedTest(name = "aging {0}, {1}, age {2} -> senior {3}")
  @CsvSource({
    "true, LIVING, 2400000, true",
    "true, LIVING, 2399999, false",
    "false, LIVING, 2400000, false",
    "true, UNDEAD, 2400000, false",
    "true, DECEASED, 2400000, true"
  })
  @DisplayName("the synced senior label follows the live aging setting, not a stale flag")
  void seniorFollowsTheLiveSetting(
      final boolean enabled,
      final PetLifeState lifeState,
      final long ageTicks,
      final boolean expected) {
    final DogsUnleashedConfig original = DogsUnleashed.SERVER_CONFIG;
    try {
      DogsUnleashed.SERVER_CONFIG = original.withAging(new AgingSettings(enabled, 100));
      final NbtCompound nbt = new NbtCompound();
      nbt.putUuid(ModNbtKeys.PET_ID, UUID.randomUUID());
      nbt.putUuid(ModNbtKeys.OWNER_ID, UUID.randomUUID());
      nbt.putString(ModNbtKeys.BREED_TYPE, UnleashedDogBreed.BEAGLE.serializedId());
      nbt.putString(ModNbtKeys.LIFE_STATE, lifeState.serializedName());
      nbt.putLong(ModNbtKeys.ADULT_AGE_TICKS, ageTicks);
      assertEquals(expected, PetSyncData.seniorOf(PetData.fromNbt(nbt, REGISTRIES)));
    } finally {
      DogsUnleashed.SERVER_CONFIG = original;
    }
  }

  @ParameterizedTest(name = "bond {0} round-trips")
  @EnumSource(DogBondStep.class)
  @DisplayName("codec round-trips every bond step so the client can name it")
  void codecRoundTripsBond(final DogBondStep bond) {
    final PetSyncData sample = samplePet(UnleashedDogBreed.HUSKY, "Balto");
    final PetSyncData pet =
        new PetSyncData(
            sample.petId(),
            sample.breed(),
            sample.name(),
            sample.health(),
            sample.maxHealth(),
            sample.posX(),
            sample.posY(),
            sample.posZ(),
            sample.dimension(),
            sample.lifeState(),
            sample.baby(),
            sample.collarColor(),
            sample.coatVariant(),
            sample.huskyEyeVariant(),
            sample.furStage(),
            sample.movementSpeed(),
            sample.attackDamage(),
            sample.composition(),
            sample.riding(),
            sample.away(),
            sample.senior(),
            bond,
            sample.personality(),
            0,
            DogPortraitOutfit.NONE);
    final RegistryByteBuf buf = newRegistryBuf();
    PetSyncData.CODEC.encode(buf, pet);
    assertEquals(bond, PetSyncData.CODEC.decode(buf).bond());
  }

  @ParameterizedTest(name = "{0} saved bond points -> {1}")
  @CsvSource({"0, STRANGER", "49, STRANGER", "50, FAMILIAR", "349, FRIEND", "700, DEVOTED"})
  @DisplayName("a stored pet syncs the bond step its saved points have reached")
  void storedPetSyncsItsBondStep(final int points, final DogBondStep expected) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putUuid(ModNbtKeys.PET_ID, UUID.randomUUID());
    nbt.putUuid(ModNbtKeys.OWNER_ID, UUID.randomUUID());
    nbt.putString(ModNbtKeys.BREED_TYPE, UnleashedDogBreed.BEAGLE.serializedId());
    nbt.putInt(ModNbtKeys.BOND_POINTS, points);
    assertEquals(
        expected, PetSyncData.from(PetData.fromNbt(nbt, REGISTRIES), PetAwayStatus.HOME).bond());
  }

  static Stream<Arguments> displayBreedCases() {
    return Stream.of(
        Arguments.of(
            "pure breed ignores composition",
            samplePet(UnleashedDogBreed.BEAGLE, "Rex"),
            UnleashedDogBreed.BEAGLE),
        Arguments.of(
            "cross-breed without composition falls back to its own breed",
            samplePet(UnleashedDogBreed.CROSS_BREED, "Pixel"),
            UnleashedDogBreed.CROSS_BREED),
        Arguments.of(
            "cross-breed resolves the dominant composition breed",
            samplePet(
                UnleashedDogBreed.CROSS_BREED,
                "Pixel",
                List.of(
                    new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.25f),
                    new BreedShare(UnleashedDogBreed.HUSKY, 0.75f))),
            UnleashedDogBreed.HUSKY),
        Arguments.of(
            "cross-breed breaks share ties by serialized id",
            samplePet(
                UnleashedDogBreed.CROSS_BREED,
                "Pixel",
                List.of(
                    new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.5f),
                    new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f))),
            UnleashedDogBreed.BEAGLE));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("displayBreedCases")
  @DisplayName("displayBreed resolves the breed the client should render")
  void displayBreedResolvesRenderBreed(
      final String label, final PetSyncData pet, final UnleashedDogBreed expected) {
    assertEquals(expected, pet.displayBreed());
  }

  static Stream<DogTraits> personalities() {
    return Stream.of(
        DogTraits.NONE,
        DogTraits.of(DogTrait.PLAYFUL),
        DogTraits.of(DogTrait.TIMID, DogTrait.LAZY));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("personalities")
  @DisplayName("codec round-trips the personality so the cards can show it")
  void codecRoundTripsPersonality(final DogTraits traits) {
    final PetSyncData base = samplePet(UnleashedDogBreed.BEAGLE, "Rex");
    final PetSyncData pet =
        new PetSyncData(
            base.petId(),
            base.breed(),
            base.name(),
            base.health(),
            base.maxHealth(),
            base.posX(),
            base.posY(),
            base.posZ(),
            base.dimension(),
            base.lifeState(),
            base.baby(),
            base.collarColor(),
            base.coatVariant(),
            base.huskyEyeVariant(),
            base.furStage(),
            base.movementSpeed(),
            base.attackDamage(),
            base.composition(),
            base.riding(),
            base.away(),
            base.senior(),
            base.bond(),
            traits,
            0,
            DogPortraitOutfit.NONE);
    final RegistryByteBuf buf = newRegistryBuf();
    PetSyncData.CODEC.encode(buf, pet);
    assertEquals(traits, PetSyncData.CODEC.decode(buf).personality());
  }

  static Stream<Arguments> looks() {
    return Stream.of(
        Arguments.of(0, DogPortraitOutfit.NONE),
        Arguments.of(
            4,
            new DogPortraitOutfit(
                Map.of(
                    DogEquipmentSlot.ARMOUR,
                    new ItemStack(Items.WOLF_ARMOR),
                    DogEquipmentSlot.PENDANT,
                    new ItemStack(Items.EMERALD),
                    DogEquipmentSlot.SADDLEBAGS,
                    new ItemStack(ModItems.SADDLEBAGS)),
                Optional.of(DyeColor.LIME),
                new ItemStack(Items.STICK),
                Optional.empty(),
                true,
                true,
                false)));
  }

  @ParameterizedTest(name = "growth stage {0}")
  @MethodSource("looks")
  @DisplayName("codec round-trips the growth stage and outfit so previews show the dog as it is")
  void codecRoundTripsLook(final int growthStage, final DogPortraitOutfit outfit) {
    final PetSyncData base = samplePet(UnleashedDogBreed.DIRE_WOLF, "Ghost");
    final PetSyncData pet =
        new PetSyncData(
            base.petId(),
            base.breed(),
            base.name(),
            base.health(),
            base.maxHealth(),
            base.posX(),
            base.posY(),
            base.posZ(),
            base.dimension(),
            base.lifeState(),
            base.baby(),
            base.collarColor(),
            base.coatVariant(),
            base.huskyEyeVariant(),
            base.furStage(),
            base.movementSpeed(),
            base.attackDamage(),
            base.composition(),
            base.riding(),
            base.away(),
            base.senior(),
            base.bond(),
            base.personality(),
            growthStage,
            outfit);
    final RegistryByteBuf buf = newRegistryBuf();
    PetSyncData.CODEC.encode(buf, pet);
    final PetSyncData decoded = PetSyncData.CODEC.decode(buf);
    assertEquals(growthStage, decoded.growthStage());
    assertEquals(outfit, decoded.outfit());
  }

  @ParameterizedTest(name = "growth stage {0}")
  @MethodSource("looks")
  @DisplayName("a stored pet syncs the growth stage and outfit it last had")
  void storedPetSyncsItsLook(final int growthStage, final DogPortraitOutfit outfit) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putUuid(ModNbtKeys.PET_ID, UUID.randomUUID());
    nbt.putUuid(ModNbtKeys.OWNER_ID, UUID.randomUUID());
    nbt.putString(ModNbtKeys.BREED_TYPE, UnleashedDogBreed.DIRE_WOLF.serializedId());
    nbt.putInt(ModNbtKeys.GROWTH_STAGE, growthStage);
    nbt.put(
        ModNbtKeys.PORTRAIT_OUTFIT,
        DogPortraitOutfit.CODEC
            .encodeStart(REGISTRIES.getOps(NbtOps.INSTANCE), outfit)
            .getOrThrow());
    final PetSyncData synced =
        PetSyncData.from(PetData.fromNbt(nbt, REGISTRIES), PetAwayStatus.HOME);
    assertEquals(growthStage, synced.growthStage());
    assertEquals(outfit, synced.outfit());
  }
}
