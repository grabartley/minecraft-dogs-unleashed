package com.grahambartley.dogsunleashed.compat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** Reads the shipped {@code en_us} lang file so tests can assert that a key actually resolves. */
final class LangKeys {

  private static final String LANG_PATH = "/assets/dogs-unleashed/lang/en_us.json";

  private static final JsonObject EN_US = load();

  private LangKeys() {}

  static boolean contains(final String key) {
    return EN_US.has(key);
  }

  static String get(final String key) {
    return EN_US.get(key).getAsString();
  }

  private static JsonObject load() {
    try (InputStream stream = LangKeys.class.getResourceAsStream(LANG_PATH)) {
      if (stream == null) {
        throw new IllegalStateException(LANG_PATH + " is missing from the test classpath");
      }
      return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
