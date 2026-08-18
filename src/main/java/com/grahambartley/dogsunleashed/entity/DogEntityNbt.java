package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class DogEntityNbt {

  private final UnleashedDogEntity dog;

  DogEntityNbt(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static int sanitizedTreatBuffTicks(final int savedTicks) {
    return Math.max(0, savedTicks);
  }

  /** Saves written before command modes existed carry no id, so the sitting pose stands in. */
  public static DogCommand commandForLegacySave(final boolean sitting) {
    return sitting ? DogCommand.SIT : DogCommand.FOLLOW;
  }

  public static int variantOrdinalOr(
      final NbtCompound nbt, final String key, final int fallbackOrdinal) {
    return nbt.contains(key, NbtElement.NUMBER_TYPE) ? nbt.getInt(key) : fallbackOrdinal;
  }

  /** A partially written anchor is treated as no anchor, so all three axes must be present. */
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
    final DogTraits traits = this.dog.getTraits();
    if (DogCoats.hasCoatVariants(traits.rigSourceBreed())) {
      nbt.putInt(ModNbtKeys.COAT_VARIANT, traits.coatVariantOrdinal());
    }
    if (traits.rigSourceBreed().hasEyeColorVariants()) {
      nbt.putInt(ModNbtKeys.EYE_COLOR_VARIANT, traits.eyeColorVariantOrdinal());
    }
    nbt.putInt(ModNbtKeys.COLLAR_COLOR, this.dog.getCollarColor().getId());
    this.dog.getAmbienceEffects().writeNbt(nbt);
    this.dog.getSleepController().writeNbt(nbt);
    this.dog.getPlaySession().writeNbt(nbt);
    nbt.putInt(ModNbtKeys.TREAT_BUFF_TICKS, this.dog.getTreatBuffState().getRemainingTicks());
    nbt.putInt(ModNbtKeys.COMMAND_MODE, this.dog.getCommand().id());
    final BlockPos anchor = this.dog.getCommandController().getAnchorPos();
    if (anchor != null) {
      nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_X, anchor.getX());
      nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_Y, anchor.getY());
      nbt.putInt(ModNbtKeys.COMMAND_ANCHOR_Z, anchor.getZ());
    }
    nbt.putBoolean(ModNbtKeys.SPAWNED_BY_DOG_SPAWNER, this.dog.isSpawnedByDogSpawner());
    nbt.putInt(ModNbtKeys.CURING_TICKS, this.dog.getCuring().getRemainingTicks());
    if (this.dog.getCuring().getCurerId() != null) {
      nbt.putUuid(ModNbtKeys.CURING_PLAYER_ID, this.dog.getCuring().getCurerId());
    }
    this.dog.getLineage().writeNbt(nbt);
    this.dog.getEquipmentHolder().writeNbt(nbt);
  }

  void readNbt(final NbtCompound nbt) {
    this.dog.readAngerFromNbt(this.dog.getWorld(), nbt);
    if (nbt.contains(ModNbtKeys.GENOME, NbtElement.COMPOUND_TYPE)) {
      final DogGenome genome = DogGenome.fromNbt(nbt.getCompound(ModNbtKeys.GENOME));
      if (genome != null) {
        this.dog.applyGenome(genome);
      }
    }
    final DogTraits currentTraits = this.dog.getTraits();
    this.dog.applyTraits(
        new DogTraits(
            this.dog.getRigSourceBreed(),
            variantOrdinalOr(nbt, ModNbtKeys.COAT_VARIANT, currentTraits.coatVariantOrdinal()),
            variantOrdinalOr(
                nbt, ModNbtKeys.EYE_COLOR_VARIANT, currentTraits.eyeColorVariantOrdinal())));
    if (nbt.contains(ModNbtKeys.COLLAR_COLOR, NbtElement.NUMBER_TYPE)) {
      this.dog.setCollarColor(DyeColor.byId(nbt.getInt(ModNbtKeys.COLLAR_COLOR)));
    }
    this.dog.getAmbienceEffects().readNbt(nbt);
    this.dog.getSleepController().readNbt(nbt);
    this.dog.getPlaySession().readNbt(nbt);
    if (nbt.contains(ModNbtKeys.TREAT_BUFF_TICKS, NbtElement.NUMBER_TYPE)) {
      final int treatBuffTicks = sanitizedTreatBuffTicks(nbt.getInt(ModNbtKeys.TREAT_BUFF_TICKS));
      this.dog.getTreatBuffState().setRemainingTicksFromSave(treatBuffTicks);
      if (treatBuffTicks > 0) {
        DogTreatBuff.apply(this.dog);
      }
    }
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
      this.dog.getCommandController().setAnchorPosFromSave(anchor);
    }
    if (nbt.contains(ModNbtKeys.SPAWNED_BY_DOG_SPAWNER)) {
      this.dog.setSpawnedByDogSpawner(nbt.getBoolean(ModNbtKeys.SPAWNED_BY_DOG_SPAWNER));
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
    this.dog.getUndeadState().applyAttributeScaling();
  }
}
