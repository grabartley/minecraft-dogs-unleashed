package com.grahambartley.dogsunleashed.render.statue;

import com.grahambartley.dogsunleashed.block.DogStatue;
import com.grahambartley.dogsunleashed.block.entity.DogStatueBlockEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.portrait.DogFacing;
import com.grahambartley.dogsunleashed.portrait.DogLook;
import com.grahambartley.dogsunleashed.portrait.DogLooks;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class StatueFigures {

  public static final class Figure {
    private final DogStatue statue;
    private final UnleashedDogEntity dog;
    private boolean started;

    private Figure(final DogStatue statue, final UnleashedDogEntity dog) {
      this.statue = statue;
      this.dog = dog;
    }

    public DogStatue statue() {
      return this.statue;
    }

    public UnleashedDogEntity dog() {
      return this.dog;
    }

    boolean started() {
      return this.started;
    }

    void markStarted() {
      this.started = true;
    }
  }

  private final Map<BlockPos, Figure> figures = new HashMap<>();
  private @Nullable World world;

  public @Nullable Figure of(
      final DogStatueBlockEntity base, final DogStatue statue, final Direction facing) {
    final World world = base.getWorld();
    if (world != this.world) {
      this.figures.clear();
      this.world = world;
    }
    final Figure current = this.figures.get(base.getPos());
    if (current != null && current.statue().equals(statue)) {
      DogFacing.face(current.dog(), facing.asRotation());
      return current;
    }
    final UnleashedDogEntity dog = world == null ? null : carve(world, base.getPos(), statue);
    if (dog == null) {
      this.figures.remove(base.getPos());
      return null;
    }
    DogFacing.face(dog, facing.asRotation());
    final Figure figure = new Figure(statue, dog);
    this.figures.put(base.getPos(), figure);
    return figure;
  }

  public void forget(final DogStatueBlockEntity base) {
    this.figures.remove(base.getPos());
  }

  private static @Nullable UnleashedDogEntity carve(
      final World world, final BlockPos pos, final DogStatue statue) {
    final UnleashedDogEntity dog = DogLooks.create(world, DogLook.plain(statue.look()));
    if (dog == null) {
      return null;
    }
    dog.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    statue.shownPose().applyTo(dog);
    return dog;
  }
}
