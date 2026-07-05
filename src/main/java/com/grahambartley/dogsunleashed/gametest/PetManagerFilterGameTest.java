package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.PetAliveFilter;
import com.grahambartley.dogsunleashed.pet.PetData;
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

/**
 * Locks the contract of the 4-arg {@link PetManager#getPetsByOwnerFiltered}, the single filtering
 * entry point every production caller (currently {@code ModNetworking}) uses. Breed, alive-state,
 * and name-search filters layer together, and a {@code null} breed, {@code ALL} alive filter, or
 * empty/null search query is a no-op for that axis. A dedicated batch of cases pins the JVM default
 * locale to Turkish to lock the name search as locale-neutral ({@code Locale.ROOT} case folding),
 * so "I"/"i" matches do not silently drop on a Turkish-locale JVM.
 *
 * <p>This lives in the gametest suite rather than {@code src/test/java} because constructing a
 * {@link PetData} loads {@code UnleashedDogEntity} for its persisted default constants, and that
 * class only passes bytecode verification on the access-widened runtime classpath, not the plain
 * unit-test classpath. No world ticking, entity spawning, or time manipulation is required, so each
 * case runs in {@code EMPTY_STRUCTURE} and completes immediately after asserting.
 */
public final class PetManagerFilterGameTest implements FabricGameTest {

  private static final UUID OWNER = UUID.nameUUIDFromBytes("petmanager-filter-owner".getBytes());

  /** A single filter scenario and the pet names it should return, in registration order. */
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

  /** A name-search scenario asserted while the JVM default locale is forced to Turkish. */
  private record LocaleSearchCase(String name, String searchQuery, List<String> expectedNames) {}

  private static final List<LocaleSearchCase> LOCALE_SEARCH_CASES =
      List.of(
          // Under a Turkish default locale, "I".toLowerCase() folds to the dotless "ı", so a
          // locale-sensitive search would lowercase the query "BIS" to "bıs" and fail to find the
          // dotted "i" in "Biscuit". Locale.ROOT keeps both sides on the dotted "i" and matches.
          new LocaleSearchCase("Biscuit", "BIS", List.of("Biscuit")),
          // Same dotted/dotless trap mid-name: "ISK" must still locate "Whiskey".
          new LocaleSearchCase("Whiskey", "ISK", List.of("Whiskey")),
          // ASCII control: plain case-insensitive matching is unchanged under Turkish.
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
    petManager.registerPet(pet("Biscuit", UnleashedDogBreed.HUSKY, true));
    petManager.registerPet(pet("Whiskey", UnleashedDogBreed.HUSKY, true));
    petManager.registerPet(pet("Bella", UnleashedDogBreed.HUSKY, true));

    // Force the classic locale where default-locale case folding diverges from Locale.ROOT. The
    // set/filter/restore is synchronous so the global default is only Turkish for this single call.
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
    petManager.registerPet(pet("Rex", UnleashedDogBreed.HUSKY, true));
    petManager.registerPet(pet("Bella", UnleashedDogBreed.HUSKY, false));
    petManager.registerPet(pet("Max", UnleashedDogBreed.BEAGLE, true));
    petManager.registerPet(pet("Luna", UnleashedDogBreed.BEAGLE, false));
    return petManager;
  }

  private static PetData pet(
      final String name, final UnleashedDogBreed breed, final boolean alive) {
    return new PetData(
        UUID.nameUUIDFromBytes(name.getBytes()),
        OWNER,
        breed,
        name,
        20.0f,
        20.0f,
        BlockPos.ORIGIN,
        "minecraft:overworld",
        alive);
  }
}
