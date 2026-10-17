package com.grahambartley.dogsunleashed.item.pendant;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PendantResourcesTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/dogs-unleashed");
  private static final Path DATA = Path.of("src/main/resources/data/dogs-unleashed");
  private static final List<String> LOCALES =
      List.of("en_us", "de_de", "es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn");

  static Stream<String> itemIds() {
    final List<String> ids = new ArrayList<>();
    ids.add("blank_pendant");
    for (final PendantType type : PendantType.values()) {
      ids.add(type.itemId());
    }
    return ids.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("itemIds")
  @DisplayName("every pendant item ships a model, a sprite and a recipe")
  void everyItemShipsItsFiles(final String itemId) {
    assertTrue(Files.isRegularFile(ASSETS.resolve("models/item/" + itemId + ".json")), itemId);
    assertTrue(Files.isRegularFile(ASSETS.resolve("textures/item/" + itemId + ".png")), itemId);
    assertTrue(Files.isRegularFile(DATA.resolve("recipe/" + itemId + ".json")), itemId);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("itemIds")
  @DisplayName("every pendant item is named in every language")
  void everyItemIsNamedInEveryLanguage(final String itemId) throws IOException {
    for (final String locale : LOCALES) {
      assertTrue(lang(locale).has("item.dogs-unleashed." + itemId), locale + " " + itemId);
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.dogsunleashed.item.pendant.PendantResourcesTest#types")
  @DisplayName("every charged pendant explains its effect in every language")
  void everyEffectIsExplainedInEveryLanguage(final PendantType type) throws IOException {
    for (final String locale : LOCALES) {
      final JsonObject lang = lang(locale);
      assertTrue(lang.has(type.effectTranslationKey()), locale + " " + type.id());
      assertTrue(lang.has(PendantItem.NO_STACKING_KEY), locale);
    }
  }

  static Stream<PendantType> types() {
    return Stream.of(PendantType.values());
  }

  private static JsonObject lang(final String locale) throws IOException {
    return JsonParser.parseString(Files.readString(ASSETS.resolve("lang/" + locale + ".json")))
        .getAsJsonObject();
  }
}
