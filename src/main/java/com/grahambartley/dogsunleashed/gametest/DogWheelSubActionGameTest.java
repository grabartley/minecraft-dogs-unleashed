package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.DogWheelSubAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import com.grahambartley.dogsunleashed.network.handler.WheelActionNetworkHandler;
import com.grahambartley.dogsunleashed.network.payload.OpenCommandWheelPayload;
import com.grahambartley.dogsunleashed.network.payload.SelectWheelSubActionPayload;
import com.grahambartley.dogsunleashed.network.payload.WheelSubActionState;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class DogWheelSubActionGameTest implements FabricGameTest {

  private static final String BATCH = "dog-wheel-sub-action";
  private static final int ACTION_ID = 0;
  private static final String REFUSAL_KEY = "screen.dogs-unleashed.command_wheel.ride.busy";

  private record Fixture(
      UnleashedDogEntity dog,
      ServerPlayerEntity owner,
      DogWheelSubActions registry,
      List<String> runs) {}

  private static Fixture fixture(final TestContext context, final String refusalKey) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(
            context, DogTestData.HUSKY, new BlockPos(0, 1, 0), owner.getUuid());
    dog.setAiDisabled(true);
    final List<String> runs = new ArrayList<>();
    final DogWheelSubActions registry = new DogWheelSubActions();
    registry.register(
        new DogWheelSubAction(
            ACTION_ID,
            "command.dogs-unleashed.actions",
            Identifier.of("minecraft", "bell"),
            (target, player) -> refusalKey,
            (target, player) ->
                runs.add(player.getUuidAsString() + ":" + target.getUuidAsString())));
    return new Fixture(dog, owner, registry, runs);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void ownerRunsAnAvailableSubAction(final TestContext context) {
    final Fixture fixture = fixture(context, null);

    WheelActionNetworkHandler.selectSubAction(
        fixture.registry(),
        new SelectWheelSubActionPayload(fixture.dog().getUuid(), ACTION_ID),
        fixture.owner());

    context.assertTrue(
        fixture
            .runs()
            .equals(
                List.of(fixture.owner().getUuidAsString() + ":" + fixture.dog().getUuidAsString())),
        "The owner's choice should run once for this dog, runs=" + fixture.runs());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void strangerCannotRunASubAction(final TestContext context) {
    final Fixture fixture = fixture(context, null);
    final ServerPlayerEntity stranger = context.createMockCreativeServerPlayerInWorld();

    WheelActionNetworkHandler.selectSubAction(
        fixture.registry(),
        new SelectWheelSubActionPayload(fixture.dog().getUuid(), ACTION_ID),
        stranger);

    context.assertTrue(fixture.runs().isEmpty(), "A stranger's choice must not run");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void refusedSubActionDoesNotRunEvenIfTheClientSendsIt(final TestContext context) {
    final Fixture fixture = fixture(context, REFUSAL_KEY);

    WheelActionNetworkHandler.selectSubAction(
        fixture.registry(),
        new SelectWheelSubActionPayload(fixture.dog().getUuid(), ACTION_ID),
        fixture.owner());

    context.assertTrue(fixture.runs().isEmpty(), "The server must re-check the refusal");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void unknownSubActionIdDoesNothing(final TestContext context) {
    final Fixture fixture = fixture(context, null);

    WheelActionNetworkHandler.selectSubAction(
        fixture.registry(),
        new SelectWheelSubActionPayload(fixture.dog().getUuid(), ACTION_ID + 1),
        fixture.owner());

    context.assertTrue(fixture.runs().isEmpty(), "An unregistered id must not run anything");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void removedDogCannotRunASubAction(final TestContext context) {
    final Fixture fixture = fixture(context, null);
    fixture.dog().discard();

    WheelActionNetworkHandler.selectSubAction(
        fixture.registry(),
        new SelectWheelSubActionPayload(fixture.dog().getUuid(), ACTION_ID),
        fixture.owner());

    context.assertTrue(fixture.runs().isEmpty(), "A removed dog must not run anything");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void openWheelPayloadCarriesEachSubActionAndItsRefusal(final TestContext context) {
    final Fixture fixture = fixture(context, null);
    fixture
        .registry()
        .register(
            new DogWheelSubAction(
                ACTION_ID + 1,
                "command.dogs-unleashed.ride",
                Identifier.of("minecraft", "saddle"),
                (target, player) -> REFUSAL_KEY,
                (target, player) -> {}));

    final OpenCommandWheelPayload payload =
        ModNetworking.openCommandWheelPayload(fixture.registry(), fixture.owner(), fixture.dog());

    final List<WheelSubActionState> expected =
        List.of(
            new WheelSubActionState(
                0, "command.dogs-unleashed.actions", Identifier.of("minecraft", "bell"), ""),
            new WheelSubActionState(
                1,
                "command.dogs-unleashed.ride",
                Identifier.of("minecraft", "saddle"),
                "screen.dogs-unleashed.command_wheel.ride.busy"));
    context.assertTrue(
        payload.subActions().equals(expected),
        "The wheel should list each sub-action with its refusal, got " + payload.subActions());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void deadDogCannotRunASubAction(final TestContext context) {
    final Fixture fixture = fixture(context, null);
    fixture.dog().setHealth(0.0f);

    WheelActionNetworkHandler.selectSubAction(
        fixture.registry(),
        new SelectWheelSubActionPayload(fixture.dog().getUuid(), ACTION_ID),
        fixture.owner());

    context.assertTrue(fixture.runs().isEmpty(), "A dead dog must not run anything");
    context.complete();
  }
}
