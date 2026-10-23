package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.DogStatue;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogPortraitTest {

  private static final DynamicRegistryManager REGISTRIES =
      DynamicRegistryManager.of(Registries.REGISTRIES);

  private static final DogStatueLook BEAGLE =
      new DogStatueLook(UnleashedDogBreed.BEAGLE, false, 1, -1, 0, 0, null, null);

  private static final DogGenome MOSTLY_SHIBA =
      new DogGenome(
          List.of(
              new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.75f),
              new BreedShare(UnleashedDogBreed.HUSKY, 0.25f)),
          20.0,
          0.3,
          4.0,
          DogGenome.BASELINE_FUR_GROWTH_RATE,
          UnleashedDogBreed.SHIBA_INU);

  static Stream<Arguments> portraits() {
    return Stream.of(
        Arguments.of(new DogPortrait(BEAGLE, "Bentley", PortraitSize.SMALL, DogStatuePose.SIT)),
        Arguments.of(
            new DogPortrait(
                new DogStatueLook(UnleashedDogBreed.HUSKY, true, 2, 1, 3, 0, null, null),
                "",
                PortraitSize.WIDE,
                DogStatuePose.HOWL)),
        Arguments.of(
            new DogPortrait(
                new DogStatueLook(
                    UnleashedDogBreed.CROSS_BREED, false, -1, -1, 1, 0, MOSTLY_SHIBA, null),
                "Mochi",
                PortraitSize.LARGE,
                DogStatuePose.STAND)),
        Arguments.of(
            new DogPortrait(
                BEAGLE,
                "Pumpkin",
                PortraitSize.WIDE,
                DogStatuePose.STAND,
                new DogPortraitOutfit(
                    Map.of(
                        DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR),
                        DogEquipmentSlot.SADDLEBAGS, new ItemStack(ModItems.SADDLEBAGS)),
                    Optional.of(DyeColor.PINK),
                    new ItemStack(ModItems.FRISBEE),
                    Optional.empty(),
                    true,
                    true,
                    false))));
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("portraits")
  @DisplayName("a portrait survives NBT, so a hung portrait keeps its dog across a reload")
  void nbtRoundTrips(final DogPortrait portrait) {
    assertEquals(
        portrait, DogPortrait.fromNbt(REGISTRIES, portrait.toNbt(REGISTRIES)).orElseThrow());
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("portraits")
  @DisplayName("a portrait survives the network codec, so every player sees the same dog")
  void packetRoundTrips(final DogPortrait portrait) {
    final RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), REGISTRIES);
    DogPortrait.PACKET_CODEC.encode(buffer, portrait);
    assertEquals(portrait, DogPortrait.PACKET_CODEC.decode(buffer));
  }

  @Test
  @DisplayName("NBT with no look reads as no portrait rather than a wrong dog")
  void missingLookIsNoPortrait() {
    assertTrue(DogPortrait.fromNbt(REGISTRIES, new NbtCompound()).isEmpty());
  }

  @ParameterizedTest(name = "\"{0}\" is titled \"{1}\"")
  @CsvSource({
    "'', ''",
    "Bentley, Bentley",
    "'Sir Barksalot the Third of the Long Name Hills, Esq', 'Sir Barksalot the Third of the Long Name Hills, Es'"
  })
  @DisplayName("a painted portrait keeps the dog's name, cut like a statue's")
  void paintedKeepsTheName(final String name, final String title) {
    final DogPortrait portrait =
        DogPortrait.painted(BEAGLE, name, PortraitSize.WIDE, DogPortraitOutfit.NONE);
    assertEquals(title, portrait.name());
    assertEquals(DogStatue.cut(name), portrait.name());
    assertEquals(PortraitSize.WIDE, portrait.size());
    assertEquals(DogStatuePose.SIT, portrait.pose());
  }

  @ParameterizedTest(name = "a {0} portrait at {1} is next {2}")
  @CsvSource({
    "BEAGLE, SIT, STAND",
    "BEAGLE, STAND, SIT",
    "HUSKY, SIT, STAND",
    "HUSKY, STAND, HOWL",
    "HUSKY, HOWL, SIT"
  })
  @DisplayName(
      "a portrait steps through the statue poses, howling only for breeds that howl, and keeps its dog")
  void nextPoseCycles(
      final UnleashedDogBreed breed, final DogStatuePose pose, final DogStatuePose next) {
    final DogPortrait portrait =
        new DogPortrait(
            new DogStatueLook(breed, false, 1, -1, 0, 0, null, null),
            "Rex",
            PortraitSize.WIDE,
            pose);
    final DogPortrait stepped = portrait.nextPose();
    assertEquals(next, stepped.pose());
    assertEquals(portrait.look(), stepped.look());
    assertEquals(portrait.name(), stepped.name());
    assertEquals(portrait.size(), stepped.size());
  }

  @ParameterizedTest(name = "a {0} portrait saved {1} shows {2}")
  @CsvSource({"BEAGLE, HOWL, SIT", "HUSKY, HOWL, HOWL", "BEAGLE, STAND, STAND"})
  @DisplayName("a howl pose on a breed that cannot howl is painted sitting")
  void shownPoseFallsBackToSitting(
      final UnleashedDogBreed breed, final DogStatuePose pose, final DogStatuePose shown) {
    assertEquals(
        shown,
        new DogPortrait(
                new DogStatueLook(breed, false, 1, -1, 0, 0, null, null),
                "",
                PortraitSize.SMALL,
                pose)
            .shownPose());
  }

  @ParameterizedTest(name = "a {0} portrait saved before poses existed")
  @EnumSource(PortraitSize.class)
  @DisplayName("a portrait saved before poses existed loads sitting, as it was painted")
  void portraitWithoutAPoseLoadsSitting(final PortraitSize size) {
    final NbtCompound saved =
        new DogPortrait(BEAGLE, "Bentley", size, DogStatuePose.STAND).toNbt(REGISTRIES);
    saved.remove("pose");
    assertEquals(
        new DogPortrait(BEAGLE, "Bentley", size, DogStatuePose.SIT),
        DogPortrait.fromNbt(REGISTRIES, saved).orElseThrow());
  }

  @Test
  @DisplayName("a cross breed is painted in front of its largest share's home biome")
  void crossBreedUsesItsRigBreedsBackground() {
    assertEquals(
        PortraitBackground.FOREST,
        new DogPortrait(BEAGLE, "", PortraitSize.SMALL, DogStatuePose.SIT).background());
    assertEquals(
        PortraitBackground.CHERRY_GROVE,
        new DogPortrait(
                new DogStatueLook(
                    UnleashedDogBreed.CROSS_BREED, false, -1, -1, 0, 0, MOSTLY_SHIBA, null),
                "",
                PortraitSize.SMALL,
                DogStatuePose.SIT)
            .background());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogStatuePose.class)
  @DisplayName("changing a portrait's pose keeps its dog, name and size")
  void withPoseKeepsTheRest(final DogStatuePose pose) {
    final DogPortrait portrait =
        new DogPortrait(BEAGLE, "Bentley", PortraitSize.LARGE, DogStatuePose.SIT);
    assertEquals(
        new DogPortrait(BEAGLE, "Bentley", PortraitSize.LARGE, pose), portrait.withPose(pose));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogStatuePose.class)
  @DisplayName("posing a portrait keeps the outfit the dog was painted in")
  void poseKeepsTheOutfit(final DogStatuePose pose) {
    final DogPortraitOutfit outfit =
        new DogPortraitOutfit(
            Map.of(DogEquipmentSlot.SADDLEBAGS, new ItemStack(ModItems.SADDLEBAGS)),
            Optional.of(DyeColor.BLUE),
            ItemStack.EMPTY,
            Optional.empty(),
            true,
            false,
            false);
    final DogPortrait portrait = DogPortrait.painted(BEAGLE, "Bentley", PortraitSize.SMALL, outfit);
    assertEquals(outfit, portrait.withPose(pose).outfit());
    assertEquals(outfit, portrait.nextPose().outfit());
  }

  @ParameterizedTest(name = "an outfit saved as {0}")
  @ValueSource(strings = {"broken", "42"})
  @DisplayName("a portrait with an outfit it cannot read still loads its dog, undressed")
  void unreadableOutfitKeepsTheDog(final String garbage) {
    final NbtCompound saved =
        new DogPortrait(BEAGLE, "Bentley", PortraitSize.SMALL, DogStatuePose.STAND)
            .toNbt(REGISTRIES);
    saved.putString("outfit", garbage);
    final DogPortrait loaded = DogPortrait.fromNbt(REGISTRIES, saved).orElseThrow();
    assertEquals(BEAGLE, loaded.look());
    assertEquals(DogPortraitOutfit.NONE, loaded.outfit());
  }
}
