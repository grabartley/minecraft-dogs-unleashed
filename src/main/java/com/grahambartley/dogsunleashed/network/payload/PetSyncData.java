package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import java.util.List;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;

public record PetSyncData(
    String petId,
    UnleashedDogBreed breed,
    String name,
    float health,
    float maxHealth,
    int posX,
    int posY,
    int posZ,
    String dimension,
    PetLifeState lifeState,
    boolean baby,
    int collarColor,
    int coatVariant,
    int huskyEyeVariant,
    float movementSpeed,
    float attackDamage,
    List<BreedShare> composition) {

  public static final PacketCodec<RegistryByteBuf, PetSyncData> CODEC =
      PacketCodec.of(PetSyncData::write, PetSyncData::read);

  public static PetSyncData from(final PetData petData) {
    return new PetSyncData(
        petData.getPetId().toString(),
        petData.getBreed(),
        petData.getName(),
        petData.getHealth(),
        petData.getMaxHealth(),
        petData.getLastKnownPosition().getX(),
        petData.getLastKnownPosition().getY(),
        petData.getLastKnownPosition().getZ(),
        petData.getDimension(),
        petData.getLifeState(),
        petData.isBaby(),
        petData.getCollarColorId(),
        petData.getCoatVariant(),
        petData.getHuskyEyeVariant(),
        petData.getMovementSpeed(),
        petData.getAttackDamage(),
        petData.getComposition());
  }

  public static PetSyncData fromDog(final UnleashedDogEntity dog) {
    final BlockPos pos = dog.getBlockPos();
    final DogGenome genome = dog.getGenome();
    return new PetSyncData(
        dog.getUuid().toString(),
        dog.getBreed(),
        dog.hasCustomName() ? dog.getCustomName().getString() : "",
        dog.getHealth(),
        dog.getMaxHealth(),
        pos.getX(),
        pos.getY(),
        pos.getZ(),
        dog.getWorld().getRegistryKey().getValue().toString(),
        dog.isUndead() ? PetLifeState.UNDEAD : PetLifeState.LIVING,
        dog.isBaby(),
        dog.getCollarColor().getId(),
        PetData.coatVariantOf(dog),
        PetData.huskyEyeVariantOf(dog),
        (float) dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED),
        (float) dog.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
        genome != null ? genome.composition() : List.of());
  }

  public boolean alive() {
    return this.lifeState.isAlive();
  }

  public boolean undead() {
    return this.lifeState == PetLifeState.UNDEAD;
  }

  public UnleashedDogBreed displayBreed() {
    if (this.breed != UnleashedDogBreed.CROSS_BREED || this.composition.isEmpty()) {
      return this.breed;
    }
    return DogGenome.dominantOf(this.composition);
  }

  void write(final RegistryByteBuf buf) {
    buf.writeString(this.petId);
    buf.writeString(this.breed.serializedId());
    buf.writeString(this.name);
    buf.writeFloat(this.health);
    buf.writeFloat(this.maxHealth);
    buf.writeInt(this.posX);
    buf.writeInt(this.posY);
    buf.writeInt(this.posZ);
    buf.writeString(this.dimension);
    buf.writeString(this.lifeState.serializedName());
    buf.writeBoolean(this.baby);
    buf.writeInt(this.collarColor);
    buf.writeInt(this.coatVariant);
    buf.writeInt(this.huskyEyeVariant);
    buf.writeFloat(this.movementSpeed);
    buf.writeFloat(this.attackDamage);
    BreedShareCodec.writeList(buf, this.composition);
  }

  static PetSyncData read(final RegistryByteBuf buf) {
    return new PetSyncData(
        buf.readString(),
        UnleashedDogBreed.fromSerializedId(buf.readString()),
        buf.readString(),
        buf.readFloat(),
        buf.readFloat(),
        buf.readInt(),
        buf.readInt(),
        buf.readInt(),
        buf.readString(),
        PetLifeState.fromSerializedName(buf.readString()),
        buf.readBoolean(),
        buf.readInt(),
        buf.readInt(),
        buf.readInt(),
        buf.readFloat(),
        buf.readFloat(),
        BreedShareCodec.readList(buf));
  }
}
