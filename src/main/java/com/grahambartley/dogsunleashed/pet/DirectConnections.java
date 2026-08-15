package com.grahambartley.dogsunleashed.pet;

import java.util.List;

/**
 * One dog's immediate family: parents, mates (co-parents of at least one shared child), siblings
 * (dogs sharing at least one parent), and children. Lists are deterministic (registration order)
 * and never contain the focus dog itself.
 */
public record DirectConnections(
    PetData self,
    List<PetData> parents,
    List<PetData> mates,
    List<PetData> siblings,
    List<PetData> children) {}
