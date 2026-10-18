package com.grahambartley.dogsunleashed.entity.genome;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.ArrayList;
import java.util.List;

public record DogGeneLocus(UnleashedDogBreed breed, DogGeneTrait trait) {

  public static List<DogGeneLocus> lociOf(final UnleashedDogBreed breed) {
    final List<DogGeneLocus> loci = new ArrayList<>();
    for (final DogGeneTrait trait : DogGeneTrait.values()) {
      if (trait.appliesTo(breed)) {
        loci.add(new DogGeneLocus(breed, trait));
      }
    }
    return List.copyOf(loci);
  }

  public GeneDominance dominance() {
    return this.trait.dominance(this.breed);
  }

  public String alleleTranslationKey(final int allele) {
    return this.trait.alleleTranslationKey(this.breed, allele);
  }
}
