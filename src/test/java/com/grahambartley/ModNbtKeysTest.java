package com.grahambartley;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModNbtKeysTest {

  @Test
  @DisplayName("Pet persistence keys should stay stable for save compatibility")
  void testPetPersistenceKeys() {
    assertEquals("PetId", ModNbtKeys.PET_ID);
    assertEquals("OwnerId", ModNbtKeys.OWNER_ID);
    assertEquals("BreedType", ModNbtKeys.BREED_TYPE);
    assertEquals("Dimension", ModNbtKeys.DIMENSION);
    assertEquals("Alive", ModNbtKeys.ALIVE);
    assertEquals("PortraitBaby", ModNbtKeys.PORTRAIT_BABY);
    assertEquals("PortraitCollar", ModNbtKeys.PORTRAIT_COLLAR);
    assertEquals("portraitCoatVariant", ModNbtKeys.PORTRAIT_COAT_VARIANT);
    assertEquals("PortraitHuskyEye", ModNbtKeys.PORTRAIT_HUSKY_EYE);
  }

  @Test
  @DisplayName("Dog entity keys should stay stable for NBT compatibility")
  void testDogEntityKeys() {
    assertEquals("CoatVariant", ModNbtKeys.COAT_VARIANT);
    assertEquals("EyeColorVariant", ModNbtKeys.EYE_COLOR_VARIANT);
    assertEquals("CollarColor", ModNbtKeys.COLLAR_COLOR);
    assertEquals("SleepingInBed", ModNbtKeys.SLEEPING_IN_BED);
    assertEquals("BedPosX", ModNbtKeys.BED_POS_X);
    assertEquals("BedPosY", ModNbtKeys.BED_POS_Y);
    assertEquals("BedPosZ", ModNbtKeys.BED_POS_Z);
    assertEquals("CarryingBall", ModNbtKeys.CARRYING_BALL);
  }
}
