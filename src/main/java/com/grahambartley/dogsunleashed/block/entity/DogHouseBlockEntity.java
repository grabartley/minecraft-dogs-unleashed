package com.grahambartley.dogsunleashed.block.entity;

import com.grahambartley.dogsunleashed.ModBlockEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DogHouseBlockEntity extends BlockEntity implements GeoBlockEntity, AssignedDogHolder {

  private static final String NBT_ASSIGNED_DOG = "AssignedDog";

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private UUID assignedDogUuid = null;

  public DogHouseBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.DOG_HOUSE, pos, state);
  }

  @Override
  public boolean hasAssignedDog() {
    return this.assignedDogUuid != null;
  }

  @Override
  public UUID getAssignedDogUuid() {
    return this.assignedDogUuid;
  }

  @Override
  public void setAssignedDog(UnleashedDogEntity dog) {
    this.assignedDogUuid = dog.getUuid();
    this.markDirty();
    this.syncToClients();
  }

  @Override
  public void clearAssignedDog(World world) {
    if (this.assignedDogUuid != null && world != null) {
      final UnleashedDogEntity dog = getAssignedDog(world);
      if (dog != null) {
        dog.getSleepController().clearAssignedBed();
      }
    }
    this.assignedDogUuid = null;
    this.markDirty();
    this.syncToClients();
  }

  @Override
  public UnleashedDogEntity getAssignedDog(World world) {
    if (this.assignedDogUuid == null || world == null) {
      return null;
    }

    if (world instanceof ServerWorld serverWorld) {
      final Entity entity = serverWorld.getEntity(this.assignedDogUuid);
      if (entity instanceof UnleashedDogEntity dog) {
        return dog;
      }
    }
    return null;
  }

  /** The renderer draws the occupant from the client copy, so assignment changes must be synced. */
  private void syncToClients() {
    if (this.world != null && !this.world.isClient) {
      this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }
  }

  @Override
  protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.writeNbt(nbt, registryLookup);
    if (this.assignedDogUuid != null) {
      nbt.putUuid(NBT_ASSIGNED_DOG, this.assignedDogUuid);
    }
  }

  @Override
  protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.readNbt(nbt, registryLookup);
    if (nbt.containsUuid(NBT_ASSIGNED_DOG)) {
      this.assignedDogUuid = nbt.getUuid(NBT_ASSIGNED_DOG);
    } else {
      this.assignedDogUuid = null;
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
