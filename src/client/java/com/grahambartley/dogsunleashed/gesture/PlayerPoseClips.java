package com.grahambartley.dogsunleashed.gesture;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayerPoseClips {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "player_poses");
  static final Identifier PAT_ID = Identifier.of(MOD_ID, "player_poses/pat.json");
  static final String PAT_CLIP = "pat";

  private static final Logger LOGGER = LoggerFactory.getLogger(PlayerPoseClips.class);
  private static PlayerPoseClip pat;

  private PlayerPoseClips() {}

  public static synchronized PlayerPoseClip pat() {
    if (pat == null) {
      pat = load();
    }
    return pat;
  }

  public static synchronized void clear() {
    pat = null;
  }

  private static PlayerPoseClip load() {
    try {
      final Optional<Reader> reader = RigProfiles.openShipped(PAT_ID);
      if (reader.isEmpty()) {
        return PlayerPoseClip.EMPTY;
      }
      try (Reader open = reader.get()) {
        return PlayerPoseClip.parse(JsonParser.parseReader(open).getAsJsonObject(), PAT_CLIP);
      }
    } catch (final IOException | RuntimeException exception) {
      LOGGER.warn("Unreadable player pose clip {}: {}", PAT_ID, exception.getMessage());
      return PlayerPoseClip.EMPTY;
    }
  }
}
