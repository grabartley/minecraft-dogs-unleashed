package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetAge;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.portrait.DogLook;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record PetSyncData(
    String petId,
    String name,
    float health,
    float maxHealth,
    int posX,
    int posY,
    int posZ,
    String dimension,
    PetLifeState lifeState,
    DogStatueLook look,
    DogPortraitOutfit outfit,
    boolean senior,
    float movementSpeed,
    float attackDamage,
    RidingView riding,
    PetAwayStatus away,
    DogBondStep bond,
    DogTraits personality) {

  public static final PacketCodec<RegistryByteBuf, PetSyncData> CODEC =
      PacketCodec.of(PetSyncData::write, PetSyncData::read);

  public static PetSyncData from(final PetData petData, final PetAwayStatus away) {
    return new PetSyncData(
        petData.getPetId().toString(),
        petData.getName(),
        petData.getHealth(),
        petData.getMaxHealth(),
        petData.getLastKnownPosition().getX(),
        petData.getLastKnownPosition().getY(),
        petData.getLastKnownPosition().getZ(),
        petData.getDimension(),
        petData.getLifeState(),
        petData.getLook().withGenes(null),
        petData.getOutfit(),
        petData.isSenior(DogsUnleashed.SERVER_CONFIG.aging()),
        petData.getMovementSpeed(),
        petData.getAttackDamage(),
        RidingView.of(
            petData.getBreed(), petData.getLook().growthStage(), petData.getStaminaPercent()),
        away,
        DogBondStep.ofPoints(petData.getBondPoints()),
        petData.getPersonality());
  }

  public static PetSyncData fromDog(final UnleashedDogEntity dog) {
    return from(
        PetData.snapshotOf(
            dog,
            dog.getOwnerUuid(),
            dog.hasCustomName() ? dog.getCustomName().getString() : "",
            dog.isUndead() ? PetLifeState.UNDEAD : PetLifeState.LIVING),
        PetAwayStatus.HOME);
  }

  public UnleashedDogBreed breed() {
    return this.look.breed();
  }

  public List<BreedShare> composition() {
    return this.look.composition();
  }

  public boolean alive() {
    return this.lifeState.isAlive();
  }

  public boolean summonable() {
    return this.alive() && !this.away.isAway();
  }

  public boolean undead() {
    return this.lifeState.isUndead();
  }

  public PetAge age() {
    return PetAge.of(this.look.baby(), this.senior);
  }

  public UnleashedDogBreed displayBreed() {
    final List<BreedShare> composition = this.composition();
    if (this.breed() != UnleashedDogBreed.CROSS_BREED || composition.isEmpty()) {
      return this.breed();
    }
    return DogGenome.dominantOf(composition);
  }

  public DogLook portraitLook() {
    final DogStatueLook shown =
        this.displayBreed().hasEyeColorVariants()
            ? this.look
            : this.look.withEyeColorVariant(UnleashedDogEntity.UNSET_VARIANT);
    return new DogLook(shown, this.outfit, this.undead(), this.senior);
  }

  void write(final RegistryByteBuf buf) {
    buf.writeString(this.petId);
    buf.writeString(this.name);
    buf.writeFloat(this.health);
    buf.writeFloat(this.maxHealth);
    buf.writeInt(this.posX);
    buf.writeInt(this.posY);
    buf.writeInt(this.posZ);
    buf.writeString(this.dimension);
    buf.writeString(this.lifeState.serializedName());
    DogStatueLook.PACKET_CODEC.encode(buf, this.look);
    DogPortraitOutfit.PACKET_CODEC.encode(buf, this.outfit);
    buf.writeBoolean(this.senior);
    buf.writeFloat(this.movementSpeed);
    buf.writeFloat(this.attackDamage);
    this.riding.write(buf);
    buf.writeVarInt(this.away.ordinal());
    buf.writeVarInt(this.bond.ordinal());
    this.personality.write(buf);
  }

  static PetSyncData read(final RegistryByteBuf buf) {
    return new PetSyncData(
        buf.readString(),
        buf.readString(),
        buf.readFloat(),
        buf.readFloat(),
        buf.readInt(),
        buf.readInt(),
        buf.readInt(),
        buf.readString(),
        PetLifeState.fromSerializedName(buf.readString()),
        DogStatueLook.PACKET_CODEC.decode(buf),
        DogPortraitOutfit.PACKET_CODEC.decode(buf),
        buf.readBoolean(),
        buf.readFloat(),
        buf.readFloat(),
        RidingView.read(buf),
        PetAwayStatus.byId(buf.readVarInt()),
        DogBondStep.byId(buf.readVarInt()),
        DogTraits.read(buf));
  }
}
