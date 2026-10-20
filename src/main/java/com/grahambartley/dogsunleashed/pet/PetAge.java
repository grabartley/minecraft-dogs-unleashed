package com.grahambartley.dogsunleashed.pet;

public enum PetAge {
  PUPPY("screen.dogs-unleashed.pet_details.age_baby"),
  ADULT("screen.dogs-unleashed.pet_details.age_adult"),
  SENIOR("screen.dogs-unleashed.pet_details.age_senior");

  private final String translationKey;

  PetAge(final String translationKey) {
    this.translationKey = translationKey;
  }

  public static PetAge of(final boolean baby, final boolean senior) {
    if (baby) {
      return PUPPY;
    }
    return senior ? SENIOR : ADULT;
  }

  public String translationKey() {
    return this.translationKey;
  }
}
