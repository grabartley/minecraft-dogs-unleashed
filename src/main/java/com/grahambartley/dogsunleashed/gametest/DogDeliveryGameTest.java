package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.DogSaddlebagsGameTest.bags;
import static com.grahambartley.dogsunleashed.gametest.DogSaddlebagsGameTest.open;
import static com.grahambartley.dogsunleashed.gametest.DogSaddlebagsGameTest.wearingBags;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.away.AwayDogs;
import com.grahambartley.dogsunleashed.entity.delivery.Deliveries;
import com.grahambartley.dogsunleashed.entity.delivery.Delivery;
import com.grahambartley.dogsunleashed.entity.delivery.DeliveryDeparture;
import com.grahambartley.dogsunleashed.entity.delivery.DeliveryLeg;
import com.grahambartley.dogsunleashed.entity.delivery.DeliveryLifecycle;
import com.grahambartley.dogsunleashed.entity.delivery.DeliveryRounds;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.listener.AwayDogListener;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogDeliveryGameTest implements FabricGameTest {

  private static final String BATCH = "dog-delivery";
  private static final int TICK_LIMIT = 20;
  private static final BlockPos RECIPIENT_POS = new BlockPos(1, 2, 1);

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void deliveringTakesTheDogOutOfTheWorld(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);

    final Delivery delivery = DeliveryDeparture.send(dog, owner, recipient).orElseThrow();

    context.assertTrue(dog.isRemoved(), "The dog should leave the world");
    context.assertTrue(
        context.getWorld().getEntity(dog.getUuid()) == null, "Nothing should stand in for it");
    context.assertEquals(
        AwayDogs.statusOf(server(context), dog.getUuid()),
        PetAwayStatus.DELIVERING,
        "The Pet Manager status");
    context.assertTrue(
        AwayDogs.busyKey(server(context), dog.getUuid()).isPresent(),
        "A dog on a delivery can't be summoned");
    context.assertEquals(delivery.recipient(), recipient.getUuid(), "Recipient");
    finish(context, dog);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aDogWithoutSaddlebagsStaysHome(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.SADDLEBAGS, ItemStack.EMPTY);

    context.assertTrue(
        DeliveryDeparture.send(dog, owner, recipient).isEmpty(), "No bags, no delivery");
    context.assertFalse(dog.isRemoved(), "The dog should stay");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void onlyTheOwnerCanSendTheDog(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);

    context.assertTrue(
        DeliveryDeparture.send(dog, stranger, owner).isEmpty(), "A stranger can't send it");
    context.assertTrue(
        DeliveryDeparture.send(dog, owner, owner).isEmpty(), "The owner can't send it to themself");
    context.assertFalse(dog.isRemoved(), "The dog should stay");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theDogArrivesBesideTheRecipientWithItsBagsFull(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    bags(dog).setStack(0, new ItemStack(Items.DIAMOND, 5));
    bags(dog).setStack(6, new ItemStack(Items.BREAD, 12));

    final UnleashedDogEntity arrived = arriveAtRecipient(context, dog, owner, recipient);

    context.assertTrue(arrived.squaredDistanceTo(recipient) < 9.0, "Beside the recipient");
    context.assertEquals(bags(arrived).count(Items.DIAMOND), 5, "Diamonds on arrival");
    context.assertEquals(bags(arrived).count(Items.BREAD), 12, "Bread on arrival");
    context.assertEquals(
        AwayDogs.statusOf(server(context), dog.getUuid()), PetAwayStatus.WAITING, "Status");
    context.assertTrue(
        arrived.isInvulnerableTo(arrived.getDamageSources().generic()),
        "A dog waiting with a delivery can't be hurt");
    finish(context, arrived);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theRecipientTakesItemsAndNothingIsLostOrMade(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    bags(dog).setStack(2, new ItemStack(Items.DIAMOND, 5));
    bags(dog).setStack(3, new ItemStack(Items.EMERALD, 7));
    final int diamondsBefore = diamondsHeldBy(dog, owner, recipient);

    final UnleashedDogEntity arrived = arriveAtRecipient(context, dog, owner, recipient);
    final DogEquipmentScreenHandler handler = open(recipient, arrived);
    context.assertTrue(handler.canUse(recipient), "The recipient may open the bags");
    handler.quickMove(recipient, DogEquipmentScreenHandler.BAG_SLOT_START + 2);

    context.assertEquals(recipient.getInventory().count(Items.DIAMOND), 5, "Diamonds taken");
    context.assertEquals(bags(arrived).count(Items.DIAMOND), 0, "Diamonds left in the bags");
    context.assertEquals(
        diamondsHeldBy(arrived, owner, recipient), diamondsBefore, "Diamonds in the world");
    context.assertEquals(bags(arrived).count(Items.EMERALD), 7, "Untouched emeralds stay");
    finish(context, arrived);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theRecipientCannotChangeTheGearOrFillTheBags(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.PENDANT, new ItemStack(Items.GOLD_INGOT));
    final UnleashedDogEntity arrived = arriveAtRecipient(context, dog, owner, recipient);
    recipient.getInventory().setStack(0, new ItemStack(Items.WOLF_ARMOR));
    recipient.getInventory().setStack(1, new ItemStack(Items.COBBLESTONE, 32));
    final DogEquipmentScreenHandler handler = open(recipient, arrived);

    for (int index = 0; index < DogEquipmentScreenHandler.DOG_SLOT_COUNT; index++) {
      context.assertFalse(handler.getSlot(index).isEnabled(), "Gear slot " + index + " is hidden");
      context.assertFalse(
          handler.getSlot(index).canTakeItems(recipient), "Gear slot " + index + " is locked");
    }
    handler.quickMove(recipient, DogEquipmentScreenHandler.ROUTING.hotbarStart());
    handler.quickMove(recipient, DogEquipmentScreenHandler.ROUTING.hotbarStart() + 1);

    context.assertTrue(
        arrived.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isEmpty(),
        "The recipient can't put armour on");
    context.assertTrue(
        arrived.getEquipmentHolder().getStack(DogEquipmentSlot.PENDANT).isOf(Items.GOLD_INGOT),
        "The pendant stays");
    context.assertEquals(bags(arrived).count(Items.COBBLESTONE), 0, "Nothing goes in the bags");
    context.assertEquals(
        recipient.getInventory().count(Items.COBBLESTONE), 32, "The recipient keeps the stone");
    finish(context, arrived);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aStrangerCannotOpenTheBags(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final ServerPlayerEntity stranger = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity arrived =
        arriveAtRecipient(context, wearingBags(context, owner), owner, recipient);

    context.assertFalse(open(stranger, arrived).canUse(stranger), "Only the recipient may open");
    finish(context, arrived);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void closingTheBagsSendsTheDogHome(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity arrived =
        arriveAtRecipient(context, wearingBags(context, owner), owner, recipient);

    open(recipient, arrived).onClosed(recipient);

    context.runAtTick(
        3,
        () -> {
          context.assertTrue(arrived.isRemoved(), "The dog should set off home");
          context.assertEquals(
              AwayDogs.statusOf(server(context), arrived.getUuid()),
              PetAwayStatus.RETURNING,
              "Status");
          finish(context, arrived);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theWholeRoundTripConservesTheBags(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    bags(dog).setStack(0, new ItemStack(Items.DIAMOND, 4));
    bags(dog).setStack(1, new ItemStack(Items.DIAMOND, 6));
    final int before = diamondsHeldBy(dog, owner, recipient);

    final UnleashedDogEntity arrived = arriveAtRecipient(context, dog, owner, recipient);
    open(recipient, arrived).quickMove(recipient, DogEquipmentScreenHandler.BAG_SLOT_START);
    final Delivery homeward = DeliveryRounds.sendHome(arrived).orElseThrow();
    DeliveryRounds.advance(server(context), homeward, homeward.legEndsAt());
    final UnleashedDogEntity home = loaded(context, dog.getUuid());

    context.assertTrue(home != null && home.squaredDistanceTo(owner) < 9.0, "Home by its owner");
    context.assertEquals(bags(home).count(Items.DIAMOND), 6, "Diamonds still in the bags");
    context.assertEquals(diamondsHeldBy(home, owner, recipient), before, "Diamonds overall");
    context.assertTrue(
        Deliveries.get(server(context)).of(dog.getUuid()).isEmpty(), "The delivery is over");
    finish(context, home);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRecipientWhoLeftSendsTheDogHomeWithEverything(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    bags(dog).setStack(5, new ItemStack(Items.DIAMOND, 9));
    final Delivery sent = DeliveryDeparture.send(dog, owner, recipient).orElseThrow();
    final Delivery toSomeoneGone =
        new Delivery(
            sent.pet(),
            sent.owner(),
            UUID.randomUUID(),
            sent.entityType(),
            sent.dog(),
            DeliveryLeg.TO_RECIPIENT,
            sent.legTicks(),
            sent.legEndsAt());
    Deliveries.get(server(context)).replace(toSomeoneGone);

    DeliveryRounds.advance(server(context), toSomeoneGone, toSomeoneGone.legEndsAt());
    final Delivery turnedBack = Deliveries.get(server(context)).of(dog.getUuid()).orElseThrow();
    context.assertEquals(turnedBack.leg(), DeliveryLeg.TO_OWNER, "The dog turns back");
    DeliveryRounds.advance(server(context), turnedBack, turnedBack.legEndsAt());
    final UnleashedDogEntity home = loaded(context, dog.getUuid());

    context.assertTrue(home != null, "The dog should come home");
    context.assertEquals(bags(home).count(Items.DIAMOND), 9, "Every diamond comes back");
    finish(context, home);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theDogWaitsForAnOfflineOwnerBeforeComingHome(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    final Delivery sent = DeliveryDeparture.send(dog, owner, recipient).orElseThrow();
    final Delivery ownerGone =
        new Delivery(
            sent.pet(),
            UUID.randomUUID(),
            sent.recipient(),
            sent.entityType(),
            sent.dog(),
            DeliveryLeg.TO_OWNER,
            sent.legTicks(),
            sent.legEndsAt());
    Deliveries.get(server(context)).replace(ownerGone);

    DeliveryRounds.advance(server(context), ownerGone, ownerGone.legEndsAt());

    context.assertTrue(loaded(context, dog.getUuid()) == null, "No one to come home to yet");
    context.assertTrue(
        AwayDogs.isOffWorld(server(context), dog.getUuid()), "The dog is kept safe away");
    finish(context, dog);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aWaitingDogWhoseChunkUnloadsHeadsHomeOnce(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity dog = wearingBags(context, owner);
    bags(dog).setStack(1, new ItemStack(Items.DIAMOND, 8));
    final UnleashedDogEntity arrived = arriveAtRecipient(context, dog, owner, recipient);

    ServerEntityEvents.ENTITY_UNLOAD.invoker().onUnload(arrived, context.getWorld());
    AwayDogListener.discardIfAway(arrived, context.getWorld());
    final Delivery homeward = Deliveries.get(server(context)).of(dog.getUuid()).orElseThrow();
    context.assertEquals(homeward.leg(), DeliveryLeg.TO_OWNER, "The unloaded dog heads home");

    context.runAtTick(
        3,
        () -> {
          context.assertTrue(arrived.isRemoved(), "The copy left in the chunk is discarded");
          DeliveryRounds.advance(server(context), homeward, homeward.legEndsAt());
          final UnleashedDogEntity home = loaded(context, dog.getUuid());
          context.assertTrue(home != null && home != arrived, "One dog comes home");
          context.assertEquals(bags(home).count(Items.DIAMOND), 8, "Every diamond comes home");
          context.assertFalse(
              DeliveryLifecycle.isStaleCopy(server(context), home), "The dog at home is not stale");
          finish(context, home);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aDogThatDiesWhileWaitingEndsItsDelivery(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity arrived =
        arriveAtRecipient(context, wearingBags(context, owner), owner, recipient);

    arrived.kill();

    context.assertTrue(
        Deliveries.get(server(context)).of(arrived.getUuid()).isEmpty(), "No delivery is left");
    context.assertTrue(
        AwayDogs.busyKey(server(context), arrived.getUuid()).isEmpty(),
        "The dog is no longer reported busy");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theRecipientChangingWorldOrLeavingSendsTheDogHome(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity recipient = PendantTestHelper.survivalOwnerAt(context, RECIPIENT_POS);
    final UnleashedDogEntity arrived =
        arriveAtRecipient(context, wearingBags(context, owner), owner, recipient);

    DeliveryRounds.recipientLeft(server(context), recipient);

    context.assertTrue(arrived.isRemoved(), "The dog sets off home");
    context.assertEquals(
        AwayDogs.statusOf(server(context), arrived.getUuid()), PetAwayStatus.RETURNING, "Status");
    finish(context, arrived);
  }

  private static UnleashedDogEntity arriveAtRecipient(
      final TestContext context,
      final UnleashedDogEntity dog,
      final ServerPlayerEntity owner,
      final ServerPlayerEntity recipient) {
    final Delivery sent = DeliveryDeparture.send(dog, owner, recipient).orElseThrow();
    DeliveryRounds.advance(server(context), sent, sent.legEndsAt());
    final UnleashedDogEntity arrived = loaded(context, dog.getUuid());
    context.assertTrue(arrived != null, "The dog should reach the recipient");
    return arrived;
  }

  private static UnleashedDogEntity loaded(final TestContext context, final UUID pet) {
    final Entity entity = context.getWorld().getEntity(pet);
    return entity instanceof UnleashedDogEntity dog && !dog.isRemoved() ? dog : null;
  }

  private static int diamondsHeldBy(final UnleashedDogEntity dog, final PlayerEntity... players) {
    int total = bags(dog).count(Items.DIAMOND);
    for (final PlayerEntity player : players) {
      total += player.getInventory().count(Items.DIAMOND);
    }
    return total;
  }

  private static MinecraftServer server(final TestContext context) {
    return context.getWorld().getServer();
  }

  private static void finish(final TestContext context, final Entity dog) {
    Deliveries.get(server(context)).remove(dog.getUuid());
    context.complete();
  }
}
