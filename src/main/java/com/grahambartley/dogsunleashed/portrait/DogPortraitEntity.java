package com.grahambartley.dogsunleashed.portrait;

import com.grahambartley.dogsunleashed.ModPortraits;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class DogPortraitEntity extends AbstractDecorationEntity {

  private static final TrackedData<NbtCompound> PORTRAIT =
      DataTracker.registerData(DogPortraitEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

  private static final String PORTRAIT_KEY = "portrait";
  private static final String FACING_KEY = "facing";

  private @Nullable DogPortrait portrait;

  public DogPortraitEntity(final EntityType<? extends DogPortraitEntity> type, final World world) {
    super(type, world);
  }

  public DogPortraitEntity(
      final World world, final BlockPos pos, final Direction facing, final DogPortrait portrait) {
    super(ModPortraits.DOG_PORTRAIT_ENTITY, world, pos);
    this.setPortrait(portrait);
    this.setFacing(facing);
  }

  public Optional<DogPortrait> portrait() {
    return Optional.ofNullable(this.portrait);
  }

  public PortraitSize size() {
    return this.portrait == null ? PortraitSize.SMALL : this.portrait.size();
  }

  private void setPortrait(final DogPortrait portrait) {
    this.dataTracker.set(PORTRAIT, portrait.toNbt());
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    builder.add(PORTRAIT, new NbtCompound());
  }

  @Override
  public void onTrackedDataSet(final TrackedData<?> data) {
    if (PORTRAIT.equals(data)) {
      this.portrait = DogPortrait.fromNbt(this.dataTracker.get(PORTRAIT)).orElse(null);
      this.updateAttachmentPosition();
    }
  }

  @Override
  protected Box calculateBoundingBox(final BlockPos pos, final Direction side) {
    return PortraitBounds.of(pos, side, this.size());
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    if (this.portrait != null) {
      nbt.put(PORTRAIT_KEY, this.portrait.toNbt());
    }
    nbt.putByte(FACING_KEY, (byte) this.facing.getHorizontal());
    super.writeCustomDataToNbt(nbt);
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    DogPortrait.fromNbt(nbt.getCompound(PORTRAIT_KEY)).ifPresent(this::setPortrait);
    this.facing = Direction.fromHorizontal(nbt.getByte(FACING_KEY));
    super.readCustomDataFromNbt(nbt);
    this.setFacing(this.facing);
  }

  @Override
  public ActionResult interact(final PlayerEntity player, final Hand hand) {
    if (!PortraitPoseRules.poses(
        this.portrait != null, hand == Hand.MAIN_HAND, player.getMainHandStack().isEmpty())) {
      return ActionResult.PASS;
    }
    if (!this.getWorld().isClient) {
      this.setPortrait(this.portrait.nextPose());
      this.playSound(SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, 1.0f, 1.0f);
      this.emitGameEvent(GameEvent.BLOCK_CHANGE, player);
    }
    return ActionResult.success(this.getWorld().isClient);
  }

  @Override
  public void onBreak(final @Nullable Entity breaker) {
    if (!this.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
      return;
    }
    this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0f, 1.0f);
    if (!(breaker instanceof PlayerEntity player && player.isInCreativeMode())) {
      this.dropStack(this.getPickBlockStack());
    }
  }

  @Override
  public void onPlace() {
    this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0f, 1.0f);
  }

  @Override
  public ItemStack getPickBlockStack() {
    return this.portrait == null
        ? new ItemStack(ModPortraits.BLANK_CANVAS)
        : ModPortraits.portraitStack(this.portrait);
  }

  @Override
  public void refreshPositionAndAngles(
      final double x, final double y, final double z, final float yaw, final float pitch) {
    this.setPosition(x, y, z);
  }

  @Override
  public void updateTrackedPositionAndAngles(
      final double x,
      final double y,
      final double z,
      final float yaw,
      final float pitch,
      final int interpolationSteps) {
    this.setPosition(x, y, z);
  }

  @Override
  public Vec3d getSyncedPos() {
    return Vec3d.of(this.attachedBlockPos);
  }

  @Override
  public Packet<ClientPlayPacketListener> createSpawnPacket(final EntityTrackerEntry entry) {
    return new EntitySpawnS2CPacket(this, this.facing.getId(), this.getAttachedBlockPos());
  }

  @Override
  public void onSpawnPacket(final EntitySpawnS2CPacket packet) {
    super.onSpawnPacket(packet);
    this.setFacing(Direction.byId(packet.getEntityData()));
  }
}
