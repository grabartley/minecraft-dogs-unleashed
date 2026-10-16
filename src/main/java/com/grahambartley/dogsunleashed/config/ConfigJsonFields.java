package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ConfigJsonFields {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigJsonFields.class);

  private final JsonObject root;

  ConfigJsonFields(final JsonObject root) {
    this.root = root;
  }

  boolean readBoolean(final String key, final boolean fallback) {
    if (!root.has(key)) {
      return fallback;
    }
    final JsonElement element = root.get(key);
    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
      return element.getAsBoolean();
    }
    warnInvalid(key, element, "true or false", fallback);
    return fallback;
  }

  int readInt(final String key, final int fallback) {
    return (int) readNumber(key, fallback);
  }

  float readFloat(final String key, final float fallback) {
    return (float) readNumber(key, fallback);
  }

  Map<String, Integer> readIntMap(final String key) {
    final Map<String, Integer> values = new LinkedHashMap<>();
    if (!root.has(key)) {
      return values;
    }
    final JsonElement element = root.get(key);
    if (!element.isJsonObject()) {
      warnInvalid(key, element, "an object of numbers", "defaults");
      return values;
    }
    for (final Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
      final OptionalDouble number = finiteNumber(entry.getValue());
      if (number.isPresent()) {
        values.put(entry.getKey(), (int) number.getAsDouble());
      } else {
        warnInvalid(key + "." + entry.getKey(), entry.getValue(), "a number", "default");
      }
    }
    return values;
  }

  private double readNumber(final String key, final double fallback) {
    if (!root.has(key)) {
      return fallback;
    }
    final JsonElement element = root.get(key);
    final OptionalDouble number = finiteNumber(element);
    if (number.isPresent()) {
      return number.getAsDouble();
    }
    warnInvalid(key, element, "a number", fallback);
    return fallback;
  }

  private static OptionalDouble finiteNumber(final JsonElement element) {
    if (!element.isJsonPrimitive()) {
      return OptionalDouble.empty();
    }
    final JsonPrimitive primitive = element.getAsJsonPrimitive();
    if (!primitive.isNumber()) {
      return OptionalDouble.empty();
    }
    final double value = primitive.getAsDouble();
    return Double.isFinite(value) ? OptionalDouble.of(value) : OptionalDouble.empty();
  }

  private static void warnInvalid(
      final String key, final JsonElement element, final String expected, final Object fallback) {
    LOGGER.warn(
        "Dogs Unleashed config value {}={} is not {}, using {}", key, element, expected, fallback);
  }
}
