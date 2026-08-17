package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import com.grahambartley.dogsunleashed.entity.fetch.FetchProjectileEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

public final class DogPlaySession {

  static final String NO_ACTIVE_FETCH_TYPE = "";

  private static final int FETCH_DETECTION_XZ_RANGE = 128;
  private static final int FETCH_DETECTION_Y_RANGE = 64;

  private static final Map<UUID, UUID> ACTIVE_PLAY_SESSIONS = new HashMap<>();

  private final UnleashedDogEntity dog;

  private boolean inPlayMode = false;
  private BlockPos activeFetchBlockPos = null;

  DogPlaySession(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  /** Whether a dog counts as mid-fetch, and whether answering needs a projectile scan. */
  public enum FetchStatus {
    IDLE,
    FETCHING,
    SCAN_FOR_PROJECTILE
  }

  public static FetchStatus fetchStatus(
      final boolean inPlayMode,
      final boolean carryingFetchItem,
      final boolean hasFetchTarget,
      final boolean hasPlayPartner) {
    if (!inPlayMode) {
      return FetchStatus.IDLE;
    }
    if (carryingFetchItem || hasFetchTarget) {
      return FetchStatus.FETCHING;
    }
    if (!hasPlayPartner) {
      return FetchStatus.IDLE;
    }
    return FetchStatus.SCAN_FOR_PROJECTILE;
  }

  public static boolean isAnyDogInPlayModeFor(final UUID playerUuid) {
    return ACTIVE_PLAY_SESSIONS.containsKey(playerUuid);
  }

  /**
   * Client-safe mirror of {@link #isAnyDogInPlayModeFor}. {@code ACTIVE_PLAY_SESSIONS} only lives
   * on the logical server, so on a dedicated server the client evaluates the play-mode gate by
   * scanning tracked dogs for a synced play partner matching the player. The scan range mirrors
   * fetch detection; dogs beyond client tracking range are invisible here, which only skips the
   * cosmetic prediction, never the server-authoritative throw.
   */
  public static boolean isAnyNearbyDogInPlayModeFor(final PlayerEntity player) {
    return !player
        .getWorld()
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            fetchDetectionBox(player),
            dog -> player.getUuid().equals(dog.getPlayPartnerPlayerUuid()))
        .isEmpty();
  }

  public static boolean isAnyDogInPlayMode() {
    return !ACTIVE_PLAY_SESSIONS.isEmpty();
  }

  /**
   * Clears the JVM-global active play sessions map. The map otherwise lives for the lifetime of the
   * JVM; in singleplayer it survives world reloads and in gametest batches it leaks state between
   * tests. Called by test {@code @BeforeBatch} hooks and by {@code SERVER_STOPPED}.
   */
  public static void clearActivePlaySessions() {
    ActivePlaySessions.clearAll(ACTIVE_PLAY_SESSIONS);
  }

  private static Box fetchDetectionBox(final Entity entity) {
    return entity
        .getBoundingBox()
        .expand(FETCH_DETECTION_XZ_RANGE, FETCH_DETECTION_Y_RANGE, FETCH_DETECTION_XZ_RANGE);
  }

  public boolean isInPlayMode() {
    return this.inPlayMode;
  }

  @Nullable
  public BlockPos getActiveFetchBlockPos() {
    return this.activeFetchBlockPos;
  }

  public void setActiveFetchBlockPos(final @Nullable BlockPos pos) {
    this.activeFetchBlockPos = pos;
  }

  public void startPlayMode(final PlayerEntity player, final FetchItemType fetchItemType) {
    final UUID priorDogUuid =
        ActivePlaySessions.takeover(ACTIVE_PLAY_SESSIONS, player.getUuid(), this.dog.getUuid());
    if (priorDogUuid != null) {
      this.endPlayModeForDog(priorDogUuid);
    }
    this.inPlayMode = true;
    this.dog.setPlayPartnerPlayerUuid(player.getUuid());
    this.activeFetchBlockPos = null;
    this.dog.setActiveFetchType(fetchItemType);
    this.dog.getCommandController().demoteSitToFollow();
  }

  public void endPlayMode() {
    ActivePlaySessions.clear(
        ACTIVE_PLAY_SESSIONS,
        this.inPlayMode,
        this.dog.getPlayPartnerPlayerUuid(),
        this.dog.getUuid());
    this.inPlayMode = false;
    this.dog.setPlayPartnerPlayerUuid(null);
    this.activeFetchBlockPos = null;
    this.dog.setActiveFetchType(null);
    this.dog.setCarryingFetchItem(false);
  }

