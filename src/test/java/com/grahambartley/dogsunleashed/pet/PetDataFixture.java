package com.grahambartley.dogsunleashed.pet;

import static com.grahambartley.dogsunleashed.TestRegistries.REGISTRIES;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfitFixture;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public final class PetDataFixture {

  public static final float HEALTH = 18.0f;
  public static final BlockPos POS = new BlockPos(10, 64, -20);
  public static final String DIMENSION = "minecraft:overworld";

  private PetDataFixture() {}

  public static PetData pet() {
    return pet(UUID.randomUUID());
  }

  public static PetData pet(final UUID ownerId) {
    return new PetData(
        UUID.randomUUID(),
        ownerId,
        UnleashedDogBreed.HUSKY,
        "Balto",
        HEALTH,
        20.0f,
        POS,
        DIMENSION,
        PetLifeState.LIVING);
  }

  public static PetData wearing(final PetData pet, final DogPortraitOutfit outfit) {
    final NbtCompound nbt = pet.toNbt(REGISTRIES);
    wear(nbt, outfit);
    return PetData.fromNbt(nbt, REGISTRIES);
  }

  public static void wear(final NbtCompound nbt, final DogPortraitOutfit outfit) {
    nbt.put(ModNbtKeys.PORTRAIT_OUTFIT, DogPortraitOutfitFixture.save(outfit));
  }
}
