package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogPortraitOutfitTest {

  private static final DynamicRegistryManager REGISTRIES =
      DynamicRegistryManager.of(Registries.REGISTRIES);

  private static ItemStack dyedArmour(final int colour) {
    final ItemStack armour = new ItemStack(Items.WOLF_ARMOR);
    armour.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(colour, true));
    return armour;
  }

  static Stream<Arguments> outfits() {
    return Stream.of(
        Arguments.of(DogPortraitOutfit.NONE),
        Arguments.of(
            new DogPortraitOutfit(
                Map.of(
                    DogEquipmentSlot.ARMOUR,
                    dyedArmour(0x3366AA),
                    DogEquipmentSlot.SADDLEBAGS,
                    new ItemStack(ModItems.SADDLEBAGS)),
                Optional.of(DyeColor.LIME),
                ItemStack.EMPTY,
                Optional.of(Identifier.of("dogs-unleashed", "tennis_ball")),
                true,
                false,
                false)),
        Arguments.of(
            new DogPortraitOutfit(
                Map.of(DogEquipmentSlot.COSMETIC, new ItemStack(Items.CARVED_PUMPKIN)),
                Optional.empty(),
                ItemStack.EMPTY,
                Optional.empty(),
                false,
                true,
                true)));
  }

  private static NbtElement save(final DogPortraitOutfit outfit) {
    return DogPortraitOutfit.CODEC
        .encodeStart(REGISTRIES.getOps(NbtOps.INSTANCE), outfit)
        .getOrThrow();
  }

  private static DogPortraitOutfit load(final NbtElement saved) {
    return DogPortraitOutfit.CODEC.parse(REGISTRIES.getOps(NbtOps.INSTANCE), saved).getOrThrow();
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("outfits")
  @DisplayName("an outfit survives saving, gear colours and all")
  void outfitRoundTrips(final DogPortraitOutfit outfit) {
    final DogPortraitOutfit loaded = load(save(outfit));
    assertEquals(outfit, loaded);
    assertEquals(outfit.hashCode(), loaded.hashCode());
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("outfits")
  @DisplayName("two outfits with the same gear are equal even when the stacks are separate copies")
  void equalByValue(final DogPortraitOutfit outfit) {
    final DogPortraitOutfit copy =
        new DogPortraitOutfit(
            outfit.gear(),
            outfit.collar(),
            outfit.carried(),
            outfit.fetchType(),
            outfit.muddy(),
            outfit.senior(),
            outfit.undead());
    assertEquals(outfit, copy);
    assertEquals(outfit.hashCode(), copy.hashCode());
  }

  @ParameterizedTest(name = "armour dyed {0} against {1}")
  @MethodSource("differentDyes")
  @DisplayName("armour dyed a different colour is a different outfit")
  void dyeColourMatters(final int first, final int second) {
    final DogPortraitOutfit one =
        new DogPortraitOutfit(
            Map.of(DogEquipmentSlot.ARMOUR, dyedArmour(first)),
            Optional.empty(),
            ItemStack.EMPTY,
            Optional.empty(),
            false,
            false,
            false);
    final DogPortraitOutfit two =
        new DogPortraitOutfit(
            Map.of(DogEquipmentSlot.ARMOUR, dyedArmour(second)),
            Optional.empty(),
            ItemStack.EMPTY,
            Optional.empty(),
            false,
            false,
            false);
    assertNotEquals(one, two);
  }

  static Stream<Arguments> differentDyes() {
    return Stream.of(Arguments.of(0x3366AA, 0xAA3366), Arguments.of(0x000000, 0xFFFFFF));
  }

  @Test
  @DisplayName("empty slots are dropped, so a bare dog is the same as no outfit")
  void emptySlotsAreDropped() {
    final DogPortraitOutfit bare =
        new DogPortraitOutfit(
            Map.of(DogEquipmentSlot.ARMOUR, ItemStack.EMPTY),
            Optional.empty(),
            ItemStack.EMPTY,
            Optional.empty(),
            false,
            false,
            false);
    assertTrue(bare.gear().isEmpty());
    assertEquals(DogPortraitOutfit.NONE, bare);
    assertTrue(bare.worn(DogEquipmentSlot.ARMOUR).isEmpty());
  }

  @Test
  @DisplayName("changing a stack after it is saved in an outfit leaves the outfit alone")
  void outfitKeepsItsOwnCopies() {
    final ItemStack saddlebags = new ItemStack(ModItems.SADDLEBAGS);
    final DogPortraitOutfit outfit =
        new DogPortraitOutfit(
            Map.of(DogEquipmentSlot.SADDLEBAGS, saddlebags),
            Optional.empty(),
            ItemStack.EMPTY,
            Optional.empty(),
            false,
            false,
            false);
    saddlebags.setCount(0);
    assertEquals(1, outfit.worn(DogEquipmentSlot.SADDLEBAGS).getCount());
  }

  @Test
  @DisplayName("an outfit saved with nothing in it reads back as no outfit")
  void emptySaveIsNone() {
    assertEquals(DogPortraitOutfit.NONE, load(new NbtCompound()));
  }

  @Test
  @DisplayName(
      "gear naming an item that no longer exists is dropped and the rest of the outfit loads")
  void unknownItemIsDropped() {
    final NbtCompound saved =
        (NbtCompound)
            save(
                new DogPortraitOutfit(
                    Map.of(
                        DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR),
                        DogEquipmentSlot.SADDLEBAGS, new ItemStack(ModItems.SADDLEBAGS)),
                    Optional.of(DyeColor.LIME),
                    ItemStack.EMPTY,
                    Optional.empty(),
                    true,
                    false,
                    false));
    saved.getCompound("gear").getCompound("saddlebags").putString("id", "gone:old_bag");
    final DogPortraitOutfit loaded = load(saved);
    assertTrue(loaded.worn(DogEquipmentSlot.ARMOUR).isOf(Items.WOLF_ARMOR));
    assertTrue(loaded.worn(DogEquipmentSlot.SADDLEBAGS).isEmpty());
    assertEquals(Optional.of(DyeColor.LIME), loaded.collar());
    assertTrue(loaded.muddy());
  }

  @ParameterizedTest(name = "carried {0}, fetch type {1} carries {2}")
  @CsvSource({"true, false, true", "false, true, true", "true, true, true", "false, false, false"})
  @DisplayName("a dog is carrying when it holds an item or a fetch toy")
  void carryingFollowsTheToy(final boolean item, final boolean type, final boolean carrying) {
    final DogPortraitOutfit outfit =
        new DogPortraitOutfit(
            Map.of(),
            Optional.empty(),
            item ? new ItemStack(Items.STICK) : ItemStack.EMPTY,
            type ? Optional.of(Identifier.of("dogs-unleashed", "frisbee")) : Optional.empty(),
            false,
            false,
            false);
    assertEquals(carrying, outfit.carrying());
  }

  @Test
  @DisplayName("changing a stack handed out by an outfit leaves the outfit alone")
  void accessorsHandOutCopies() {
    final DogPortraitOutfit outfit =
        new DogPortraitOutfit(
            Map.of(DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR)),
            Optional.empty(),
            new ItemStack(Items.STICK),
            Optional.empty(),
            false,
            false,
            false);
    final int hash = outfit.hashCode();
    outfit.carried().setCount(0);
    outfit.gear().get(DogEquipmentSlot.ARMOUR).setCount(0);
    assertEquals(hash, outfit.hashCode());
    assertTrue(outfit.carried().isOf(Items.STICK));
  }

  @Test
  @DisplayName("a carried coloured frisbee keeps its colour through saving")
  void frisbeeColourRoundTrips() {
    final ItemStack frisbee = new ItemStack(ModItems.FRISBEE);
    frisbee.set(ModComponents.FRISBEE_COLOR, DyeColor.PURPLE);
    final DogPortraitOutfit outfit =
        new DogPortraitOutfit(
            Map.of(),
            Optional.empty(),
            frisbee,
            Optional.of(Identifier.of("dogs-unleashed", "frisbee")),
            false,
            false,
            false);
    assertEquals(DyeColor.PURPLE, load(save(outfit)).carried().get(ModComponents.FRISBEE_COLOR));
  }

  @ParameterizedTest
  @MethodSource("outfits")
  @DisplayName("an outfit survives the network round trip")
  void packetRoundTrips(final DogPortraitOutfit outfit) {
    final RegistryByteBuf buf = new RegistryByteBuf(Unpooled.buffer(), REGISTRIES);
    DogPortraitOutfit.PACKET_CODEC.encode(buf, outfit);
    assertEquals(outfit, DogPortraitOutfit.PACKET_CODEC.decode(buf));
  }
}
