package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newRegistryBuf;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.samplePet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneTrait;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinecraftBootstrapExtension.class)
class SyncDogConnectionsPayloadTest {

  @Test
  @DisplayName("codec round-trips the focus composition and genes")
  void codecRoundTripsComposition() {
    final ConnectionDogSyncData self =
        new ConnectionDogSyncData(
            samplePet(UnleashedDogBreed.HUSKY, "Luna"),
            UUID.randomUUID().toString(),
            "Alex",
            List.of(
                new GeneSyncData(
                    new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT), 0, List.of(5))),
            true);
    final ConnectionDogSyncData parent =
        new ConnectionDogSyncData(
            samplePet(UnleashedDogBreed.BEAGLE, "Max"),
            UUID.randomUUID().toString(),
            "",
            List.of(),
            false);
    final SyncDogConnectionsPayload payload =
        new SyncDogConnectionsPayload(
            self,
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)),
            List.of(parent),
            List.of(),
            List.of(),
            List.of(),
            false);
    final RegistryByteBuf buf = newRegistryBuf();
    SyncDogConnectionsPayload.CODEC.encode(buf, payload);
    assertEquals(payload, SyncDogConnectionsPayload.CODEC.decode(buf));
  }
}
