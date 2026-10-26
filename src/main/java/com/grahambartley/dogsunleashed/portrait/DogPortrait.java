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

public record DogPortrait(DogLook look, String name, PortraitSize size, DogStatuePose pose) {

  private record LegacyOutfitFlags(boolean undead, boolean senior) {

    private static final Codec<LegacyOutfitFlags> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.BOOL
                            .lenientOptionalFieldOf("undead", false)
                            .forGetter(LegacyOutfitFlags::undead),
                        Codec.BOOL
                            .lenientOptionalFieldOf("senior", false)
                            .forGetter(LegacyOutfitFlags::senior))
                    .apply(instance, LegacyOutfitFlags::new));
  }

  public static final Codec<DogPortrait> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      DogStatueLook.CODEC
                          .fieldOf("look")
                          .forGetter(portrait -> portrait.look.statue()),
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
                          .lenientOptionalFieldOf("outfit", DogPortraitOutfit.NONE)
                          .forGetter(portrait -> portrait.look.outfit()),
                      Codec.BOOL
                          .lenientOptionalFieldOf("undead")
                          .forGetter(portrait -> setFlag(portrait.look.undead())),
                      Codec.BOOL
                          .lenientOptionalFieldOf("senior")
                          .forGetter(portrait -> setFlag(portrait.look.senior())),
                      LegacyOutfitFlags.CODEC
                          .lenientOptionalFieldOf("outfit")
                          .forGetter(portrait -> Optional.empty()))
                  .apply(instance, DogPortrait::decoded));

  public static final PacketCodec<RegistryByteBuf, DogPortrait> PACKET_CODEC =
      PacketCodecs.registryCodec(CODEC);

  public DogPortrait(
      final DogStatueLook look,
      final String name,
      final PortraitSize size,
      final DogStatuePose pose) {
    this(DogLook.plain(look), name, size, pose);
  }

  public static DogPortrait painted(
      final DogLook look, final String name, final PortraitSize size) {
    return new DogPortrait(look, DogStatue.cut(name), size, DogStatuePose.SIT);
  }

  public DogPortrait withPose(final DogStatuePose pose) {
    return new DogPortrait(this.look, this.name, this.size, pose);
  }

  public DogPortrait nextPose() {
    return this.withPose(this.pose.next(this.look.statue()));
  }

  public DogStatuePose shownPose() {
    return this.pose.shownFor(this.look.statue());
  }

  public PortraitBackground background() {
    return PortraitBackground.of(this.look.statue().rigBreed());
  }

  public NbtCompound toNbt(final RegistryWrapper.WrapperLookup registries) {
    return CODEC
        .encodeStart(registries.getOps(NbtOps.INSTANCE), this)
        .result()
        .filter(NbtCompound.class::isInstance)
        .map(NbtCompound.class::cast)
        .orElseGet(NbtCompound::new);
  }

  private static DogPortrait decoded(
      final DogStatueLook statue,
      final String name,
      final PortraitSize size,
      final DogStatuePose pose,
      final DogPortraitOutfit outfit,
      final Optional<Boolean> undead,
      final Optional<Boolean> senior,
      final Optional<LegacyOutfitFlags> legacy) {
    return new DogPortrait(
        new DogLook(
            statue,
            outfit,
            undead.orElseGet(() -> legacy.map(LegacyOutfitFlags::undead).orElse(false)),
            senior.orElseGet(() -> legacy.map(LegacyOutfitFlags::senior).orElse(false))),
        name,
        size,
        pose);
  }

  private static Optional<Boolean> setFlag(final boolean flag) {
    return flag ? Optional.of(true) : Optional.empty();
  }

  public static Optional<DogPortrait> fromNbt(
      final RegistryWrapper.WrapperLookup registries, final NbtElement nbt) {
    return CODEC.parse(registries.getOps(NbtOps.INSTANCE), nbt).result();
  }
}
