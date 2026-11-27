package com.grahambartley.dogsunleashed.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogsUnleashedInfoEntriesTest {

  private static final List<String> MECHANIC_IDS =
      List.of(
          "tennis_ball",
          "stick",
          "frisbee",
          "dog_bed",
          "dog_grave",
          "dog_statue",
          "dog_treat",
          "rope_toy",
          "taming_food",
          "breeding_food");

  static Stream<String> ids() {
    return DogsUnleashedInfoEntries.ids().stream();
  }

  static Stream<Arguments> breeds() {
    return Stream.of(UnleashedDogBreed.values()).map(Arguments::of);
  }

  @ParameterizedTest(name = "{0} resolves to a translated description")
  @MethodSource("ids")
  @DisplayName(
      "every registered info page has an en_us description, so no raw key reaches a viewer")
  void everyInfoPageHasATranslation(final String id) {
    final String key = DogsUnleashedInfoEntries.descriptionKey(id);
    assertTrue(LangKeys.contains(key), key + " is missing from en_us.json");
    assertFalse(LangKeys.get(key).isBlank(), key + " must not be blank");
  }

  @Test
  @DisplayName("info page ids are unique, so no viewer registers two pages for the same id")
  void infoPageIdsAreUnique() {
    final List<String> ids = DogsUnleashedInfoEntries.ids();
    final Set<String> unique = new HashSet<>(ids);
    assertEquals(ids.size(), unique.size(), "duplicate ids in " + ids);
  }

  @Test
  @DisplayName("every mod mechanic that has its own item gets an info page")
  void mechanicsWithItemsHaveInfoPages() {
    assertTrue(
        DogsUnleashedInfoEntries.ids().containsAll(MECHANIC_IDS),
        "expected " + MECHANIC_IDS + " within " + DogsUnleashedInfoEntries.ids());
  }

  @ParameterizedTest(name = "{0} spawn egg page tracks isNaturallySpawning")
  @MethodSource("breeds")
  @DisplayName("a breed gets a spawn egg info page exactly when it has a spawn egg to hover")
  void spawnEggPagesMatchBreedsWithSpawnEggs(final UnleashedDogBreed breed) {
    final String id = breed.serializedId() + "_spawn_egg";
    assertEquals(
        breed.isNaturallySpawning(),
        DogsUnleashedInfoEntries.ids().contains(id),
        id + " page presence must match whether the breed has a spawn egg");
  }

  @Test
  @DisplayName("description keys are namespaced under info.dogs-unleashed so they cannot collide")
  void descriptionKeysAreNamespaced() {
    assertEquals(
        "info.dogs-unleashed.tennis_ball", DogsUnleashedInfoEntries.descriptionKey("tennis_ball"));
  }

  static Stream<Arguments> woodenItems() {
    return Stream.of(
        Arguments.of("dog_house", ModItems.DOG_HOUSE),
        Arguments.of("dog_bed", ModItems.DOG_BED),
        Arguments.of("sled", ModItems.SLED));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woodenItems")
  @DisplayName("a wooden item's info page shows it in every vanilla wood")
  void woodenItemPagesShowEveryWood(final String id, final Item item) {
    final DogsUnleashedInfoEntries.InfoEntry entry =
        DogsUnleashedInfoEntries.all().stream()
            .filter(candidate -> candidate.id().equals(id))
            .findFirst()
            .orElseThrow();

    assertEquals(11, entry.stacks().size());
    assertEquals(
        11,
        entry.stacks().stream()
            .filter(stack -> stack.isOf(item))
            .map(stack -> stack.get(ModComponents.WOOD))
            .distinct()
            .count());
  }
}
