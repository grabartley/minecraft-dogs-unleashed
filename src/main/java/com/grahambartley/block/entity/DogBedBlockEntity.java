package com.grahambartley.block.entity;

import com.grahambartley.ModBlockEntities;
import com.grahambartley.entity.UnleashedDogEntity;
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
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DogBedBlockEntity extends BlockEntity implements GeoBlockEntity {

  private static final String NBT_COLOR = "Color";
  private static final String NBT_ASSIGNED_DOG = "AssignedDog";

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private DyeColor color = DyeColor.RED;
  private UUID assignedDogUuid = null;

  public DogBedBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.DOG_BED, pos, state);
  }

  public DyeColor getColor() {
    return this.color;
  }

  public void setColor(DyeColor color) {
    this.color = color;
    this.markDirty();
    if (this.world != null) {
      this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }
  }

  public boolean hasAssignedDog() {
    return this.assignedDogUuid != null;
  }

  public UUID getAssignedDogUuid() {
    return this.assignedDogUuid;
  }

  public void setAssignedDog(UnleashedDogEntity dog) {
    this.assignedDogUuid = dog.getUuid();
    this.markDirty();
  }

  public void clearAssignedDog(World world) {
    if (this.assignedDogUuid != null && world != null) {
      final UnleashedDogEntity dog = getAssignedDog(world);
      if (dog != null) {
        dog.clearAssignedBed();
      }
    }
    this.assignedDogUuid = null;
    this.markDirty();
  }

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

  @Override
  protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.writeNbt(nbt, registryLookup);
    nbt.putInt(NBT_COLOR, this.color.getId());
    if (this.assignedDogUuid != null) {
      nbt.putUuid(NBT_ASSIGNED_DOG, this.assignedDogUuid);
    }
  }

  @Override
  protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.readNbt(nbt, registryLookup);
    if (nbt.contains(NBT_COLOR)) {
      this.color = DyeColor.byId(nbt.getInt(NBT_COLOR));
    }
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
