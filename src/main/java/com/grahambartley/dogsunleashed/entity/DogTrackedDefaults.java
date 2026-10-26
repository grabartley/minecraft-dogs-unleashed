package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.scent.ScentPhase;
import java.util.Optional;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

final class DogTrackedDefaults {

  private DogTrackedDefaults() {}

  static void addTo(final DataTracker.Builder builder) {
    builder.add(UnleashedDogEntity.ANGER_TIME, 0);
    builder.add(UnleashedDogEntity.COLLAR_COLOR, DogCollar.DEFAULT_COLOR.getId());
    builder.add(UnleashedDogEntity.TAIL_WAG_TIMER, 0);
    builder.add(UnleashedDogEntity.SHAKE_PROGRESS, 0);
    builder.add(UnleashedDogEntity.TREAT_BUFF_TICKS, 0);
    builder.add(UnleashedDogEntity.HEAD_TILTING, false);
    builder.add(UnleashedDogEntity.SLEEPING_IN_BED, false);
    builder.add(UnleashedDogEntity.COMMANDED_TO_SLEEP, false);
    builder.add(UnleashedDogEntity.ASSIGNED_BED_POS, Optional.empty());
    builder.add(UnleashedDogEntity.IS_CARRYING_FETCH_ITEM, false);
    builder.add(UnleashedDogEntity.ACTIVE_FETCH_TYPE_ID, DogPlaySession.NO_ACTIVE_FETCH_TYPE);
    builder.add(UnleashedDogEntity.CARRIED_FETCH_ITEM_STACK, ItemStack.EMPTY);
    builder.add(UnleashedDogEntity.PENDANT_ITEM, ItemStack.EMPTY);
    builder.add(UnleashedDogEntity.COSMETIC_ITEM, ItemStack.EMPTY);
    builder.add(UnleashedDogEntity.COSTUME_ITEM, ItemStack.EMPTY);
    builder.add(UnleashedDogEntity.SADDLEBAGS_ITEM, ItemStack.EMPTY);
    builder.add(UnleashedDogEntity.PLAY_PARTNER_UUID, Optional.empty());
    builder.add(UnleashedDogEntity.COMMAND, DogCommand.FOLLOW.id());
    builder.add(UnleashedDogEntity.COAT_VARIANT, 0);
    builder.add(UnleashedDogEntity.EYE_COLOR_VARIANT, 0);
    builder.add(UnleashedDogEntity.HOWLING, false);
    builder.add(UnleashedDogEntity.GROWLING, false);
    builder.add(UnleashedDogEntity.GENOME, new NbtCompound());
    builder.add(UnleashedDogEntity.FUR_STAGE, DogFurStage.NATURAL.ordinal());
    builder.add(UnleashedDogEntity.GROWTH_STAGE, DireWolfGrowthStage.YEARLING.number());
    builder.add(UnleashedDogEntity.NAPPING, false);
    builder.add(UnleashedDogEntity.SCENT_PHASE, ScentPhase.NONE.id());
    builder.add(UnleashedDogEntity.BONE_DIGGING, false);
    builder.add(UnleashedDogEntity.SENIOR, false);
    builder.add(UnleashedDogEntity.MUDDY, false);
    builder.add(UnleashedDogEntity.BOND_STEP, DogBondStep.STRANGER.ordinal());
    builder.add(UnleashedDogEntity.TUG_PLAYER_ID, DogTug.NO_PLAYER);
    builder.add(UnleashedDogEntity.TUG_BALANCE, 0.0f);
    builder.add(UnleashedDogEntity.BOWL_MEAL, false);
  }
}
