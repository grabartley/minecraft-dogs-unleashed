package com.grahambartley.dogsunleashed.render.cross;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import java.util.List;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class CoatPalettes {

  public record PaletteWeight(Identifier palette, int percent) {}

  private static final float PERCENT = 100.0f;

  private CoatPalettes() {}

  public static Identifier idOf(
      final UnleashedDogBreed breed, final @Nullable UnleashedDogCoat coat) {
    final String name =
        coat == null ? breed.serializedId() : breed.serializedId() + "_" + coat.getTexturePrefix();
    return Identifier.of(MOD_ID, "textures/entity/palette/" + name + ".png");
  }

  public static List<PaletteWeight> pullsOf(final CrossBreedLook look) {
    return look.influences().stream()
        .map(
            influence -> {
              final List<UnleashedDogCoat> coats = DogCoats.coatsOf(influence.breed());
              final UnleashedDogCoat coat =
                  coats.isEmpty()
                      ? null
                      : coats.get(look.coatIndexFor(influence.breed(), coats.size()));
              return new PaletteWeight(
                  idOf(influence.breed(), coat), Math.round(influence.weight() * PERCENT));
            })
        .filter(weight -> weight.percent() > 0)
        .toList();
  }
}
