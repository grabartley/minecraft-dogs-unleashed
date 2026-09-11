package com.grahambartley.dogsunleashed.compat.emi;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.compat.DogsUnleashedInfoEntries;
import com.grahambartley.dogsunleashed.compat.DogsUnleashedInfoEntries.InfoEntry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import net.minecraft.util.Identifier;

public final class DogsUnleashedEmiPlugin implements EmiPlugin {

  private static final String RECIPE_ID_PREFIX = "/info/";

  @Override
  public void register(final EmiRegistry registry) {
    for (final InfoEntry entry : DogsUnleashedInfoEntries.all()) {
      registry.addRecipe(
          new EmiInfoRecipe(
              List.of(EmiIngredient.of(entry.stacks().stream().map(EmiStack::of).toList())),
              entry.description(),
              Identifier.of(DogsUnleashed.MOD_ID, RECIPE_ID_PREFIX + entry.id())));
    }
  }
}
