package com.grahambartley.dogsunleashed.entity.delivery;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public final class DeliverySnapshots {

  static final String TAGS = "Tags";

  private DeliverySnapshots() {}

  public static NbtCompound withoutTag(final NbtCompound snapshot, final String tag) {
    if (snapshot.contains(TAGS, NbtElement.LIST_TYPE)) {
      snapshot
          .getList(TAGS, NbtElement.STRING_TYPE)
          .removeIf(entry -> tag.equals(entry.asString()));
    }
    return snapshot;
  }
}
