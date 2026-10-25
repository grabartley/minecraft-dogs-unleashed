package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfits;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record PortraitLook(
    boolean baby,
    int coatVariant,
    int huskyEyeVariant,
    int furStage,
    int growthStage,
    DogPortraitOutfit outfit) {

  public static final PortraitLook DEFAULT =
      new PortraitLook(
          false,
          UnleashedDogEntity.UNSET_VARIANT,
          UnleashedDogEntity.UNSET_VARIANT,
          DogFurStage.NATURAL.ordinal(),
          0,
          DogPortraitOutfit.NONE);

  public static final PacketCodec<RegistryByteBuf, PortraitLook> PACKET_CODEC =
      PacketCodec.of(PortraitLook::write, PortraitLook::read);

  public static PortraitLook of(final UnleashedDogEntity dog) {
    return new PortraitLook(
        dog.isBaby(),
        PetData.coatVariantOf(dog),
        PetData.huskyEyeVariantOf(dog),
        dog.getFur().stage().ordinal(),
        dog.getGrowth().stage().number(),
        DogPortraitOutfits.of(dog));
  }

  public PortraitLook withSenior(final boolean senior) {
    return new PortraitLook(
        this.baby,
        this.coatVariant,
        this.huskyEyeVariant,
        this.furStage,
        this.growthStage,
        this.outfit.withSenior(senior));
  }

  private void write(final RegistryByteBuf buf) {
    buf.writeBoolean(this.baby);
    buf.writeInt(this.coatVariant);
    buf.writeInt(this.huskyEyeVariant);
    buf.writeVarInt(this.furStage);
    buf.writeVarInt(this.growthStage);
    DogPortraitOutfit.PACKET_CODEC.encode(buf, this.outfit);
  }

  private static PortraitLook read(final RegistryByteBuf buf) {
    return new PortraitLook(
        buf.readBoolean(),
        buf.readInt(),
        buf.readInt(),
        buf.readVarInt(),
        buf.readVarInt(),
        DogPortraitOutfit.PACKET_CODEC.decode(buf));
  }
}
