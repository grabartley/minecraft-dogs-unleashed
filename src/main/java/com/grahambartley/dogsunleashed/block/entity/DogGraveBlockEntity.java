package com.grahambartley.dogsunleashed.block.entity;

import com.grahambartley.dogsunleashed.ModBlockEntities;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DogGraveBlockEntity extends BlockEntity implements GeoBlockEntity {

  private static final String NBT_DOG_UUID = "DogUuid";
  private static final String NBT_DOG_NAME = "DogName";
  private static final String NBT_FLOWER_COLOR = "FlowerColor";

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private UUID dogUuid = null;
  private String dogName = "";
  private DyeColor flowerColor = DyeColor.RED;

  public DogGraveBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.DOG_GRAVE, pos, state);
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

  @Override
  protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.writeNbt(nbt, registryLookup);
    if (this.dogUuid != null) {
      nbt.putUuid(NBT_DOG_UUID, this.dogUuid);
    }
    nbt.putString(NBT_DOG_NAME, this.dogName);
    nbt.putInt(NBT_FLOWER_COLOR, this.flowerColor.getId());
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
