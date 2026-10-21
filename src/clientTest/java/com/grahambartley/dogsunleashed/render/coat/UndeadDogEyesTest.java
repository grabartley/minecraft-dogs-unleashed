package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.QuadUv;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import com.grahambartley.dogsunleashed.render.rig.SlotRegion;
import com.grahambartley.dogsunleashed.render.rig.SlotRegions;
import com.grahambartley.dogsunleashed.render.rig.Texel;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

class UndeadDogEyesTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  private static final Map<UnleashedDogBreed, List<String>> TEXTURES_BY_RIG =
      Map.of(
          UnleashedDogBreed.BEAGLE, List.of("beagle_blackwhite", "beagle_tri1"),
          UnleashedDogBreed.GOLDEN_RETRIEVER, List.of("goldenretriever"),
          UnleashedDogBreed.HUSKY,
              List.of("husky_blackwhite_blueblue", "husky_graywhite_hazelhazel"),
          UnleashedDogBreed.DACHSHUND, List.of("dachshund_blacktan", "dachshund_red"),
          UnleashedDogBreed.SHIBA_INU, List.of("shibainu_red", "shibainu_sesame"),
          UnleashedDogBreed.PINSCHER, List.of("pinscher_blacktan", "pinscher_red"));

  private static final int PUPIL_MAX_LUMA = 90;

  private static final int CUTOUT_ALPHA = 26;

  private static final int EYE_REACH = 2;

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
  }

  static Stream<Arguments> rigTextures() {
    return TEXTURES_BY_RIG.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream().map(tex -> Arguments.of(entry.getKey(), tex)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every shipped rig keeps both of its declared pupils on its head")
  void shippedRigsKeepBothPupils(final UnleashedDogBreed rig) {
    final UndeadDogEyes eyes = eyesOf(rig);

    assertEquals(2, eyes.pupils().size(), "a dog has two eyes");
    assertEquals(BUNDLED.profileOf(rig).pupils(), eyes.pupils());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("the atlas size comes from the rig's geometry")
  void atlasSizeComesFromTheGeometry(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final UndeadDogEyes eyes = eyesOf(rig);

    assertEquals((int) model.properties().textureWidth(), eyes.atlasWidth());
    assertEquals((int) model.properties().textureHeight(), eyes.atlasHeight());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("each pupil sits on the face of the head that the closed eyelids cover")
  void pupilsSitUnderTheClosedEyelids(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    ShippedRigs.pose(model, rig.serializedId(), "sleep", profile.bonesIn(Slot.EYES)::contains);
    final CubeGeometry.Bounds eyes = new CubeGeometry.Bounds();
    for (final String name : profile.bonesIn(Slot.EYES)) {
      final GeoBone lid = model.getBone(name).orElseThrow();
      lid.getCubes().stream()
          .flatMap(cube -> ShippedRigs.liveCorners(lid, cube).stream())
          .forEach(eyes::include);
    }
    final int width = (int) model.properties().textureWidth();
    final int height = (int) model.properties().textureHeight();

    for (final Texel pupil : profile.pupils()) {
      final Optional<Vector3f> onHead = pupilOnHead(model, profile, pupil, width, height);
      assertTrue(onHead.isPresent(), rig + " pupil " + pupil + " is not on the head");
      assertTrue(
          eyes.containsXy(onHead.get().x, onHead.get().y),
          rig + " pupil " + pupil + " at " + onHead.get() + " is not under the closed eyelids");
    }
  }

  @ParameterizedTest(name = "{0} closed eyelids cover the pupils on {1}")
  @MethodSource("everyCoat")
  @DisplayName("on every coat the closed eyelids paint over both pupils, so a blink shows")
  void closedEyelidsPaintOverThePupils(final UnleashedDogBreed rig, final String texture)
      throws IOException {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    ShippedRigs.pose(model, rig.serializedId(), "sleep", profile.bonesIn(Slot.EYES)::contains);
    final BufferedImage image = ImageIO.read(resource("textures/entity/" + texture + ".png"));
    final int width = (int) model.properties().textureWidth();
    final int height = (int) model.properties().textureHeight();
    final List<GeoBone> lids =
        profile.bonesIn(Slot.EYES).stream()
            .map(model::getBone)
            .flatMap(Optional::stream)
            .map(GeoBone.class::cast)
            .toList();

    for (final Texel pupil : profile.pupils()) {
      final CubeGeometry.Face face =
          headFaceUnder(model, profile, pupil, width, height).orElseThrow();
      final Vector3f outward = face.posedNormal();
      final Vector3f onHead = pointOf(face, pupil, width, height).orElseThrow();
      assertTrue(
          lids.stream()
              .flatMap(
                  lid -> lid.getCubes().stream().map(cube -> LidFace.facing(lid, cube, outward)))
              .flatMap(front -> front.uvAlong(onHead, outward).stream())
              .anyMatch(uv -> alphaAt(image, uv) >= CUTOUT_ALPHA),
          rig + " closed eyelids leave pupil " + pupil + " showing on " + texture);
    }
  }

  @ParameterizedTest(name = "{0} pupils land on dark pixels of {1}")
  @MethodSource("rigTextures")
  @DisplayName("every declared pupil lands on an actual dark eye pixel of the shipped art")
  void pupilsLandOnDarkPixelsOfTheShippedArt(final UnleashedDogBreed rig, final String texture)
      throws IOException {
    final UndeadDogEyes eyes = eyesOf(rig);
    final BufferedImage image = ImageIO.read(resource("textures/entity/" + texture + ".png"));
    assertTrue(eyes.fits(image.getWidth(), image.getHeight()), texture + " size");

    for (final Texel pupil : eyes.pupils()) {
      final int rgb = image.getRGB(pupil.x(), pupil.y());
      final int luma =
          (((rgb >> 16) & 0xFF) * 299 + ((rgb >> 8) & 0xFF) * 587 + (rgb & 0xFF) * 114) / 1000;
      assertTrue(
          luma <= PUPIL_MAX_LUMA,
          rig + " pupil " + pupil + " on " + texture + " reads bright fur (luma " + luma + ")");
    }
  }

  static Stream<Arguments> everyCoatTexture() throws IOException, URISyntaxException {
    final Path folder =
        Path.of(
            UndeadDogEyesTest.class.getResource("/assets/dogs-unleashed/textures/entity").toURI());
    try (Stream<Path> files = Files.list(folder)) {
      final List<String> names =
          files
              .map(path -> path.getFileName().toString())
              .filter(name -> name.endsWith(".png") && !name.endsWith("_collar.png"))
              .map(name -> name.substring(0, name.length() - ".png".length()))
              .toList();
      return rigs()
          .flatMap(
              rig ->
                  names.stream()
                      .filter(
                          name ->
                              name.equals(rig.serializedId())
                                  || name.startsWith(rig.serializedId() + "_"))
                      .map(name -> Arguments.of(rig, name)));
    }
  }

  @ParameterizedTest(name = "{0} closed eyelids change only the eyes on {1}")
  @MethodSource("everyCoat")
  @DisplayName("on every coat a closed eyelid differs from the head behind it only at the eyes")
  void closedEyelidsChangeOnlyTheEyes(final UnleashedDogBreed rig, final String texture)
      throws IOException {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    ShippedRigs.pose(model, rig.serializedId(), "sleep", profile.bonesIn(Slot.EYES)::contains);
    final BufferedImage image = ImageIO.read(resource("textures/entity/" + texture + ".png"));
    final int width = (int) model.properties().textureWidth();
    final int height = (int) model.properties().textureHeight();
    final Texel firstPupil = profile.pupils().get(0);
    final CubeGeometry.Face face =
        headFaceUnder(model, profile, firstPupil, width, height).orElseThrow();
    final Vector3f outward = face.posedNormal();
    final LidFace head = LidFace.posed(face);
    final List<String> changed = new ArrayList<>();

    for (final String name : profile.bonesIn(Slot.EYES)) {
      final GeoBone lid = model.getBone(name).orElseThrow();
      for (final GeoCube cube : lid.getCubes()) {
        final LidFace front = LidFace.facing(lid, cube, outward);
        for (final Texel texel : front.texels(width, height)) {
          if (alphaAt(image, texel) < CUTOUT_ALPHA) {
            continue;
          }
          final Optional<Texel> behind =
              front
                  .pointAt((texel.x() + 0.5f) / width, (texel.y() + 0.5f) / height)
                  .flatMap(point -> head.uvAlong(point, outward))
                  .map(uv -> new Texel((int) (uv[0] * width), (int) (uv[1] * height)));
          if (behind.isEmpty()) {
            changed.add(name + " " + texel + " hangs off the face");
          } else if (image.getRGB(texel.x(), texel.y())
                  != image.getRGB(behind.get().x(), behind.get().y())
              && profile.pupils().stream().noneMatch(pupil -> nextTo(pupil, behind.get()))) {
            changed.add(name + " " + texel + " over " + behind.get());
          }
        }
      }
    }
    assertTrue(
        changed.isEmpty(), rig + " on " + texture + " closes over more than the eyes: " + changed);
  }

  static Stream<Arguments> everyCoat() throws IOException, URISyntaxException {
    return everyCoatTexture()
        .filter(
            arguments -> {
              final UnleashedDogBreed rig = (UnleashedDogBreed) arguments.get()[0];
              final String path = "textures/entity/" + arguments.get()[1] + ".png";
              return Arrays.stream(RigProfile.Overlay.values())
                  .map(BUNDLED.profileOf(rig)::overlay)
                  .flatMap(Optional::stream)
                  .noneMatch(id -> id.getPath().equals(path));
            });
  }

  @ParameterizedTest(name = "{0} eyes fit {1}")
  @MethodSource("everyCoatTexture")
  @DisplayName("every coat texture is the size of its rig's atlas, so its eyes are painted")
  void everyCoatFitsItsRigAtlas(final UnleashedDogBreed rig, final String texture)
      throws IOException {
    final BufferedImage image = ImageIO.read(resource("textures/entity/" + texture + ".png"));

    assertTrue(
        eyesOf(rig).fits(image.getWidth(), image.getHeight()),
        texture + " is " + image.getWidth() + "x" + image.getHeight());
  }

  @Test
  @DisplayName("a pupil that no longer sits on the head is dropped")
  void pupilOffTheHeadIsDropped() {
    final List<SlotRegion> head = List.of(new SlotRegion("head", Direction.NORTH, 6, 46, 6, 6));

    final UndeadDogEyes eyes =
        UndeadDogEyes.of(
            ShippedRigs.model("beagle"), List.of(new Texel(7, 47), new Texel(40, 40)), head);

    assertEquals(List.of(new Texel(7, 47)), eyes.pupils());
  }

  @ParameterizedTest(name = "{0}x{1} fits={2}")
  @CsvSource({"128, 128, true", "64, 64, false", "128, 64, false", "64, 128, false"})
  @DisplayName("eyes only fit a texture the same size as their atlas")
  void fitsOnlyTheirOwnAtlasSize(final int width, final int height, final boolean fits) {
    assertEquals(fits, new UndeadDogEyes(128, 128, List.of()).fits(width, height));
  }

  @Test
  @DisplayName("no eyes fit no real texture")
  void noneFitsNothing() {
    assertFalse(UndeadDogEyes.NONE.fits(64, 64));
    assertTrue(UndeadDogEyes.NONE.pupils().isEmpty());
  }

  private static UndeadDogEyes eyesOf(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    return UndeadDogEyes.of(model, profile.pupils(), SlotRegions.of(model, profile, Slot.HEAD));
  }

  private static Optional<Vector3f> pupilOnHead(
      final BakedGeoModel model,
      final RigProfile profile,
      final Texel pupil,
      final int width,
      final int height) {
    return headFaceUnder(model, profile, pupil, width, height)
        .flatMap(face -> pointOf(face, pupil, width, height));
  }

  private static Optional<CubeGeometry.Face> headFaceUnder(
      final BakedGeoModel model,
      final RigProfile profile,
      final Texel pupil,
      final int width,
      final int height) {
    return headFaces(model, profile).stream()
        .filter(
            face -> QuadUv.covers(face, (pupil.x() + 0.5f) / width, (pupil.y() + 0.5f) / height))
        .findFirst();
  }

  private static Optional<Vector3f> pointOf(
      final CubeGeometry.Face face, final Texel pupil, final int width, final int height) {
    return QuadUv.pointAt(face, (pupil.x() + 0.5f) / width, (pupil.y() + 0.5f) / height)
        .map(face.pose()::point);
  }

  private static boolean nextTo(final Texel pupil, final Texel texel) {
    return Math.abs(pupil.x() - texel.x()) <= EYE_REACH
        && Math.abs(pupil.y() - texel.y()) <= EYE_REACH;
  }

  private static int alphaAt(final BufferedImage image, final Texel texel) {
    return image.getRGB(texel.x(), texel.y()) >>> 24;
  }

  private static int alphaAt(final BufferedImage image, final float[] uv) {
    final int x = Math.min(image.getWidth() - 1, (int) (uv[0] * image.getWidth()));
    final int y = Math.min(image.getHeight() - 1, (int) (uv[1] * image.getHeight()));
    return image.getRGB(x, y) >>> 24;
  }

  private record LidFace(Vector3f[] corners, float[] us, float[] vs) {

    static LidFace facing(final GeoBone lid, final GeoCube cube, final Vector3f outward) {
      final Matrix4f pose = ShippedRigs.liveMatrix(lid, cube);
      final Comparator<GeoQuad> facingOutward =
          Comparator.comparingDouble(
              quad ->
                  pose.transformDirection(new Vector3f(quad.normal())).normalize().dot(outward));
      return Arrays.stream(cube.quads())
          .filter(Objects::nonNull)
          .max(facingOutward.thenComparingDouble(quad -> of(quad, pose).reach(outward)))
          .map(quad -> of(quad, pose))
          .orElseThrow();
    }

    static LidFace posed(final CubeGeometry.Face face) {
      return new LidFace(
          Arrays.stream(face.corners()).map(face.pose()::point).toArray(Vector3f[]::new),
          face.us(),
          face.vs());
    }

    List<Texel> texels(final int width, final int height) {
      final QuadUv.Span u = QuadUv.spanOf(this.us);
      final QuadUv.Span v = QuadUv.spanOf(this.vs);
      final List<Texel> texels = new ArrayList<>();
      for (int y = Math.round(v.min() * height); y < Math.round(v.max() * height); y++) {
        for (int x = Math.round(u.min() * width); x < Math.round(u.max() * width); x++) {
          texels.add(new Texel(x, y));
        }
      }
      return texels;
    }

    Optional<Vector3f> pointAt(final float u, final float v) {
      final float du1 = this.us[1] - this.us[0];
      final float dv1 = this.vs[1] - this.vs[0];
      final float du3 = this.us[3] - this.us[0];
      final float dv3 = this.vs[3] - this.vs[0];
      final float determinant = du1 * dv3 - du3 * dv1;
      if (Math.abs(determinant) < 1.0e-9f) {
        return Optional.empty();
      }
      final float a = ((u - this.us[0]) * dv3 - du3 * (v - this.vs[0])) / determinant;
      final float b = (du1 * (v - this.vs[0]) - (u - this.us[0]) * dv1) / determinant;
      return Optional.of(
          new Vector3f(this.corners[0])
              .add(new Vector3f(this.corners[1]).sub(this.corners[0]).mul(a))
              .add(new Vector3f(this.corners[3]).sub(this.corners[0]).mul(b)));
    }

    private static LidFace of(final GeoQuad quad, final Matrix4f pose) {
      final GeoVertex[] vertices = quad.vertices();
      final Vector3f[] corners = new Vector3f[vertices.length];
      final float[] us = new float[vertices.length];
      final float[] vs = new float[vertices.length];
      for (int i = 0; i < vertices.length; i++) {
        corners[i] = pose.transformPosition(new Vector3f(vertices[i].position()));
        us[i] = vertices[i].texU();
        vs[i] = vertices[i].texV();
      }
      return new LidFace(corners, us, vs);
    }

    private double reach(final Vector3f outward) {
      return Arrays.stream(this.corners).mapToDouble(outward::dot).average().orElseThrow();
    }

    Optional<float[]> uvAlong(final Vector3f point, final Vector3f outward) {
      final Vector3f origin = this.corners[0];
      final Matrix3f edges =
          new Matrix3f(
              new Vector3f(this.corners[1]).sub(origin),
              new Vector3f(this.corners[3]).sub(origin),
              outward);
      if (Math.abs(edges.determinant()) < 1.0e-12f) {
        return Optional.empty();
      }
      final Vector3f weights = edges.invert().transform(new Vector3f(point).sub(origin));
      if (weights.x < 0 || weights.x > 1 || weights.y < 0 || weights.y > 1) {
        return Optional.empty();
      }
      return Optional.of(
          new float[] {
            this.us[0]
                + weights.x * (this.us[1] - this.us[0])
                + weights.y * (this.us[3] - this.us[0]),
            this.vs[0]
                + weights.x * (this.vs[1] - this.vs[0])
                + weights.y * (this.vs[3] - this.vs[0])
          });
    }
  }

  private static List<CubeGeometry.Face> headFaces(
      final BakedGeoModel model, final RigProfile profile) {
    return profile.bonesIn(Slot.HEAD).stream()
        .map(model::getBone)
        .flatMap(Optional::stream)
        .map(GeoBone.class::cast)
        .flatMap(bone -> CubeGeometry.facesOf(bone).stream())
        .toList();
  }

  private static InputStream resource(final String path) {
    final InputStream stream =
        UndeadDogEyesTest.class.getResourceAsStream("/assets/dogs-unleashed/" + path);
    if (stream == null) {
      throw new IllegalArgumentException("missing asset " + path);
    }
    return stream;
  }
}
