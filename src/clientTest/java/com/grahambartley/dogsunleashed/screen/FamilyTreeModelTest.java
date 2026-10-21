package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.network.payload.RidingView;
import com.grahambartley.dogsunleashed.network.payload.SyncDogConnectionsPayload;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FamilyTreeModelTest {

  private final List<String> requested = new ArrayList<>();
  private FamilyTreeModel model;

  @BeforeEach
  void openOnPup() {
    model = new FamilyTreeModel(dog("pup"), requested::add);
    model.start();
  }

  @Test
  @DisplayName("opening the tree asks for the focus dog's connections and shows only it")
  void startRequestsTheFocusDog() {
    assertEquals(List.of("pup"), requested);
    assertEquals("pup", model.focusId());
    assertEquals("pup", model.selectedId());
    assertEquals(List.of("pup"), List.copyOf(model.positions().keySet()));
  }

  @Test
  @DisplayName("connections for the focus dog lay out its parents a row above it")
  void acceptLaysOutRelatives() {
    model.accept(connections("pup", List.of("mum", "dad"), List.of(), false));

    assertEquals(3, model.positions().size());
    assertEquals(-1, model.positions().get("mum").depth());
    assertEquals(0, model.positions().get("pup").depth());
    assertEquals("mum", model.node("mum").pet().name());
    assertFalse(model.anyListTruncated());
  }

  @Test
  @DisplayName("clicking a relative selects it, expands it and asks for its connections once")
  void selectAndExpandRequestsOnce() {
    model.accept(connections("pup", List.of("mum"), List.of(), false));

    model.selectAndExpand("mum");
    model.selectAndExpand("mum");

    assertEquals("mum", model.selectedId());
    assertTrue(model.isExpanded("mum"));
    assertEquals(List.of("pup", "mum"), requested);
  }

  @Test
  @DisplayName("toggling an expanded relative collapses it, but the focus never collapses")
  void toggleCollapsesRelativesOnly() {
    model.accept(connections("pup", List.of("mum"), List.of(), false));
    model.selectAndExpand("mum");
    model.accept(connections("mum", List.of("gran"), List.of(), false));

    model.toggleSelectedExpansion();

    assertFalse(model.isExpanded("mum"));
    assertFalse(model.positions().containsKey("gran"));

    model.selectAndExpand("pup");
    model.toggleSelectedExpansion();

    assertTrue(model.isExpanded("pup"));
  }

  @Test
  @DisplayName("a selection that drops out of view falls back to the focus dog")
  void hiddenSelectionFallsBackToFocus() {
    model.accept(connections("pup", List.of("mum"), List.of(), false));
    model.selectAndExpand("mum");

    model.accept(connections("pup", List.of(), List.of(), false));

    assertEquals("pup", model.selectedId());
    assertNull(model.positions().get("mum"));
  }

  @Test
  @DisplayName("refocusing makes the selected dog the root and starts its tree afresh")
  void refocusRestartsFromTheSelection() {
    model.accept(connections("pup", List.of("mum"), List.of(), true));
    model.selectAndExpand("mum");

    assertTrue(model.refocusOnSelected());

    assertEquals("mum", model.focusId());
    assertFalse(model.anyListTruncated());
    assertTrue(model.isExpanded("mum"));
    assertFalse(model.isExpanded("pup"));
    assertEquals(List.of("pup", "mum", "mum"), requested);
  }

  @Test
  @DisplayName("refocusing on the dog already in focus does nothing")
  void refocusOnFocusIsANoOp() {
    assertFalse(model.refocusOnSelected());
    assertEquals(List.of("pup"), requested);
  }

  @Test
  @DisplayName("a truncated list from the server raises the notice")
  void truncatedListsRaiseTheNotice() {
    model.accept(connections("pup", List.of(), List.of(), true));

    assertTrue(model.anyListTruncated());
  }

  @Test
  @DisplayName("re-expanding a dog whose known relatives pass the node cap is undone and flagged")
  void expansionPastTheCapIsUndone() {
    final List<String> litter =
        IntStream.range(0, FamilyTreeModel.MAX_VISIBLE_NODES).mapToObj(i -> "kid" + i).toList();
    model.accept(connections("pup", List.of("mum"), List.of(), false));
    model.selectAndExpand("mum");
    model.accept(connections("mum", List.of(), litter, false));
    model.toggleSelectedExpansion();

    model.selectAndExpand("mum");

    assertTrue(model.nodeLimitHit());
    assertFalse(model.isExpanded("mum"));
    assertEquals(2, model.positions().size());
  }

  @Test
  @DisplayName("expanding is refused once the tree already shows the node cap")
  void expansionAtTheCapIsRefused() {
    final List<String> litter =
        IntStream.range(0, FamilyTreeModel.MAX_VISIBLE_NODES).mapToObj(i -> "kid" + i).toList();
    model.accept(connections("pup", List.of(), litter, false));

    model.selectAndExpand("kid0");

    assertTrue(model.nodeLimitHit());
    assertFalse(model.isExpanded("kid0"));
    assertEquals(List.of("pup"), requested);
  }

  @Test
  @DisplayName("the node budget matches the documented cap")
  void nodeBudgetIsDocumented() {
    assertEquals(64, FamilyTreeModel.MAX_VISIBLE_NODES);
  }

  private static SyncDogConnectionsPayload connections(
      final String self,
      final List<String> parents,
      final List<String> children,
      final boolean truncated) {
    return new SyncDogConnectionsPayload(
        dog(self),
        List.of(),
        parents.stream().map(FamilyTreeModelTest::dog).toList(),
        List.of(),
        List.of(),
        children.stream().map(FamilyTreeModelTest::dog).toList(),
        truncated);
  }

  private static ConnectionDogSyncData dog(final String id) {
    return new ConnectionDogSyncData(
        new PetSyncData(
            id,
            UnleashedDogBreed.HUSKY,
            id,
            20,
            20,
            0,
            64,
            0,
            "minecraft:overworld",
            PetLifeState.LIVING,
            false,
            0,
            0,
            0,
            DogFurStage.NATURAL.ordinal(),
            0.3f,
            4f,
            List.of(),
            RidingView.NONE,
            PetAwayStatus.HOME,
            false,
            DogBondStep.STRANGER),
        "owner",
        "Owner",
        List.of(),
        false);
  }
}
