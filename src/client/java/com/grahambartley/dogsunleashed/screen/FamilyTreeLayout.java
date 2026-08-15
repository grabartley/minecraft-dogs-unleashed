package com.grahambartley.dogsunleashed.screen;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Pure layout math for the family tree canvas: which dogs are visible given the expanded set, which
 * generation row each one sits in, and where in the row it goes. Positions are in abstract units
 * (one unit of x per node slot, one unit of y per generation); the screen multiplies by pixel
 * spacing and applies zoom and pan. Everything here is deterministic and free of GL state so it is
 * unit-testable.
 */
public final class FamilyTreeLayout {

  /** One dog's direct family as plain id lists, mirroring the sync payload's four lists. */
  public record Relations(
      List<String> parents, List<String> mates, List<String> siblings, List<String> children) {}

  /** A laid-out node: {@code x} in row-slot units, {@code depth} in generations from the focus. */
  public record NodePosition(float x, int depth) {}

  private FamilyTreeLayout() {}

  /**
   * The dogs shown on the canvas: the focus dog plus, for every expanded dog whose relations are
   * known, its direct connections. Collapsing a dog naturally prunes relatives only reachable
   * through it because visibility is re-derived from scratch.
   */
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

  /**
   * Assigns each visible dog a generation depth relative to the focus (negative above, positive
   * below), walking outward from the focus through expanded dogs: parents one row up, children one
   * row down, mates and siblings on the same row. The first relation to reach a dog wins, which
   * keeps depths stable as the tree grows.
   */
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

  /**
   * Lays out the visible dogs as a human family tree: generations in rows, each row ordered by the
   * average position of a dog's parents in the row above so children hang under their parents, and
   * dogs who married into the family (no visible parents) pulled next to their mate. Rows are
   * centered on x = 0.
   */
  public static Map<String, NodePosition> layout(
      final String focusId,
      final Set<String> expandedIds,
      final Map<String, Relations> relationsById) {
    final Set<String> visible = computeVisible(focusId, expandedIds, relationsById);
    final Map<String, Integer> depthById =
        assignDepths(focusId, expandedIds, relationsById, visible);
    final Map<String, Set<String>> parentsByChild = visibleParents(relationsById, visible);
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

  /**
   * Orders one generation row. The sort key is the average slot index of the dog's visible parents
   * in the rows already laid out; a dog with no visible parents borrows its mate's key (nudged
   * right) so couples sit together; anything else keeps its discovery order. The sort is stable, so
   * ties preserve discovery order.
   */
  static List<String> orderRow(
      final List<String> row,
      final Map<String, Set<String>> parentsByChild,
      final Map<String, Set<String>> matesById,
      final Map<String, Float> laidOutSlotById) {
    final Map<String, Float> keyById = new HashMap<>();
    for (int i = 0; i < row.size(); i++) {
      keyById.put(row.get(i), (float) i);
    }
    for (final String id : row) {
      final Float parentKey = averageSlot(parentsByChild.get(id), laidOutSlotById);
      if (parentKey != null) {
        keyById.put(id, parentKey);
      }
    }
    for (final String id : row) {
      if (averageSlot(parentsByChild.get(id), laidOutSlotById) != null) {
        continue;
      }
      for (final String mate : matesById.getOrDefault(id, Set.of())) {
        final Float mateKey = keyById.get(mate);
        if (mateKey != null && row.contains(mate)) {
          keyById.put(id, mateKey + 0.01f);
          break;
        }
      }
    }
    final List<String> ordered = new ArrayList<>(row);
    ordered.sort((a, b) -> Float.compare(keyById.getOrDefault(a, 0f), keyById.getOrDefault(b, 0f)));
    return ordered;
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

  /** Parent edges among visible dogs, merged from every known relation record. */
  public static Map<String, Set<String>> visibleParents(
      final Map<String, Relations> relationsById, final Set<String> visible) {
    final Map<String, Set<String>> parentsByChild = new LinkedHashMap<>();
    for (final Map.Entry<String, Relations> entry : relationsById.entrySet()) {
      final String id = entry.getKey();
      if (visible.contains(id)) {
        for (final String parentId : entry.getValue().parents()) {
          if (visible.contains(parentId)) {
            parentsByChild.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(parentId);
          }
        }
      }
      for (final String childId : entry.getValue().children()) {
        if (visible.contains(childId) && visible.contains(id)) {
          parentsByChild.computeIfAbsent(childId, k -> new LinkedHashSet<>()).add(id);
        }
      }
    }
    return parentsByChild;
  }

  /** Mate edges among visible dogs, merged from every known relation record, symmetric. */
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
