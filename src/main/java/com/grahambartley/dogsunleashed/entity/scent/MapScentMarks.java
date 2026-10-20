package com.grahambartley.dogsunleashed.entity.scent;

import java.util.List;
import java.util.Map;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class MapScentMarks {

  private MapScentMarks() {}

  public static List<ScentLandmark> of(final ItemStack stack) {
    if (!stack.isOf(Items.FILLED_MAP)) {
      return List.of();
    }
    return of(
        stack.getOrDefault(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT));
  }

  public static List<ScentLandmark> of(final MapDecorationsComponent decorations) {
    return decorations.decorations().entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> ScentLandmark.at(entry.getValue().x(), entry.getValue().z()))
        .toList();
  }
}
