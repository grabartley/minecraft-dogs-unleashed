package com.grahambartley.block.entity;

import com.grahambartley.ModBlockEntities;
import com.grahambartley.ModComponents;
import com.grahambartley.entity.fetch.FetchCarriedItemProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
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

public class FrisbeeBlockEntity extends BlockEntity
    implements GeoBlockEntity, FetchCarriedItemProvider {

  private static final String NBT_COLOR = "Color";

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private DyeColor color = DyeColor.WHITE;

  public FrisbeeBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.FRISBEE, pos, state);
  }

  public DyeColor getColor() {
    return this.color;
  }

  @Override
  public void enrichCarriedItemStack(ItemStack stack) {
    stack.set(ModComponents.FRISBEE_COLOR, this.color);
  }

  @Override
  public void restoreFromCarriedItemStack(ItemStack stack) {
    this.setColor(stack.getOrDefault(ModComponents.FRISBEE_COLOR, DyeColor.WHITE));
  }

  public void setColor(DyeColor color) {
    this.color = color;
    this.markDirty();
    if (this.world != null) {
      this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }
  }

  @Override
  protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.writeNbt(nbt, registryLookup);
    nbt.putInt(NBT_COLOR, this.color.getId());
  }

  @Override
  protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.readNbt(nbt, registryLookup);
    if (nbt.contains(NBT_COLOR)) {
      this.color = DyeColor.byId(nbt.getInt(NBT_COLOR));
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
    return cache;
  }
}
