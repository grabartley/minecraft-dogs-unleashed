package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.goal.PackGuardGoal;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class PackGuardGoalGameTest implements FabricGameTest {

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void everySwordAxeAndConventionWeaponCountsAsAWeapon(final TestContext context) {
    final List<TagKey<Item>> weaponTags =
        List.of(
            ItemTags.SWORDS,
            ItemTags.AXES,
            ConventionalItemTags.MELEE_WEAPON_TOOLS,
            ConventionalItemTags.RANGED_WEAPON_TOOLS);
    for (final TagKey<Item> tag : weaponTags) {
      for (final RegistryEntry<Item> entry : Registries.ITEM.iterateEntries(tag)) {
        context.assertTrue(
            PackGuardGoal.isWeapon(new ItemStack(entry)),
            entry.getIdAsString() + " in " + tag.id() + " should count as a weapon");
      }
    }
    for (final Item item : List.of(Items.TRIDENT, Items.BOW, Items.CROSSBOW, Items.MACE)) {
      context.assertTrue(
          PackGuardGoal.isWeapon(new ItemStack(item)), item + " should count as a weapon");
    }
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void foodAndToolsAreNotWeapons(final TestContext context) {
    for (final Item item : List.of(Items.BEEF, Items.STICK, Items.IRON_PICKAXE, Items.SHEARS)) {
      context.assertFalse(
          PackGuardGoal.isWeapon(new ItemStack(item)), item + " should not count as a weapon");
    }
    context.complete();
  }
}
