package com.grahambartley.dogsunleashed.render.armour;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;

public final class WolfArmourTextures {

  private final List<List<CubeGeometry.Face>> cubes;
  private final int width;
  private final int height;
  private final Map<String, Identifier> byName = new HashMap<>();

  public WolfArmourTextures(
      final List<List<CubeGeometry.Face>> cubes, final int width, final int height) {
    this.cubes = List.copyOf(cubes);
    this.width = width;
    this.height = height;
  }

  public Identifier layer(final ArmourLook look, final ArmourPainter.Layer layer) {
    return this.cached(
        look.name().toLowerCase(Locale.ROOT) + "_" + layer.name().toLowerCase(Locale.ROOT),
        () -> ArmourPainter.paint(this.cubes, this.width, this.height, look, layer, false));
  }

  public Identifier cracks(final int level) {
    return this.cached(
        "cracks_" + level, () -> ArmourPainter.cracks(this.cubes, this.width, this.height, level));
  }

  private Identifier cached(final String name, final Supplier<int[]> painter) {
    return this.byName.computeIfAbsent(
        name,
        key ->
            ArmourTextures.upload(
                Identifier.of(MOD_ID, "armour/wolf/" + key),
                painter.get(),
                this.width,
                this.height));
  }
}
