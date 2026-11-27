package com.grahambartley.dogsunleashed.block.entity;

import com.grahambartley.dogsunleashed.ModBlockEntities;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.DogFurnitureItem;
import com.grahambartley.dogsunleashed.wood.WoodPlanks;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
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

public class DogBedBlockEntity extends BlockEntity implements GeoBlockEntity, AssignedDogHolder {

  private static final String NBT_COLOR = "Color";
  private static final String NBT_ASSIGNED_DOG = "AssignedDog";

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private DyeColor color = DogFurnitureItem.DEFAULT_COLOUR;
  private WoodType wood = WoodPlanks.DEFAULT;
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
    this.syncToClients();
  }

  public WoodType getWood() {
    return this.wood;
  }

  public void setWood(final WoodType wood) {
    this.wood = wood;
    this.markDirty();
    this.syncToClients();
  }

  private void syncToClients() {
    if (this.world != null && !this.world.isClient) {
      this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }
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

  @Override
  protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.writeNbt(nbt, registryLookup);
    nbt.putInt(NBT_COLOR, this.color.getId());
    WoodPlanks.write(nbt, this.wood);
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
    this.wood = WoodPlanks.read(nbt);
    if (nbt.containsUuid(NBT_ASSIGNED_DOG)) {
      this.assignedDogUuid = nbt.getUuid(NBT_ASSIGNED_DOG);
    } else {
      this.assignedDogUuid = null;
    }
  }

  @Override
  protected void addComponents(ComponentMap.Builder builder) {
    super.addComponents(builder);
    builder.add(ModComponents.DOG_BED_COLOR, this.color);
    builder.add(ModComponents.WOOD, this.wood);
  }

  @Override
  protected void readComponents(BlockEntity.ComponentsAccess components) {
    super.readComponents(components);
    this.color =
        components.getOrDefault(ModComponents.DOG_BED_COLOR, DogFurnitureItem.DEFAULT_COLOUR);
    this.wood = components.getOrDefault(ModComponents.WOOD, WoodPlanks.DEFAULT);
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
