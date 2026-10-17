package com.grahambartley.dogsunleashed.render.rig;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class SlotRegions {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "slot_regions");

  @FunctionalInterface
  public interface Models {
    Optional<BakedGeoModel> modelOf(UnleashedDogBreed rig);
  }

  private static final SlotRegions SHARED = new SlotRegions(RigModels::loaded, RigProfiles::of);

  private final Models models;
  private final Function<UnleashedDogBreed, RigProfile> profiles;
  private final Map<UnleashedDogBreed, Map<Slot, List<SlotRegion>>> byRig =
      new EnumMap<>(UnleashedDogBreed.class);

  public SlotRegions(final Models models, final Function<UnleashedDogBreed, RigProfile> profiles) {
    this.models = models;
    this.profiles = profiles;
  }

  public static SlotRegions shared() {
    return SHARED;
  }

  public synchronized List<SlotRegion> regionsOf(final UnleashedDogBreed rig, final Slot slot) {
    final Map<Slot, List<SlotRegion>> slots = this.byRig.get(rig);
    if (slots != null && slots.containsKey(slot)) {
      return slots.get(slot);
    }
    final Optional<BakedGeoModel> model = this.models.modelOf(rig);
    if (model.isEmpty()) {
      return List.of();
    }
    final List<SlotRegion> regions = of(model.get(), this.profiles.apply(rig), slot);
    this.byRig.computeIfAbsent(rig, key -> new EnumMap<>(Slot.class)).put(slot, regions);
    return regions;
  }

  public synchronized void clear() {
    this.byRig.clear();
  }

  public static List<SlotRegion> of(
      final BakedGeoModel model, final RigProfile profile, final Slot slot) {
    final int atlasWidth = (int) model.properties().textureWidth();
    final int atlasHeight = (int) model.properties().textureHeight();
    final List<SlotRegion> regions = new ArrayList<>();
    for (final String boneName : profile.bonesIn(slot)) {
      final Optional<GeoBone> bone = model.getBone(boneName);
      if (bone.isEmpty()) {
        continue;
      }
      for (final CubeGeometry.Face face : CubeGeometry.facesOf(bone.get())) {
        regionOf(face, atlasWidth, atlasHeight).ifPresent(regions::add);
      }
    }
    return List.copyOf(regions);
  }

  static Optional<SlotRegion> regionOf(
      final CubeGeometry.Face face, final int atlasWidth, final int atlasHeight) {
    final int[] columns = coveredTexels(face.us(), atlasWidth);
    final int[] rows = coveredTexels(face.vs(), atlasHeight);
    if (columns[1] < columns[0] || rows[1] < rows[0]) {
      return Optional.empty();
    }
    final Direction facing = Direction.getFacing(face.normal().x, face.normal().y, face.normal().z);
    return Optional.of(
        new SlotRegion(
            face.boneName(),
            facing,
            columns[0],
            rows[0],
            columns[1] - columns[0] + 1,
            rows[1] - rows[0] + 1));
  }

  private static int[] coveredTexels(final float[] uvs, final int atlasSize) {
    float min = Float.POSITIVE_INFINITY;
    float max = Float.NEGATIVE_INFINITY;
    for (final float uv : uvs) {
      min = Math.min(min, uv * atlasSize);
      max = Math.max(max, uv * atlasSize);
    }
    return new int[] {
      Math.max(0, (int) Math.ceil(min - 0.5f)),
      Math.min(atlasSize - 1, (int) Math.floor(max - 0.5f))
    };
  }
}
