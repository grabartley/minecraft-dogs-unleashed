package com.grahambartley.dogsunleashed.render.growth;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public final class GrowthScaleTree {

  private final Map<String, Float> cumulative = new HashMap<>();
  private final Map<String, Float> local = new HashMap<>();

  private GrowthScaleTree() {}

  public static GrowthScaleTree of(
      final Map<String, @Nullable String> parents, final Map<String, Float> slotScales) {
    final GrowthScaleTree tree = new GrowthScaleTree();
    for (final String bone : parents.keySet()) {
      tree.resolve(bone, parents, slotScales);
    }
    return tree;
  }

  public float cumulativeScale(final String bone) {
    return this.cumulative.getOrDefault(bone, 1.0f);
  }

  public float localScale(final String bone) {
    return this.local.getOrDefault(bone, 1.0f);
  }

  private float resolve(
      final String bone,
      final Map<String, @Nullable String> parents,
      final Map<String, Float> slotScales) {
    final Float known = this.cumulative.get(bone);
    if (known != null) {
      return known;
    }
    final String parent = parents.get(bone);
    final float parentScale =
        parent == null || !parents.containsKey(parent)
            ? 1.0f
            : this.resolve(parent, parents, slotScales);
    final float own = slotScales.getOrDefault(bone, parentScale);
    this.cumulative.put(bone, own);
    this.local.put(bone, own / parentScale);
    return own;
  }
}
