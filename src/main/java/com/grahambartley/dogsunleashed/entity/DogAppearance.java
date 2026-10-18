package com.grahambartley.dogsunleashed.entity;

public record DogAppearance(
    UnleashedDogBreed rigSourceBreed, int coatVariantOrdinal, int eyeColorVariantOrdinal) {}
