package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigJsonFieldsTest {

  private static ConfigJsonFields fieldsWithValue(final String jsonValue) {
    final JsonObject root =
        JsonParser.parseString("{\"key\": " + jsonValue + "}").getAsJsonObject();
    return new ConfigJsonFields(root);
  }

  @ParameterizedTest(name = "{0} reads as itself")
  @ValueSource(booleans = {true, false})
  @DisplayName("readBoolean returns a JSON boolean as-is")
  void readBooleanReturnsJsonBoolean(final boolean value) {
    assertEquals(value, fieldsWithValue(String.valueOf(value)).readBoolean("key", !value));
  }

  @ParameterizedTest(name = "{0} falls back")
  @ValueSource(strings = {"\"yes\"", "\"true\"", "1", "0", "{}", "[]", "null"})
  @DisplayName("readBoolean falls back for anything that is not a JSON boolean")
  void readBooleanFallsBackForNonBoolean(final String jsonValue) {
    assertTrue(fieldsWithValue(jsonValue).readBoolean("key", true));
  }

  @Test
  @DisplayName("readBoolean falls back when the key is missing")
  void readBooleanFallsBackWhenMissing() {
    assertTrue(new ConfigJsonFields(new JsonObject()).readBoolean("key", true));
  }

  @ParameterizedTest(name = "{0} reads as 42")
  @ValueSource(strings = {"42", "42.9", "4.2e1"})
  @DisplayName("readInt truncates any finite JSON number")
  void readIntReadsFiniteNumber(final String jsonValue) {
    assertEquals(42, fieldsWithValue(jsonValue).readInt("key", 7));
  }

  @ParameterizedTest(name = "{0} falls back")
  @ValueSource(strings = {"\"abc\"", "\"12\"", "true", "{}", "[1]", "null", "NaN", "Infinity"})
  @DisplayName("readInt falls back for anything that is not a finite JSON number")
  void readIntFallsBackForNonNumber(final String jsonValue) {
    assertEquals(7, fieldsWithValue(jsonValue).readInt("key", 7));
  }

  @Test
  @DisplayName("readInt saturates a number beyond the int range instead of wrapping")
  void readIntSaturatesHugeNumber() {
    assertEquals(Integer.MAX_VALUE, fieldsWithValue("1e20").readInt("key", 7));
  }

  @Test
  @DisplayName("readInt falls back when the key is missing")
  void readIntFallsBackWhenMissing() {
    assertEquals(7, new ConfigJsonFields(new JsonObject()).readInt("key", 7));
  }

  @Test
  @DisplayName("readFloat returns a finite JSON number")
  void readFloatReadsFiniteNumber() {
    assertEquals(0.25f, fieldsWithValue("0.25").readFloat("key", 1.0f));
  }

  @ParameterizedTest(name = "{0} falls back")
  @ValueSource(strings = {"\"loud\"", "false", "{}", "[]", "null", "NaN", "-Infinity"})
  @DisplayName("readFloat falls back for anything that is not a finite JSON number")
  void readFloatFallsBackForNonNumber(final String jsonValue) {
    assertEquals(1.0f, fieldsWithValue(jsonValue).readFloat("key", 1.0f));
  }

  @Test
  @DisplayName("readIntMap keeps numeric entries and drops invalid ones")
  void readIntMapKeepsNumbersAndDropsInvalidEntries() {
    final Map<String, Integer> values =
        fieldsWithValue("{\"husky\": 40, \"beagle\": \"high\", \"shiba\": {}, \"pug\": null}")
            .readIntMap("key");
    assertEquals(Map.of("husky", 40), values);
  }

  @ParameterizedTest(name = "{0} reads as empty")
  @ValueSource(strings = {"5", "\"husky\"", "[]", "null"})
  @DisplayName("readIntMap returns an empty map when the value is not an object")
  void readIntMapReturnsEmptyForNonObject(final String jsonValue) {
    assertTrue(fieldsWithValue(jsonValue).readIntMap("key").isEmpty());
  }

  @Test
  @DisplayName("readIntMap returns an empty map when the key is missing")
  void readIntMapReturnsEmptyWhenMissing() {
    assertTrue(new ConfigJsonFields(new JsonObject()).readIntMap("key").isEmpty());
  }
}
