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

  private ModAttachments() {}

  public static void initialize() {}
}
