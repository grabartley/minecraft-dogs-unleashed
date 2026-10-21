package com.grahambartley.dogsunleashed;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class ModAttachments {

  public static final AttachmentType<BlockPos> FOX_RAID_TARGET =
      AttachmentRegistry.create(Identifier.of(DogsUnleashed.MOD_ID, "fox_raid_target"));

  public static final AttachmentType<Boolean> FOX_STOLEN_BONE =
      AttachmentRegistry.createPersistent(
          Identifier.of(DogsUnleashed.MOD_ID, "fox_stolen_bone"), Codec.BOOL);

  public static final AttachmentType<Double> SLED_SNOW_BLOCKS =
      AttachmentRegistry.<Double>builder()
          .persistent(Codec.DOUBLE)
          .copyOnDeath()
          .buildAndRegister(Identifier.of(DogsUnleashed.MOD_ID, "sled_snow_blocks"));

  private ModAttachments() {}

  public static void initialize() {}
}
