package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;
import static com.grahambartley.dogsunleashed.gametest.util.SaddlebagsTestHelper.bags;
import static com.grahambartley.dogsunleashed.gametest.util.SaddlebagsTestHelper.open;
import static com.grahambartley.dogsunleashed.gametest.util.SaddlebagsTestHelper.wearingBags;

import com.grahambartley.dogsunleashed.ModCosmeticItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class DogGearSortingGameTest implements FabricGameTest {

  private static final String BATCH = "dog-gear-sorting";
  private static final int TICK_LIMIT = 20;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theFirstSlotIsBackedByTheBagContents(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = fullyGeared(context, owner);
    final DogEquipmentScreenHandler handler = open(owner, dog);

    context.assertTrue(
        handler.getSlot(0).inventory == bags(dog),
        "Sort mods that pick the first slot's inventory should find the bags");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingEveryGearInventoryLeavesTheGearInPlace(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = fullyGeared(context, owner);
    final DogEquipmentScreenHandler handler = open(owner, dog);
    final Map<DogEquipmentSlot, ItemStack> before = worn(dog);

    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      compact(gearInventory(handler, slot));
    }

    assertWornUnchanged(context, dog, before);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void writingAnotherSlotsGearIsIgnored(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = fullyGeared(context, owner);
    final DogEquipmentScreenHandler handler = open(owner, dog);
    final Map<DogEquipmentSlot, ItemStack> before = worn(dog);

    for (final DogEquipmentSlot target : DogEquipmentSlot.values()) {
      for (final ItemStack gear : before.values()) {
        if (!gear.isEmpty() && !target.canHold(gear)) {
          gearInventory(handler, target).setStack(0, gear.copy());
        }
      }
      gearInventory(handler, target).setStack(0, new ItemStack(Items.BREAD));
    }

    assertWornUnchanged(context, dog, before);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void misplacedGearCanStillBeTakenOff(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, bandana());
    final DogEquipmentScreenHandler handler = open(owner, dog);

    handler.onSlotClick(
        DogEquipmentScreenHandler.gearSlotIndex(DogEquipmentSlot.ARMOUR),
        0,
        SlotActionType.PICKUP,
        owner);

    context.assertTrue(
        handler.getCursorStack().isOf(bandana().getItem()),
        "The bandana comes off the armour slot");
    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isEmpty(),
        "The armour slot is empty again");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingTheBagsReordersTheirContents(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = fullyGeared(context, owner);
    bags(dog).setStack(8, new ItemStack(Items.DIAMOND, 4));
    bags(dog).setStack(3, new ItemStack(Items.BREAD, 7));
    final DogEquipmentScreenHandler handler = open(owner, dog);
    final Map<DogEquipmentSlot, ItemStack> before = worn(dog);

    compact(handler.getSlot(0).inventory);

    context.assertTrue(bags(dog).getStack(0).isOf(Items.BREAD), "Bread moves to the front");
    context.assertTrue(bags(dog).getStack(1).isOf(Items.DIAMOND), "Diamonds follow");
    context.assertEquals(bags(dog).count(Items.DIAMOND), 4, "No diamonds lost");
    assertWornUnchanged(context, dog, before);
    context.complete();
  }

  private static UnleashedDogEntity fullyGeared(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog = wearingBags(context, owner);
    PendantTestHelper.wear(dog, PendantType.WARDING_UNDEAD);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSMETIC, bandana());
    return dog;
  }

  private static ItemStack bandana() {
    return new ItemStack(ModCosmeticItems.of(DogCosmetic.BANDANA));
  }

  private static Inventory gearInventory(
      final DogEquipmentScreenHandler handler, final DogEquipmentSlot slot) {
    return handler.getSlot(DogEquipmentScreenHandler.gearSlotIndex(slot)).inventory;
  }

  private static void compact(final Inventory inventory) {
    final List<ItemStack> stacks = new ArrayList<>();
    for (int index = 0; index < inventory.size(); index++) {
      if (!inventory.getStack(index).isEmpty()) {
        stacks.add(inventory.getStack(index).copy());
      }
    }
    for (int index = 0; index < inventory.size(); index++) {
      inventory.setStack(index, index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY);
    }
  }

  private static Map<DogEquipmentSlot, ItemStack> worn(final UnleashedDogEntity dog) {
    final Map<DogEquipmentSlot, ItemStack> worn = new EnumMap<>(DogEquipmentSlot.class);
    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      worn.put(slot, dog.getEquipmentHolder().getStack(slot).copy());
    }
    return worn;
  }

  private static void assertWornUnchanged(
      final TestContext context,
      final UnleashedDogEntity dog,
      final Map<DogEquipmentSlot, ItemStack> before) {
    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      context.assertTrue(
          ItemStack.areEqual(before.get(slot), dog.getEquipmentHolder().getStack(slot)),
          "The " + slot + " slot should keep " + before.get(slot));
    }
  }
}
