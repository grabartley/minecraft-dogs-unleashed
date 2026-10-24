package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PortraitGearTest {

  private static final DynamicRegistryManager REGISTRIES =
      DynamicRegistryManager.of(Registries.REGISTRIES);

  @ParameterizedTest(name = "a stack of {0}")
  @ValueSource(ints = {1, 16, 64})
  @DisplayName("a painted item keeps one of it and drops names, so only how it looks is saved")
  void keepsOnlyOneUnnamedItem(final int count) {
    final ItemStack stick = new ItemStack(Items.STICK, count);
    stick.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Best stick"));
    final ItemStack shown = PortraitGear.shownPart(stick);
    assertEquals(1, shown.getCount());
    assertTrue(shown.isOf(Items.STICK));
    assertNull(shown.get(DataComponentTypes.CUSTOM_NAME));
  }

  @ParameterizedTest(name = "dyed {0}")
  @ValueSource(ints = {0x3366AA, 0xFFFFFF, 0x000000})
  @DisplayName("dye colour and wear are kept, since they change how the armour looks")
  void keepsDyeAndWear(final int colour) {
    final ItemStack armour = new ItemStack(Items.WOLF_ARMOR);
    armour.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(colour, true));
    armour.setDamage(40);
    final ItemStack shown = PortraitGear.shownPart(armour);
    assertEquals(colour, shown.get(DataComponentTypes.DYED_COLOR).rgb());
    assertEquals(40, shown.getDamage());
  }

  @ParameterizedTest(name = "a frisbee coloured {0}")
  @EnumSource(
      value = DyeColor.class,
      names = {"RED", "LIME", "BLACK"})
  @DisplayName(
      "the mod's own colour on an item is kept, so a coloured frisbee paints in its colour")
  void keepsModColours(final DyeColor colour) {
    final ItemStack frisbee = new ItemStack(ModItems.FRISBEE);
    frisbee.set(ModComponents.FRISBEE_COLOR, colour);
    assertEquals(colour, PortraitGear.shownPart(frisbee).get(ModComponents.FRISBEE_COLOR));
  }

  @Test
  @DisplayName("a box carried in a dog's mouth keeps its look but not its contents")
  void dropsContainerContents() {
    final ItemStack box = new ItemStack(Items.SHULKER_BOX);
    box.set(
        DataComponentTypes.CONTAINER,
        ContainerComponent.fromStacks(List.of(new ItemStack(Items.WRITTEN_BOOK, 16))));
    final ItemStack shown = PortraitGear.shownPart(box);
    assertTrue(shown.isOf(Items.SHULKER_BOX));
    assertNull(shown.get(DataComponentTypes.CONTAINER));
  }

  @Test
  @DisplayName("a shiny item keeps its shine without keeping its enchantments")
  void keepsTheGlintOnly() {
    final ItemStack shiny = new ItemStack(ModItems.TENNIS_BALL);
    shiny.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    final ItemStack shown = PortraitGear.shownPart(shiny);
    assertTrue(shown.hasGlint());
    assertFalse(PortraitGear.shownPart(new ItemStack(Items.STICK)).hasGlint());
  }

  @Test
  @DisplayName("an empty stack stays empty")
  void emptyStaysEmpty() {
    assertTrue(PortraitGear.shownPart(ItemStack.EMPTY).isEmpty());
  }

  @ParameterizedTest(name = "item id {0}")
  @ValueSource(strings = {"gone:old_bag", "minecraft:not_an_item"})
  @DisplayName("an item that no longer exists reads back as nothing instead of failing the load")
  void unknownItemReadsAsEmpty(final String id) {
    final NbtCompound saved = new NbtCompound();
    saved.putString("id", id);
    saved.putInt("count", 1);
    final ItemStack read =
        PortraitGear.LENIENT_CODEC.parse(REGISTRIES.getOps(NbtOps.INSTANCE), saved).getOrThrow();
    assertTrue(read.isEmpty());
  }

  @Test
  @DisplayName("a real item still reads back")
  void knownItemReads() {
    final Item item = Items.WOLF_ARMOR;
    final var ops = REGISTRIES.getOps(NbtOps.INSTANCE);
    final ItemStack read =
        PortraitGear.LENIENT_CODEC
            .parse(
                ops, PortraitGear.LENIENT_CODEC.encodeStart(ops, new ItemStack(item)).getOrThrow())
            .getOrThrow();
    assertTrue(read.isOf(item));
  }
}
