package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import com.grahambartley.dogsunleashed.portrait.PortraitSize;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ModPortraitsTest {

  static Stream<Arguments> items() {
    return Stream.of(
        Arguments.of("blank_canvas", ModPortraits.BLANK_CANVAS, 64),
        Arguments.of("dog_portrait", ModPortraits.DOG_PORTRAIT, 1));
  }

  @ParameterizedTest(name = "dogs-unleashed:{0}")
  @MethodSource("items")
  @DisplayName("each portrait item is registered under its id and stacks like its vanilla kin")
  void itemIsRegistered(final String path, final Item item, final int maxCount) {
    assertSame(item, Registries.ITEM.get(Identifier.of(DogsUnleashed.MOD_ID, path)));
    assertEquals(maxCount, item.getMaxCount());
  }

  @Test
  @DisplayName("the hanging portrait entity is registered under dogs-unleashed:dog_portrait")
  void entityIsRegistered() {
    assertSame(
        ModPortraits.DOG_PORTRAIT_ENTITY,
        Registries.ENTITY_TYPE.get(Identifier.of(DogsUnleashed.MOD_ID, "dog_portrait")));
  }

  @Test
  @DisplayName("a portrait stack carries the portrait it was made from")
  void portraitStackCarriesThePortrait() {
    final DogPortrait portrait =
        new DogPortrait(
            new DogStatueLook(UnleashedDogBreed.DACHSHUND, false, 0, -1, 0, 0, null, null),
            "Frank",
            PortraitSize.LARGE,
            DogStatuePose.STAND);
    final ItemStack stack = ModPortraits.portraitStack(portrait);
    assertSame(ModPortraits.DOG_PORTRAIT, stack.getItem());
    assertEquals(portrait, stack.get(ModComponents.DOG_PORTRAIT));
  }
}
