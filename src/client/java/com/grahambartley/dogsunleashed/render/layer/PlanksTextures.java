package com.grahambartley.dogsunleashed.render.layer;

import com.google.common.base.Suppliers;
import com.grahambartley.dogsunleashed.wood.WoodPlanks;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.block.WoodType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class PlanksTextures {

  private static final Supplier<Map<WoodType, Identifier>> TEXTURES =
      Suppliers.memoize(
          () ->
              WoodPlanks.all().stream()
                  .collect(
                      Collectors.toUnmodifiableMap(
                          Function.identity(), PlanksTextures::textureFor)));

  private PlanksTextures() {}

  public static Identifier of(final WoodType wood) {
    return TEXTURES.get().getOrDefault(wood, TEXTURES.get().get(WoodPlanks.DEFAULT));
  }

  private static Identifier textureFor(final WoodType wood) {
    final Identifier planks = Registries.BLOCK.getId(WoodPlanks.planksOf(wood));
    return Identifier.of(planks.getNamespace(), "textures/block/" + planks.getPath() + ".png");
  }
}
