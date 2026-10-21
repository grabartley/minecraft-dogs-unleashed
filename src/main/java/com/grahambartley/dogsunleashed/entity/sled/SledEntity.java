package com.grahambartley.dogsunleashed.entity.sled;

import com.grahambartley.dogsunleashed.ModItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SledEntity extends Entity implements GeoEntity {

  public static final float STEP_HEIGHT = 1.0f;
  public static final int HURT_WOBBLE_TICKS = 10;
  static final float BREAK_DAMAGE = 40.0f;
  static final float DAMAGE_PER_HIT = 10.0f;
  static final int LERP_STEPS = 3;
  static final double MOVING_THRESHOLD = 0.0025;
  private static final RawAnimation BOUNCE = RawAnimation.begin().thenLoop("animation.sled.bounce");
  private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sled.idle");

  static final TrackedData<NbtCompound> PULLERS =
      DataTracker.registerData(SledEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
  static final TrackedData<Float> STAMINA =
      DataTracker.registerData(SledEntity.class, TrackedDataHandlerRegistry.FLOAT);
  static final TrackedData<Boolean> STAMINA_LOCKED_OUT =
      DataTracker.registerData(SledEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  static final TrackedData<Integer> WOBBLE_TICKS =
      DataTracker.registerData(SledEntity.class, TrackedDataHandlerRegistry.INTEGER);

  private final SledTeam team = new SledTeam(this);
  private final SledDrive drive = new SledDrive(this);
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private float damageTaken;
  private int lerpTicks;
  private double lerpX;
  private double lerpY;
  private double lerpZ;
  private float lerpYaw;

  public SledEntity(final EntityType<? extends SledEntity> type, final World world) {
    super(type, world);
    this.intersectionChecked = true;
  }

  public SledTeam getTeam() {
    return this.team;
  }

  public SledDrive getDrive() {
    return this.drive;
  }

  public float staminaFraction() {
    return this.dataTracker.get(STAMINA);
  }

  public boolean isStaminaLockedOut() {
    return this.dataTracker.get(STAMINA_LOCKED_OUT);
  }

  public int wobbleTicks() {
    return this.dataTracker.get(WOBBLE_TICKS);
  }

  public void placeFacing(final float yaw) {
    this.setYaw(yaw);
    this.prevYaw = yaw;
    this.drive.face(yaw);
  }

  public @Nullable PlayerEntity rider() {
    return this.getFirstPassenger() instanceof PlayerEntity player ? player : null;
  }

  void syncPullers(final NbtCompound tracked) {
    this.dataTracker.set(PULLERS, tracked);
  }

  void syncStamina(final float fraction, final boolean lockedOut) {
    if (this.staminaFraction() != fraction) {
      this.dataTracker.set(STAMINA, fraction);
    }
    if (this.isStaminaLockedOut() != lockedOut) {
      this.dataTracker.set(STAMINA_LOCKED_OUT, lockedOut);
    }
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    builder.add(PULLERS, new NbtCompound());
    builder.add(STAMINA, 0.0f);
    builder.add(STAMINA_LOCKED_OUT, false);
    builder.add(WOBBLE_TICKS, 0);
  }

  @Override
  public void onTrackedDataSet(final TrackedData<?> data) {
    super.onTrackedDataSet(data);
    if (PULLERS.equals(data)) {
      this.team.onTrackedIds(this.dataTracker.get(PULLERS));
    }
  }

  @Override
  public void tick() {
    super.tick();
    if (this.wobbleTicks() > 0) {
      this.dataTracker.set(WOBBLE_TICKS, this.wobbleTicks() - 1);
    }
    if (this.getWorld().isClient) {
      this.lerpToTracked();
      this.team.clientTick();
      return;
    }
    this.damageTaken = Math.max(0.0f, this.damageTaken - 1.0f);
    this.team.tick(this.drive.isSprinting());
    final Vec3d before = this.getPos();
    this.drive.tick(this.team.present(), this.rider(), this.team.canSprint());
    if (this.rider() instanceof ServerPlayerEntity player) {
      SledMileage.record(player, this, this.getPos().subtract(before).horizontalLength());
    }
    this.checkBlockCollision();
  }

  @Override
  public ActionResult interact(final PlayerEntity player, final Hand hand) {
    if (player.shouldCancelInteraction()) {
      if (this.getWorld().isClient) {
        return ActionResult.SUCCESS;
      }
      return this.team.unhitchOwnedBy(player) > 0 ? ActionResult.CONSUME : ActionResult.PASS;
    }
    if (this.team.hasLeashedCandidates(player)) {
      if (this.getWorld().isClient) {
        return ActionResult.SUCCESS;
      }
      if (this.team.hitchLeashedDogs(player) == 0) {
        player.sendMessage(
            Text.translatable(
                    SledHitchRules.room(this.team.size()) == 0
                        ? "message.dogs-unleashed.sled.full"
                        : "message.dogs-unleashed.sled.cannot_pull")
                .formatted(Formatting.GRAY),
            true);
      }
      return ActionResult.CONSUME;
    }
    if (this.hasPassengers() || player.hasVehicle()) {
      return ActionResult.PASS;
    }
    if (!this.getWorld().isClient) {
      return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
    }
    return ActionResult.SUCCESS;
  }

  @Override
  public boolean damage(final DamageSource source, final float amount) {
    if (this.getWorld().isClient || this.isRemoved()) {
      return true;
    }
    if (this.isInvulnerableTo(source)) {
      return false;
    }
    this.dataTracker.set(WOBBLE_TICKS, HURT_WOBBLE_TICKS);
    this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
    final boolean creative =
        source.getAttacker() instanceof PlayerEntity attacker
            && attacker.getAbilities().creativeMode;
    this.damageTaken += amount * DAMAGE_PER_HIT;
    if (creative) {
      this.discard();
    } else if (this.damageTaken > BREAK_DAMAGE) {
      this.breakApart();
    }
    return true;
  }

  private void breakApart() {
    this.kill();
    if (this.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
      final ItemStack stack = new ItemStack(ModItems.SLED);
      stack.set(DataComponentTypes.CUSTOM_NAME, this.getCustomName());
      this.dropStack(stack);
    }
  }

  @Override
  public void remove(final RemovalReason reason) {
    if (!this.getWorld().isClient && reason.shouldDestroy()) {
      this.team.releaseAll();
    }
    super.remove(reason);
  }

  @Override
  protected void removePassenger(final Entity passenger) {
    super.removePassenger(passenger);
    this.drive.setSprintHeld(false);
  }

  @Override
  protected boolean canAddPassenger(final Entity passenger) {
    return !this.hasPassengers() && passenger instanceof PlayerEntity;
  }

  @Override
  public @Nullable LivingEntity getControllingPassenger() {
    return null;
  }

  @Override
  protected void updatePassengerPosition(
      final Entity passenger, final Entity.PositionUpdater positionUpdater) {
    super.updatePassengerPosition(passenger, positionUpdater);
    final float turn = MathHelper.wrapDegrees(this.getYaw() - this.prevYaw);
    passenger.setYaw(passenger.getYaw() + turn);
    passenger.setHeadYaw(passenger.getHeadYaw() + turn);
    if (passenger instanceof LivingEntity living) {
      living.setBodyYaw(this.getYaw());
    }
  }

  @Override
  public Vec3d updatePassengerForDismount(final LivingEntity passenger) {
    final Vec3d side =
        SledFormation.offset(new SledFormation.Slot(0.0, this.getWidth()), this.getYaw(), 0.0);
    return this.getPos().add(side);
  }

  @Override
  public boolean canHit() {
    return !this.isRemoved();
  }

  @Override
  public boolean isCollidable() {
    return false;
  }

  @Override
  public boolean isPushable() {
    return false;
  }

  @Override
  public float getStepHeight() {
    return STEP_HEIGHT;
  }

  @Override
  protected double getGravity() {
    return SledDrive.GRAVITY;
  }

  @Override
  public ItemStack getPickBlockStack() {
    return new ItemStack(ModItems.SLED);
  }

  @Override
  public void updateTrackedPositionAndAngles(
      final double x,
      final double y,
      final double z,
      final float yaw,
      final float pitch,
      final int interpolationSteps) {
    this.lerpX = x;
    this.lerpY = y;
    this.lerpZ = z;
    this.lerpYaw = yaw;
    this.lerpTicks = LERP_STEPS;
  }

  @Override
  public double getLerpTargetX() {
    return this.lerpTicks > 0 ? this.lerpX : this.getX();
  }

  @Override
  public double getLerpTargetY() {
    return this.lerpTicks > 0 ? this.lerpY : this.getY();
  }

  @Override
  public double getLerpTargetZ() {
    return this.lerpTicks > 0 ? this.lerpZ : this.getZ();
  }

  @Override
  public float getLerpTargetYaw() {
    return this.lerpTicks > 0 ? this.lerpYaw : this.getYaw();
  }

  private void lerpToTracked() {
    if (this.lerpTicks > 0) {
      this.lerpPosAndRotation(
          this.lerpTicks, this.lerpX, this.lerpY, this.lerpZ, this.lerpYaw, this.getPitch());
      this.lerpTicks--;
    }
  }

  public boolean isMoving() {
    return this.getPos().subtract(this.prevX, this.prevY, this.prevZ).horizontalLengthSquared()
        > MOVING_THRESHOLD;
  }

  @Override
  protected void readCustomDataFromNbt(final NbtCompound nbt) {
    this.team.readNbt(nbt);
    this.drive.readNbt(nbt);
  }

  @Override
  protected void writeCustomDataToNbt(final NbtCompound nbt) {
    this.team.writeNbt(nbt);
    this.drive.writeNbt(nbt);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "movement",
            2,
            state -> state.setAndContinue(state.getAnimatable().isMoving() ? BOUNCE : IDLE)));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
