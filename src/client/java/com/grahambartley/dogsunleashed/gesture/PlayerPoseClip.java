package com.grahambartley.dogsunleashed.gesture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.joml.Vector3f;

public final class PlayerPoseClip {

  public static final PlayerPoseClip EMPTY = new PlayerPoseClip(0.0f, Map.of());

  public enum Channel {
    ROTATION("rotation"),
    POSITION("position");

    private final String key;

    Channel(final String key) {
      this.key = key;
    }
  }

  record Track(float[] times, Vector3f[] values) {

    Vector3f sample(final float time) {
      if (time <= this.times[0]) {
        return new Vector3f(this.values[0]);
      }
      final int last = this.times.length - 1;
      if (time >= this.times[last]) {
        return new Vector3f(this.values[last]);
      }
      int after = Arrays.binarySearch(this.times, time);
      if (after >= 0) {
        return new Vector3f(this.values[after]);
      }
      after = -after - 1;
      final int before = after - 1;
      final float t = (time - this.times[before]) / (this.times[after] - this.times[before]);
      return new Vector3f(this.values[before]).lerp(this.values[after], t);
    }
  }

  private final float length;
  private final Map<String, Track> tracks;

  private PlayerPoseClip(final float length, final Map<String, Track> tracks) {
    this.length = length;
    this.tracks = tracks;
  }

  public static PlayerPoseClip parse(final JsonObject file, final String clipName) {
    final JsonObject clip = file.getAsJsonObject("animations").getAsJsonObject(clipName);
    final Map<String, Track> tracks = new HashMap<>();
    for (final Map.Entry<String, JsonElement> bone : clip.getAsJsonObject("bones").entrySet()) {
      for (final Channel channel : Channel.values()) {
        final JsonObject keys = bone.getValue().getAsJsonObject().getAsJsonObject(channel.key);
        if (keys != null) {
          tracks.put(key(bone.getKey(), channel), track(keys));
        }
      }
    }
    return new PlayerPoseClip(clip.get("animation_length").getAsFloat(), Map.copyOf(tracks));
  }

  public float length() {
    return this.length;
  }

  public Vector3f sample(final String bone, final Channel channel, final float seconds) {
    final Track track = this.tracks.get(key(bone, channel));
    return track == null ? new Vector3f() : track.sample(seconds);
  }

  private static Track track(final JsonObject keys) {
    final TreeMap<Float, Vector3f> sorted = new TreeMap<>();
    for (final Map.Entry<String, JsonElement> key : keys.entrySet()) {
      final JsonElement value = key.getValue();
      final JsonArray vector =
          value.isJsonArray()
              ? value.getAsJsonArray()
              : value.getAsJsonObject().getAsJsonArray("vector");
      sorted.put(
          Float.parseFloat(key.getKey()),
          new Vector3f(
              vector.get(0).getAsFloat(), vector.get(1).getAsFloat(), vector.get(2).getAsFloat()));
    }
    final float[] times = new float[sorted.size()];
    final Vector3f[] values = new Vector3f[sorted.size()];
    int i = 0;
    for (final Map.Entry<Float, Vector3f> entry : sorted.entrySet()) {
      times[i] = entry.getKey();
      values[i] = entry.getValue();
      i++;
    }
    return new Track(times, values);
  }

  private static String key(final String bone, final Channel channel) {
    return bone + "/" + channel.key;
  }
}
