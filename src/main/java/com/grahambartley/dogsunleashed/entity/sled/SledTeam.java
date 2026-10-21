package com.grahambartley.dogsunleashed.entity.sled;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.advancement.SledTeamCriterion;
import com.grahambartley.dogsunleashed.entity.DireWolfStamina;
import com.grahambartley.dogsunleashed.entity.DogSledHarness;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

public final class SledTeam {

  private static final String IDS = "Ids";

  private record Puller(UUID dog, UUID owner) {}

  private final SledEntity sled;
  private final List<Puller> pullers = new ArrayList<>();
  private final List<UnleashedDogEntity> present = new ArrayList<>();
  private int[] syncedIds = new int[0];

  SledTeam(final SledEntity sled) {
    this.sled = sled;
  }

  public int size() {
    return this.sled.getWorld().isClient ? this.syncedIds.length : this.pullers.size();
  }

  public int[] pullerIds() {
    return this.syncedIds.clone();
  }

  public List<UnleashedDogEntity> present() {
    return List.copyOf(this.present);
  }

  public boolean has(final UnleashedDogEntity dog) {
    if (this.sled.getWorld().isClient) {
      for (final int id : this.syncedIds) {
        if (id == dog.getId()) {
          return true;
        }
      }
      return false;
    }
    return this.indexOf(dog) >= 0;
  }

  public int slotOf(final UnleashedDogEntity dog) {
    return this.present.indexOf(dog);
  }

  public int indexOf(final UnleashedDogEntity dog) {
    for (int index = 0; index < this.pullers.size(); index++) {
      if (this.pullers.get(index).dog().equals(dog.getUuid())) {
        return index;
      }
    }
    return -1;
  }

  public double widestDog() {
    return this.present.stream().mapToDouble(Entity::getWidth).max().orElse(0.0);
  }

  public int hitchLeashedDogs(final PlayerEntity player) {
    final Box reach = this.sled.getBoundingBox().expand(SledHitchRules.LEAD_REACH);
    final List<UnleashedDogEntity> leashed =
        this.sled
            .getWorld()
            .getEntitiesByClass(
                UnleashedDogEntity.class, reach, dog -> dog.getLeashHolder() == player);
    int hitched = 0;
    for (final UnleashedDogEntity dog : leashed) {
      if (SledHitchRules.room(this.pullers.size()) == 0 || !canPull(dog, player)) {
        continue;
      }
      dog.detachLeash(true, false);
      this.pullers.add(new Puller(dog.getUuid(), player.getUuid()));
      this.present.add(dog);
      dog.getComponents().get(DogSledHarness.class).hitchTo(this.sled);
      hitched++;
    }
    if (hitched > 0) {
      this.syncIds();
      if (player instanceof ServerPlayerEntity serverPlayer) {
        SledTeamCriterion.INSTANCE.trigger(serverPlayer, this.pullers.size());
      }
    }
    return hitched;
  }

