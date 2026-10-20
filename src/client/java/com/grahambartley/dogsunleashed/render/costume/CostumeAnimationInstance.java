package com.grahambartley.dogsunleashed.render.costume;

import net.minecraft.util.Identifier;

public final class CostumeAnimationInstance {

  private static final long COSTUME_MARK = 1L << 63;
  private static final int ENTITY_BITS = 32;
  private static final long ENTITY_MASK = 0xFFFFFFFFL;

  private CostumeAnimationInstance() {}

  public static long idOf(final int entityId, final Identifier costume) {
    return COSTUME_MARK | ((long) costume.hashCode() << ENTITY_BITS) | (entityId & ENTITY_MASK);
  }
}
