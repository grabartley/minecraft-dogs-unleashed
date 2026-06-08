package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnleashedDogEntityTest {
  private static final Path UNLEASHED_DOG_ENTITY_SOURCE =
      Path.of("src/main/java/com/grahambartley/entity/UnleashedDogEntity.java");

  @Test
  @DisplayName("unleashed dog entity should track the active fetch type for client sync")
  void unleashedDogEntityShouldTrackActiveFetchType() throws IOException {
    String source = Files.readString(UNLEASHED_DOG_ENTITY_SOURCE);

    assertTrue(source.contains("private static final TrackedData<String> ACTIVE_FETCH_TYPE_ID"));
    assertTrue(source.contains("builder.add(ACTIVE_FETCH_TYPE_ID, NO_ACTIVE_FETCH_TYPE);"));
    assertTrue(source.contains("FetchTypes.forId(Identifier.of(activeFetchTypeId))"));
    assertTrue(source.contains("activeFetchType.id().toString()"));
  }

  @Test
  @DisplayName(
      "unleashed dog entity should set and clear active fetch type through the shared setter")
  void unleashedDogEntityShouldUseSharedFetchTypeSetter() throws IOException {
    String source = Files.readString(UNLEASHED_DOG_ENTITY_SOURCE);

    assertTrue(source.contains("this.setActiveFetchType(fetchItemType);"));
    assertTrue(source.contains("this.setActiveFetchType(null);"));
  }
}
