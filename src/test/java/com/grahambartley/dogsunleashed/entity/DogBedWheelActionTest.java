package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.delivery.DogDeliveries;
import com.grahambartley.dogsunleashed.entity.expedition.DogExpeditions;
import com.grahambartley.dogsunleashed.entity.trick.DogTrick;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.IntStream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DogBedWheelActionTest {

  static IntStream otherActionIds() {
    return IntStream.concat(
        IntStream.of(DogExpeditions.WHEEL_ACTION_ID, DogDeliveries.WHEEL_ACTION_ID),
        Arrays.stream(DogTrick.values()).mapToInt(DogTrick::wheelActionId));
  }

  @ParameterizedTest(name = "action id {0}")
  @MethodSource("otherActionIds")
  @DisplayName("assign bed has its own slot in the Actions ring")
  void assignBedHasItsOwnSlot(final int otherId) {
    assertNotEquals(otherId, DogBedWheelAction.WHEEL_ACTION_ID);
  }

  @Test
  @DisplayName("assign bed is always offered, with its own label and icon")
  void assignBedIsAlwaysOffered() {
    final DogWheelSubAction action = DogBedWheelAction.action();
    assertEquals(DogBedWheelAction.WHEEL_ACTION_ID, action.id());
    assertEquals("command.dogs-unleashed.assign_bed", action.translationKey());
    assertEquals("dogs-unleashed:command_wheel/assign_bed", action.icon().toString());
    assertNull(action.refusal().refusalKey(null, null));
    assertSame(DogWheelSubAction.ALWAYS_SHOWN, action.visibility());
  }

  @Test
  @DisplayName("assign bed has its own icon sprite next to the other wheel icons")
  void assignBedHasItsOwnSprite() {
    final Identifier icon = DogBedWheelAction.WHEEL_ICON;
    assertTrue(
        Files.isRegularFile(
            Path.of("src/client/resources/assets", icon.getNamespace(), "textures/gui/sprites")
                .resolve(icon.getPath() + ".png")),
        icon + " has no sprite");
  }

  @Test
  @DisplayName("assign bed registers once into the Actions ring")
  void assignBedRegistersOnce() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    DogBedWheelAction.register(registry);
    assertEquals(
        DogBedWheelAction.WHEEL_LABEL_KEY,
        registry.byId(DogBedWheelAction.WHEEL_ACTION_ID).orElseThrow().translationKey());
  }
}
