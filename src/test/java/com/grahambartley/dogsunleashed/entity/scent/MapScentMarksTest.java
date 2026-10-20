package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Map;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.item.map.MapDecorationTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinecraftBootstrapExtension.class)
class MapScentMarksTest {

  @Test
  @DisplayName("a buried treasure map's red X becomes the column to dig")
  void readsTheTreasureMark() {
    final MapDecorationsComponent decorations =
        MapDecorationsComponent.DEFAULT.with(
            "+",
            new MapDecorationsComponent.Decoration(
                MapDecorationTypes.RED_X, 1240.0, -376.0, 180.0F));
    assertEquals(List.of(new ScentLandmark(1240, -376)), MapScentMarks.of(decorations));
  }

  @Test
  @DisplayName("every marker on an explorer map is read, in a stable order")
  void readsEveryMarker() {
    final MapDecorationsComponent decorations =
        new MapDecorationsComponent(
            Map.of(
                "b",
                new MapDecorationsComponent.Decoration(
                    MapDecorationTypes.MONUMENT, -800.5, 64.2, 180.0F),
                "a",
                new MapDecorationsComponent.Decoration(
                    MapDecorationTypes.MANSION, 3000.0, 2900.0, 180.0F)));
    assertEquals(
        List.of(new ScentLandmark(3000, 2900), new ScentLandmark(-801, 64)),
        MapScentMarks.of(decorations));
  }

  @Test
  @DisplayName("a map with no markers gives nothing to follow")
  void nothingWithoutMarkers() {
    assertEquals(List.of(), MapScentMarks.of(MapDecorationsComponent.DEFAULT));
  }
}
