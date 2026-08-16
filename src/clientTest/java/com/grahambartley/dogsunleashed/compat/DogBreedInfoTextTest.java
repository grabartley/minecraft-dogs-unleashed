package com.grahambartley.dogsunleashed.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogBreedInfoTextTest {

  private static String translationKeyOf(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] argsOf(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }

  private static List<String> biomeKeysIn(final Text joined) {
    return joined.getSiblings().stream()
        .filter(sibling -> sibling.getContent() instanceof TranslatableTextContent)
        .map(DogBreedInfoTextTest::translationKeyOf)
        .toList();
  }

  static Stream<Arguments> breeds() {
    return Stream.of(UnleashedDogBreed.values()).map(Arguments::of);
  }

  static Stream<Arguments> naturallySpawningBreeds() {
    return Stream.of(UnleashedDogBreed.values())
        .filter(UnleashedDogBreed::isNaturallySpawning)
        .map(Arguments::of);
  }

  static Stream<Arguments> biomeKeys() {
    return Stream.of(
        Arguments.of(BiomeKeys.SNOWY_TAIGA, "biome.minecraft.snowy_taiga"),
        Arguments.of(BiomeKeys.CHERRY_GROVE, "biome.minecraft.cherry_grove"),
        Arguments.of(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, "biome.minecraft.old_growth_birch_forest"),
        Arguments.of(BiomeKeys.BEACH, "biome.minecraft.beach"));
  }

  @ParameterizedTest(name = "{0} maps to {1}")
  @MethodSource("biomeKeys")
  @DisplayName("biome keys map to the vanilla biome translation keys players already see")
  void biomeKeysMapToVanillaTranslationKeys(
      final RegistryKey<Biome> biome, final String expectedKey) {
    assertEquals(expectedKey, DogBreedInfoText.biomeTranslationKey(biome));
  }

  @ParameterizedTest(name = "{0} lists every biome it spawns in")
  @MethodSource("naturallySpawningBreeds")
  @DisplayName("the biome line names every biome from the breed's spawn settings, in order")
  void biomeLineListsEverySpawnBiome(final UnleashedDogBreed breed) {
    final Text biomesLine = DogBreedInfoText.biomesLine(breed.spawnSettings());
    assertEquals(DogBreedInfoText.BIOMES_KEY, translationKeyOf(biomesLine));

    final List<String> expected =
        Arrays.stream(breed.spawnSettings().biomes())
            .map(DogBreedInfoText::biomeTranslationKey)
            .toList();
    assertEquals(expected, biomeKeysIn((Text) argsOf(biomesLine)[0]));
  }

  @ParameterizedTest(name = "{0} stats line reads its attributes back")
  @MethodSource("breeds")
  @DisplayName("the stats line is derived from the breed's attributes, so retuning cannot drift")
  void statsLineIsDerivedFromBreedAttributes(final UnleashedDogBreed breed) {
    final Text statsLine = DogBreedInfoText.statsLine(breed);
    assertEquals(DogBreedInfoText.STATS_KEY, translationKeyOf(statsLine));
    assertArrayEqualsAsStrings(
        new Object[] {
          DogBreedInfoText.formatHealth(breed.attributes().maxHealth()),
          DogBreedInfoText.formatAttackDamage(breed.attributes().attackDamage()),
          DogBreedInfoText.formatMovementSpeed(breed.attributes().movementSpeed())
        },
        argsOf(statsLine));
  }

  private static void assertArrayEqualsAsStrings(final Object[] expected, final Object[] actual) {
    assertEquals(Arrays.asList(expected), Arrays.asList(actual));
  }

  @ParameterizedTest(name = "{0} gets a biome line only when it spawns naturally")
  @MethodSource("breeds")
  @DisplayName("breeds that never spawn in the world skip the biome line instead of showing none")
  void onlyNaturallySpawningBreedsGetABiomeLine(final UnleashedDogBreed breed) {
    final List<String> keys =
        DogBreedInfoText.lines(breed).stream().map(DogBreedInfoTextTest::translationKeyOf).toList();
    if (breed.isNaturallySpawning()) {
      assertEquals(List.of(DogBreedInfoText.BIOMES_KEY, DogBreedInfoText.STATS_KEY), keys);
    } else {
      assertEquals(List.of(DogBreedInfoText.STATS_KEY), keys);
    }
  }

  @ParameterizedTest(name = "{0} formats as {1}")
  @CsvSource({"25.0, 25", "10.0, 10", "17.5, 18"})
  @DisplayName("health renders as a whole number of health points")
  void healthFormatsAsWholeNumber(final double maxHealth, final String expected) {
    assertEquals(expected, DogBreedInfoText.formatHealth(maxHealth));
  }

  @ParameterizedTest(name = "{0} formats as {1}")
  @CsvSource({"5.0, 5.0", "3.5, 3.5", "2.0, 2.0"})
  @DisplayName("attack damage keeps one decimal so half-heart differences stay visible")
  void attackDamageKeepsOneDecimal(final double attackDamage, final String expected) {
    assertEquals(expected, DogBreedInfoText.formatAttackDamage(attackDamage));
  }

  @ParameterizedTest(name = "{0} formats as {1}")
  @CsvSource({"0.3, 0.30", "0.25, 0.25", "0.32, 0.32"})
  @DisplayName("movement speed keeps two decimals because breeds differ by hundredths")
  void movementSpeedKeepsTwoDecimals(final double movementSpeed, final String expected) {
    assertEquals(expected, DogBreedInfoText.formatMovementSpeed(movementSpeed));
  }

  @Test
  @DisplayName("the shared spawn egg lines have en_us translations")
  void sharedSpawnEggLinesAreTranslated() {
    assertTrue(
        LangKeys.contains(DogBreedInfoText.BIOMES_KEY),
        DogBreedInfoText.BIOMES_KEY + " is missing from en_us.json");
    assertTrue(
        LangKeys.contains(DogBreedInfoText.STATS_KEY),
        DogBreedInfoText.STATS_KEY + " is missing from en_us.json");
  }
}
