package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.PetAliveFilter;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class PetManagerFilterGameTest implements FabricGameTest {

  private static final UUID OWNER = UUID.nameUUIDFromBytes("petmanager-filter-owner".getBytes());

  private record FilterCase(
      String name,
      @Nullable UnleashedDogBreed breedFilter,
      PetAliveFilter aliveFilter,
      @Nullable String searchQuery,
      List<String> expectedNames) {}

  private static final List<FilterCase> FILTER_CASES =
      List.of(
          new FilterCase(
              "nullBreedAllEmptySearchReturnsEveryone",
              null,
              PetAliveFilter.ALL,
              "",
              List.of("Rex", "Bella", "Max", "Luna")),
          new FilterCase(
              "nullSearchBehavesLikeEmptySearch",
              null,
              PetAliveFilter.ALL,
              null,
              List.of("Rex", "Bella", "Max", "Luna")),
          new FilterCase(
              "breedMatchKeepsOnlyThatBreed",
              UnleashedDogBreed.HUSKY,
              PetAliveFilter.ALL,
              "",
              List.of("Rex", "Bella")),
          new FilterCase(
              "breedMissReturnsNothing",
              UnleashedDogBreed.GOLDEN_RETRIEVER,
              PetAliveFilter.ALL,
              "",
              List.of()),
          new FilterCase(
              "aliveFilterDropsTheDeceased", null, PetAliveFilter.ALIVE, "", List.of("Rex", "Max")),
          new FilterCase(
              "deceasedFilterDropsTheLiving",
              null,
              PetAliveFilter.DECEASED,
              "",
              List.of("Bella", "Luna")),
          new FilterCase(
              "searchMatchesPartialNames", null, PetAliveFilter.ALL, "e", List.of("Rex", "Bella")),
          new FilterCase(
              "searchIsCaseInsensitive", null, PetAliveFilter.ALL, "BELLA", List.of("Bella")),
          new FilterCase(
              "breedAliveAndSearchFiltersLayerTogether",
              UnleashedDogBreed.HUSKY,
              PetAliveFilter.ALIVE,
              "e",
              List.of("Rex")));

  @CustomTestProvider
  public List<TestFunction> getPetsByOwnerFilteredCases() {
    return FILTER_CASES.stream()
        .map(
            testCase ->
                new TestFunction(
                    "defaultBatch",
                    "petmanagerfiltergametest.getpetsbyownerfiltered." + testCase.name(),
                    FabricGameTest.EMPTY_STRUCTURE,
                    20,
                    0L,
                    true,
                    ctx -> assertFilterCase(ctx, testCase)))
        .toList();
  }

  @CustomTestProvider
  public List<TestFunction> unknownOwnerCases() {
    return List.of(
        new TestFunction(
            "defaultBatch",
            "petmanagerfiltergametest.unknownowner.noFilters",
            FabricGameTest.EMPTY_STRUCTURE,
            20,
            0L,
            true,
            ctx -> assertUnknownOwnerEmpty(ctx, null, PetAliveFilter.ALL)),
        new TestFunction(
            "defaultBatch",
            "petmanagerfiltergametest.unknownowner.withBreedAndAliveFilter",
            FabricGameTest.EMPTY_STRUCTURE,
            20,
            0L,
            true,
            ctx -> assertUnknownOwnerEmpty(ctx, UnleashedDogBreed.HUSKY, PetAliveFilter.ALIVE)));
  }

  private record LocaleSearchCase(String name, String searchQuery, List<String> expectedNames) {}

  private static final List<LocaleSearchCase> LOCALE_SEARCH_CASES =
      List.of(
          new LocaleSearchCase("Biscuit", "BIS", List.of("Biscuit")),
          new LocaleSearchCase("Whiskey", "ISK", List.of("Whiskey")),
          new LocaleSearchCase("Bella", "bel", List.of("Bella")));

  @CustomTestProvider
  public List<TestFunction> localeNeutralSearchCases() {
    return LOCALE_SEARCH_CASES.stream()
        .map(
            testCase ->
                new TestFunction(
                    "defaultBatch",
                    "petmanagerfiltergametest.localeneutralsearch."
                        + testCase.searchQuery().toLowerCase(Locale.ROOT),
                    FabricGameTest.EMPTY_STRUCTURE,
                    20,
                    0L,
                    true,
                    ctx -> assertLocaleNeutralCase(ctx, testCase)))
        .toList();
  }

  private void assertLocaleNeutralCase(final TestContext context, final LocaleSearchCase testCase) {
    final PetManager petManager = new PetManager();
    petManager.registerPet(pet("Biscuit", UnleashedDogBreed.HUSKY, PetLifeState.LIVING));
    petManager.registerPet(pet("Whiskey", UnleashedDogBreed.HUSKY, PetLifeState.LIVING));
    petManager.registerPet(pet("Bella", UnleashedDogBreed.HUSKY, PetLifeState.LIVING));

    final Locale previousDefault = Locale.getDefault();
    final List<String> actual;
    try {
      Locale.setDefault(Locale.forLanguageTag("tr"));
      actual =
          petManager
              .getPetsByOwnerFiltered(OWNER, null, PetAliveFilter.ALL, testCase.searchQuery())
              .stream()
              .map(PetData::getName)
              .toList();
    } finally {
      Locale.setDefault(previousDefault);
    }

    context.assertTrue(
        testCase.expectedNames().equals(actual),
        "Turkish-locale search for '"
            + testCase.searchQuery()
            + "' expected "
            + testCase.expectedNames()
            + " but got "
            + actual);
    context.complete();
  }

  private void assertFilterCase(final TestContext context, final FilterCase testCase) {
    final PetManager petManager = seededRoster();
    final List<String> actual =
        petManager
            .getPetsByOwnerFiltered(
                OWNER, testCase.breedFilter(), testCase.aliveFilter(), testCase.searchQuery())
            .stream()
            .map(PetData::getName)
            .toList();
    context.assertTrue(
        testCase.expectedNames().equals(actual),
        "Filter '"
            + testCase.name()
            + "' expected "
            + testCase.expectedNames()
            + " but got "
            + actual);
    context.complete();
  }

  private void assertUnknownOwnerEmpty(
      final TestContext context,
      @Nullable final UnleashedDogBreed breedFilter,
      final PetAliveFilter aliveFilter) {
    final PetManager petManager = seededRoster();
    final UUID stranger = UUID.nameUUIDFromBytes("petmanager-filter-stranger".getBytes());
    final List<PetData> result =
        petManager.getPetsByOwnerFiltered(stranger, breedFilter, aliveFilter, "");
    context.assertTrue(
        result.isEmpty(), "An owner with no pets should yield an empty result, got " + result);
    context.complete();
  }

  private static PetManager seededRoster() {
    final PetManager petManager = new PetManager();
    petManager.registerPet(pet("Rex", UnleashedDogBreed.HUSKY, PetLifeState.LIVING));
    petManager.registerPet(pet("Bella", UnleashedDogBreed.HUSKY, PetLifeState.DECEASED));
    petManager.registerPet(pet("Max", UnleashedDogBreed.BEAGLE, PetLifeState.LIVING));
    petManager.registerPet(pet("Luna", UnleashedDogBreed.BEAGLE, PetLifeState.DECEASED));
    return petManager;
  }

  private static PetData pet(
      final String name, final UnleashedDogBreed breed, final PetLifeState lifeState) {
    return new PetData(
        UUID.nameUUIDFromBytes(name.getBytes()),
        OWNER,
        breed,
        name,
        20.0f,
        20.0f,
        BlockPos.ORIGIN,
        "minecraft:overworld",
        lifeState);
  }
}
