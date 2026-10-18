package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.GeneSyncData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class FamilyTreeGeneLines {

  static final int HEADER_COLOR = 0xFFFFFF;
  static final int GENE_COLOR = 0xAAAAAA;
  static final int CARRIER_COLOR = 0xE0A040;
  static final int HINT_COLOR = 0x777777;
  static final String GENES_KEY = "screen.dogs-unleashed.family_tree.genes";
  static final String GENE_KEY = "screen.dogs-unleashed.family_tree.gene";
  static final String CARRIES_KEY = "screen.dogs-unleashed.family_tree.carries";
  static final String HIDDEN_KEY = "screen.dogs-unleashed.family_tree.carried_hidden";

  private FamilyTreeGeneLines() {}

  static List<GeneLine> of(final ConnectionDogSyncData dog, final Translator translator) {
    if (dog.genes().isEmpty()) {
      return List.of();
    }
    final List<GeneLine> lines = new ArrayList<>();
    lines.add(new GeneLine(translator.translate(GENES_KEY), HEADER_COLOR));
    for (final GeneSyncData gene : dog.genes()) {
      final DogGeneLocus locus = gene.locus();
      lines.add(
          new GeneLine(
              translator.translate(
                  GENE_KEY,
                  translator.translate(locus.trait().translationKey()),
                  translator.translate(locus.alleleTranslationKey(gene.shown()))),
              GENE_COLOR));
      if (gene.carrier()) {
        final String carried =
            gene.carried().stream()
                .map(allele -> translator.translate(locus.alleleTranslationKey(allele)))
                .collect(Collectors.joining(", "));
        lines.add(new GeneLine(translator.translate(CARRIES_KEY, carried), CARRIER_COLOR));
      }
    }
    if (!dog.carriedGenesRevealed()) {
      lines.add(new GeneLine(translator.translate(HIDDEN_KEY), HINT_COLOR));
    }
    return lines;
  }

  static boolean isCarrier(final ConnectionDogSyncData dog) {
    return dog.genes().stream().anyMatch(GeneSyncData::carrier);
  }

  record GeneLine(String text, int color) {}

  interface Translator {
    String translate(String key, Object... args);
  }
}
