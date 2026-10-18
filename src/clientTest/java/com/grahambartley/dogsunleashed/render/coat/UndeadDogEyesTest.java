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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class UndeadDogEyesTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  private static final Map<UnleashedDogBreed, List<String>> TEXTURES_BY_RIG =
      Map.of(
          UnleashedDogBreed.BEAGLE, List.of("beagle_blackwhite", "beagle_tri1"),
          UnleashedDogBreed.GOLDEN_RETRIEVER, List.of("goldenretriever"),
          UnleashedDogBreed.HUSKY,
              List.of("husky_blackwhite_blueblue", "husky_graywhite_hazelhazel"),
          UnleashedDogBreed.DACHSHUND, List.of("dachshund_blacktan", "dachshund_red"),
          UnleashedDogBreed.SHIBA_INU, List.of("shibainu_red", "shibainu_sesame"));

  private static final int PUPIL_MAX_LUMA = 90;

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
      final Optional<Vector3f> onHead =
          headFaces(model, profile).stream()
              .filter(
                  face ->
                      QuadUv.covers(face, (pupil.x() + 0.5f) / width, (pupil.y() + 0.5f) / height))
              .findFirst()
              .flatMap(
                  face ->
                      QuadUv.pointAt(face, (pupil.x() + 0.5f) / width, (pupil.y() + 0.5f) / height)
                          .map(face.pose()::point));
      assertTrue(onHead.isPresent(), rig + " pupil " + pupil + " is not on the head");
      assertTrue(
          eyes.containsXy(onHead.get().x, onHead.get().y),
          rig + " pupil " + pupil + " at " + onHead.get() + " is not under the closed eyelids");
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
