package com.grahambartley.network;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PacketLimitsTest {

  @Test
  @DisplayName("SetPetName name max length should be 32 characters")
  void testSetPetNameNameMaxLength() {
    assertEquals(32, PacketLimits.SET_PET_NAME_NAME_MAX_LENGTH);
  }

  @Test
  @DisplayName("RequestPets search query max length should be 64 characters")
  void testRequestPetsSearchQueryMaxLength() {
    assertEquals(64, PacketLimits.REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH);
  }
}
