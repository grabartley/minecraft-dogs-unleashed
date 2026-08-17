package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.genome.DogGenomeCombiner;
import com.grahambartley.dogsunleashed.pet.PetRegistrar;
import com.grahambartley.dogsunleashed.util.BreedingOwnerResolver;
import java.util.UUID;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public final class DogLineage {

  private final UnleashedDogEntity dog;
  private @Nullable UUID parentDogUuid = null;
  private @Nullable UUID secondParentDogUuid = null;

  DogLineage(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  /** The breeding readiness of one dog in a pair, as the mate gate reads it. */
  public record MateState(boolean tamed, boolean sitting, boolean inLove) {}

  /** Only the partner's sitting pose gates the pair; the initiator's is asymmetric on purpose. */
  public static boolean matesCanBreed(final MateState self, final MateState partner) {
    return self.tamed()
        && partner.tamed()
        && !partner.sitting()
        && self.inLove()
        && partner.inLove();
  }

  public static UnleashedDogBreed childBreed(
      final UnleashedDogBreed first, final UnleashedDogBreed second) {
    return first == second && first != UnleashedDogBreed.CROSS_BREED
        ? first
        : UnleashedDogBreed.CROSS_BREED;
  }

  private static MateState mateStateOf(final UnleashedDogEntity dog) {
    return new MateState(dog.isTamed(), dog.isInSittingPose(), dog.isInLove());
  }

  boolean canBreedWith(final AnimalEntity other) {
    if (other == this.dog || !(other instanceof UnleashedDogEntity partner)) {
      return false;
    }
    return matesCanBreed(mateStateOf(this.dog), mateStateOf(partner));
  }

  @Nullable
  PassiveEntity createChild(final ServerWorld world, final PassiveEntity entity) {
    if (!(entity instanceof UnleashedDogEntity partner)) {
      return null;
    }
    final UnleashedDogBreed childBreed = childBreed(this.dog.getBreed(), partner.getBreed());
    final UnleashedDogEntity baby = ModEntities.getDogEntityType(childBreed).create(world);
    if (baby == null) {
      return null;
    }
    baby.setBaby(true);
    baby.getLineage().setParentDogUuid(this.dog.getUuid());
    baby.getLineage().setSecondParentDogUuid(partner.getUuid());
    if (childBreed == UnleashedDogBreed.CROSS_BREED) {
      baby.applyGenome(
          DogGenomeCombiner.combine(
              this.dog.getAppearanceRoller().genomeOrPure(),
              partner.getAppearanceRoller().genomeOrPure(),
              this.dog.getRandom()));
      baby.setHealth(baby.getMaxHealth());
    }
    baby.getAppearanceRoller().rollAppearance(SpawnReason.BREEDING);
    final PlayerEntity lovingPlayer = this.dog.getLovingPlayer();
    if (lovingPlayer != null) {
      baby.getInteractions().tame(lovingPlayer);
    } else {
      final UUID inheritedOwnerUuid =
          BreedingOwnerResolver.resolveInheritedOwnerUuid(
              this.dog.getOwnerUuid(), partner.getOwnerUuid());
      if (inheritedOwnerUuid != null) {
        baby.setOwnerUuid(inheritedOwnerUuid);
        baby.setTamed(true, true);
        // The baby is still unpositioned here; AnimalEntity#breed moves and spawns it right after,
        // and the resulting ENTITY_LOAD makes PetLocationSyncListener write the real position.
        PetRegistrar.registerPetFor(baby, inheritedOwnerUuid);
      }
    }
    return baby;
  }

  public void setParentDogUuid(final UUID parentDogUuid) {
    this.parentDogUuid = parentDogUuid;
  }

  @Nullable
  public UUID getParentDogUuid() {
    return this.parentDogUuid;
  }

  public void setSecondParentDogUuid(final UUID secondParentDogUuid) {
    this.secondParentDogUuid = secondParentDogUuid;
  }

  @Nullable
  public UUID getSecondParentDogUuid() {
    return this.secondParentDogUuid;
  }

  @Nullable
  public UnleashedDogEntity getParentDog() {
    if (this.parentDogUuid == null || !(this.dog.getWorld() instanceof ServerWorld serverWorld)) {
      return null;
    }
    return serverWorld.getEntity(this.parentDogUuid) instanceof UnleashedDogEntity parent
            && parent.isAlive()
        ? parent
        : null;
  }

  void writeNbt(final NbtCompound nbt) {
    if (this.parentDogUuid != null) {
      nbt.putUuid(ModNbtKeys.PARENT_DOG_ID, this.parentDogUuid);
    }
    if (this.secondParentDogUuid != null) {
      nbt.putUuid(ModNbtKeys.SECOND_PARENT_DOG_ID, this.secondParentDogUuid);
    }
  }

  void readNbt(final NbtCompound nbt) {
    if (nbt.containsUuid(ModNbtKeys.PARENT_DOG_ID)) {
      this.parentDogUuid = nbt.getUuid(ModNbtKeys.PARENT_DOG_ID);
    }
    if (nbt.containsUuid(ModNbtKeys.SECOND_PARENT_DOG_ID)) {
      this.secondParentDogUuid = nbt.getUuid(ModNbtKeys.SECOND_PARENT_DOG_ID);
    }
  }
}
