package com.grahambartley.dogsunleashed.block.entity;

import com.grahambartley.dogsunleashed.ModBlockEntities;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.DogStatue;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class DogStatueBlockEntity extends BlockEntity {

  private static final String NBT_STATUE = "Statue";

  private @Nullable DogStatue statue;

  public DogStatueBlockEntity(final BlockPos pos, final BlockState state) {
    super(ModBlockEntities.DOG_STATUE, pos, state);
  }

  public @Nullable DogStatue statue() {
    return this.statue;
  }

  public void carve(final DogStatue carving) {
    this.setStatue(carving.withPose(DogStatuePose.SIT));
  }

  public boolean cyclePose() {
    if (this.statue == null) {
      return false;
    }
    this.setStatue(this.statue.nextPose());
    return true;
  }

  private void setStatue(final @Nullable DogStatue statue) {
    this.statue = statue;
    this.markDirty();
    if (this.world != null) {
      this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }
  }

  @Override
  protected void writeNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.writeNbt(nbt, registries);
    if (this.statue != null) {
      DogStatue.CODEC
          .encodeStart(NbtOps.INSTANCE, this.statue)
          .result()
          .ifPresent(encoded -> nbt.put(NBT_STATUE, encoded));
    }
  }

  @Override
  protected void readNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.readNbt(nbt, registries);
    this.statue =
        nbt.contains(NBT_STATUE)
            ? DogStatue.CODEC.parse(NbtOps.INSTANCE, nbt.get(NBT_STATUE)).result().orElse(null)
            : null;
  }

  @Override
  protected void readComponents(final BlockEntity.ComponentsAccess components) {
    super.readComponents(components);
    this.statue = components.get(ModComponents.DOG_STATUE);
  }

  @Override
  protected void addComponents(final ComponentMap.Builder builder) {
    super.addComponents(builder);
    if (this.statue != null) {
      builder.add(ModComponents.DOG_STATUE, this.statue);
    }
  }

  @Override
  public void removeFromCopiedStackNbt(final NbtCompound nbt) {
    nbt.remove(NBT_STATUE);
  }

  @Override
  public Packet<ClientPlayPacketListener> toUpdatePacket() {
    return BlockEntityUpdateS2CPacket.create(this);
  }

  @Override
  public NbtCompound toInitialChunkDataNbt(final RegistryWrapper.WrapperLookup registries) {
    return this.createNbt(registries);
  }
}
