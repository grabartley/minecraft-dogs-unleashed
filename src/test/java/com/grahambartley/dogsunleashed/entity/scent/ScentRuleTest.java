package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
}
