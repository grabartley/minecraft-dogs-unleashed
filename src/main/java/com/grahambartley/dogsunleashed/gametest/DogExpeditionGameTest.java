package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.DOG_POS;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModArmorItems;
import com.grahambartley.dogsunleashed.entity.DogCollar;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.expedition.DogExpeditions;
import com.grahambartley.dogsunleashed.entity.expedition.Expedition;
import com.grahambartley.dogsunleashed.entity.expedition.ExpeditionFinds;
import com.grahambartley.dogsunleashed.entity.expedition.ExpeditionHomecoming;
import com.grahambartley.dogsunleashed.entity.expedition.Expeditions;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.armor.DogArmorTier;
import com.grahambartley.dogsunleashed.listener.ExpeditionListener;
import com.grahambartley.dogsunleashed.network.handler.WheelActionNetworkHandler;
import com.grahambartley.dogsunleashed.network.payload.SelectWheelSubActionPayload;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

public final class DogExpeditionGameTest implements FabricGameTest {

  private static final String BATCH = "dog-expedition";
  private static final long MIDDAY = 6000L;

  @BeforeBatch(batchId = BATCH)
  public void freezeMidday(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    world.setTimeOfDay(MIDDAY);
  }

  @AfterBatch(batchId = BATCH)
  public void restoreDaylight(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void exploringTakesTheDogOutOfTheWorld(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, owner);

    chooseExplore(dog, owner);

    context.assertTrue(dog.isRemoved(), "The dog should leave the world");
    context.assertTrue(
        context.getWorld().getEntity(dog.getUuid()) == null, "Nothing should stand in for it");
    context.assertTrue(
        Expeditions.get(context.getWorld().getServer()).isAway(dog.getUuid()),
        "The dog should be recorded as away");
    Expeditions.get(context.getWorld().getServer()).remove(dog.getUuid());
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aDogThatCannotBeRebuiltStaysAwayInsteadOfVanishing(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final Expedition broken =
        new Expedition(
            UUID.randomUUID(),
            owner.getUuid(),
            Identifier.of(DogsUnleashed.MOD_ID, "not_a_dog"),
            new NbtCompound(),
            Identifier.ofVanilla("overworld"),
            0,
            0,
            0L,
            0L);
    final Expeditions expeditions = Expeditions.get(context.getWorld().getServer());
    expeditions.add(broken);

    context.assertTrue(
        ExpeditionHomecoming.returnHome(broken, owner).isEmpty(), "Nothing should come back");
    context.assertTrue(expeditions.isAway(broken.pet()), "The saved dog should be kept");
    expeditions.remove(broken.pet());
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aStaleCopyOfAnAwayDogIsDiscardedWhenItLoads(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity copy = ownedDog(context, owner);
    final Expeditions expeditions = Expeditions.get(context.getWorld().getServer());
    expeditions.add(
        new Expedition(
            copy.getUuid(),
            owner.getUuid(),
            Identifier.of(DogsUnleashed.MOD_ID, "husky"),
            new NbtCompound(),
            Identifier.ofVanilla("overworld"),
            0,
            0,
            Long.MAX_VALUE,
            Long.MAX_VALUE));

    ExpeditionListener.discardIfAway(copy, context.getWorld());

    context.runAtEveryTick(
        () -> {
          if (copy.isRemoved()) {
            expeditions.remove(copy.getUuid());
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void theDogComesHomeWithOneFindAndAMap(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, owner);

    chooseExplore(dog, owner);
    final UnleashedDogEntity back =
        ExpeditionHomecoming.returnHome(away(context, dog), owner).orElseThrow();

    context.assertEquals(back.getUuid(), dog.getUuid(), "The same dog should come home");
    context.assertTrue(back.isAlive() && !back.isRemoved(), "The dog should be back in the world");
    final List<ItemStack> drops = drops(context);
    context.assertEquals(drops.size(), 2, "Stacks dropped at the owner's feet");
    final List<ItemStack> maps =
        drops.stream().filter(stack -> stack.isOf(Items.FILLED_MAP)).toList();
    context.assertEquals(maps.size(), 1, "Maps brought back");
    final MapDecorationsComponent marks =
        maps.getFirst()
            .getOrDefault(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT);
    context.assertEquals(marks.decorations().size(), 1, "Markers on the map");
    context.assertTrue(
        maps.getFirst().get(DataComponentTypes.ITEM_NAME).getContent()
                instanceof TranslatableTextContent name
            && name.getKey().equals(ExpeditionFinds.MAP_NAME_KEY),
        "The map should be named as an expedition map");
    context.assertFalse(
        Expeditions.get(context.getWorld().getServer()).isAway(dog.getUuid()),
        "The trip should be over");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void theTripLeavesTheDogsGearUntouched(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, owner);
    final ItemStack armour = new ItemStack(ModArmorItems.of(DogArmorTier.IRON));
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, armour.copy());
    dog.getComponents().get(DogCollar.class).setColor(DyeColor.LIME);

    chooseExplore(dog, owner);
    final UnleashedDogEntity back =
        ExpeditionHomecoming.returnHome(away(context, dog), owner).orElseThrow();

    context.assertTrue(
        ItemStack.areEqual(armour, back.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR)),
        "The armour should come back as it left");
    context.assertEquals(
        back.getComponents().get(DogCollar.class).getColor(), DyeColor.LIME, "Collar colour");
    context.assertEquals(back.getHealth(), dog.getHealth(), "Health");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aSittingDogStaysHome(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, owner);
    dog.setSitting(true);
    dog.setInSittingPose(true);

    chooseExplore(dog, owner);

    context.assertFalse(dog.isRemoved(), "A sitting dog should stay");
    context.assertFalse(
        Expeditions.get(context.getWorld().getServer()).isAway(dog.getUuid()),
        "A sitting dog should not be recorded as away");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void comingHomeUnlocksTheExpeditionAdvancement(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = ownedDog(context, owner);
    final AdvancementEntry offExploring =
        context
            .getWorld()
            .getServer()
            .getAdvancementLoader()
            .get(Identifier.of(DogsUnleashed.MOD_ID, "off_exploring"));

    chooseExplore(dog, owner);
    context.assertFalse(
        owner.getAdvancementTracker().getProgress(offExploring).isDone(),
        "Setting off alone should not unlock it");
    ExpeditionHomecoming.returnHome(away(context, dog), owner);

    context.assertTrue(
        owner.getAdvancementTracker().getProgress(offExploring).isDone(),
        "Coming home should unlock off_exploring");
    context.complete();
  }

  private static UnleashedDogEntity ownedDog(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS, owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }

  private static void chooseExplore(final UnleashedDogEntity dog, final ServerPlayerEntity owner) {
    WheelActionNetworkHandler.selectSubAction(
        DogWheelSubActions.REGISTERED,
        new SelectWheelSubActionPayload(dog.getUuid(), DogExpeditions.WHEEL_ACTION_ID),
        owner);
  }

  private static Expedition away(final TestContext context, final Entity dog) {
    return Expeditions.get(context.getWorld().getServer()).of(dog.getUuid()).orElseThrow();
  }

  private static List<ItemStack> drops(final TestContext context) {
    return context
        .getWorld()
        .getEntitiesByClass(ItemEntity.class, context.getTestBox().expand(2.0), entity -> true)
        .stream()
        .map(ItemEntity::getStack)
        .toList();
  }
}
