package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.NodePosition;
import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.Relations;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FamilyTreeLayoutTest {

  private static final String FOCUS = "focus";
  private static final String MOTHER = "mother";
  private static final String FATHER = "father";
  private static final String MATE = "mate";
  private static final String SISTER = "sister";
  private static final String CHILD = "child";
  private static final String GRANDMA = "grandma";
  private static final String GRANDPA = "grandpa";

  private static Map<String, Relations> familyRelations() {
    return Map.of(
        FOCUS,
        new Relations(List.of(MOTHER, FATHER), List.of(MATE), List.of(SISTER), List.of(CHILD)),
        MOTHER,
        new Relations(
            List.of(GRANDMA, GRANDPA), List.of(FATHER), List.of(), List.of(FOCUS, SISTER)));
  }

  private static Map<String, List<String>> familyParents() {
    return Map.of(
        FOCUS, List.of(MOTHER, FATHER),
        SISTER, List.of(MOTHER, FATHER),
        MOTHER, List.of(GRANDMA, GRANDPA),
        CHILD, List.of(FOCUS, MATE));
  }

  static Stream<Arguments> visibleParentCases() {
    return Stream.of(
        Arguments.of(CHILD, Set.of(FOCUS, MATE, CHILD), Set.of(FOCUS, MATE)),
        Arguments.of(CHILD, Set.of(FOCUS, CHILD), Set.of(FOCUS)),
        Arguments.of(FOCUS, Set.of(FOCUS, MOTHER, FATHER), Set.of(MOTHER, FATHER)),
        Arguments.of(MOTHER, Set.of(FOCUS, MOTHER), null),
        Arguments.of(GRANDMA, Set.of(GRANDMA, MOTHER), null));
  }

  @ParameterizedTest(name = "{0} among {1} -> parents {2}")
  @MethodSource("visibleParentCases")
  @DisplayName("a child's drawn parents are its own visible parent ids")
  void visibleParentsComeFromEachChildsParentIds(
      final String child, final Set<String> visible, final Set<String> expected) {
    assertEquals(expected, FamilyTreeLayout.visibleParents(familyParents(), visible).get(child));
  }

  @Test
  @DisplayName("with only the focus expanded, exactly the direct connections are visible")
  void visibleIsFocusPlusDirectConnectionsInitially() {
    final Set<String> visible =
        FamilyTreeLayout.computeVisible(FOCUS, Set.of(FOCUS), familyRelations());

    assertEquals(Set.of(FOCUS, MOTHER, FATHER, MATE, SISTER, CHILD), visible);
  }

  @Test
  @DisplayName("expanding a parent pulls in the grandparents")
  void expandingAParentRevealsGrandparents() {
    final Set<String> visible =
        FamilyTreeLayout.computeVisible(FOCUS, Set.of(FOCUS, MOTHER), familyRelations());

    assertTrue(visible.contains(GRANDMA), "grandma should be visible after expanding mother");
    assertTrue(visible.contains(GRANDPA), "grandpa should be visible after expanding mother");
  }

  @Test
  @DisplayName("collapsing re-derives visibility, so grandparents fold away with their branch")
  void collapsingPrunesBranchesOnlyReachableThroughTheCollapsedDog() {
    final Map<String, Relations> relations = familyRelations();
    final Set<String> expandedThenCollapsed =
        FamilyTreeLayout.computeVisible(FOCUS, Set.of(FOCUS), relations);

    assertFalse(
        expandedThenCollapsed.contains(GRANDMA),
        "grandma is only reachable through mother and must fold away when mother is collapsed");
  }

  @ParameterizedTest(name = "{0} sits in generation {1}")
  @MethodSource("expectedDepths")
  @DisplayName("generations are assigned relative to the focus dog")
  void depthsFollowGenerations(final String id, final int expectedDepth) {
    final Map<String, NodePosition> positions =
        FamilyTreeLayout.layout(FOCUS, Set.of(FOCUS, MOTHER), familyRelations(), familyParents());

    assertEquals(expectedDepth, positions.get(id).depth(), id);
  }

  static Stream<Arguments> expectedDepths() {
    return Stream.of(
        Arguments.of(FOCUS, 0),
        Arguments.of(MATE, 0),
        Arguments.of(SISTER, 0),
        Arguments.of(MOTHER, -1),
        Arguments.of(FATHER, -1),
        Arguments.of(GRANDMA, -2),
        Arguments.of(GRANDPA, -2),
        Arguments.of(CHILD, 1));
  }

  @Test
  @DisplayName("a dog who married into the family sits directly beside its mate")
  void marriedInMateIsAdjacentToItsPartner() {
    final Map<String, NodePosition> positions =
        FamilyTreeLayout.layout(FOCUS, Set.of(FOCUS, MOTHER), familyRelations(), familyParents());

    final float gap = positions.get(MATE).x() - positions.get(FOCUS).x();
    assertEquals(1.0f, gap, 0.001f, "mate should occupy the slot immediately right of the focus");
  }

  @Test
  @DisplayName("children hang under the average position of their parents")
  void childrenHangUnderTheirParents() {
    final Map<String, NodePosition> positions =
        FamilyTreeLayout.layout(FOCUS, Set.of(FOCUS, MOTHER), familyRelations(), familyParents());

    final float parentMidpoint = (positions.get(MOTHER).x() + positions.get(FATHER).x()) / 2f;
    assertTrue(
        Math.abs(positions.get(FOCUS).x() - parentMidpoint) <= 1.0f,
        "focus at "
            + positions.get(FOCUS).x()
            + " should sit near its parents' midpoint "
            + parentMidpoint);
  }

  static Stream<Arguments> rowOrders() {
    final Map<String, Float> parentSlots = Map.of(MOTHER, 0f, FATHER, 1f);
    final Map<String, Set<String>> bothParents =
        Map.of(FOCUS, Set.of(MOTHER, FATHER), SISTER, Set.of(MOTHER, FATHER));
    return Stream.of(
        Arguments.of(
            List.of(FOCUS, SISTER, MATE),
            bothParents,
            Map.of(FOCUS, Set.of(MATE), MATE, Set.of(FOCUS)),
            parentSlots,
            List.of(FOCUS, MATE, SISTER)),
        Arguments.of(
            List.of("a", "x", "b"),
            Map.of(),
            Map.of("a", Set.of("b"), "b", Set.of("a")),
            Map.of(),
            List.of("x", "b", "a")),
        Arguments.of(
            List.of("older", "younger"),
            Map.of("older", Set.of(FATHER), "younger", Set.of(MOTHER)),
            Map.of(),
            parentSlots,
            List.of("younger", "older")));
  }

  @ParameterizedTest(name = "{0} -> {4}")
  @MethodSource("rowOrders")
  @DisplayName("a row sorts by parents and keeps each married-in mate beside its partner")
  void rowsSortByParentsAndKeepMatesTogether(
      final List<String> row,
      final Map<String, Set<String>> parentsByChild,
      final Map<String, Set<String>> matesById,
      final Map<String, Float> parentSlots,
      final List<String> expected) {
    assertEquals(expected, FamilyTreeLayout.orderRow(row, parentsByChild, matesById, parentSlots));
  }

  @Test
  @DisplayName("every row is centered on x = 0")
  void rowsAreCentered() {
    final Map<String, NodePosition> positions =
        FamilyTreeLayout.layout(FOCUS, Set.of(FOCUS, MOTHER), familyRelations(), familyParents());

    final float grandparentRowCenter =
        (positions.get(GRANDMA).x() + positions.get(GRANDPA).x()) / 2f;
    assertEquals(0f, grandparentRowCenter, 0.001f);
    assertEquals(0f, positions.get(CHILD).x(), 0.001f, "an only child row centers on zero");
  }

  @Test
  @DisplayName("an unknown focus with no relations still lays out as a single node")
  void unknownFocusLaysOutAlone() {
    final Map<String, NodePosition> positions =
        FamilyTreeLayout.layout("loner", Set.of("loner"), Map.of(), Map.of());

    assertEquals(1, positions.size());
    assertEquals(0f, positions.get("loner").x(), 0.001f);
    assertEquals(0, positions.get("loner").depth());
  }
}
