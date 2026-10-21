package com.grahambartley.dogsunleashed.portrait;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public enum PortraitSize {
  SMALL("1x1", 1, 1),
  WIDE("2x1", 2, 1),
  LARGE("2x2", 2, 2);

  public static final Codec<PortraitSize> CODEC =
      Codec.STRING.xmap(PortraitSize::fromSerializedName, PortraitSize::serializedName);

  public static final PacketCodec<ByteBuf, PortraitSize> PACKET_CODEC =
      PacketCodecs.STRING.xmap(PortraitSize::fromSerializedName, PortraitSize::serializedName);

  private final String serializedName;
  private final int width;
  private final int height;

  PortraitSize(final String serializedName, final int width, final int height) {
    this.serializedName = serializedName;
    this.width = width;
    this.height = height;
  }

  public String serializedName() {
    return this.serializedName;
  }

  public int width() {
    return this.width;
  }

  public int height() {
    return this.height;
  }

  public PortraitSize next() {
    final PortraitSize[] sizes = values();
    return sizes[(this.ordinal() + 1) % sizes.length];
  }

  public static PortraitSize fromSerializedName(final String name) {
    return Arrays.stream(values())
        .filter(size -> size.serializedName.equals(name))
        .findFirst()
        .orElse(SMALL);
  }
}
