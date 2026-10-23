package com.grahambartley.dogsunleashed.render.portrait;

import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import com.grahambartley.dogsunleashed.portrait.PortraitSize;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PortraitTextureCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(PortraitTextureCache.class);

  @FunctionalInterface
  public interface Baker {
    @Nullable
    Identifier bake(DogPortrait portrait);
  }

  private record Key(DogStatueLook look, PortraitSize size, DogStatuePose pose) {
    static Key of(final DogPortrait portrait) {
      return new Key(portrait.look(), portrait.size(), portrait.shownPose());
    }
  }

  private final Baker baker;
  private final Map<Key, Optional<Identifier>> baked = new HashMap<>();
  private final Map<Key, DogPortrait> pending = new LinkedHashMap<>();

  public PortraitTextureCache(final Baker baker) {
    this.baker = baker;
  }

  public @Nullable Identifier textureOf(final DogPortrait portrait) {
    final Key key = Key.of(portrait);
    final Optional<Identifier> texture = this.baked.get(key);
    if (texture != null) {
      return texture.orElse(null);
    }
    this.pending.putIfAbsent(key, portrait);
    return null;
  }

  public void bakePending() {
    this.pending.forEach((key, portrait) -> this.baked.put(key, this.tryBake(portrait)));
    this.pending.clear();
  }

  private Optional<Identifier> tryBake(final DogPortrait portrait) {
    try {
      return Optional.ofNullable(this.baker.bake(portrait));
    } catch (final RuntimeException exception) {
      LOGGER.warn("Could not paint a dog portrait", exception);
      return Optional.empty();
    }
  }

  public void clear(final TextureManager textureManager) {
    this.baked.values().forEach(texture -> texture.ifPresent(textureManager::destroyTexture));
    this.baked.clear();
    this.pending.clear();
  }
}
