package com.grahambartley.dogsunleashed.sound;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class SoundAssets {

  static final String NAMESPACE = "dogs-unleashed:";
  private static final Path ROOT = root();

  private SoundAssets() {}

  private static Path root() {
    try {
      return Path.of(SoundAssets.class.getResource("/assets/dogs-unleashed").toURI());
    } catch (URISyntaxException ex) {
      throw new IllegalStateException("cannot find the mod's assets", ex);
    }
  }

  static Path path(final String relative) {
    return ROOT.resolve(relative);
  }

  static Path clipPath(final String soundId) {
    return path("sounds/" + soundId.substring(NAMESPACE.length()) + ".ogg");
  }

  static JsonObject readJson(final String relative) {
    try {
      return JsonParser.parseString(Files.readString(path(relative), StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (IOException ex) {
      throw new IllegalStateException("cannot read " + relative, ex);
    }
  }
}