  void endOnRemoval(final RemovalReason reason) {
    if (ActivePlaySessions.shouldEndOnRemoval(reason)) {
      this.endPlayMode();
    }
  }

  void endOtherNearbyPlayModes(final PlayerEntity player) {
    for (final UnleashedDogEntity other :
        this.dog
            .getWorld()
            .getEntitiesByClass(
                UnleashedDogEntity.class,
                fetchDetectionBox(this.dog),
                candidate ->
                    candidate != this.dog
                        && candidate.getPlaySession().isInPlayMode()
                        && player.getUuid().equals(candidate.getPlayPartnerPlayerUuid()))) {
      other.getPlaySession().endPlayMode();
    }
  }

  public boolean isActivelyFetching() {
    final UUID partnerUuid = this.dog.getPlayPartnerPlayerUuid();
    return switch (fetchStatus(
        this.inPlayMode,
        this.dog.isCarryingFetchItem(),
        this.activeFetchBlockPos != null,
        partnerUuid != null)) {
      case IDLE -> false;
      case FETCHING -> true;
      case SCAN_FOR_PROJECTILE -> this.hasPartnerProjectileNearby(partnerUuid);
    };
  }

  private boolean hasPartnerProjectileNearby(final UUID partnerUuid) {
    return !this.dog
        .getWorld()
        .getEntitiesByClass(
            Entity.class,
            fetchDetectionBox(this.dog),
            entity ->
                entity instanceof FetchProjectileEntity
                    && entity instanceof ProjectileEntity projectile
                    && projectile.getOwner() instanceof PlayerEntity player
                    && partnerUuid.equals(player.getUuid()))
        .isEmpty();
  }

  private void endPlayModeForDog(final UUID priorDogUuid) {
    if (!(this.dog.getWorld() instanceof ServerWorld serverWorld)) {
      return;
    }
    for (final ServerWorld world : serverWorld.getServer().getWorlds()) {
      final Entity prior = world.getEntity(priorDogUuid);
      if (prior instanceof UnleashedDogEntity priorDog) {
        priorDog.getPlaySession().endPlayMode();
        return;
      }
    }
  }

  static @Nullable FetchItemType fetchTypeFromId(final String activeFetchTypeId) {
    if (activeFetchTypeId.isEmpty()) {
      return null;
    }
    return FetchTypes.forId(Identifier.of(activeFetchTypeId));
  }

  static String fetchTypeIdOf(final @Nullable FetchItemType activeFetchType) {
    return activeFetchType != null ? activeFetchType.id().toString() : NO_ACTIVE_FETCH_TYPE;
  }

  void writeNbt(final NbtCompound nbt) {
    nbt.putBoolean(ModNbtKeys.CARRYING_BALL, this.dog.isCarryingFetchItem());
    final String activeFetchTypeId = this.dog.getActiveFetchTypeId();
    if (!activeFetchTypeId.isEmpty()) {
      nbt.putString(ModNbtKeys.ACTIVE_FETCH_TYPE_ID, activeFetchTypeId);
    }
    final ItemStack carried = this.dog.getCarriedFetchItemStack();
    if (!carried.isEmpty()) {
      ItemStack.CODEC
          .encodeStart(this.dog.getWorld().getRegistryManager().getOps(NbtOps.INSTANCE), carried)
          .result()
          .ifPresent(tag -> nbt.put(ModNbtKeys.CARRIED_FETCH_ITEM_STACK, tag));
    }
  }

  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.CARRYING_BALL)) {
      this.dog.setCarryingFetchItem(nbt.getBoolean(ModNbtKeys.CARRYING_BALL));
    }
    if (nbt.contains(ModNbtKeys.ACTIVE_FETCH_TYPE_ID, NbtElement.STRING_TYPE)) {
      this.dog.setActiveFetchTypeId(nbt.getString(ModNbtKeys.ACTIVE_FETCH_TYPE_ID));
    }
    if (nbt.contains(ModNbtKeys.CARRIED_FETCH_ITEM_STACK)) {
      ItemStack.CODEC
          .parse(
              this.dog.getWorld().getRegistryManager().getOps(NbtOps.INSTANCE),
              nbt.get(ModNbtKeys.CARRIED_FETCH_ITEM_STACK))
          .result()
          .ifPresent(this.dog::setCarriedFetchItemStack);
    }
  }
}
