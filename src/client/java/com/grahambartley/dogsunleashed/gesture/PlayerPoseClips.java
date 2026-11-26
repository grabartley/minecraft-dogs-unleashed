package com.grahambartley.dogsunleashed.gesture;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.google.gson.JsonObject;
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
  static final String KNEEL_CLIP = "pat";
  static final String STAND_CLIP = "pat_tall";

  public record Pat(PlayerPoseClip kneel, PlayerPoseClip stand) {

    static final Pat EMPTY = new Pat(PlayerPoseClip.EMPTY, PlayerPoseClip.EMPTY);

    public float length() {
      return Math.min(this.kneel.length(), this.stand.length());
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(PlayerPoseClips.class);
  private static Pat pat;

  private PlayerPoseClips() {}

  public static synchronized Pat pat() {
    if (pat == null) {
      pat = load();
    }
    return pat;
  }

  public static synchronized void clear() {
    pat = null;
  }

  private static Pat load() {
    try {
      final Optional<Reader> reader = RigProfiles.openShipped(PAT_ID);
      if (reader.isEmpty()) {
        return Pat.EMPTY;
      }
      try (Reader open = reader.get()) {
        final JsonObject file = JsonParser.parseReader(open).getAsJsonObject();
        return new Pat(
            PlayerPoseClip.parse(file, KNEEL_CLIP), PlayerPoseClip.parse(file, STAND_CLIP));
      }
    } catch (final IOException | RuntimeException exception) {
      LOGGER.warn("Unreadable player pose clip {}: {}", PAT_ID, exception.getMessage());
      return Pat.EMPTY;
    }
  }
}
