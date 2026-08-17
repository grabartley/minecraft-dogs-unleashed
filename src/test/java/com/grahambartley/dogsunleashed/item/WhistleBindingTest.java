package com.grahambartley.dogsunleashed.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.PetData;
import java.util.List;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinecraftBootstrapExtension.class)
class WhistleBindingTest {

  private static final UUID SCOUT = UUID.fromString("11111111-0000-0000-0000-000000000000");
  private static final UUID BAILEY = UUID.fromString("22222222-0000-0000-0000-000000000000");

  private static PetData pet(final UUID id, final String name, final boolean alive) {
    return new PetData(
        id,
        UUID.randomUUID(),
        UnleashedDogBreed.HUSKY,
        name,
        10.0f,
        10.0f,
        new BlockPos(0, 64, 0),
        "minecraft:overworld",
        alive);
  }

  @Test
  @DisplayName("a whistle bound to a living dog in the pack resolves to that dog")
  void boundLivingDogResolves() {
    final List<PetData> pets = List.of(pet(SCOUT, "Scout", true), pet(BAILEY, "Bailey", true));

    assertEquals(SCOUT, WhistleBinding.boundPet(pets, SCOUT).getPetId());
    assertEquals("Bailey", WhistleBinding.boundPet(pets, BAILEY).getName());
  }

  @Test
  @DisplayName("an unbound whistle resolves to nothing")
  void unboundWhistleResolvesToNothing() {
    assertNull(WhistleBinding.boundPet(List.of(pet(SCOUT, "Scout", true)), null));
  }

  @Test
  @DisplayName("a whistle bound to a dog that has died resolves to nothing")
  void deadDogResolvesToNothing() {
    assertNull(WhistleBinding.boundPet(List.of(pet(SCOUT, "Scout", false)), SCOUT));
  }

  @Test
  @DisplayName("a whistle bound to a dog outside this pack resolves to nothing")
  void dogOutsideThePackResolvesToNothing() {
    assertNull(WhistleBinding.boundPet(List.of(pet(BAILEY, "Bailey", true)), SCOUT));
  }

  @Test
  @DisplayName("an owner with no dogs at all resolves to nothing")
  void emptyPackResolvesToNothing() {
    assertNull(WhistleBinding.boundPet(List.of(), SCOUT));
  }

  @Test
  @DisplayName("binding follows the dog it names rather than a position in the pack")
  void bindingIsIndependentOfPackOrder() {
    final List<PetData> forwards = List.of(pet(SCOUT, "Scout", true), pet(BAILEY, "Bailey", true));
    final List<PetData> backwards = List.of(pet(BAILEY, "Bailey", true), pet(SCOUT, "Scout", true));

    assertEquals(
        WhistleBinding.boundPet(forwards, SCOUT).getName(),
        WhistleBinding.boundPet(backwards, SCOUT).getName());
  }
}
