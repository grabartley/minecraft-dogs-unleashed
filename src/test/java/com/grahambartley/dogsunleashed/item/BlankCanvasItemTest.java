package com.grahambartley.dogsunleashed.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModPortraits;
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
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BlankCanvasItemTest {

  @Test
  @DisplayName("a fresh canvas paints the smallest portrait")
  void freshCanvasIsSmall() {
    assertEquals(
        PortraitSize.SMALL, BlankCanvasItem.sizeOf(new ItemStack(ModPortraits.BLANK_CANVAS)));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(PortraitSize.class)
  @DisplayName("a canvas remembers the size it was set to")
  void canvasKeepsItsChosenSize(final PortraitSize size) {
    final ItemStack canvas = new ItemStack(ModPortraits.BLANK_CANVAS);
    canvas.set(ModComponents.PORTRAIT_SIZE, size);
    assertEquals(size, BlankCanvasItem.sizeOf(canvas));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(PortraitSize.class)
  @DisplayName("sizes read with vanilla's painting dimensions text, width first")
  void dimensionsUseVanillaPaintingText(final PortraitSize size) {
    final TranslatableTextContent content =
        (TranslatableTextContent) BlankCanvasItem.dimensionsOf(size).getContent();
    assertEquals("painting.dimensions", content.getKey());
    assertEquals(List.of(size.width(), size.height()), List.of(content.getArgs()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(PortraitSize.class)
  @DisplayName("the tooltip shows the chosen size and how to use the canvas")
  void tooltipShowsSizeAndHint(final PortraitSize size) {
    final ItemStack canvas = new ItemStack(ModPortraits.BLANK_CANVAS);
    canvas.set(ModComponents.PORTRAIT_SIZE, size);
    final List<Text> tooltip = new ArrayList<>();
    canvas.getItem().appendTooltip(canvas, Item.TooltipContext.DEFAULT, tooltip, TooltipType.BASIC);
    assertEquals(
        List.of("painting.dimensions", "item.dogs-unleashed.blank_canvas.hint"),
        tooltip.stream()
            .map(line -> ((TranslatableTextContent) line.getContent()).getKey())
            .toList());
    assertEquals(
        List.of(size.width(), size.height()),
        List.of(((TranslatableTextContent) tooltip.get(0).getContent()).getArgs()));
  }
}
