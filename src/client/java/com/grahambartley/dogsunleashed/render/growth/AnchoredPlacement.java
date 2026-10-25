package com.grahambartley.dogsunleashed.render.growth;

import java.util.Optional;
import java.util.function.Function;
import org.joml.Vector3f;

public record AnchoredPlacement(RigAnchors anchors, Vector3f rootPivot, GrowthScaleTree tree) {

  public record Shift(Vector3f to, float scale, Vector3f from) {

    public Shift {
      to = new Vector3f(to);
      from = new Vector3f(from);
    }

    @Override
    public Vector3f to() {
      return new Vector3f(this.to);
    }

    @Override
    public Vector3f from() {
      return new Vector3f(this.from);
    }
  }

  public AnchoredPlacement {
    rootPivot = new Vector3f(rootPivot);
  }

  @Override
  public Vector3f rootPivot() {
    return new Vector3f(this.rootPivot);
  }

  public Optional<Shift> shiftOf(final String bone, final Function<String, Vector3f> posedPivot) {
    final String anchor = this.anchors.anchors().get(bone);
    if (anchor == null) {
      return Optional.empty();
    }
    final Vector3f pivot = posedPivot.apply(bone);
    return Optional.of(
        new Shift(this.carry(anchor, pivot, posedPivot), this.tree.cumulativeScale(bone), pivot));
  }

  private Vector3f carry(
      final String bone, final Vector3f point, final Function<String, Vector3f> posedPivot) {
    final String anchor = this.anchors.anchors().get(bone);
    if (anchor == null) {
      return this.scaled(point, this.rootPivot, this.rootPivot, bone);
    }
    final Vector3f pivot = posedPivot.apply(bone);
    return this.scaled(point, pivot, this.carry(anchor, pivot, posedPivot), bone);
  }

  private Vector3f scaled(
      final Vector3f point, final Vector3f from, final Vector3f to, final String bone) {
    return new Vector3f(point).sub(from).mul(this.tree.cumulativeScale(bone)).add(to);
  }
}
