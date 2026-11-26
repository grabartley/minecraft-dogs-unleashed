package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonPrimitive;
import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WoodPlanksTest {

  private static final BlockPos POS = new BlockPos(3, 64, -7);

  static Stream<Arguments> woods() {
    return Stream.of(
        Arguments.of(WoodType.OAK, Blocks.OAK_PLANKS, "minecraft:textures/block/oak_planks.png"),
        Arguments.of(
            WoodType.SPRUCE, Blocks.SPRUCE_PLANKS, "minecraft:textures/block/spruce_planks.png"),
        Arguments.of(
            WoodType.BIRCH, Blocks.BIRCH_PLANKS, "minecraft:textures/block/birch_planks.png"),
        Arguments.of(
            WoodType.ACACIA, Blocks.ACACIA_PLANKS, "minecraft:textures/block/acacia_planks.png"),
        Arguments.of(
            WoodType.CHERRY, Blocks.CHERRY_PLANKS, "minecraft:textures/block/cherry_planks.png"),
        Arguments.of(
            WoodType.JUNGLE, Blocks.JUNGLE_PLANKS, "minecraft:textures/block/jungle_planks.png"),
        Arguments.of(
            WoodType.DARK_OAK,
            Blocks.DARK_OAK_PLANKS,
            "minecraft:textures/block/dark_oak_planks.png"),
        Arguments.of(
            WoodType.CRIMSON, Blocks.CRIMSON_PLANKS, "minecraft:textures/block/crimson_planks.png"),
        Arguments.of(
            WoodType.WARPED, Blocks.WARPED_PLANKS, "minecraft:textures/block/warped_planks.png"),
        Arguments.of(
            WoodType.MANGROVE,
            Blocks.MANGROVE_PLANKS,
            "minecraft:textures/block/mangrove_planks.png"),
        Arguments.of(
            WoodType.BAMBOO, Blocks.BAMBOO_PLANKS, "minecraft:textures/block/bamboo_planks.png"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("a wood resolves to vanilla's own planks block")
  void planksAreVanillas(final WoodType wood, final Block planks, final String texture) {
    assertSame(planks, WoodPlanks.planksOf(wood));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("a wood draws vanilla's planks texture, so a resource pack restyles it")
  void textureIsVanillas(final WoodType wood, final Block planks, final String texture) {
    assertEquals(Identifier.of(texture), WoodPlanks.textureOf(wood));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("every vanilla wood is offered")
  void everyVanillaWoodIsOffered(final WoodType wood, final Block planks, final String texture) {
    assertTrue(WoodPlanks.all().contains(wood));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("a wood survives being saved and loaded by name")
  void woodSurvivesSaving(final WoodType wood, final Block planks, final String texture) {
    final NbtCompound nbt = new NbtCompound();
    WoodPlanks.write(nbt, wood);

    assertSame(wood, WoodPlanks.read(nbt));
    assertSame(wood, WoodPlanks.byName(wood.name()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("an item reports the wood stored on it")
  void itemReportsItsWood(final WoodType wood, final Block planks, final String texture) {
    final ItemStack stack = new ItemStack(ModItems.DOG_BED);
    stack.set(ModComponents.WOOD, wood);

    assertSame(wood, WoodPlanks.of(stack));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("the tooltip names the wood with vanilla's own planks name")
  void tooltipUsesThePlanksName(final WoodType wood, final Block planks, final String texture) {
    assertEquals(
        planks.getTranslationKey(),
        ((TranslatableTextContent) WoodPlanks.tooltipOf(wood).getContent()).getKey());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("a wooden block's particles look like its planks")
  void woodenBlockAppearsAsItsPlanks(
      final WoodType wood, final Block planks, final String texture) {
    final BlockState bed = ModBlocks.DOG_BED.getDefaultState();
    final DogBedBlockEntity entity = new DogBedBlockEntity(POS, bed);
    entity.setWood(wood);
    final BlockView world = mock(BlockView.class);
    when(world.getBlockState(POS)).thenReturn(bed);
    when(world.getBlockEntity(POS)).thenReturn(entity);

    assertEquals(planks.getDefaultState(), WoodPlanks.appearanceOf(bed, world, POS));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("particles kicked up by running on top of a wooden block look like its planks")
  void runningParticlesAppearAsThePlanksBelow(
      final WoodType wood, final Block planks, final String texture) {
    final BlockState bed = ModBlocks.DOG_BED.getDefaultState();
    final BlockPos below = POS.down();
    final DogBedBlockEntity entity = new DogBedBlockEntity(below, bed);
    entity.setWood(wood);
    final BlockView world = mock(BlockView.class);
    when(world.getBlockState(POS)).thenReturn(Blocks.AIR.getDefaultState());
    when(world.getBlockState(below)).thenReturn(bed);
    when(world.getBlockEntity(below)).thenReturn(entity);

    assertEquals(planks.getDefaultState(), WoodPlanks.appearanceOf(bed, world, POS));
  }

  @ParameterizedTest(name = "\"{0}\" falls back to oak")
  @ValueSource(strings = {"", "pale_oak", "redwood", "not a wood"})
  @DisplayName("a missing or unknown wood counts as oak, so old saves and removed mods still load")
  void unknownWoodIsOak(final String name) {
    assertSame(WoodType.OAK, WoodPlanks.byName(name));
    assertSame(WoodType.OAK, WoodPlanks.read(new NbtCompound()));
    assertSame(WoodType.OAK, WoodPlanks.of(new ItemStack(ModItems.DOG_HOUSE)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woodenItems")
  @DisplayName("a wooden item has one stack per vanilla wood, in vanilla's order")
  void stacksListEveryVanillaWood(final Item item) {
    final List<ItemStack> stacks = WoodPlanks.stacksOf(item);

    assertEquals(
        List.of(
            WoodType.OAK,
            WoodType.SPRUCE,
            WoodType.BIRCH,
            WoodType.ACACIA,
            WoodType.CHERRY,
            WoodType.JUNGLE,
            WoodType.DARK_OAK,
            WoodType.CRIMSON,
            WoodType.WARPED,
            WoodType.MANGROVE,
            WoodType.BAMBOO),
        stacks.stream().map(stack -> stack.get(ModComponents.WOOD)).toList());
    stacks.forEach(stack -> assertSame(item, stack.getItem()));
  }

  static Stream<Item> woodenItems() {
    return Stream.of(ModItems.DOG_HOUSE, ModItems.DOG_BED, ModItems.SLED);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("the tooltip line added to an item names the wood stored on it")
  void appendedTooltipNamesTheWood(final WoodType wood, final Block planks, final String texture) {
    final ItemStack stack = new ItemStack(ModItems.SLED);
    stack.set(ModComponents.WOOD, wood);
    final List<Text> tooltip = new ArrayList<>();

    WoodPlanks.appendTooltip(stack, tooltip);

    assertEquals(List.of(WoodPlanks.tooltipOf(wood)), tooltip);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("a wood saved in an item component loads back as the same wood")
  void codecRoundTrips(final WoodType wood, final Block planks, final String texture) {
    final var json = WoodPlanks.CODEC.encodeStart(JsonOps.INSTANCE, wood).getOrThrow();

    assertEquals(new JsonPrimitive(wood.name()), json);
    assertSame(wood, WoodPlanks.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
  }

  @ParameterizedTest(name = "\"{0}\" loads as oak")
  @ValueSource(strings = {"", "pale_oak", "redwood", "not a wood"})
  @DisplayName("an item component naming a wood with no vanilla planks loads as oak")
  void codecFallsBackToOak(final String name) {
    assertSame(
        WoodType.OAK,
        WoodPlanks.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(name)).getOrThrow());
    assertEquals(WoodPlanks.textureOf(WoodType.OAK), WoodPlanks.textureOf(WoodPlanks.byName(name)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("plainBlocks")
  @DisplayName("a block that is not wooden keeps its own particles")
  void plainBlockKeepsItsLook(final Block block) {
    final BlockState state = block.getDefaultState();

    assertSame(state, WoodPlanks.appearanceOf(state, mock(BlockView.class), POS));
  }

  static Stream<Block> plainBlocks() {
    return Stream.of(Blocks.STONE, Blocks.OAK_PLANKS, ModBlocks.FOOD_BOWL);
  }
}
