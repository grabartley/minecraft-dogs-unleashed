package com.grahambartley.dogsunleashed.render.portrait;

import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.PortraitSize;
import java.util.HashMap;
import java.util.Iterator;
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

  private record Key(
      DogStatueLook look, PortraitSize size, DogStatuePose pose, DogPortraitOutfit outfit) {
    static Key of(final DogPortrait portrait) {
      return new Key(portrait.look(), portrait.size(), portrait.shownPose(), portrait.outfit());
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

  public void bakeNextPending() {
    final Iterator<Map.Entry<Key, DogPortrait>> next = this.pending.entrySet().iterator();
    if (!next.hasNext()) {
      return;
    }
    final Map.Entry<Key, DogPortrait> entry = next.next();
    next.remove();
    this.baked.put(entry.getKey(), this.tryBake(entry.getValue()));
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
