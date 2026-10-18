package com.grahambartley.dogsunleashed.render.growth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage.SlotScales;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import java.util.List;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

class GrowthRigScaleTest {

  private static final float EPSILON = 1.0e-5f;
  private static final RigProfile PROFILE =
      RigProfile.parse(
          JsonParser.parseString(
                  """
                  {
                    "head": ["head"],
                    "leg_front_left": ["leg"]
                  }
                  """)
              .getAsJsonObject());

  @Test
  @DisplayName("unscaled slots leave every bone, the lift and the height alone")
  void unscaledSlotsChangeNothing() {
    final GrowthRigScale scale = GrowthRigScale.build(wolf(), PROFILE, SlotScales.NONE);

    assertEquals(1.0f, scale.localScale("leg"), EPSILON);
    assertEquals(1.0f, scale.localScale("head"), EPSILON);
    assertEquals(0.0f, scale.lift(), EPSILON);
    assertEquals(1.0f, scale.heightScale(), EPSILON);
  }

  @Test
  @DisplayName("each profile slot gets its own scale, and bones outside every slot keep theirs")
  void slotsScaleTheirBones() {
    final GrowthRigScale scale =
        GrowthRigScale.build(wolf(), PROFILE, new SlotScales(1.25f, 1.0f, 0.8f, 1.0f));

    assertEquals(1.25f, scale.localScale("head"), EPSILON);
    assertEquals(0.8f, scale.localScale("leg"), EPSILON);
    assertEquals(1.0f, scale.localScale("body"), EPSILON);
  }

  @Test
  @DisplayName("longer legs lift the body by the extra length of the leg")
  void longerLegsLiftTheBody() {
    final GrowthRigScale scale =
        GrowthRigScale.build(wolf(), PROFILE, new SlotScales(1.0f, 1.0f, 1.2f, 1.0f));

    assertEquals(0.375f * 0.2f, scale.lift(), EPSILON);
  }

  @Test
  @DisplayName("a rig whose profile names no legs gets no ground fit")
  void noLegsMeansNoGroundFit() {
    final RigProfile headOnly =
        RigProfile.parse(JsonParser.parseString("{\"head\": [\"head\"]}").getAsJsonObject());

    final GrowthRigScale scale =
        GrowthRigScale.build(wolf(), headOnly, new SlotScales(1.0f, 1.0f, 1.2f, 1.0f));

    assertEquals(0.0f, scale.lift(), EPSILON);
    assertEquals(1.0f, scale.heightScale(), EPSILON);
  }

  private static BakedGeoModel wolf() {
    final GeoBone body = bone(null, "body", 8, 0.375f, 0.75f);
    final GeoBone head = bone(body, "head", 10, 0.625f, 0.875f);
    final GeoBone leg = bone(body, "leg", 6, 0.0f, 0.375f);
    body.getChildBones().add(head);
    body.getChildBones().add(leg);
    return new BakedGeoModel(List.of(body), null);
  }

  private static GeoBone bone(
      final GeoBone parent,
      final String name,
      final float pivotY,
      final float bottom,
      final float top) {
    final GeoBone bone = new GeoBone(parent, name, false, 0.0, false, false);
    bone.updatePivot(0, pivotY, 0);
    bone.getCubes()
        .add(
            new GeoCube(
                new GeoQuad[] {face(bottom, Direction.DOWN), face(top, Direction.UP)},
                Vec3d.ZERO,
                Vec3d.ZERO,
                new Vec3d(2, (top - bottom) * 16, 2),
                0,
                false));
    return bone;
  }

  private static GeoQuad face(final float y, final Direction direction) {
    final GeoVertex[] vertices = {
      new GeoVertex(0, y, 0),
      new GeoVertex(0.125, y, 0),
      new GeoVertex(0.125, y, 0.125),
      new GeoVertex(0, y, 0.125)
    };
    return new GeoQuad(vertices, new Vector3f(0, direction.getOffsetY(), 0), direction);
  }
}
