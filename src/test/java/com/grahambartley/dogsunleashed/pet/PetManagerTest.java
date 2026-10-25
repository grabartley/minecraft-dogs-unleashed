package com.grahambartley.dogsunleashed.pet;

import static com.grahambartley.dogsunleashed.TestRegistries.REGISTRIES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PetManagerTest {

  static Stream<Arguments> outfits() {
    return Stream.of(
        Arguments.of(DogPortraitOutfit.NONE),
        Arguments.of(
            new DogPortraitOutfit(
                Map.of(
                    DogEquipmentSlot.ARMOUR,
                    new ItemStack(Items.WOLF_ARMOR),
                    DogEquipmentSlot.SADDLEBAGS,
                    new ItemStack(ModItems.SADDLEBAGS)),
                Optional.of(DyeColor.PURPLE),
                ItemStack.EMPTY,
                Optional.empty(),
                true,
                false,
                false)));
  }

  @ParameterizedTest
  @MethodSource("outfits")
  @DisplayName("a saved pet keeps its outfit when the manager is saved and reloaded")
  void savedPetKeepsItsOutfit(final DogPortraitOutfit outfit) {
    final UUID ownerId = UUID.randomUUID();
    final PetData pet = petWearing(ownerId, outfit);
    final PetManager manager = new PetManager();
    manager.registerPet(pet);

    final PetManager reloaded =
        PetManager.fromNbt(manager.writeNbt(new NbtCompound(), REGISTRIES), REGISTRIES);

    assertEquals(outfit, reloaded.getPet(ownerId, pet.getPetId()).getOutfit());
  }

  private static PetData petWearing(final UUID ownerId, final DogPortraitOutfit outfit) {
    final NbtCompound nbt =
        new PetData(
                UUID.randomUUID(),
                ownerId,
                UnleashedDogBreed.BEAGLE,
                "Scout",
                20.0f,
                20.0f,
                BlockPos.ORIGIN,
                "minecraft:overworld",
                PetLifeState.LIVING)
            .toNbt(REGISTRIES);
    nbt.put(
        ModNbtKeys.PORTRAIT_OUTFIT,
        DogPortraitOutfit.CODEC
            .encodeStart(REGISTRIES.getOps(NbtOps.INSTANCE), outfit)
            .getOrThrow());
    return PetData.fromNbt(nbt, REGISTRIES);
  }
}
