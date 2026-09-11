package com.grahambartley.dogsunleashed.pet;

import java.util.List;

public record DirectConnections(
    PetData self,
    List<PetData> parents,
    List<PetData> mates,
    List<PetData> siblings,
    List<PetData> children) {}
