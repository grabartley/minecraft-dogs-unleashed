package com.grahambartley.dogsunleashed.render.growth;

import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public record RigAnchors(@Nullable String root, Map<String, String> anchors) {

  public record Extent(Vector3f center, float volume) {}

  public static final RigAnchors NONE = new RigAnchors(null, Map.of());

  private static final Map<Slot, Slot> HANGS_FROM = new EnumMap<>(Slot.class);

  static {
    HANGS_FROM.put(Slot.MOUTH, Slot.HEAD);
    HANGS_FROM.put(Slot.EYES, Slot.HEAD);
    HANGS_FROM.put(Slot.EAR_LEFT, Slot.HEAD);
    HANGS_FROM.put(Slot.EAR_RIGHT, Slot.HEAD);
    HANGS_FROM.put(Slot.HEAD, Slot.NECK);
    HANGS_FROM.put(Slot.NECK, Slot.TORSO);
    HANGS_FROM.put(Slot.TAIL, Slot.TORSO);
    HANGS_FROM.put(Slot.LEG_FRONT_LEFT, Slot.TORSO);
    HANGS_FROM.put(Slot.LEG_FRONT_RIGHT, Slot.TORSO);
    HANGS_FROM.put(Slot.LEG_BACK_LEFT, Slot.TORSO);
    HANGS_FROM.put(Slot.LEG_BACK_RIGHT, Slot.TORSO);
  }

  public RigAnchors {
    anchors = Map.copyOf(anchors);
  }

  public static RigAnchors of(
      final Map<String, @Nullable String> parents,
      final RigProfile profile,
      final Map<String, Extent> extents) {
    final Finder finder = new Finder(parents, profile, extents);
    final Optional<String> root = finder.slotTop(Slot.TORSO);
    if (root.isEmpty()) {
      return NONE;
    }
    final Map<String, String> anchors = new HashMap<>();
    for (final Map.Entry<String, @Nullable String> entry : parents.entrySet()) {
      if (entry.getValue() == null && !entry.getKey().equals(root.get())) {
        anchors.put(entry.getKey(), finder.anchorOf(entry.getKey(), root.get()));
      }
    }
    anchors.replaceAll((bone, anchor) -> loops(bone, anchors) ? root.get() : anchor);
    return new RigAnchors(root.get(), anchors);
  }

  public Map<String, @Nullable String> logicalParents(final Map<String, @Nullable String> parents) {
    final Map<String, @Nullable String> logical = new HashMap<>(parents);
    logical.putAll(this.anchors);
    return logical;
  }

  private static boolean loops(final String bone, final Map<String, String> anchors) {
    final Set<String> seen = new HashSet<>();
    String at = bone;
    while (at != null) {
      if (!seen.add(at)) {
        return true;
      }
      at = anchors.get(at);
    }
    return false;
  }

  private record Finder(
      Map<String, @Nullable String> parents, RigProfile profile, Map<String, Extent> extents) {

    String anchorOf(final String bone, final String root) {
      Slot slot = this.slotOf(bone).orElseGet(() -> this.nearestBodySlot(bone, root));
      while (slot != Slot.TORSO) {
        final Optional<String> candidate = this.slotTop(slot);
        if (candidate.isPresent() && !candidate.get().equals(bone)) {
          return candidate.get();
        }
        slot = HANGS_FROM.get(slot);
      }
      return root;
    }

    Optional<String> slotTop(final Slot slot) {
      final Set<String> bones = new HashSet<>(this.profile.bonesIn(slot));
      if (slot == Slot.EAR_LEFT || slot == Slot.EAR_RIGHT) {
        bones.addAll(this.profile.ears().variantBones());
      }
      return bones.stream()
          .filter(this.parents::containsKey)
          .map(this::topOf)
          .distinct()
          .max(
              Comparator.comparingDouble((String top) -> this.volumeOf(top))
                  .thenComparing(Comparator.reverseOrder()));
    }

    private Optional<Slot> slotOf(final String bone) {
      for (final Slot slot : HANGS_FROM.keySet()) {
        if (this.profile.bonesIn(slot).contains(bone)) {
          return Optional.of(slot);
        }
      }
      if (this.profile.bonesIn(Slot.TORSO).contains(bone)) {
        return Optional.of(Slot.TORSO);
      }
      return this.profile.ears().variantBones().contains(bone)
          ? Optional.of(Slot.EAR_LEFT)
          : Optional.empty();
    }

    private Slot nearestBodySlot(final String bone, final String root) {
      final Extent own = this.extents.get(bone);
      final Optional<String> head = this.slotTop(Slot.HEAD);
      if (own == null || head.isEmpty() || !this.extents.containsKey(head.get())) {
        return Slot.TORSO;
      }
      final Extent torso = this.extents.get(root);
      return torso == null
              || own.center().distanceSquared(this.extents.get(head.get()).center())
                  < own.center().distanceSquared(torso.center())
          ? Slot.HEAD
          : Slot.TORSO;
    }

    private String topOf(final String bone) {
      String at = bone;
      final Set<String> seen = new HashSet<>();
      while (this.parents.get(at) != null
          && this.parents.containsKey(this.parents.get(at))
          && seen.add(at)) {
        at = this.parents.get(at);
      }
      return at;
    }

    private float volumeOf(final String bone) {
      final Extent extent = this.extents.get(bone);
      return extent == null ? 0.0f : extent.volume();
    }
  }
}
