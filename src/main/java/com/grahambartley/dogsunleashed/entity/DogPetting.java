package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.gesture.PlayerGesture;
import com.grahambartley.dogsunleashed.gesture.PlayerGestures;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class DogPetting extends DogComponent {

  static final double OTHER_PAT_SEARCH_BLOCKS = 16.0;
  private static final float LOOK_TURN_DEGREES = 30.0f;

  private final UnleashedDogEntity dog;
  private long lastPettedAt = PettingRules.NEVER;
  private long requestedAt = PettingRules.NEVER;
  private @Nullable UUID petterUuid;
  private Vec3d askedFrom = Vec3d.ZERO;

  DogPetting(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  @Nullable
  ActionResult tryPet(final PlayerEntity player, final Hand hand) {
    if (!PettingRules.isPetGesture(
        player.isSneaking(),
        hand == Hand.MAIN_HAND,
        player.getStackInHand(hand).isEmpty(),
        standsSteady(player))) {
      return null;
    }
    if (!PettingRules.welcomes(
            this.stateFor(player), this.now(), this.lastPettedAt, this.requestedAt)
        || (this.dog.isInSittingPose() && !this.canReachNeck(player))
        || this.pettingAnotherDog(player)) {
      return null;
    }
    this.petterUuid = player.getUuid();
    this.askedFrom = player.getPos();
    this.requestedAt = this.now();
    this.show(player, PlayerGesture.INVITE);
    return ActionResult.CONSUME;
  }

  public Optional<PlayerEntity> approachingPetter() {
    return PettingRules.isApproaching(this.now(), this.requestedAt)
        ? this.petter()
        : Optional.empty();
  }

  public Optional<PlayerEntity> currentPetter() {
    return PettingRules.isBeingPetted(this.now(), this.lastPettedAt)
        ? this.petter()
        : Optional.empty();
  }

  public PetSpot.Place spotBeside(final PlayerEntity player) {
    return PetSpot.of(
        player.getX(),
        player.getZ(),
        player.getHeadYaw(),
        player.getMainArm() == Arm.RIGHT,
        this.headReach(),
        this.dog.getWidth() / 2.0,
        this.kneelFor(player),
        PetStance.bows(this.headTopAbove(player)));
  }

  public boolean isAtHandOf(final PlayerEntity player) {
    final PetSpot.Place spot = this.spotBeside(player);
    return PetSpot.arrived(Math.hypot(spot.x() - this.dog.getX(), spot.z() - this.dog.getZ()))
        || this.canReachNeck(player);
  }

  @Override
  void tick() {
    if (this.petterUuid == null) {
      return;
    }
    final boolean asking = this.requestedAt != PettingRules.NEVER;
    final boolean petting = PettingRules.isBeingPetted(this.now(), this.lastPettedAt);
    if (!asking && !petting) {
      this.petterUuid = null;
      return;
    }
    final Optional<PlayerEntity> petter = this.petter();
    if (petter.isEmpty() || this.callsItOff(petter.get(), asking)) {
      petter.ifPresent(this::release);
      this.forget();
      return;
    }
    final boolean atHand = this.isAtHandOf(petter.get());
    if (atHand || petting) {
      this.face(petter.get());
    }
    if (asking
        && atHand
        && PettingRules.hasSettled(this.now(), this.requestedAt)
        && PettingRules.facesSpot(
            this.dog.getBodyYaw(), this.spotBeside(petter.get()).faceYawDegrees())) {
      this.requestedAt = PettingRules.NEVER;
      final PettingRules.DogState state = this.stateFor(petter.get());
      if (state.calm() && state.free()) {
        this.begin(petter.get());
      } else {
        this.release(petter.get());
        this.forget();
      }
    }
  }

  boolean isPettedBy(final PlayerEntity player) {
    return player.getUuid().equals(this.petterUuid)
        && (this.requestedAt != PettingRules.NEVER
            || PettingRules.isBeingPetted(this.now(), this.lastPettedAt));
  }

  private boolean callsItOff(final PlayerEntity petter, final boolean asking) {
    return !petter.isAlive()
        || !petter.getMainHandStack().isEmpty()
        || !standsSteady(petter)
        || PettingRules.walkedAway(petter.getPos().distanceTo(this.askedFrom))
        || this.turnedAwayFrom(petter)
        || (asking && !PettingRules.isApproaching(this.now(), this.requestedAt));
  }

  private void forget() {
    this.requestedAt = PettingRules.NEVER;
    this.lastPettedAt = PettingRules.NEVER;
    this.petterUuid = null;
  }

  private void face(final PlayerEntity petter) {
    final PetSpot.Place spot = this.spotBeside(petter);
    final float turned = PettingRules.turnedInToward(this.dog.getBodyYaw(), spot.faceYawDegrees());
    this.dog.setYaw(turned);
    this.dog.setBodyYaw(turned);
    this.dog
        .getLookControl()
        .lookAt(
            spot.faceX(),
            petter.getY() + this.dog.getStandingEyeHeight(),
            spot.faceZ(),
            LOOK_TURN_DEGREES,
            LOOK_TURN_DEGREES);
  }

  private boolean turnedAwayFrom(final PlayerEntity petter) {
    final Vec3d neck = this.neck();
    return PettingRules.turnedAway(
        petter.getHeadYaw(), neck.x - petter.getX(), neck.z - petter.getZ());
  }

  private boolean canReachNeck(final PlayerEntity player) {
    final Vec3d neck = this.neck();
    return PetSpot.reachesNeck(
        Math.hypot(neck.x - player.getX(), neck.z - player.getZ()),
        this.dog.getY() - player.getY(),
        this.kneelFor(player));
  }

  private Vec3d neck() {
    final float yaw = this.dog.getBodyYaw() * MathHelper.RADIANS_PER_DEGREE;
    final double reach = this.headReach().neck();
    return new Vec3d(
        this.dog.getX() - MathHelper.sin(yaw) * reach,
        this.dog.getY(),
        this.dog.getZ() + MathHelper.cos(yaw) * reach);
  }

  private boolean pettingAnotherDog(final PlayerEntity player) {
    return !this.dog
        .getWorld()
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            player.getBoundingBox().expand(OTHER_PAT_SEARCH_BLOCKS),
            other ->
                other != this.dog && other.getComponents().get(DogPetting.class).isPettedBy(player))
        .isEmpty();
  }

  private float kneelFor(final PlayerEntity player) {
    return PetStance.kneelWeight(this.headTopAbove(player));
  }

  private float headTopAbove(final PlayerEntity player) {
    return (float) (this.dog.getY() - player.getY()) + this.headReach().top();
  }

  private String pettedClipFor(final PlayerEntity player) {
    return PetStance.bows(this.headTopAbove(player))
        ? DogAnimationKeys.PETTED_BOW
        : DogAnimationKeys.PETTED;
  }

  private DogHeadReach headReach() {
    return DogHeadReach.of(this.dog);
  }

  private void begin(final PlayerEntity player) {
    this.lastPettedAt = this.now();
    final String clip = this.pettedClipFor(player);
    this.dog.triggerAnim(clip, clip);
    this.dog.getAmbienceEffects().startTailWag();
    this.dog.getAmbienceEffects().burstHearts();
    this.dog.playSound(
        SoundEvents.ENTITY_WOLF_PANT,
        DogsUnleashed.SERVER_CONFIG.sound().barkVolume(),
        this.dog.getVocalization().getBarkPitch());
    if (this.dog.isOwner(player)) {
      this.dog.getComponents().get(DogBond.class).reward(DogBondRules.Source.PET);
    }
    this.show(player, PlayerGesture.PAT);
  }

  private void release(final PlayerEntity player) {
    this.dog.stopTriggeredAnim(DogAnimationKeys.PETTED, DogAnimationKeys.PETTED);
    this.dog.stopTriggeredAnim(DogAnimationKeys.PETTED_BOW, DogAnimationKeys.PETTED_BOW);
    this.show(player, PlayerGesture.RELEASE);
  }

  private void show(final PlayerEntity player, final PlayerGesture gesture) {
    if (player instanceof ServerPlayerEntity serverPlayer) {
      PlayerGestures.show(serverPlayer, Hand.MAIN_HAND, gesture, this.dog.getId());
    }
  }

  private Optional<PlayerEntity> petter() {
    return this.petterUuid == null
        ? Optional.empty()
        : Optional.ofNullable(this.dog.getWorld().getPlayerByUuid(this.petterUuid));
  }

  private long now() {
    return this.dog.getWorld().getTime();
  }

  private static boolean standsSteady(final PlayerEntity player) {
    return player.isOnGround()
        && !player.hasVehicle()
        && !player.getAbilities().flying
        && !player.isSwimming()
        && !player.isFallFlying()
        && !player.isTouchingWater();
  }

  private PettingRules.DogState stateFor(final PlayerEntity player) {
    return new PettingRules.DogState(
        this.dog.getSleepController().isAsleep(),
        this.dog.getTarget() != null && this.dog.getTarget().isAlive(),
        this.dog.shouldAngerAt(player),
        DogIdleness.isOccupied(this.dog),
        this.dog.getComponents().get(DogTrickPerformance.class).isPerforming(),
        this.dog.hasVehicle(),
        this.dog.hasPassengers(),
        this.dog.getComponents().get(DogSledHarness.class).isHitched(),
        this.dog.getAnimationState().isHowling(),
        this.dog.getAnimationState().isShaking(),
        this.dog.getGrowl().isGrowling());
  }
}
