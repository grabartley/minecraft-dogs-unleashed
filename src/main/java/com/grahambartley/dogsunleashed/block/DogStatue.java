package com.grahambartley.dogsunleashed.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record DogStatue(DogStatueLook look, DogStatuePose pose, String name) {

  public static final int MAX_NAME_LENGTH = 50;

  public static final Codec<DogStatue> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      DogStatueLook.CODEC.fieldOf("look").forGetter(DogStatue::look),
                      DogStatuePose.CODEC
                          .optionalFieldOf("pose", DogStatuePose.SIT)
                          .forGetter(DogStatue::pose),
                      Codec.string(0, MAX_NAME_LENGTH)
                          .optionalFieldOf("name", "")
                          .forGetter(DogStatue::name))
                  .apply(instance, DogStatue::new));

  public static final PacketCodec<ByteBuf, DogStatue> PACKET_CODEC = PacketCodecs.codec(CODEC);

  public static DogStatue carved(final DogStatueLook look, final String name) {
    return new DogStatue(look, DogStatuePose.SIT, cut(name));
  }

  public static String cut(final String name) {
    if (name.length() <= MAX_NAME_LENGTH) {
      return name;
    }
    final int end =
        Character.isLowSurrogate(name.charAt(MAX_NAME_LENGTH))
            ? MAX_NAME_LENGTH - 1
            : MAX_NAME_LENGTH;
    return name.substring(0, end);
  }

  public DogStatue withPose(final DogStatuePose pose) {
    return new DogStatue(this.look, pose, this.name);
  }

  public DogStatue nextPose() {
    return this.withPose(this.pose.next(this.look));
  }

  public DogStatuePose shownPose() {
    return this.pose.shownFor(this.look);
  }
}
