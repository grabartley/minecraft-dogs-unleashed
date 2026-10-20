package com.grahambartley.dogsunleashed.render.statue;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.DogStatue;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.grahambartley.dogsunleashed.block.entity.DogStatueBlockEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.EntityType;
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

  private final Map<DogStatueBlockEntity, Figure> figures = new WeakHashMap<>();

  public @Nullable Figure of(
      final DogStatueBlockEntity base, final DogStatue statue, final Direction facing) {
    final Figure current = this.figures.get(base);
    if (current != null && current.statue().equals(statue)) {
      face(current.dog(), facing);
      return current;
    }
    final World world = base.getWorld();
    final UnleashedDogEntity dog = world == null ? null : carve(world, base.getPos(), statue);
    if (dog == null) {
      this.figures.remove(base);
      return null;
    }
    face(dog, facing);
    final Figure figure = new Figure(statue, dog);
    this.figures.put(base, figure);
    return figure;
  }

  private static @Nullable UnleashedDogEntity carve(
      final World world, final BlockPos pos, final DogStatue statue) {
    final EntityType<UnleashedDogEntity> type = ModEntities.getDogEntityType(statue.look().breed());
    final UnleashedDogEntity dog = type == null ? null : type.create(world);
    if (dog == null) {
      return null;
    }
    dog.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    dog.setBaby(statue.look().baby());
    dog.readCustomDataFromNbt(statue.look().toNbt());
    final DogStatuePose pose = statue.shownPose();
    final boolean sitting = pose != DogStatuePose.STAND;
    dog.setSitting(sitting);
    dog.setInSittingPose(sitting);
    dog.getAnimationState().setHowling(pose == DogStatuePose.HOWL);
    return dog;
  }

  private static void face(final UnleashedDogEntity dog, final Direction facing) {
    final float yaw = facing.asRotation();
    dog.setYaw(yaw);
    dog.setBodyYaw(yaw);
    dog.setHeadYaw(yaw);
    dog.prevYaw = yaw;
    dog.prevBodyYaw = yaw;
    dog.prevHeadYaw = yaw;
    dog.setPitch(0.0f);
    dog.prevPitch = 0.0f;
  }
}
