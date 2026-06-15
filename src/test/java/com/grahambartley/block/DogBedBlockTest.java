package com.grahambartley.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.MinecraftBootstrapExtension;
import com.grahambartley.ModBlocks;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogBedBlockTest {

  @Test
  @DisplayName("dog bed hardness matches oak wood (2.0) so it breaks in a comparable time")
  void hardnessMatchesOakWood() {
    assertEquals(2.0f, ModBlocks.DOG_BED.getHardness(), 0.0f);
  }

  @Test
  @DisplayName("pendingAssignment maps the player UUID to the dog UUID and clears on first consume")
  void pendingAssignmentRoundTrip() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID dogUuid = UUID.randomUUID();

    DogBedBlock.setPendingAssignment(playerUuid, dogUuid);

    assertEquals(dogUuid, DogBedBlock.consumePendingAssignment(playerUuid));
    assertNull(
        DogBedBlock.consumePendingAssignment(playerUuid),
        "Second consume should return null because the assignment is single-use");
  }
}
