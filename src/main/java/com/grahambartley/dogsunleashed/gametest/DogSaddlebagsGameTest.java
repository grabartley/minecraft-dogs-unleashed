package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;
import static com.grahambartley.dogsunleashed.gametest.util.SaddlebagsTestHelper.bags;
import static com.grahambartley.dogsunleashed.gametest.util.SaddlebagsTestHelper.open;
import static com.grahambartley.dogsunleashed.gametest.util.SaddlebagsTestHelper.ownedDog;
import static com.grahambartley.dogsunleashed.gametest.util.SaddlebagsTestHelper.wearingBags;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogRecreation;
import com.grahambartley.dogsunleashed.entity.DogSaddlebags;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class DogSaddlebagsGameTest implements FabricGameTest {

  private static final String BATCH = "dog-saddlebags";
  private static final int TICK_LIMIT = 20;
  private static final int DEATH_TICK_LIMIT = 40;
  private static final int DEATH_ASSERT_TICK = 30;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void saddlebagsFitOnlyTheSaddlebagsSlot(final TestContext context) {
    final ItemStack bags = new ItemStack(ModItems.SADDLEBAGS);
    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      context.assertTrue(
          slot.canHold(bags) == (slot == DogEquipmentSlot.SADDLEBAGS),
          "Saddlebags fit the " + slot + " slot: " + slot.canHold(bags));
    }
    context.assertEquals(bags.getMaxCount(), 1, "Saddlebags should not stack");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void bagSlotsOpenOnlyWhileSaddlebagsAreWorn(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, owner);
    final DogEquipmentScreenHandler handler = open(owner, dog);
    final int firstBag = DogEquipmentScreenHandler.BAG_SLOT_START;

    context.assertFalse(handler.getSlot(firstBag).isEnabled(), "No bag slots without saddlebags");
    context.assertFalse(
        handler.getSlot(firstBag).canInsert(new ItemStack(Items.BREAD)),
        "Nothing goes in bags that are not there");
    dog.getEquipmentHolder()
        .setStack(DogEquipmentSlot.SADDLEBAGS, new ItemStack(ModItems.SADDLEBAGS));
    for (int index = 0; index < DogSaddlebags.SIZE; index++) {
      context.assertTrue(
          handler.getSlot(firstBag + index).isEnabled(), "Bag slot " + index + " should open");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void shiftClickFillsTheBagsOnceTheGearSlotsSayNo(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    owner.getInventory().setStack(0, new ItemStack(Items.BREAD, 20));
    final DogEquipmentScreenHandler handler = open(owner, dog);

    handler.quickMove(owner, DogEquipmentScreenHandler.ROUTING.hotbarStart());

    context.assertEquals(count(bags(dog), Items.BREAD), 20, "Bread in the bags");
    context.assertTrue(owner.getInventory().getStack(0).isEmpty(), "The hotbar slot empties");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void fullBagsCannotBeTakenOff(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    bags(dog).setStack(4, new ItemStack(Items.DIAMOND, 3));
    final DogEquipmentScreenHandler handler = open(owner, dog);
    final int bagsSlot = DogEquipmentScreenHandler.gearSlotIndex(DogEquipmentSlot.SADDLEBAGS);

    context.assertFalse(
        handler.getSlot(bagsSlot).canTakeItems(owner), "Full bags must stay on the dog");
    bags(dog).removeStack(4);
    context.assertTrue(
        handler.getSlot(bagsSlot).canTakeItems(owner), "Empty bags come off like other gear");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void bagContentsSurviveASaveAndLoad(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    bags(dog).setStack(0, new ItemStack(Items.DIAMOND, 5));
    bags(dog).setStack(8, new ItemStack(Items.WRITABLE_BOOK));

    final UnleashedDogEntity loaded =
        DogRecreation.fromSnapshot(
            dog.getType(), dog.writeNbt(new NbtCompound()), context.getWorld());

    context.assertTrue(loaded != null, "The dog should load");
    context.assertTrue(
        loaded.getComponents().get(DogSaddlebags.class).isWorn(), "The saddlebags stay worn");
    context.assertEquals(count(bags(loaded), Items.DIAMOND), 5, "Diamonds after load");
    context.assertEquals(count(bags(loaded), Items.WRITABLE_BOOK), 1, "Books after load");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = DEATH_TICK_LIMIT)
  public void deathDropsTheBagsAndEverythingInThem(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    bags(dog).setStack(0, new ItemStack(Items.DIAMOND, 5));
    bags(dog).setStack(3, new ItemStack(Items.DIAMOND, 2));
    bags(dog).setStack(7, new ItemStack(Items.BREAD, 9));

    dog.kill();

    context.runAtTick(
        DEATH_ASSERT_TICK,
        () -> {
          context.assertEquals(dropped(context, Items.DIAMOND), 7, "Diamonds dropped");
          context.assertEquals(dropped(context, Items.BREAD), 9, "Bread dropped");
          context.assertEquals(dropped(context, ModItems.SADDLEBAGS), 1, "Saddlebags dropped");
          context.complete();
        });
  }

  private static int count(final SimpleInventory inventory, final Item item) {
    return inventory.count(item);
  }

  private static int dropped(final TestContext context, final Item item) {
    return context
        .getWorld()
        .getEntitiesByClass(
            ItemEntity.class,
            context.getTestBox().expand(2.0),
            entity -> entity.getStack().isOf(item))
        .stream()
        .mapToInt(entity -> entity.getStack().getCount())
        .sum();
  }
}
