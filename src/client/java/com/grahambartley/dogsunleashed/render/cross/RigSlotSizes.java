package com.grahambartley.dogsunleashed.render.cross;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.render.cross.CrossProportions.SlotSizes;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class RigSlotSizes {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "rig_slot_sizes");

  private static final List<Slot> LEG_SLOTS =
      List.of(Slot.LEG_FRONT_LEFT, Slot.LEG_FRONT_RIGHT, Slot.LEG_BACK_LEFT, Slot.LEG_BACK_RIGHT);
  private static final Map<BakedGeoModel, SlotSizes> CACHE = new WeakHashMap<>();

  private RigSlotSizes() {}

  public static synchronized SlotSizes of(final BakedGeoModel model, final RigProfile profile) {
    return CACHE.computeIfAbsent(model, ignored -> measure(model, profile));
  }

  public static synchronized void clear() {
    CACHE.clear();
  }

  static SlotSizes measure(final BakedGeoModel model, final RigProfile profile) {
    final CubeGeometry.Bounds whole = new CubeGeometry.Bounds();
    includeAll(model.topLevelBones(), whole);
    if (whole.isEmpty()) {
      return new SlotSizes(0.0f, 0.0f, 0.0f, 0.0f);
    }
    final float height = whole.max().y - whole.min().y;
    final CubeGeometry.Bounds torso = boundsOf(model, profile, Slot.TORSO);
    final boolean hasLegs =
        LEG_SLOTS.stream().anyMatch(slot -> !boundsOf(model, profile, slot).isEmpty());
    return new SlotSizes(
        longestSide(boundsOf(model, profile, Slot.HEAD)) / height,
        longestSide(torso) / height,
        hasLegs && !torso.isEmpty() ? (torso.min().y - whole.min().y) / height : 0.0f,
        longestSide(boundsOf(model, profile, Slot.TAIL)) / height);
  }

  private static CubeGeometry.Bounds boundsOf(
      final BakedGeoModel model, final RigProfile profile, final Slot slot) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    for (final String name : profile.bonesIn(slot)) {
      model.getBone(name).ifPresent(bone -> include(bone, bounds));
    }
    return bounds;
  }

  private static float longestSide(final CubeGeometry.Bounds bounds) {
    if (bounds.isEmpty()) {
      return 0.0f;
    }
    final Vector3f size = bounds.max().sub(bounds.min());
    return Math.max(size.x, Math.max(size.y, size.z));
  }

  private static void includeAll(final List<GeoBone> bones, final CubeGeometry.Bounds bounds) {
    for (final GeoBone bone : bones) {
      include(bone, bounds);
      includeAll(bone.getChildBones(), bounds);
    }
  }

  private static void include(final GeoBone bone, final CubeGeometry.Bounds bounds) {
    final CubeGeometry.Bounds own = CubeGeometry.posedBoundsOf(bone);
    if (!own.isEmpty()) {
      bounds.include(own.min());
      bounds.include(own.max());
    }
  }
}
