package com.grahambartley.dogsunleashed.render.growth;

import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class GrowthRigScale {

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;
  private static final List<Slot> LEG_SLOTS =
      List.of(Slot.LEG_FRONT_LEFT, Slot.LEG_FRONT_RIGHT, Slot.LEG_BACK_LEFT, Slot.LEG_BACK_RIGHT);
  private static final Map<BakedGeoModel, Map<DireWolfGrowthStage.SlotScales, GrowthRigScale>>
      BY_SCALES = new WeakHashMap<>();

  private final GrowthScaleTree tree;
  private final GrowthGroundFit fit;
  private final AnchoredPlacement placement;
  private final Map<String, GeoBone> bones;

  private GrowthRigScale(
      final GrowthScaleTree tree,
      final GrowthGroundFit fit,
      final AnchoredPlacement placement,
      final Map<String, GeoBone> bones) {
    this.tree = tree;
    this.fit = fit;
    this.placement = placement;
    this.bones = bones;
  }

  public static GrowthRigScale of(
      final BakedGeoModel model, final RigProfile profile, final DireWolfGrowthStage stage) {
    return of(model, profile, stage.slotScales());
  }

  public static synchronized GrowthRigScale of(
      final BakedGeoModel model,
      final RigProfile profile,
      final DireWolfGrowthStage.SlotScales scales) {
    return BY_SCALES
        .computeIfAbsent(model, ignored -> new HashMap<>())
        .computeIfAbsent(scales, ignored -> build(model, profile, scales));
  }

  public float localScale(final String bone) {
    return this.tree.localScale(bone);
  }

  public Optional<AnchoredPlacement.Shift> shiftOf(final String bone) {
    return this.placement.shiftOf(bone, name -> posedPivotOf(this.bones.get(name)));
  }

  public Optional<String> anchorOf(final String bone) {
    return Optional.ofNullable(this.placement.anchors().anchors().get(bone));
  }

  public float lift() {
    return this.fit.lift();
  }

  public float heightScale() {
    return this.fit.heightScale();
  }

  static GrowthRigScale build(
      final BakedGeoModel model,
      final RigProfile profile,
      final DireWolfGrowthStage.SlotScales scales) {
    final Map<String, GeoBone> bones = new HashMap<>();
    collect(model.topLevelBones(), bones);
    final Map<String, String> parents = new HashMap<>();
    bones.forEach(
        (name, bone) ->
            parents.put(name, bone.getParent() == null ? null : bone.getParent().getName()));
    final Map<String, Float> slotScales = new HashMap<>();
    profile.bonesIn(Slot.HEAD).forEach(bone -> slotScales.put(bone, scales.head()));
    profile.bonesIn(Slot.TORSO).forEach(bone -> slotScales.put(bone, scales.torso()));
    profile.bonesIn(Slot.TAIL).forEach(bone -> slotScales.put(bone, scales.tail()));
    LEG_SLOTS.forEach(
        slot -> profile.bonesIn(slot).forEach(bone -> slotScales.put(bone, scales.legs())));
    final RigAnchors anchors = RigAnchors.of(parents, profile, extentsOf(bones));
    final Map<String, String> logicalParents = anchors.logicalParents(parents);
    final GrowthScaleTree tree = GrowthScaleTree.of(logicalParents, slotScales);
    final AnchoredPlacement placement =
        new AnchoredPlacement(anchors, restPivotOf(bones.get(anchors.root())), tree);

    final List<GrowthGroundFit.Leg> legs = new ArrayList<>();
    for (final Slot slot : LEG_SLOTS) {
      legOf(bones, logicalParents, profile.bonesIn(slot), tree).ifPresent(legs::add);
    }
    final CubeGeometry.Bounds whole = new CubeGeometry.Bounds();
    bones.values().forEach(bone -> include(whole, bone));
    final GeoBone head =
        profile.bonesIn(Slot.HEAD).stream()
            .map(bones::get)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    if (legs.isEmpty() || whole.isEmpty() || head == null) {
      return new GrowthRigScale(tree, GrowthGroundFit.NONE, placement, bones);
    }
    return new GrowthRigScale(
        tree,
        GrowthGroundFit.of(
            legs, whole.max().y, head.getPivotY() / MODEL_UNITS_PER_BLOCK, scales.head()),
        placement,
        bones);
  }

  private static Map<String, RigAnchors.Extent> extentsOf(final Map<String, GeoBone> bones) {
    final Map<String, RigAnchors.Extent> extents = new HashMap<>();
    bones.forEach(
        (name, bone) -> {
          final CubeGeometry.Bounds own = CubeGeometry.posedBoundsOf(bone);
          if (!own.isEmpty()) {
            final Vector3f size = own.max().sub(own.min());
            extents.put(name, new RigAnchors.Extent(own.center(), size.x * size.y * size.z));
          }
        });
    return extents;
  }

  private static Vector3f restPivotOf(final GeoBone bone) {
    return bone == null
        ? new Vector3f()
        : new Vector3f(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
  }

  static Vector3f posedPivotOf(final GeoBone bone) {
    return bone == null
        ? new Vector3f()
        : new Vector3f(
            bone.getPivotX() - bone.getPosX(),
            bone.getPivotY() + bone.getPosY(),
            bone.getPivotZ() + bone.getPosZ());
  }

  private static Optional<GrowthGroundFit.Leg> legOf(
      final Map<String, GeoBone> bones,
      final Map<String, String> logicalParents,
      final Set<String> slotBones,
      final GrowthScaleTree tree) {
    GeoBone root = null;
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    for (final String name : slotBones) {
      final GeoBone bone = bones.get(name);
      if (bone == null) {
        continue;
      }
      include(bounds, bone);
      if (bone.getParent() == null || !slotBones.contains(bone.getParent().getName())) {
        root = bone;
      }
    }
    final GeoBone parent = root == null ? null : bones.get(logicalParents.get(root.getName()));
    if (parent == null || bounds.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        new GrowthGroundFit.Leg(
            parent.getPivotY() / MODEL_UNITS_PER_BLOCK,
            tree.cumulativeScale(parent.getName()),
            root.getPivotY() / MODEL_UNITS_PER_BLOCK,
            bounds.min().y,
            tree.cumulativeScale(root.getName())));
  }

  private static void include(final CubeGeometry.Bounds bounds, final GeoBone bone) {
    final CubeGeometry.Bounds own = CubeGeometry.posedBoundsOf(bone);
    if (!own.isEmpty()) {
      bounds.include(own.min());
      bounds.include(own.max());
    }
  }

  private static void collect(final List<GeoBone> level, final Map<String, GeoBone> out) {
    for (final GeoBone bone : level) {
      out.put(bone.getName(), bone);
      collect(bone.getChildBones(), out);
    }
  }
}
