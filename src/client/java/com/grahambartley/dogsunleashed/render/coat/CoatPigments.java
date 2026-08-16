package com.grahambartley.dogsunleashed.render.coat;

import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Blends pigment colours across a dog's ancestry. Continuous colour is the one part of a coat that
 * genuinely averages: pattern masks are inherited whole and face features are stamped untouched.
 */
public final class CoatPigments {

  /**
   * One ancestor's contribution: the coat it donates pigment from, and how much of the dog it is.
   */
  public record Donor(CoatRecipe recipe, float share) {}

  private CoatPigments() {}

  /**
   * Returns the pigment per slot of {@code layout}, shifted toward each donor by its share. A dog
   * with a single ancestor comes back with its own pigments untouched, so pure breeds render
   * exactly as painted.
   */
  public static Map<String, Integer> blend(final CoatRecipe layout, final List<Donor> donors) {
    final List<Donor> weighted = normalised(donors);
    final Map<String, Integer> blended = new LinkedHashMap<>();
    if (weighted.isEmpty()) {
      for (final String slot : layout.slots()) {
        blended.put(slot, layout.pigment(slot));
      }
      return blended;
    }

    for (int index = 0; index < layout.slots().size(); index++) {
      final String slot = layout.slots().get(index);
      float red = 0;
      float green = 0;
      float blue = 0;
      for (final Donor donor : weighted) {
        final int colour = donor.recipe().pigmentAt(index);
        red += ((colour >> 16) & 0xFF) * donor.share();
        green += ((colour >> 8) & 0xFF) * donor.share();
        blue += (colour & 0xFF) * donor.share();
      }
      blended.put(slot, (round(red) << 16) | (round(green) << 8) | round(blue));
    }
    return blended;
  }

  public static List<BreedShare> normalisedShares(final List<BreedShare> composition) {
    float total = 0;
    for (final BreedShare share : composition) {
      if (share.share() > 0) {
        total += share.share();
      }
    }
    if (total <= 0) {
      return List.of();
    }
    final List<BreedShare> normalised = new ArrayList<>(composition.size());
    for (final BreedShare share : composition) {
      if (share.share() > 0) {
        normalised.add(new BreedShare(share.breed(), share.share() / total));
      }
    }
    return normalised;
  }

  private static List<Donor> normalised(final List<Donor> donors) {
    float total = 0;
    for (final Donor donor : donors) {
      if (donor.share() > 0) {
        total += donor.share();
      }
    }
    if (total <= 0) {
      return List.of();
    }
    final List<Donor> normalised = new ArrayList<>(donors.size());
    for (final Donor donor : donors) {
      if (donor.share() > 0) {
        normalised.add(new Donor(donor.recipe(), donor.share() / total));
      }
    }
    return normalised;
  }

  private static int round(final float value) {
    return Math.clamp(Math.round(value), 0, 255);
  }
}
