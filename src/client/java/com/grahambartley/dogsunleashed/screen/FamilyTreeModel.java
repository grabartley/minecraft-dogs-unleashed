package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.SyncDogConnectionsPayload;
import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.NodePosition;
import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.Relations;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

final class FamilyTreeModel {

  static final int MAX_VISIBLE_NODES = 64;

  private final Consumer<String> connectionRequester;
  private final Map<String, ConnectionDogSyncData> nodesById = new HashMap<>();
  private final Map<String, Relations> relationsById = new HashMap<>();
  private final Map<String, List<String>> parentIdsById = new HashMap<>();
  private final Set<String> expandedIds = new LinkedHashSet<>();
  private final Set<String> requestedIds = new LinkedHashSet<>();

  private String focusId;
  private @Nullable String selectedId;
  private @Nullable String lastExpandedId;
  private Map<String, NodePosition> positions = Map.of();
  private boolean nodeLimitHit = false;
  private boolean anyListTruncated = false;

  FamilyTreeModel(
      final ConnectionDogSyncData focusDog, final Consumer<String> connectionRequester) {
    this.connectionRequester = connectionRequester;
    this.focusId = focusDog.pet().petId();
    storeNode(focusDog);
    this.expandedIds.add(focusId);
    this.selectedId = focusId;
  }

  void start() {
    requestConnections(focusId);
    recomputeLayout();
  }

  void accept(final SyncDogConnectionsPayload payload) {
    final String id = payload.self().pet().petId();
    storeNode(payload.self());
    payload.parents().forEach(this::storeNode);
    payload.mates().forEach(this::storeNode);
    payload.siblings().forEach(this::storeNode);
    payload.children().forEach(this::storeNode);
    relationsById.put(
        id,
        new Relations(
            petIds(payload.parents()),
            petIds(payload.mates()),
            petIds(payload.siblings()),
            petIds(payload.children())));
    anyListTruncated |= payload.truncated();
    recomputeLayout();
  }

  void selectAndExpand(final String dogId) {
    selectedId = dogId;
    expandDog(dogId);
  }

  void toggleSelectedExpansion() {
    if (selectedId == null) {
      return;
    }
    if (expandedIds.contains(selectedId) && !selectedId.equals(focusId)) {
      expandedIds.remove(selectedId);
      recomputeLayout();
      return;
    }
    expandDog(selectedId);
  }

  boolean refocusOnSelected() {
    if (selectedId == null || selectedId.equals(focusId)) {
      return false;
    }
    focusId = selectedId;
    expandedIds.clear();
    expandedIds.add(focusId);
    requestedIds.clear();
    nodeLimitHit = false;
    anyListTruncated = false;
    requestConnections(focusId);
    recomputeLayout();
    return true;
  }

  String focusId() {
    return focusId;
  }

  @Nullable
  String selectedId() {
    return selectedId;
  }

  @Nullable
  ConnectionDogSyncData node(final @Nullable String dogId) {
    return dogId == null ? null : nodesById.get(dogId);
  }

  Map<String, NodePosition> positions() {
    return Collections.unmodifiableMap(positions);
  }

  Map<String, Relations> relations() {
    return Collections.unmodifiableMap(relationsById);
  }

  Map<String, List<String>> parentIds() {
    return Collections.unmodifiableMap(parentIdsById);
  }

  boolean isExpanded(final String dogId) {
    return expandedIds.contains(dogId);
  }

  boolean nodeLimitHit() {
    return nodeLimitHit;
  }

  boolean anyListTruncated() {
    return anyListTruncated;
  }

  private void expandDog(final String dogId) {
    if (expandedIds.contains(dogId)) {
      return;
    }
    if (positions.size() >= MAX_VISIBLE_NODES) {
      nodeLimitHit = true;
      return;
    }
    nodeLimitHit = false;
    expandedIds.add(dogId);
    lastExpandedId = dogId;
    requestConnections(dogId);
    recomputeLayout();
  }

  private void recomputeLayout() {
    final Set<String> visible =
        FamilyTreeLayout.computeVisible(focusId, expandedIds, relationsById);
    if (visible.size() > MAX_VISIBLE_NODES && lastExpandedId != null) {
      expandedIds.remove(lastExpandedId);
      nodeLimitHit = true;
    }
    lastExpandedId = null;
    positions = FamilyTreeLayout.layout(focusId, expandedIds, relationsById, parentIdsById);
    if (selectedId != null && !positions.containsKey(selectedId)) {
      selectedId = focusId;
    }
  }

  private void requestConnections(final String dogId) {
    if (requestedIds.add(dogId)) {
      connectionRequester.accept(dogId);
    }
  }

  private void storeNode(final ConnectionDogSyncData dog) {
    nodesById.put(dog.pet().petId(), dog);
    parentIdsById.put(dog.pet().petId(), dog.parentIds());
  }

  private static List<String> petIds(final List<ConnectionDogSyncData> dogs) {
    return dogs.stream().map(dog -> dog.pet().petId()).toList();
  }
}
