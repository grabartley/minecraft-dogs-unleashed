package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class DogDeathWatch extends DogComponent {

  private static final int CHECK_INTERVAL_TICKS = 10;

  private final UnleashedDogEntity dog;
  private boolean watching;
  private boolean calledOut;
  private DogCommand commandBefore = DogCommand.FOLLOW;

  DogDeathWatch(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public boolean isKeepingWatch() {
    return this.watching;
  }

  public void keepWatchAt(final BlockPos spot, final RegistryKey<World> dimension) {
    if (!this.watching) {
      this.commandBefore = DeathWatchRules.commandAfterWatch(this.dog.getCommand());
    }
    this.watching = true;
    this.calledOut = false;
    this.dog.getCommandController().guardAt(spot, dimension);
  }

  public boolean hasCalledOut() {
    return this.calledOut;
  }

  public void standDown() {
    if (this.watching) {
      this.dog.getCommandController().apply(this.commandBefore);
    }
  }

  void end() {
    this.watching = false;
    this.calledOut = false;
  }

  @Override
  void tick() {
    if (!this.watching || this.dog.age % CHECK_INTERVAL_TICKS != 0) {
      return;
    }
    final BlockPos spot = this.dog.getCommandController().getAnchorPos();
    final LivingEntity owner = this.dog.getOwner();
    final boolean ownerAliveHere = spot != null && owner != null && owner.isAlive();
    final double ownerDistance =
        ownerAliveHere ? owner.squaredDistanceTo(Vec3d.ofBottomCenter(spot)) : Double.MAX_VALUE;
    switch (DeathWatchRules.next(ownerAliveHere, ownerDistance, this.calledOut)) {
      case CALL_OUT -> {
        this.calledOut = true;
        this.dog.getVocalization().callOut();
      }
      case STAND_DOWN -> {
        this.standDown();
        this.dog.getCommandController().acknowledge();
      }
      case KEEP_WATCH -> {}
    }
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    if (this.watching) {
      nbt.putBoolean(ModNbtKeys.DEATH_WATCH, true);
      nbt.putBoolean(ModNbtKeys.DEATH_WATCH_CALLED_OUT, this.calledOut);
      nbt.putInt(ModNbtKeys.DEATH_WATCH_COMMAND_BEFORE, this.commandBefore.id());
    }
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    this.watching = nbt.getBoolean(ModNbtKeys.DEATH_WATCH);
    this.calledOut = nbt.getBoolean(ModNbtKeys.DEATH_WATCH_CALLED_OUT);
    this.commandBefore =
        nbt.contains(ModNbtKeys.DEATH_WATCH_COMMAND_BEFORE, NbtElement.NUMBER_TYPE)
            ? DeathWatchRules.commandAfterWatch(
                DogCommand.fromId(nbt.getInt(ModNbtKeys.DEATH_WATCH_COMMAND_BEFORE)))
            : DogCommand.FOLLOW;
  }
}
