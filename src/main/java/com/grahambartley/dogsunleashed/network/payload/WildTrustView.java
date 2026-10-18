package com.grahambartley.dogsunleashed.network.payload;

import net.minecraft.network.RegistryByteBuf;

public record WildTrustView(Kind kind, int trust) {

  public static final WildTrustView NONE = new WildTrustView(Kind.NONE, 0);
  public static final WildTrustView TOO_WILD = new WildTrustView(Kind.TOO_WILD, 0);
  public static final WildTrustView SHUNNED = new WildTrustView(Kind.SHUNNED, 0);

  public enum Kind {
    NONE,
    TOO_WILD,
    SHUNNED,
    BUILDING
  }

  public static WildTrustView building(final int trust) {
    return new WildTrustView(Kind.BUILDING, trust);
  }

  void write(final RegistryByteBuf buf) {
    buf.writeEnumConstant(this.kind);
    buf.writeVarInt(this.trust);
  }

  static WildTrustView read(final RegistryByteBuf buf) {
    return new WildTrustView(buf.readEnumConstant(Kind.class), buf.readVarInt());
  }
}
