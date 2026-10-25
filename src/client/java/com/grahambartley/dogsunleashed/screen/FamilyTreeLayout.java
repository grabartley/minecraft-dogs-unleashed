package com.grahambartley.dogsunleashed.screen;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class FamilyTreeLayout {

  public record Relations(
      List<String> parents, List<String> mates, List<String> siblings, List<String> children) {}

  public record NodePosition(float x, int depth) {}

  private FamilyTreeLayout() {}

  public static Set<String> computeVisible(
      final String focusId,
      final Set<String> expandedIds,
      final Map<String, Relations> relationsById) {
    final Set<String> visible = new LinkedHashSet<>();
    final Deque<String> queue = new ArrayDeque<>();
    visible.add(focusId);
    queue.add(focusId);
    while (!queue.isEmpty()) {
      final String id = queue.poll();
      final Relations relations = relationsById.get(id);
      if (!expandedIds.contains(id) || relations == null) {
        continue;
      }
      for (final String relatedId : allRelated(relations)) {
        if (visible.add(relatedId)) {
          queue.add(relatedId);
        }
      }
    }
    return visible;
  }

  static Map<String, Integer> assignDepths(
      final String focusId,
      final Set<String> expandedIds,
      final Map<String, Relations> relationsById,
      final Set<String> visible) {
    final Map<String, Integer> depthById = new LinkedHashMap<>();
    final Deque<String> queue = new ArrayDeque<>();
    depthById.put(focusId, 0);
    queue.add(focusId);
    while (!queue.isEmpty()) {
      final String id = queue.poll();
      final Relations relations = relationsById.get(id);
      if (!expandedIds.contains(id) || relations == null) {
        continue;
      }
      final int depth = depthById.get(id);
      claimDepth(relations.parents(), depth - 1, visible, depthById, queue);
      claimDepth(relations.children(), depth + 1, visible, depthById, queue);
      claimDepth(relations.mates(), depth, visible, depthById, queue);
      claimDepth(relations.siblings(), depth, visible, depthById, queue);
    }
    for (final String id : visible) {
      depthById.putIfAbsent(id, 0);
    }
    return depthById;
  }

  private static void claimDepth(
      final List<String> ids,
      final int depth,
      final Set<String> visible,
      final Map<String, Integer> depthById,
      final Deque<String> queue) {
    for (final String id : ids) {
      if (visible.contains(id) && depthById.putIfAbsent(id, depth) == null) {
        queue.add(id);
      }
    }
  }

  public static Map<String, NodePosition> layout(
      final String focusId,
      final Set<String> expandedIds,
      final Map<String, Relations> relationsById,
      final Map<String, List<String>> parentIdsById) {
    final Set<String> visible = computeVisible(focusId, expandedIds, relationsById);
    final Map<String, Integer> depthById =
        assignDepths(focusId, expandedIds, relationsById, visible);
    final Map<String, Set<String>> parentsByChild = visibleParents(parentIdsById, visible);
    final Map<String, Set<String>> matesById = visibleMates(relationsById, visible);

    final TreeMap<Integer, List<String>> rows = new TreeMap<>();
    for (final String id : visible) {
      rows.computeIfAbsent(depthById.get(id), k -> new ArrayList<>()).add(id);
    }

    final Map<String, NodePosition> positions = new LinkedHashMap<>();
    final Map<String, Float> slotById = new HashMap<>();
    for (final Map.Entry<Integer, List<String>> row : rows.entrySet()) {
      final List<String> ordered = orderRow(row.getValue(), parentsByChild, matesById, slotById);
      for (int i = 0; i < ordered.size(); i++) {
        final float x = i - (ordered.size() - 1) / 2.0f;
        slotById.put(ordered.get(i), (float) i);
        positions.put(ordered.get(i), new NodePosition(x, row.getKey()));
      }
    }
    return positions;
  }

  static List<String> orderRow(
      final List<String> row,
      final Map<String, Set<String>> parentsByChild,
      final Map<String, Set<String>> matesById,
      final Map<String, Float> laidOutSlotById) {
    final Map<String, Float> keyById = new HashMap<>();
    final Map<String, List<String>> matesBesidePartner = new HashMap<>();
    final Set<String> besidePartner = new HashSet<>();
    for (int i = 0; i < row.size(); i++) {
      final String id = row.get(i);
      final Float parentKey = averageSlot(parentsByChild.get(id), laidOutSlotById);
      if (parentKey != null) {
        keyById.put(id, parentKey);
        continue;
      }
      final String partner =
          matesById.getOrDefault(id, Set.of()).stream()
              .filter(mate -> row.contains(mate) && !besidePartner.contains(mate))
              .findFirst()
              .orElse(null);
      if (partner == null) {
        keyById.put(id, (float) i);
      } else {
        besidePartner.add(id);
        matesBesidePartner.computeIfAbsent(partner, k -> new ArrayList<>()).add(id);
      }
    }
    final List<String> ordered = new ArrayList<>(row.size());
    row.stream()
        .filter(keyById::containsKey)
        .sorted(Comparator.comparing(keyById::get))
        .forEach(id -> appendWithMates(id, matesBesidePartner, ordered));
    return ordered;
  }

  private static void appendWithMates(
      final String id,
      final Map<String, List<String>> matesBesidePartner,
      final List<String> ordered) {
    ordered.add(id);
    for (final String mate : matesBesidePartner.getOrDefault(id, List.of())) {
      appendWithMates(mate, matesBesidePartner, ordered);
    }
  }

  private static Float averageSlot(
      final Set<String> parentIds, final Map<String, Float> laidOutSlotById) {
    if (parentIds == null || parentIds.isEmpty()) {
      return null;
    }
    float sum = 0;
    int count = 0;
    for (final String parentId : parentIds) {
      final Float slot = laidOutSlotById.get(parentId);
      if (slot != null) {
        sum += slot;
        count++;
      }
    }
    return count == 0 ? null : sum / count;
  }

  public static Map<String, Set<String>> visibleParents(
      final Map<String, List<String>> parentIdsById, final Set<String> visible) {
    final Map<String, Set<String>> parentsByChild = new LinkedHashMap<>();
    for (final String childId : visible) {
      for (final String parentId : parentIdsById.getOrDefault(childId, List.of())) {
        if (visible.contains(parentId)) {
          parentsByChild.computeIfAbsent(childId, k -> new LinkedHashSet<>()).add(parentId);
        }
      }
    }
    return parentsByChild;
  }

  public static Map<String, Set<String>> visibleMates(
      final Map<String, Relations> relationsById, final Set<String> visible) {
    final Map<String, Set<String>> matesById = new LinkedHashMap<>();
    for (final Map.Entry<String, Relations> entry : relationsById.entrySet()) {
      final String id = entry.getKey();
      if (!visible.contains(id)) {
        continue;
      }
      for (final String mateId : entry.getValue().mates()) {
        if (visible.contains(mateId)) {
          matesById.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(mateId);
          matesById.computeIfAbsent(mateId, k -> new LinkedHashSet<>()).add(id);
        }
      }
    }
    return matesById;
  }

  private static List<String> allRelated(final Relations relations) {
    final List<String> all =
        new ArrayList<>(
            relations.parents().size()
                + relations.mates().size()
                + relations.siblings().size()
                + relations.children().size());
    all.addAll(relations.parents());
    all.addAll(relations.mates());
    all.addAll(relations.siblings());
    all.addAll(relations.children());
    return all;
  }
}
