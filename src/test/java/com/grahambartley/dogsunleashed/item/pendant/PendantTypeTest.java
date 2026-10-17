package com.grahambartley.dogsunleashed.item.pendant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class PendantTypeTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/dogs-unleashed");
  private static final Path DATA = Path.of("src/main/resources/data/dogs-unleashed");
  private static final List<String> LOCALES =
      List.of("en_us", "de_de", "es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn");

  @ParameterizedTest(name = "{0}")
  @EnumSource(PendantType.class)
  @DisplayName("only warding pendants name a mob family, only guardians name a damage family")
  void familyDecidesWhatItProtectsAgainst(final PendantType type) {
    if (type.family() == PendantFamily.WARDING) {
      assertNotNull(type.wardedMobs());
    } else {
      assertNull(type.wardedMobs());
    }
    if (type.family() == PendantFamily.GUARDIAN) {
      assertNotNull(type.guardedDamage());
    } else {
      assertNull(type.guardedDamage());
    }
  }

  @Test
  @DisplayName("every pendant has its own tint so they are told apart at a glance")
  void tintsAreUnique() {
    final Set<Integer> tints =
        Arrays.stream(PendantType.values()).map(PendantType::tint).collect(Collectors.toSet());

    assertEquals(PendantType.values().length, tints.size());
  }

  static Stream<String> itemIds() {
    return Stream.concat(
        Stream.of("blank_pendant"), Arrays.stream(PendantType.values()).map(PendantType::itemId));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("itemIds")
  @DisplayName("every pendant item ships a model, a sprite, a recipe and a name in every language")
  void everyItemShipsItsFiles(final String itemId) throws IOException {
    assertTrue(Files.isRegularFile(ASSETS.resolve("models/item/" + itemId + ".json")), itemId);
    assertTrue(Files.isRegularFile(ASSETS.resolve("textures/item/" + itemId + ".png")), itemId);
    assertTrue(Files.isRegularFile(DATA.resolve("recipe/" + itemId + ".json")), itemId);
    for (final String locale : LOCALES) {
      assertTrue(lang(locale).has("item.dogs-unleashed." + itemId), locale + " " + itemId);
    }
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(PendantType.class)
  @DisplayName("every charged pendant explains its effect in every language")
  void everyEffectIsExplainedInEveryLanguage(final PendantType type) throws IOException {
    for (final String locale : LOCALES) {
      final JsonObject lang = lang(locale);
      assertTrue(lang.has(type.effectTranslationKey()), locale + " " + type.id());
      assertTrue(lang.has(PendantItem.NO_STACKING_KEY), locale);
    }
  }

  private static JsonObject lang(final String locale) throws IOException {
    return JsonParser.parseString(Files.readString(ASSETS.resolve("lang/" + locale + ".json")))
        .getAsJsonObject();
  }
}
