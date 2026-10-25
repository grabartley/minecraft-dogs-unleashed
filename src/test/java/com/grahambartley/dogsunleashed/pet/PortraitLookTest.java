package com.grahambartley.dogsunleashed.pet;

import static com.grahambartley.dogsunleashed.TestRegistries.REGISTRIES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PortraitLookTest {

  private static final PortraitLook DRESSED =
      new PortraitLook(
          true,
          3,
          1,
          DogFurStage.OVERGROWN.ordinal(),
          4,
          new DogPortraitOutfit(
              Map.of(
                  DogEquipmentSlot.ARMOUR,
                  new ItemStack(Items.WOLF_ARMOR),
                  DogEquipmentSlot.SADDLEBAGS,
                  new ItemStack(ModItems.SADDLEBAGS)),
              Optional.of(DyeColor.CYAN),
              ItemStack.EMPTY,
              Optional.empty(),
              true,
              false,
              false));

  static Stream<Arguments> looks() {
    return Stream.of(Arguments.of(PortraitLook.DEFAULT), Arguments.of(DRESSED));
  }

  @ParameterizedTest
  @MethodSource("looks")
  @DisplayName("a look survives the network round trip")
  void packetRoundTrips(final PortraitLook look) {
    final RegistryByteBuf buf = new RegistryByteBuf(Unpooled.buffer(), REGISTRIES);
    PortraitLook.PACKET_CODEC.encode(buf, look);
    assertEquals(look, PortraitLook.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("withSenior changes only the senior coat")
  void withSeniorChangesOnlyTheSeniorCoat(final boolean senior) {
    final PortraitLook changed = DRESSED.withSenior(senior);
    assertEquals(senior, changed.outfit().senior());
    assertEquals(DRESSED.outfit().withSenior(senior), changed.outfit());
    assertEquals(
        new PortraitLook(
            DRESSED.baby(),
            DRESSED.coatVariant(),
            DRESSED.huskyEyeVariant(),
            DRESSED.furStage(),
            DRESSED.growthStage(),
            changed.outfit()),
        changed);
  }
}
