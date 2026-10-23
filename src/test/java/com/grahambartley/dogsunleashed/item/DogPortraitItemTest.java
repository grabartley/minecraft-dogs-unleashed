package com.grahambartley.dogsunleashed.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModPortraits;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import com.grahambartley.dogsunleashed.portrait.PortraitSize;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogPortraitItemTest {

  private static final DogStatueLook LOOK =
      new DogStatueLook(UnleashedDogBreed.SHIBA_INU, false, 0, -1, 0, 0, null, null);

  private static List<Text> tooltipOf(final ItemStack stack) {
    final List<Text> tooltip = new ArrayList<>();
    stack.getItem().appendTooltip(stack, Item.TooltipContext.DEFAULT, tooltip, TooltipType.BASIC);
    return tooltip;
  }

  @ParameterizedTest(name = "{0} at {1}")
  @CsvSource({"Mochi, SMALL", "Bentley, WIDE", "Frank, LARGE"})
  @DisplayName("the tooltip names the dog, then gives the portrait's size and the pose hint")
  void tooltipNamesTheDog(final String name, final PortraitSize size) {
    final List<Text> tooltip =
        tooltipOf(ModPortraits.portraitStack(new DogPortrait(LOOK, name, size, DogStatuePose.SIT)));
    assertEquals(3, tooltip.size());
    assertEquals(name, tooltip.get(0).getString());
    final TranslatableTextContent dimensions =
        (TranslatableTextContent) tooltip.get(1).getContent();
    assertEquals("painting.dimensions", dimensions.getKey());
    assertEquals(List.of(size.width(), size.height()), List.of(dimensions.getArgs()));
    assertEquals(
        DogPortraitItem.POSE_HINT_KEY,
        ((TranslatableTextContent) tooltip.get(2).getContent()).getKey());
  }

  @Test
  @DisplayName("a portrait of a dog with no name shows only its size and the pose hint")
  void unnamedPortraitShowsOnlyTheSize() {
    assertEquals(
        2,
        tooltipOf(
                ModPortraits.portraitStack(
                    new DogPortrait(LOOK, "", PortraitSize.SMALL, DogStatuePose.SIT)))
            .size());
  }

  @Test
  @DisplayName("a portrait item with no portrait on it shows no tooltip lines")
  void emptyPortraitShowsNothing() {
    assertTrue(tooltipOf(new ItemStack(ModPortraits.DOG_PORTRAIT)).isEmpty());
  }
}
