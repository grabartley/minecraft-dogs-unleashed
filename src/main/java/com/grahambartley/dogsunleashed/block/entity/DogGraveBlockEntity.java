package com.grahambartley.dogsunleashed.block.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModBlockEntities;
import com.grahambartley.dogsunleashed.block.DogGraveBlock;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DogGraveBlockEntity extends BlockEntity implements GeoBlockEntity {

  private static final String NBT_DOG_UUID = "DogUuid";
  private static final String NBT_DOG_NAME = "DogName";
  private static final String NBT_FLOWER_COLOR = "FlowerColor";
  private static final String NBT_HAS_TOTEM = "HasTotem";
  private static final String NBT_TOTEM_INSTALLER_ID = "TotemInstallerId";

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private UUID dogUuid = null;
  private String dogName = "";
  private DyeColor flowerColor = DyeColor.RED;
  private boolean hasTotem = false;
  private UUID totemInstallerId = null;

  public DogGraveBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.DOG_GRAVE, pos, state);
  }

  /**
   * Graves placed before the upper half existed are a lone base with air where the top of the
   * headstone should be targetable. They heal on load, deferred a tick so no block changes happen
   * mid chunk-load. A grave whose upper cell is occupied (a legacy lightning rod, a player build)
   * is left alone.
   */
  @Override
  public void setWorld(final World world) {
    super.setWorld(world);
    if (world instanceof ServerWorld serverWorld) {
      DogsUnleashed.runNextTick(() -> this.healMissingUpperHalf(serverWorld));
    }
  }

  private void healMissingUpperHalf(final ServerWorld world) {
    if (this.isRemoved() || !world.isChunkLoaded(this.pos)) {
      return;
    }
    final BlockState state = world.getBlockState(this.pos);
    if (!(state.getBlock() instanceof DogGraveBlock)
        || state.get(DogGraveBlock.HALF) != DoubleBlockHalf.LOWER) {
      return;
    }
    if (world.getBlockState(this.pos.up()).isAir()) {
      world.setBlockState(this.pos.up(), state.with(DogGraveBlock.HALF, DoubleBlockHalf.UPPER));
    }
  }

  public UUID getDogUuid() {
    return this.dogUuid;
  }

  public void setDogUuid(UUID dogUuid) {
    this.dogUuid = dogUuid;
    this.markDirty();
    if (this.world != null) {
      this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }
  }

  public String getDogName() {
    return this.dogName;
  }

  public void setDogName(String dogName) {
    this.dogName = dogName;
    this.markDirty();
    if (this.world != null) {
      this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }
  }

  public DyeColor getFlowerColor() {
    return this.flowerColor;
  }

  public void setFlowerColor(DyeColor flowerColor) {
    this.flowerColor = flowerColor;
    this.markDirty();
    if (this.world != null) {
      this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }
  }

  public boolean hasTotem() {
    return this.hasTotem;
  }

  /** Remembered so the resurrection advancement lands on whoever set the ritual up. */
  public UUID getTotemInstallerId() {
    return this.totemInstallerId;
  }

  public void installTotem(UUID installerId) {
    this.setTotem(true, installerId);
  }

  public void clearTotem() {
    this.setTotem(false, null);
  }

  private void setTotem(boolean hasTotem, UUID totemInstallerId) {
    this.hasTotem = hasTotem;
    this.totemInstallerId = totemInstallerId;
    this.markDirty();
    if (this.world != null) {
      this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }
  }

  @Override
  protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.writeNbt(nbt, registryLookup);
    if (this.dogUuid != null) {
      nbt.putUuid(NBT_DOG_UUID, this.dogUuid);
    }
    nbt.putString(NBT_DOG_NAME, this.dogName);
    nbt.putInt(NBT_FLOWER_COLOR, this.flowerColor.getId());
    nbt.putBoolean(NBT_HAS_TOTEM, this.hasTotem);
    if (this.totemInstallerId != null) {
      nbt.putUuid(NBT_TOTEM_INSTALLER_ID, this.totemInstallerId);
    }
  }

  @Override
  protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.readNbt(nbt, registryLookup);
    if (nbt.containsUuid(NBT_DOG_UUID)) {
      this.dogUuid = nbt.getUuid(NBT_DOG_UUID);
    } else {
      this.dogUuid = null;
    }
    if (nbt.contains(NBT_DOG_NAME)) {
      this.dogName = nbt.getString(NBT_DOG_NAME);
    }
    if (nbt.contains(NBT_FLOWER_COLOR)) {
      this.flowerColor = DyeColor.byId(nbt.getInt(NBT_FLOWER_COLOR));
    }
    this.hasTotem = nbt.getBoolean(NBT_HAS_TOTEM);
    this.totemInstallerId =
        nbt.containsUuid(NBT_TOTEM_INSTALLER_ID) ? nbt.getUuid(NBT_TOTEM_INSTALLER_ID) : null;
  }

  @Override
  public Packet<ClientPlayPacketListener> toUpdatePacket() {
    return BlockEntityUpdateS2CPacket.create(this);
  }

  @Override
  public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
    return createNbt(registryLookup);
  }

  @Override
  public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
