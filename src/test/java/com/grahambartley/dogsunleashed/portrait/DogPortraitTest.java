package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.block.DogStatue;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogPortraitTest {

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
        Arguments.of(new DogPortrait(BEAGLE, "Bentley", PortraitSize.SMALL)),
        Arguments.of(
            new DogPortrait(
                new DogStatueLook(UnleashedDogBreed.HUSKY, true, 2, 1, 3, 0, null, null),
                "",
                PortraitSize.WIDE)),
        Arguments.of(
            new DogPortrait(
                new DogStatueLook(
                    UnleashedDogBreed.CROSS_BREED, false, -1, -1, 1, 0, MOSTLY_SHIBA, null),
                "Mochi",
                PortraitSize.LARGE)));
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("portraits")
  @DisplayName("a portrait survives NBT, so a hung portrait keeps its dog across a reload")
  void nbtRoundTrips(final DogPortrait portrait) {
    assertEquals(portrait, DogPortrait.fromNbt(portrait.toNbt()).orElseThrow());
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("portraits")
  @DisplayName("a portrait survives the network codec, so every player sees the same dog")
  void packetRoundTrips(final DogPortrait portrait) {
    final PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
    DogPortrait.PACKET_CODEC.encode(buffer, portrait);
    assertEquals(portrait, DogPortrait.PACKET_CODEC.decode(buffer));
  }

  @Test
  @DisplayName("NBT with no look reads as no portrait rather than a wrong dog")
  void missingLookIsNoPortrait() {
    assertTrue(DogPortrait.fromNbt(new NbtCompound()).isEmpty());
  }

  @ParameterizedTest(name = "\"{0}\" is titled \"{1}\"")
  @CsvSource({
    "'', ''",
    "Bentley, Bentley",
    "'Sir Barksalot the Third of the Long Name Hills, Esq', 'Sir Barksalot the Third of the Long Name Hills, Es'"
  })
  @DisplayName("a painted portrait keeps the dog's name, cut like a statue's")
  void paintedKeepsTheName(final String name, final String title) {
    final DogPortrait portrait = DogPortrait.painted(BEAGLE, name, PortraitSize.WIDE);
    assertEquals(title, portrait.name());
    assertEquals(DogStatue.cut(name), portrait.name());
    assertEquals(PortraitSize.WIDE, portrait.size());
  }

  @Test
  @DisplayName("a cross breed is painted in front of its largest share's home biome")
  void crossBreedUsesItsRigBreedsBackground() {
    assertEquals(
        PortraitBackground.FOREST, new DogPortrait(BEAGLE, "", PortraitSize.SMALL).background());
    assertEquals(
        PortraitBackground.CHERRY_GROVE,
        new DogPortrait(
                new DogStatueLook(
                    UnleashedDogBreed.CROSS_BREED, false, -1, -1, 0, 0, MOSTLY_SHIBA, null),
                "",
                PortraitSize.SMALL)
            .background());
  }
}
