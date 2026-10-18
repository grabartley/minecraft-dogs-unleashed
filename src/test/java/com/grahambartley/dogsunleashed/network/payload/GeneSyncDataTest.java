package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.PacketLimits.GENES_MAX_SIZE;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.AllelePair;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneTrait;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.variant.BeagleCoat;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoat;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GeneSyncDataTest {

  private static final DogGeneLocus HUSKY_COAT =
      new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT);
  private static final DogGeneLocus HUSKY_EYES =
      new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.EYES);
  private static final DogGeneLocus BEAGLE_COAT =
      new DogGeneLocus(UnleashedDogBreed.BEAGLE, DogGeneTrait.COAT);
  private static final DogGenes CARRIER =
      new DogGenes(
          Map.of(
              HUSKY_COAT,
              new AllelePair(HuskyCoat.BLACK_WHITE.ordinal(), HuskyCoat.WHITE.ordinal()),
              HUSKY_EYES,
              AllelePair.matching(HuskyEyeColor.BLUE_BLUE.ordinal()),
              BEAGLE_COAT,
              AllelePair.matching(BeagleCoat.LILAC_1.ordinal())));

  @Test
  @DisplayName("a dog that has bred shows what it carries for its rig breed only")
  void revealedGenesListCarriedAlleles() {
    assertEquals(
        List.of(
            new GeneSyncData(
                HUSKY_COAT, HuskyCoat.BLACK_WHITE.ordinal(), List.of(HuskyCoat.WHITE.ordinal())),
            new GeneSyncData(HUSKY_EYES, HuskyEyeColor.BLUE_BLUE.ordinal(), List.of())),
        GeneSyncData.visibleGenes(CARRIER, UnleashedDogBreed.HUSKY, true));
  }

  @Test
  @DisplayName("a dog that has not bred shows only what can be seen")
  void hiddenGenesListNoCarriedAlleles() {
    final List<GeneSyncData> genes =
        GeneSyncData.visibleGenes(CARRIER, UnleashedDogBreed.HUSKY, false);
    assertEquals(HuskyCoat.BLACK_WHITE.ordinal(), genes.get(0).shown());
    assertTrue(genes.stream().noneMatch(GeneSyncData::carrier));
  }

  @Test
  @DisplayName("a breed with no genes lists none")
  void breedWithoutGenesListsNone() {
    assertTrue(
        GeneSyncData.visibleGenes(CARRIER, UnleashedDogBreed.GOLDEN_RETRIEVER, true).isEmpty());
  }

  @Test
  @DisplayName("gene lists round-trip through the buffer")
  void listRoundTrip() {
    final List<GeneSyncData> genes =
        GeneSyncData.visibleGenes(CARRIER, UnleashedDogBreed.HUSKY, true);
    final RegistryByteBuf buf = newBuf();
    GeneSyncData.writeList(buf, genes);
    assertEquals(genes, GeneSyncData.readList(buf));
    assertFalse(buf.isReadable());
  }

  @ParameterizedTest(name = "shown {0}")
  @ValueSource(ints = {-1, 6, 1000})
  @DisplayName("a gene with an allele out of range is dropped")
  void outOfRangeGeneIsDropped(final int shown) {
    final RegistryByteBuf buf = newBuf();
    buf.writeVarInt(1);
    buf.writeString(UnleashedDogBreed.HUSKY.serializedId());
    buf.writeVarInt(DogGeneTrait.COAT.ordinal());
    buf.writeVarInt(shown);
    buf.writeVarInt(0);
    assertTrue(GeneSyncData.readList(buf).isEmpty());
    assertFalse(buf.isReadable());
  }

  @Test
  @DisplayName("a gene for a trait the breed lacks is dropped")
  void traitTheBreedLacksIsDropped() {
    final RegistryByteBuf buf = newBuf();
    buf.writeVarInt(1);
    buf.writeString(UnleashedDogBreed.BEAGLE.serializedId());
    buf.writeVarInt(DogGeneTrait.EYES.ordinal());
    buf.writeVarInt(0);
    buf.writeVarInt(0);
    assertTrue(GeneSyncData.readList(buf).isEmpty());
  }

  @Test
  @DisplayName("lists longer than the packet limits are cut to them")
  void oversizedListsAreCut() {
    final GeneSyncData gene =
        new GeneSyncData(HUSKY_COAT, 0, List.of(1, 2, HuskyCoat.WHITE.ordinal()));
    final RegistryByteBuf buf = newBuf();
    GeneSyncData.writeList(buf, Collections.nCopies(GENES_MAX_SIZE + 1, gene));
    final List<GeneSyncData> read = GeneSyncData.readList(buf);
    assertEquals(GENES_MAX_SIZE, read.size());
    assertEquals(List.of(1, 2), read.get(0).carried());
    assertFalse(buf.isReadable());
  }
}
