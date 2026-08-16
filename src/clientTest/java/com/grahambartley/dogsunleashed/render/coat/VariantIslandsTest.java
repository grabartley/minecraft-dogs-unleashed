package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.render.coat.VariantIslands.Island;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

class VariantIslandsTest {

  private static final int ATLAS = 128;

  /** A quad covering the given texel rectangle, with UVs normalised the way GeckoLib bakes them. */
  private static GeoQuad quad(final int x, final int y, final int width, final int height) {
    final GeoVertex[] vertices = {
      vertex(x, y), vertex(x + width, y), vertex(x + width, y + height), vertex(x, y + height)
    };
    return new GeoQuad(vertices, new Vector3f(0, 0, 1), Direction.NORTH);
  }

  private static GeoVertex vertex(final int x, final int y) {
    return new GeoVertex(new Vector3f(0, 0, 0), x / (float) ATLAS, y / (float) ATLAS);
  }

  private static GeoCube cube(final GeoQuad... quads) {
    return new GeoCube(quads, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, 0, false);
  }

  @Test
  @DisplayName("a quad's normalised uvs come back as the texel rectangle it covers")
  void quadMapsBackToTexels() {
    final List<Island> islands =
        VariantIslands.ofCubes(List.of(cube(quad(77, 0, 10, 7))), ATLAS, ATLAS);
    assertEquals(List.of(new Island(77, 0, 10, 7)), islands);
  }

  @Test
  @DisplayName("the faces of a box-uv cube collapse back into the one island the artist sees")
  void adjacentFacesCollapseIntoOneIsland() {
    // a box uv layout: top and bottom above, four sides in a row beneath
    final GeoCube boxUv =
        cube(
            quad(20, 10, 5, 5),
            quad(25, 10, 5, 5),
            quad(20, 15, 5, 8),
            quad(25, 15, 5, 8),
            quad(30, 15, 5, 8),
            quad(35, 15, 5, 8));
    final List<Island> islands = VariantIslands.ofCubes(List.of(boxUv), ATLAS, ATLAS);
    assertEquals(1, islands.size());
    assertEquals(new Island(20, 10, 20, 13), islands.get(0));
  }

  @Test
  @DisplayName(
      "faces scattered across the atlas stay separate rather than swallowing what is between")
  void scatteredFacesStaySeparate() {
    final List<Island> islands =
        VariantIslands.ofCubes(
            List.of(cube(quad(0, 0, 4, 4)), cube(quad(90, 90, 4, 4))), ATLAS, ATLAS);
    assertEquals(2, islands.size());
    assertFalse(VariantIslands.contains(islands, 45, 45), "the gap between them is not claimed");
  }

  @Test
  @DisplayName("several cubes on one bone contribute all of their islands")
  void multipleCubesContributeAllIslands() {
    final List<Island> islands =
        VariantIslands.ofCubes(
            List.of(cube(quad(65, 0, 6, 4)), cube(quad(72, 0, 4, 2))), ATLAS, ATLAS);
    assertEquals(2, islands.size());
    assertTrue(VariantIslands.contains(islands, 66, 1));
    assertTrue(VariantIslands.contains(islands, 73, 1));
  }

  @ParameterizedTest(name = "({0},{1}) inside = {2}")
  @MethodSource("points")
  @DisplayName("an island covers its own texels and stops at its edges")
  void islandCoversItsOwnTexels(final int x, final int y, final boolean inside) {
    assertEquals(inside, new Island(77, 0, 10, 7).contains(x, y));
  }

  static Stream<Arguments> points() {
    return Stream.of(
        Arguments.of(77, 0, true),
        Arguments.of(86, 6, true),
        Arguments.of(76, 0, false),
        Arguments.of(87, 0, false),
        Arguments.of(77, 7, false));
  }

  @Test
  @DisplayName("a bone with no cubes claims nothing, so a missing variant repaints nothing")
  void noCubesClaimsNothing() {
    assertEquals(List.of(), VariantIslands.ofCubes(List.of(), ATLAS, ATLAS));
    assertFalse(VariantIslands.contains(List.of(), 10, 10));
  }

  @Test
  @DisplayName("a degenerate quad is ignored rather than producing an empty island")
  void degenerateQuadIsIgnored() {
    assertEquals(List.of(), VariantIslands.ofCubes(List.of(cube(quad(5, 5, 0, 0))), ATLAS, ATLAS));
  }
}
