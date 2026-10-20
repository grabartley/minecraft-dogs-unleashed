package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ScentRuleTest {

  private static ScentRule rule(
      final boolean entities,
      final boolean blocks,
      final boolean suspicious,
      final boolean structures) {
    return new ScentRule(
        RegistryEntryList.of(),
        entities ? Optional.of(RegistryEntryList.of()) : Optional.empty(),
        blocks ? Optional.of(RegistryEntryList.of()) : Optional.empty(),
        suspicious,
        structures
            ? Optional.of(TagKey.of(RegistryKeys.STRUCTURE, Identifier.of("minecraft", "ruins")))
            : Optional.empty());
  }

  static Stream<Arguments> singleTargets() {
    return Stream.of(
        Arguments.of(rule(true, false, false, false), ScentTargetKind.ENTITY),
        Arguments.of(rule(false, true, false, false), ScentTargetKind.BLOCK),
        Arguments.of(rule(false, false, true, false), ScentTargetKind.SUSPICIOUS_BLOCK),
        Arguments.of(rule(false, false, false, true), ScentTargetKind.LANDMARK));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("singleTargets")
  @DisplayName("a rule with one target leads to that kind of target")
  void kindFollowsTheTarget(final ScentRule rule, final ScentTargetKind expected) {
    assertEquals(expected, rule.kind());
  }

  static Stream<Arguments> invalidTargets() {
    return Stream.of(
        Arguments.of("no target", rule(false, false, false, false)),
        Arguments.of("entities and blocks", rule(true, true, false, false)),
        Arguments.of("blocks and suspicious blocks", rule(false, true, true, false)),
        Arguments.of("entities and structures", rule(true, false, false, true)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidTargets")
  @DisplayName("a rule without exactly one target has no kind")
  void kindNeedsExactlyOneTarget(final String label, final ScentRule rule) {
    assertThrows(IllegalStateException.class, rule::kind);
  }

  private static ScentRule boneRuleForOres() {
    return new ScentRule(
        RegistryEntryList.of(Items.BONE.getRegistryEntry()),
        Optional.empty(),
        Optional.of(RegistryEntryList.of(Blocks.IRON_ORE.getRegistryEntry())),
        false,
        Optional.empty());
  }

  static Stream<Arguments> heldItems() {
    return Stream.of(
        Arguments.of(new ItemStack(Items.BONE), true),
        Arguments.of(new ItemStack(Items.BONE, 16), true),
        Arguments.of(new ItemStack(Items.STICK), false),
        Arguments.of(ItemStack.EMPTY, false));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("heldItems")
  @DisplayName("a rule matches only the items it lists")
  void matchesListedItems(final ItemStack stack, final boolean expected) {
    assertEquals(expected, boneRuleForOres().matchesItem(stack));
  }

  static Stream<Arguments> blocks() {
    return Stream.of(
        Arguments.of(boneRuleForOres(), Blocks.IRON_ORE, true),
        Arguments.of(boneRuleForOres(), Blocks.STONE, false),
        Arguments.of(rule(true, false, false, false), Blocks.IRON_ORE, false));
  }

  @ParameterizedTest(name = "{1} -> {2}")
  @MethodSource("blocks")
  @DisplayName("a rule matches only the blocks it lists, and none when it lists no blocks")
  void matchesListedBlocks(final ScentRule rule, final Block block, final boolean expected) {
    assertEquals(expected, rule.matchesBlock(block.getDefaultState()));
  }

  @ParameterizedTest(name = "{0} -> loads={1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"items\": \"minecraft:bone\", \"blocks\": \"minecraft:iron_ore\"} | true",
        "{\"items\": \"minecraft:bone\", \"suspicious_blocks\": true} | true",
        "{\"items\": \"minecraft:bone\", \"entities\": \"minecraft:zombie\"} | true",
        "{\"items\": \"minecraft:bone\"} | false",
        "{\"items\": \"minecraft:bone\", \"blocks\": \"minecraft:iron_ore\","
            + " \"suspicious_blocks\": true} | false"
      })
  @DisplayName("a data pack rule loads only when it names exactly one target")
  void codecNeedsExactlyOneTarget(final String json, final boolean loads) {
    assertEquals(
        loads,
        ScentRule.CODEC
            .parse(
                RegistryOps.of(JsonOps.INSTANCE, BuiltinRegistries.createWrapperLookup()),
                JsonParser.parseString(json))
            .isSuccess());
  }
}
