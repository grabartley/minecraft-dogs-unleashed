package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.config.DireWolfPackSettings;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public final class DogPack {

  private final UnleashedDogEntity dog;

  private @Nullable UUID packId;
  private long nextHuntTime;
  private @Nullable UUID threatId;
  private long threatUntil;

  DogPack(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  static WildPackData startOrJoin(final @Nullable EntityData entityData) {
    return entityData instanceof WildPackData pack ? pack : new WildPackData(UUID.randomUUID());
  }

  void joinFrom(final @Nullable EntityData entityData) {
    if (entityData instanceof WildPackData pack) {
      this.packId = pack.packId();
    }
  }

  public @Nullable UUID id() {
    return this.packId;
  }

  public boolean isWildMember() {
    return this.packId != null && !this.dog.isTamed();
  }

  public boolean isGrown() {
    return this.dog.getGrowth().applies()
        ? WildPackRules.isGrownStage(this.dog.getGrowth().stage())
        : !this.dog.isBaby();
  }

  public boolean staysOutOfFights() {
    return this.isWildMember() && !this.isGrown();
  }

  public boolean isPackmate(final @Nullable Entity other) {
    return other instanceof UnleashedDogEntity wolf
        && this.packId != null
        && wolf.getPack().isWildMember()
        && this.packId.equals(wolf.getPack().id());
  }

  public List<UnleashedDogEntity> packmatesWithin(final double range) {
    return this.dog
        .getWorld()
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            this.dog.getBoundingBox().expand(range),
            wolf -> wolf.isAlive() && (wolf == this.dog || this.isPackmate(wolf)));
  }

  public boolean mayHunt() {
    return this.keepsHunting()
        && WildPackRules.huntCooldownOver(this.dog.getWorld().getTime(), this.nextHuntTime);
  }

  public boolean keepsHunting() {
    return this.dog.getBreed().nature().huntsInPacks()
        && this.isWildMember()
        && this.isGrown()
        && settings().hunt();
  }

  public void recordKill() {
    if (this.isWildMember()) {
      this.nextHuntTime = this.dog.getWorld().getTime() + WildPackRules.HUNT_COOLDOWN_TICKS;
    }
  }

  void onHurtBy(final @Nullable Entity attacker) {
    if (attacker instanceof PlayerEntity player
        && this.isWildMember()
        && isValidThreat(player)
        && this.dog.getWorld() instanceof ServerWorld world) {
      WildPackGrudges.get(world.getServer()).record(this.packId, player.getUuid());
    }
    this.rallyAgainst(attacker);
  }

  public boolean bearsGrudge(final UUID playerId) {
    return this.isWildMember()
        && this.dog.getWorld() instanceof ServerWorld world
        && WildPackGrudges.get(world.getServer()).holds(this.packId, playerId);
  }

  public boolean bearsGrudgeAgainst(final @Nullable Entity entity) {
    return entity instanceof PlayerEntity player && this.bearsGrudge(player.getUuid());
  }

  public boolean huntsDown(final @Nullable Entity entity) {
    return settings().defend()
        && this.isGrown()
        && entity instanceof PlayerEntity player
        && isValidThreat(player)
        && this.bearsGrudge(player.getUuid());
  }

  public List<UnleashedDogEntity> pupsWithin(final double range) {
    return this.packmatesWithin(range).stream()
        .filter(wolf -> wolf != this.dog && wolf.getTrust().canBeWon())
        .toList();
  }

  private void rallyAgainst(final @Nullable Entity attacker) {
    if (!settings().defend()
        || !this.isWildMember()
        || !(attacker instanceof LivingEntity threat)
        || !isValidThreat(threat)) {
      return;
    }
    for (final UnleashedDogEntity packmate : this.packmatesWithin(WildPackRules.DEFENCE_RANGE)) {
      packmate.getPack().face(threat);
    }
  }

  public boolean isThreat(final @Nullable Entity entity) {
    return this.remembersThreat(entity) || this.bearsGrudgeAgainst(entity);
  }

  private boolean remembersThreat(final @Nullable Entity entity) {
    return entity != null
        && entity.getUuid().equals(this.threatId)
        && WildPackRules.remembersThreat(this.dog.getWorld().getTime(), this.threatUntil);
  }

  void writeNbt(final NbtCompound nbt) {
    if (this.packId != null) {
      nbt.putUuid(ModNbtKeys.WILD_PACK_ID, this.packId);
    }
  }

  void readNbt(final NbtCompound nbt) {
    this.packId =
        nbt.containsUuid(ModNbtKeys.WILD_PACK_ID) ? nbt.getUuid(ModNbtKeys.WILD_PACK_ID) : null;
  }

  private void face(final LivingEntity threat) {
    this.threatId = threat.getUuid();
    this.threatUntil = this.dog.getWorld().getTime() + WildPackRules.THREAT_MEMORY_TICKS;
    if (!this.isGrown()) {
      return;
    }
    this.dog.setTarget(threat);
    if (threat instanceof PlayerEntity) {
      this.dog.setAngryAt(threat.getUuid());
      this.dog.chooseRandomAngerTime();
    }
  }

  private boolean isValidThreat(final LivingEntity threat) {
    return threat.isAlive()
        && !this.isPackmate(threat)
        && !(threat instanceof PlayerEntity player
            && (player.isCreative() || player.isSpectator()));
  }

  private static DireWolfPackSettings settings() {
    return DogsUnleashed.SERVER_CONFIG.direWolf().packs();
  }
}
