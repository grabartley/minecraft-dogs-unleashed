package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class DogEntityNbt {

  private final UnleashedDogEntity dog;

  DogEntityNbt(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static DogCommand commandForLegacySave(final boolean sitting) {
    return sitting ? DogCommand.SIT : DogCommand.FOLLOW;
  }

  public static int variantOrdinalOr(
      final NbtCompound nbt, final String key, final int fallbackOrdinal) {
    return nbt.contains(key, NbtElement.NUMBER_TYPE) ? nbt.getInt(key) : fallbackOrdinal;
  }

  public static @Nullable BlockPos commandAnchorFrom(final NbtCompound nbt) {
    if (!nbt.contains(ModNbtKeys.COMMAND_ANCHOR_X, NbtElement.NUMBER_TYPE)
        || !nbt.contains(ModNbtKeys.COMMAND_ANCHOR_Y, NbtElement.NUMBER_TYPE)
        || !nbt.contains(ModNbtKeys.COMMAND_ANCHOR_Z, NbtElement.NUMBER_TYPE)) {
      return null;
    }
    return new BlockPos(
        nbt.getInt(ModNbtKeys.COMMAND_ANCHOR_X),
        nbt.getInt(ModNbtKeys.COMMAND_ANCHOR_Y),
        nbt.getInt(ModNbtKeys.COMMAND_ANCHOR_Z));
  }

  void writeNbt(final NbtCompound nbt) {
    this.dog.writeAngerToNbt(nbt);
    final DogGenome genome = this.dog.getGenome();
    if (genome != null) {
      nbt.put(ModNbtKeys.GENOME, genome.toNbt());
    }
    final DogAppearance appearance = this.dog.getAppearance();
    if (DogCoats.hasCoatVariants(appearance.rigSourceBreed())) {
      nbt.putInt(ModNbtKeys.COAT_VARIANT, appearance.coatVariantOrdinal());
    }
    if (appearance.rigSourceBreed().hasEyeColorVariants()) {
      nbt.putInt(ModNbtKeys.EYE_COLOR_VARIANT, appearance.eyeColorVariantOrdinal());
    }
    this.dog.getAmbienceEffects().writeNbt(nbt);
    this.dog.getSleepController().writeNbt(nbt);
    this.dog.getPlaySession().writeNbt(nbt);
    this.dog.getComponents().writeNbt(nbt);
    nbt.putInt(ModNbtKeys.COMMAND_MODE, this.dog.getCommand().id());
    final BlockPos anchor = this.dog.getCommandController().getAnchorPosInAnyDimension();
    if (anchor != null) {
      nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_X, anchor.getX());
      nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_Y, anchor.getY());
      nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_Z, anchor.getZ());
      SavedDimension.write(
          nbt,
          ModNbtKeys.COMMAND_ANCHOR_DIMENSION,
          this.dog.getCommandController().getAnchorDimension());
    }
    nbt.putInt(ModNbtKeys.CURING_TICKS, this.dog.getCuring().getRemainingTicks());
    if (this.dog.getCuring().getCurerId() != null) {
      nbt.putUuid(ModNbtKeys.CURING_PLAYER_ID, this.dog.getCuring().getCurerId());
    }
    this.dog.getLineage().writeNbt(nbt);
    this.dog.getGenes().writeNbt(nbt);
    this.dog.getEquipmentHolder().writeNbt(nbt);
    this.dog.getFur().writeNbt(nbt);
    this.dog.getGrowth().writeNbt(nbt);
    this.dog.getPack().writeNbt(nbt);
    this.dog.getTrust().writeNbt(nbt);
    this.dog.getRiding().writeNbt(nbt);
  }

  void readNbt(final NbtCompound nbt) {
    this.dog.readAngerFromNbt(this.dog.getWorld(), nbt);
    if (nbt.contains(ModNbtKeys.GENOME, NbtElement.COMPOUND_TYPE)) {
      final DogGenome genome = DogGenome.fromNbt(nbt.getCompound(ModNbtKeys.GENOME));
      if (genome != null) {
        this.dog.applyGenome(genome);
      }
    }
    final DogAppearance currentAppearance = this.dog.getAppearance();
    this.dog.applyAppearance(
        new DogAppearance(
            this.dog.getRigSourceBreed(),
            variantOrdinalOr(nbt, ModNbtKeys.COAT_VARIANT, currentAppearance.coatVariantOrdinal()),
            variantOrdinalOr(
                nbt, ModNbtKeys.EYE_COLOR_VARIANT, currentAppearance.eyeColorVariantOrdinal())));
    this.dog.getGenes().readNbt(nbt);
    this.dog.getAmbienceEffects().readNbt(nbt);
    this.dog.getSleepController().readNbt(nbt);
    this.dog.getPlaySession().readNbt(nbt);
    this.dog.getComponents().readNbt(nbt);
    if (nbt.contains(ModNbtKeys.COMMAND_MODE, NbtElement.NUMBER_TYPE)) {
      this.dog
          .getCommandController()
          .setCommandFromSave(DogCommand.fromId(nbt.getInt(ModNbtKeys.COMMAND_MODE)));
    } else {
      this.dog
          .getCommandController()
          .setCommandFromSave(commandForLegacySave(this.dog.isSitting()));
    }
    final BlockPos anchor = commandAnchorFrom(nbt);
    if (anchor != null) {
      this.dog
          .getCommandController()
          .setAnchorFromSave(
              anchor,
              SavedDimension.readOr(
                  nbt, ModNbtKeys.COMMAND_ANCHOR_DIMENSION, this.dog.getWorld().getRegistryKey()));
    }
    this.dog
        .getCuring()
        .restoreFromSave(
            nbt.getInt(ModNbtKeys.CURING_TICKS),
            nbt.containsUuid(ModNbtKeys.CURING_PLAYER_ID)
                ? nbt.getUuid(ModNbtKeys.CURING_PLAYER_ID)
                : null);
    this.dog.getLineage().readNbt(nbt);
    this.dog.getEquipmentHolder().readNbt(nbt);
    this.dog.getFur().readNbt(nbt);
    this.dog.getGrowth().readNbt(nbt);
    this.dog.getPack().readNbt(nbt);
    this.dog.getTrust().readNbt(nbt);
    this.dog.getRiding().readNbt(nbt);
    this.dog.getUndeadState().applyAttributeScaling();
  }
}
