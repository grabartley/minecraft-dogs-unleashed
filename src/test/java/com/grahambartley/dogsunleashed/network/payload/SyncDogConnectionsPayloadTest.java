package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.samplePet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SyncDogConnectionsPayloadTest {

  @Test
  @DisplayName("codec round-trips the focus composition")
  void codecRoundTripsComposition() {
    final ConnectionDogSyncData self =
        new ConnectionDogSyncData(
            samplePet(UnleashedDogBreed.HUSKY, "Luna"), UUID.randomUUID().toString(), "Alex");
    final ConnectionDogSyncData parent =
        new ConnectionDogSyncData(
            samplePet(UnleashedDogBreed.BEAGLE, "Max"), UUID.randomUUID().toString(), "");
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
    final RegistryByteBuf buf = newBuf();
    SyncDogConnectionsPayload.CODEC.encode(buf, payload);
    assertEquals(payload, SyncDogConnectionsPayload.CODEC.decode(buf));
  }
}
