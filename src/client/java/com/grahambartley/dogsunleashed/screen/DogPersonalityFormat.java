package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.personality.DogTrait;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.text.Text;

final class DogPersonalityFormat {

  static final String LABEL_KEY = "screen.dogs-unleashed.pet_details.personality";

  private DogPersonalityFormat() {}

  static String format(final DogTraits traits, final Function<DogTrait, String> traitName) {
    return traits.traits().stream().map(traitName).collect(Collectors.joining(", "));
  }

  static String format(final DogTraits traits) {
    return format(traits, trait -> Text.translatable(trait.translationKey()).getString());
  }
}
