package com.grahambartley.dogsunleashed.render.rig;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RigProfiles {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "rig_profiles");

  @FunctionalInterface
  public interface Source {
    Optional<Reader> open(Identifier id) throws IOException;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(RigProfiles.class);

  private static final RigProfiles SHARED = new RigProfiles(RigProfiles::openShipped);

  private final Source source;
  private final Map<UnleashedDogBreed, RigProfile> byRig = new EnumMap<>(UnleashedDogBreed.class);

  public RigProfiles(final Source source) {
    this.source = source;
  }

  public static RigProfiles shared() {
    return SHARED;
  }

  public static RigProfile of(final UnleashedDogBreed rig) {
    return SHARED.profileOf(rig);
  }

  public static Identifier idOf(final UnleashedDogBreed rig) {
    return Identifier.of(MOD_ID, "rig_profiles/" + rig.serializedId() + ".json");
  }

  public synchronized RigProfile profileOf(final UnleashedDogBreed rig) {
    return this.byRig.computeIfAbsent(rig, this::load);
  }

  public synchronized void clear() {
    this.byRig.clear();
  }

  private RigProfile load(final UnleashedDogBreed rig) {
    try {
      final Optional<Reader> reader = this.source.open(idOf(rig));
      if (reader.isEmpty()) {
        return RigProfile.EMPTY;
      }
      try (Reader open = reader.get()) {
        return RigProfile.parse(JsonParser.parseReader(open).getAsJsonObject());
      }
    } catch (final IOException | RuntimeException exception) {
      LOGGER.warn("Unreadable rig profile for {}: {}", rig.serializedId(), exception.getMessage());
      return RigProfile.EMPTY;
    }
  }

  static Optional<Reader> openShipped(final Identifier id) throws IOException {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client != null && client.getResourceManager() != null) {
      final Optional<Resource> resource = client.getResourceManager().getResource(id);
      return resource.isPresent() ? Optional.of(resource.get().getReader()) : Optional.empty();
    }
    return openBundled(id);
  }

  public static Optional<Reader> openBundled(final Identifier id) {
    final InputStream stream =
        RigProfiles.class.getResourceAsStream("/assets/" + id.getNamespace() + "/" + id.getPath());
    return Optional.ofNullable(stream).map(in -> new InputStreamReader(in, StandardCharsets.UTF_8));
  }
}
