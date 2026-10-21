package com.grahambartley.dogsunleashed.portrait;

import com.grahambartley.dogsunleashed.block.DogStatue;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record DogPortrait(DogStatueLook look, String name, PortraitSize size) {

  public static final Codec<DogPortrait> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      DogStatueLook.CODEC.fieldOf("look").forGetter(DogPortrait::look),
                      Codec.string(0, DogStatue.MAX_NAME_LENGTH)
                          .optionalFieldOf("name", "")
                          .forGetter(DogPortrait::name),
                      PortraitSize.CODEC
                          .optionalFieldOf("size", PortraitSize.SMALL)
                          .forGetter(DogPortrait::size))
                  .apply(instance, DogPortrait::new));

  public static final PacketCodec<ByteBuf, DogPortrait> PACKET_CODEC = PacketCodecs.codec(CODEC);

  public static DogPortrait painted(
      final DogStatueLook look, final String name, final PortraitSize size) {
    return new DogPortrait(look, DogStatue.cut(name), size);
  }

  public PortraitBackground background() {
    return PortraitBackground.of(this.look.rigBreed());
  }

  public NbtCompound toNbt() {
    return CODEC
        .encodeStart(NbtOps.INSTANCE, this)
        .result()
        .filter(NbtCompound.class::isInstance)
        .map(NbtCompound.class::cast)
        .orElseGet(NbtCompound::new);
  }

  public static Optional<DogPortrait> fromNbt(final NbtElement nbt) {
    return CODEC.parse(NbtOps.INSTANCE, nbt).result();
  }
}
