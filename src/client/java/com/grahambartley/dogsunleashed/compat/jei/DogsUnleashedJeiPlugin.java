package com.grahambartley.dogsunleashed.compat.jei;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.compat.DogsUnleashedInfoEntries;
import com.grahambartley.dogsunleashed.compat.DogsUnleashedInfoEntries.InfoEntry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IModInfoRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@JeiPlugin
public final class DogsUnleashedJeiPlugin implements IModPlugin {

  private static final Identifier PLUGIN_ID = Identifier.of(DogsUnleashed.MOD_ID, "jei_plugin");

  @Override
  public Identifier getPluginUid() {
    return PLUGIN_ID;
  }

  @Override
  public void registerRecipes(final IRecipeRegistration registration) {
    for (final InfoEntry entry : DogsUnleashedInfoEntries.all()) {
      registration.addIngredientInfo(
          entry.stacks(), VanillaTypes.ITEM_STACK, entry.description().toArray(Text[]::new));
    }
  }

  @Override
  public void registerModInfo(final IModInfoRegistration registration) {
    registration.addModAliases(DogsUnleashed.MOD_ID, "dogs", "dogsunleashed", "du");
  }
}
