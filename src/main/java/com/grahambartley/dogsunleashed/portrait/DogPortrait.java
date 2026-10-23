package com.grahambartley.dogsunleashed.portrait;

import com.grahambartley.dogsunleashed.block.DogStatue;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;

public record DogPortrait(
    DogStatueLook look,
    String name,
    PortraitSize size,
    DogStatuePose pose,
    DogPortraitOutfit outfit) {

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
                          .forGetter(DogPortrait::size),
                      DogStatuePose.CODEC
                          .optionalFieldOf("pose", DogStatuePose.SIT)
                          .forGetter(DogPortrait::pose),
                      DogPortraitOutfit.CODEC
                          .optionalFieldOf("outfit", DogPortraitOutfit.NONE)
                          .forGetter(DogPortrait::outfit))
                  .apply(instance, DogPortrait::new));

  public static final PacketCodec<RegistryByteBuf, DogPortrait> PACKET_CODEC =
      PacketCodecs.registryCodec(CODEC);

  public DogPortrait(
      final DogStatueLook look,
      final String name,
      final PortraitSize size,
      final DogStatuePose pose) {
    this(look, name, size, pose, DogPortraitOutfit.NONE);
  }

  public static DogPortrait painted(
      final DogStatueLook look,
      final String name,
      final PortraitSize size,
      final DogPortraitOutfit outfit) {
    return new DogPortrait(look, DogStatue.cut(name), size, DogStatuePose.SIT, outfit);
  }

  public DogPortrait withPose(final DogStatuePose pose) {
    return new DogPortrait(this.look, this.name, this.size, pose, this.outfit);
  }

  public DogPortrait nextPose() {
    return this.withPose(this.pose.next(this.look));
  }

  public DogStatuePose shownPose() {
    return this.pose.shownFor(this.look);
  }

  public PortraitBackground background() {
    return PortraitBackground.of(this.look.rigBreed());
  }

  public NbtCompound toNbt(final RegistryWrapper.WrapperLookup registries) {
    return CODEC
        .encodeStart(registries.getOps(NbtOps.INSTANCE), this)
        .result()
        .filter(NbtCompound.class::isInstance)
        .map(NbtCompound.class::cast)
        .orElseGet(NbtCompound::new);
  }

  public static Optional<DogPortrait> fromNbt(
      final RegistryWrapper.WrapperLookup registries, final NbtElement nbt) {
    return CODEC.parse(registries.getOps(NbtOps.INSTANCE), nbt).result();
  }
}
