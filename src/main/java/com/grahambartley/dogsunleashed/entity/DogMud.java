package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DogMud extends DogComponent {

  private static final int SPATTER_PARTICLE_COUNT = 12;
  private static final double SPATTER_SPREAD = 0.3;

  private final UnleashedDogEntity dog;
  private DogMudRules.MudState state = DogMudRules.CLEAN;

  DogMud(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public boolean isMuddy() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.MUDDY);
  }

  public void makeMuddy() {
    this.apply(new DogMudRules.MudState(true, 0, 0, 0));
  }

  public void wash() {
    this.apply(DogMudRules.CLEAN);
  }

  @Override
  void tick() {
    final World world = this.dog.getWorld();
    final BlockPos pos = this.dog.getBlockPos();
    final BlockState feet = world.getBlockState(pos);
    final BlockState ground = this.dog.getSteppingBlockState();
    if (this.state.muddy() && this.washInCauldron(world, pos, feet, ground)) {
      return;
    }
    final boolean inWater = this.dog.isTouchingWater();
    final boolean swamp = inWater && world.getBiome(pos).isIn(ConventionalBiomeTags.IS_SWAMP);
    final boolean dirtying =
        DogMudGround.dirties(
            idOf(feet), idOf(ground), swamp, this.dog.isOnGround() && world.hasRain(pos));
    this.apply(
        DogMudRules.next(
            this.state,
            DogsUnleashed.SERVER_CONFIG.gameplay().muddyDogs(),
            dirtying,
            inWater && !swamp));
  }

  void spatterShake(final ServerWorld world) {
    for (final PlayerEntity player : world.getPlayers()) {
      if (DogMudRules.spatters(
          this.isMuddy(), player.distanceTo(this.dog), this.dog.getRandom().nextInt(100))) {
        world.spawnParticles(
            new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.MUD.getDefaultState()),
            player.getX(),
            player.getBodyY(0.5),
            player.getZ(),
            SPATTER_PARTICLE_COUNT,
            SPATTER_SPREAD,
            SPATTER_SPREAD,
            SPATTER_SPREAD,
            0.0);
      }
    }
  }

  private boolean washInCauldron(
      final World world, final BlockPos pos, final BlockState feet, final BlockState ground) {
    if (isWaterCauldron(feet)) {
      LeveledCauldronBlock.decrementFluidLevel(feet, world, pos);
    } else if (isWaterCauldron(ground)) {
      LeveledCauldronBlock.decrementFluidLevel(ground, world, this.dog.getSteppingPos());
    } else {
      return false;
    }
    this.wash();
    return true;
  }

  private static boolean isWaterCauldron(final BlockState state) {
    return state.isOf(Blocks.WATER_CAULDRON) && state.get(LeveledCauldronBlock.LEVEL) > 0;
  }

  private static String idOf(final BlockState state) {
    return Registries.BLOCK.getId(state.getBlock()).toString();
  }

  private void apply(final DogMudRules.MudState next) {
    this.state = next;
    if (this.isMuddy() != next.muddy()) {
      this.dog.getDataTracker().set(UnleashedDogEntity.MUDDY, next.muddy());
    }
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    nbt.putBoolean(ModNbtKeys.MUDDY, this.state.muddy());
    nbt.putInt(ModNbtKeys.MUDDY_TICKS, this.state.muddyTicks());
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    this.apply(fromSave(nbt));
  }

  static DogMudRules.MudState fromSave(final NbtCompound nbt) {
    return nbt.getBoolean(ModNbtKeys.MUDDY)
        ? new DogMudRules.MudState(true, 0, Math.max(0, nbt.getInt(ModNbtKeys.MUDDY_TICKS)), 0)
        : DogMudRules.CLEAN;
  }
}
