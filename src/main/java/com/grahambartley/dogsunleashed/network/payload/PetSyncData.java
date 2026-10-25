package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.config.AgingSettings;
import com.grahambartley.dogsunleashed.entity.DogAgingRules;
import com.grahambartley.dogsunleashed.entity.DogBond;
import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.DogPersonality;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetAge;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PortraitLook;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
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
    PortraitLook look,
    float movementSpeed,
    float attackDamage,
    List<BreedShare> composition,
    RidingView riding,
    PetAwayStatus away,
    DogBondStep bond,
    DogTraits personality) {

  public static final PacketCodec<RegistryByteBuf, PetSyncData> CODEC =
      PacketCodec.of(PetSyncData::write, PetSyncData::read);

  public static PetSyncData from(final PetData petData, final PetAwayStatus away) {
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
        petData.portraitLook().withSenior(seniorOf(petData)),
        petData.getMovementSpeed(),
        petData.getAttackDamage(),
        petData.getComposition(),
        RidingView.of(petData.getBreed(), petData.getGrowthStage(), petData.getStaminaPercent()),
        away,
        DogBondStep.ofPoints(petData.getBondPoints()),
        petData.getPersonality());
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
        PortraitLook.of(dog),
        (float) dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED),
        (float) dog.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
        genome != null ? genome.composition() : List.of(),
        RidingView.of(
            dog.getBreed(),
            dog.getGrowth().stage().number(),
            Math.round(dog.getRiding().staminaFraction() * 100.0f)),
        PetAwayStatus.HOME,
        dog.getComponents().get(DogBond.class).step(),
        dog.getComponents().get(DogPersonality.class).traits());
  }

  static boolean seniorOf(final PetData petData) {
    final AgingSettings aging = DogsUnleashed.SERVER_CONFIG.aging();
    return DogAgingRules.isSenior(
        aging.dogAgingEnabled(),
        petData.getLifeState() == PetLifeState.UNDEAD,
        petData.getAdultAgeTicks(),
        aging.seniorAfterDays());
  }

  public boolean baby() {
    return this.look.baby();
  }

  public int coatVariant() {
    return this.look.coatVariant();
  }

  public int huskyEyeVariant() {
    return this.look.huskyEyeVariant();
  }

  public int furStage() {
    return this.look.furStage();
  }

  public int growthStage() {
    return this.look.growthStage();
  }

  public DogPortraitOutfit outfit() {
    return this.look.outfit();
  }

  public boolean senior() {
    return this.look.outfit().senior();
  }

  public boolean alive() {
    return this.lifeState.isAlive();
  }

  public boolean summonable() {
    return this.alive() && !this.away.isAway();
  }

  public boolean undead() {
    return this.lifeState == PetLifeState.UNDEAD;
  }

  public PetAge age() {
    return PetAge.of(this.baby(), this.senior());
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
    PortraitLook.PACKET_CODEC.encode(buf, this.look);
    buf.writeFloat(this.movementSpeed);
    buf.writeFloat(this.attackDamage);
    BreedShareCodec.writeList(buf, this.composition);
    this.riding.write(buf);
    buf.writeVarInt(this.away.ordinal());
    buf.writeVarInt(this.bond.ordinal());
    this.personality.write(buf);
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
        PortraitLook.PACKET_CODEC.decode(buf),
        buf.readFloat(),
        buf.readFloat(),
        BreedShareCodec.readList(buf),
        RidingView.read(buf),
        PetAwayStatus.byId(buf.readVarInt()),
        DogBondStep.byId(buf.readVarInt()),
        DogTraits.read(buf));
  }
}