  public boolean hasDogsToHitch(final PlayerEntity player) {
    final Box reach = this.sled.getBoundingBox().expand(SledHitchRules.LEAD_REACH);
    return !this.sled
        .getWorld()
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            reach,
            dog -> dog.getLeashHolder() == player && this.canPull(dog, player))
        .isEmpty();
  }

  public int unhitchOwnedBy(final PlayerEntity player) {
    int released = 0;
    for (final UnleashedDogEntity dog : List.copyOf(this.present)) {
      if (Objects.equals(this.ownerOf(dog), player.getUuid())) {
        this.remove(dog);
        dog.attachLeash(player, true);
        released++;
      }
    }
    if (released > 0) {
      this.syncIds();
    }
    return released;
  }

  public void dropLost(final UnleashedDogEntity dog) {
    if (this.indexOf(dog) < 0) {
      return;
    }
    this.remove(dog);
    dog.dropStack(new ItemStack(Items.LEAD));
    this.syncIds();
  }

  void releaseAll() {
    for (final UnleashedDogEntity dog : List.copyOf(this.present)) {
      this.remove(dog);
      dog.dropStack(new ItemStack(Items.LEAD));
    }
    final int unresolved = this.pullers.size();
    this.pullers.clear();
    for (int lead = 0; lead < unresolved; lead++) {
      this.sled.dropStack(new ItemStack(Items.LEAD));
    }
    this.syncIds();
  }

  void tick(final boolean sprinting) {
    if (!(this.sled.getWorld() instanceof ServerWorld world)) {
      return;
    }
    this.present.clear();
    for (final Puller puller : List.copyOf(this.pullers)) {
      if (!(world.getEntity(puller.dog()) instanceof UnleashedDogEntity dog)) {
        continue;
      }
      if (!SledHitchRules.staysHitched(
          dog.isAlive(),
          puller.owner().equals(dog.getOwnerUuid()),
          dog.isLeashed(),
          dog.distanceTo(this.sled))) {
        this.pullers.remove(puller);
        dog.dropStack(new ItemStack(Items.LEAD));
        continue;
      }
      dog.getComponents().get(DogSledHarness.class).hitchTo(this.sled);
      this.present.add(dog);
    }
    this.syncIds();
    this.tickStamina(sprinting);
  }

  void clientTick() {
    for (final int id : this.syncedIds) {
      if (this.sled.getWorld().getEntityById(id) instanceof UnleashedDogEntity dog) {
        dog.getComponents().get(DogSledHarness.class).hitchTo(this.sled);
      }
    }
  }

  public boolean canSprint() {
    return SledTeamStamina.canSprint(this.staminaStates());
  }

  void onTrackedIds(final NbtCompound tracked) {
    this.syncedIds = tracked.getIntArray(IDS);
  }

  void writeNbt(final NbtCompound nbt) {
    final NbtList list = new NbtList();
    for (final Puller puller : this.pullers) {
      final NbtCompound entry = new NbtCompound();
      entry.putUuid(ModNbtKeys.SLED_PULLER_DOG, puller.dog());
      entry.putUuid(ModNbtKeys.SLED_PULLER_OWNER, puller.owner());
      list.add(entry);
    }
    nbt.put(ModNbtKeys.SLED_PULLERS, list);
  }

  void readNbt(final NbtCompound nbt) {
    this.pullers.clear();
    final NbtList list = nbt.getList(ModNbtKeys.SLED_PULLERS, NbtElement.COMPOUND_TYPE);
    for (int index = 0; index < list.size() && index < SledFormation.MAX_TEAM; index++) {
      final NbtCompound entry = list.getCompound(index);
      if (entry.containsUuid(ModNbtKeys.SLED_PULLER_DOG)
          && entry.containsUuid(ModNbtKeys.SLED_PULLER_OWNER)) {
        this.pullers.add(
            new Puller(
                entry.getUuid(ModNbtKeys.SLED_PULLER_DOG),
                entry.getUuid(ModNbtKeys.SLED_PULLER_OWNER)));
      }
    }
  }

  private void tickStamina(final boolean sprinting) {
    final float drainScale = SledTeamStamina.drainScale(this.present.size());
    final List<DireWolfStamina.State> states = new ArrayList<>();
    final List<Float> capacities = new ArrayList<>();
    for (final UnleashedDogEntity dog : this.present) {
      final DogSledHarness harness = dog.getComponents().get(DogSledHarness.class);
      final DireWolfStamina.State next =
          DireWolfStamina.next(
              harness.stamina(),
              harness.capacity(),
              true,
              sprinting,
              DogsUnleashed.SERVER_CONFIG.direWolf().stamina(),
              drainScale);
      harness.storeStamina(next);
      states.add(next);
      capacities.add(harness.capacity());
    }
    this.sled.syncStamina(
        SledTeamStamina.fraction(states, capacities), !SledTeamStamina.canSprint(states));
  }

  private List<DireWolfStamina.State> staminaStates() {
    return this.present.stream()
        .map(dog -> dog.getComponents().get(DogSledHarness.class).stamina())
        .toList();
  }

  private @Nullable UUID ownerOf(final UnleashedDogEntity dog) {
    final int index = this.indexOf(dog);
    return index < 0 ? null : this.pullers.get(index).owner();
  }

  private void remove(final UnleashedDogEntity dog) {
    final int index = this.indexOf(dog);
    if (index >= 0) {
      this.pullers.remove(index);
    }
    this.present.remove(dog);
  }

  private void syncIds() {
    final int[] ids = new int[this.pullers.size()];
    int count = 0;
    for (final Puller puller : this.pullers) {
      for (final UnleashedDogEntity dog : this.present) {
        if (dog.getUuid().equals(puller.dog())) {
          ids[count++] = dog.getId();
        }
      }
    }
    final int[] resolved = Arrays.copyOf(ids, count);
    if (!Arrays.equals(resolved, this.syncedIds)) {
      this.syncedIds = resolved;
      final NbtCompound tracked = new NbtCompound();
      tracked.put(IDS, new NbtIntArray(resolved));
      this.sled.syncPullers(tracked);
    }
  }

  private boolean canPull(final UnleashedDogEntity dog, final PlayerEntity player) {
    return this.indexOf(dog) < 0
        && !dog.getComponents().get(DogSledHarness.class).isHitched()
        && dog.isTamed()
        && dog.isOwner(player)
        && dog.isAlive()
        && SledHitchRules.canPull(dog.getBreed(), dog.isBaby(), dog.getGrowth().stage());
  }
}
