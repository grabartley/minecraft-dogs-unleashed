package com.grahambartley.dogsunleashed.compat;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DogFoods;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public final class DogsUnleashedInfoEntries {

  public record InfoEntry(String id, List<ItemStack> stacks, List<Text> description) {}

  private record Definition(
      String id, Supplier<List<ItemStack>> stacks, Supplier<List<Text>> description) {}

  private static final String SPAWN_EGG_ID_SUFFIX = "_spawn_egg";

  private DogsUnleashedInfoEntries() {}

  public static String descriptionKey(final String id) {
    return "info." + DogsUnleashed.MOD_ID + "." + id;
  }

  public static List<String> ids() {
    return definitions().stream().map(Definition::id).toList();
  }

  public static List<InfoEntry> all() {
    return definitions().stream()
        .map(
            definition ->
                new InfoEntry(
                    definition.id(), definition.stacks().get(), definition.description().get()))
        .toList();
  }

  private static List<Definition> definitions() {
    final List<Definition> definitions = new ArrayList<>();
    definitions.add(item("tennis_ball", () -> ModItems.TENNIS_BALL));
    definitions.add(item("stick", () -> Items.STICK));
    definitions.add(dyed("frisbee", () -> ModItems.FRISBEE, () -> ModComponents.FRISBEE_COLOR));
    definitions.add(dyed("dog_bed", () -> ModItems.DOG_BED, () -> ModComponents.DOG_BED_COLOR));
    definitions.add(item("dog_house", () -> ModItems.DOG_HOUSE));
    definitions.add(item("dog_grave", () -> ModItems.DOG_GRAVE));
    definitions.add(item("dog_treat", () -> ModItems.DOG_TREAT));
    definitions.add(item("dog_whistle", () -> ModItems.DOG_WHISTLE));
    definitions.add(text("taming_food", () -> matchingStacks(DogFoods.tamingIngredient())));
    definitions.add(text("breeding_food", () -> matchingStacks(DogFoods.breedingIngredient())));
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      if (breed.isNaturallySpawning()) {
        definitions.add(spawnEgg(breed));
      }
    }
    return List.copyOf(definitions);
  }

  private static Definition spawnEgg(final UnleashedDogBreed breed) {
    final String id = breed.serializedId() + SPAWN_EGG_ID_SUFFIX;
    return new Definition(
        id,
        () -> List.of(new ItemStack(ModItems.getSpawnEgg(breed))),
        () ->
            Stream.concat(
                    Stream.of(Text.translatable(descriptionKey(id))),
                    DogBreedInfoText.lines(breed).stream())
                .toList());
  }

  private static Definition item(final String id, final Supplier<Item> item) {
    return text(id, () -> List.of(new ItemStack(item.get())));
  }

  private static Definition dyed(
      final String id, final Supplier<Item> item, final Supplier<ComponentType<DyeColor>> color) {
    return text(
        id,
        () ->
            Arrays.stream(DyeColor.values())
                .map(
                    dye -> {
                      final ItemStack stack = new ItemStack(item.get());
                      stack.set(color.get(), dye);
                      return stack;
                    })
                .toList());
  }

  private static Definition text(final String id, final Supplier<List<ItemStack>> stacks) {
    return new Definition(id, stacks, () -> List.of(Text.translatable(descriptionKey(id))));
  }

  private static List<ItemStack> matchingStacks(final Ingredient ingredient) {
    return Arrays.stream(ingredient.getMatchingStacks()).toList();
  }
}
