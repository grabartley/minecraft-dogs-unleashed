package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModCostumeItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import com.grahambartley.dogsunleashed.item.costume.DogCostume;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.GameMode;

public final class DogCostumeGameTest implements FabricGameTest {

  private static final String BATCH = "dog-costumes";
  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  private static final BlockPos OWNER_POS = new BlockPos(4, 2, 3);
  private static final BlockPos STRANGER_POS = new BlockPos(2, 2, 3);
  private static final int TICK_LIMIT = 20;
  private static final int DEATH_TICK_LIMIT = 40;
  private static final int DEATH_ASSERT_TICK = 20;
  private static final Item COSTUME = Items.NAUTILUS_SHELL;
  private static final int GRID_WIDTH = 3;
  private static final int STAT_SETTLE_TICKS = 5;
  private static final Item NONE = Items.AIR;
  private static final int RED_DYE_COLOR = 0xB02E26;

  private static final Map<DogCostume, List<Item>> RECIPE_GRIDS =
      Map.ofEntries(
          Map.entry(
              DogCostume.SPIDER,
              List.of(
                  Items.STRING,
                  Items.SPIDER_EYE,
                  Items.STRING,
                  Items.STRING,
                  Items.BLACK_WOOL,
                  Items.STRING)),
          Map.entry(
              DogCostume.ZOMBIE,
              List.of(
                  Items.ROTTEN_FLESH,
                  NONE,
                  Items.ROTTEN_FLESH,
                  Items.ROTTEN_FLESH,
                  Items.BLUE_WOOL,
                  Items.ROTTEN_FLESH)),
          Map.entry(
              DogCostume.SKELETON,
              List.of(Items.BONE, NONE, Items.BONE, Items.BONE, Items.WHITE_WOOL, Items.BONE)),
          Map.entry(
              DogCostume.ENDERMAN,
              List.of(NONE, Items.ENDER_PEARL, NONE, NONE, Items.BLACK_WOOL, NONE)),
          Map.entry(
              DogCostume.BEE,
              List.of(
                  Items.HONEYCOMB,
                  NONE,
                  Items.HONEYCOMB,
                  Items.YELLOW_WOOL,
                  Items.BLACK_WOOL,
                  Items.YELLOW_WOOL)),
          Map.entry(
              DogCostume.ENDER_DRAGON,
              List.of(
                  NONE,
                  Items.DRAGON_BREATH,
                  NONE,
                  Items.PHANTOM_MEMBRANE,
                  Items.BLACK_WOOL,
                  Items.PHANTOM_MEMBRANE)),
          Map.entry(
              DogCostume.CREEPER,
              List.of(
                  Items.GUNPOWDER,
                  NONE,
                  Items.GUNPOWDER,
                  Items.GUNPOWDER,
                  Items.LIME_WOOL,
                  Items.GUNPOWDER)),
          Map.entry(
              DogCostume.WITHER,
              List.of(NONE, Items.WITHER_ROSE, NONE, NONE, Items.BLACK_WOOL, NONE)),
          Map.entry(
              DogCostume.WARDEN, List.of(NONE, Items.ECHO_SHARD, NONE, NONE, Items.SCULK, NONE)),
          Map.entry(
              DogCostume.SHEEP,
              List.of(
                  Items.WHITE_WOOL,
                  NONE,
                  Items.WHITE_WOOL,
                  Items.WHITE_WOOL,
                  Items.WHITE_WOOL,
                  Items.WHITE_WOOL)),
          Map.entry(
              DogCostume.MOOSHROOM,
              List.of(
                  Items.RED_MUSHROOM,
                  NONE,
                  Items.RED_MUSHROOM,
                  Items.RED_MUSHROOM,
                  Items.RED_WOOL,
                  Items.RED_MUSHROOM)),
          Map.entry(
              DogCostume.CHICKEN,
              List.of(
                  Items.FEATHER,
                  NONE,
                  Items.FEATHER,
                  Items.FEATHER,
                  Items.WHITE_WOOL,
                  Items.FEATHER)),
          Map.entry(
              DogCostume.AXOLOTL,
              List.of(NONE, Items.TROPICAL_FISH, NONE, NONE, Items.PINK_WOOL, NONE)),
          Map.entry(
              DogCostume.FROG,
              List.of(
                  Items.SLIME_BALL,
                  NONE,
                  Items.SLIME_BALL,
                  Items.SLIME_BALL,
                  Items.GREEN_WOOL,
                  Items.SLIME_BALL)),
          Map.entry(
              DogCostume.PANDA,
              List.of(
                  Items.BAMBOO,
                  NONE,
                  Items.BAMBOO,
                  Items.BLACK_WOOL,
                  Items.WHITE_WOOL,
                  Items.BLACK_WOOL)),
          Map.entry(
              DogCostume.TURTLE,
              List.of(
                  Items.TURTLE_SCUTE, NONE, Items.TURTLE_SCUTE, NONE, Items.TURTLE_SCUTE, NONE)),
          Map.entry(
              DogCostume.ALLAY,
              List.of(NONE, Items.AMETHYST_SHARD, NONE, NONE, Items.LIGHT_BLUE_WOOL, NONE)));

  private static final List<RegistryEntry<EntityAttribute>> STATS =
      List.of(
          EntityAttributes.GENERIC_MAX_HEALTH,
          EntityAttributes.GENERIC_ARMOR,
          EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
          EntityAttributes.GENERIC_MOVEMENT_SPEED,
          EntityAttributes.GENERIC_ATTACK_DAMAGE,
          EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);

  @CustomTestProvider
  public Collection<TestFunction> everyCostumeCraftsFromItsRecipe() {
    return Stream.of(DogCostume.values())
        .map(
            costume ->
                GeneratedGameTest.of(
                    BATCH,
                    "dogcostumegametest.crafts." + costume.mob(),
                    ARENA,
                    TICK_LIMIT,
                    context -> {
                      final ItemStack crafted =
                          craft(context.getWorld(), RECIPE_GRIDS.get(costume));
                      context.assertTrue(
                          crafted.isOf(ModCostumeItems.of(costume)) && crafted.getCount() == 1,
                          costume + " recipe should craft one costume but made " + crafted);
                      context.complete();
                    }))
        .toList();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyCostumeFitsTheCostumeSlotAndNoOther(final TestContext context) {
    for (final DogCostume costume : DogCostume.values()) {
      final ItemStack stack = new ItemStack(ModCostumeItems.of(costume));
      for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
        context.assertTrue(
            slot.canHold(stack) == (slot == DogEquipmentSlot.COSTUME),
            costume + " fits the " + slot + " slot: " + slot.canHold(stack));
      }
      context.assertEquals(
          stack.getMaxCount(), 1, costume + " should not stack, like the other dog gear");
    }
    context.complete();
  }

  @CustomTestProvider
  public Collection<TestFunction> aWornCostumeChangesNoStats() {
    return Stream.of(DogCostume.values())
        .map(
            costume ->
                GeneratedGameTest.of(
                    BATCH,
                    "dogcostumegametest.nostats." + costume.mob(),
                    ARENA,
                    TICK_LIMIT,
                    context -> {
                      final UnleashedDogEntity dog =
                          DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
                      dog.setAiDisabled(true);
                      final List<Double> bare = STATS.stream().map(dog::getAttributeValue).toList();
                      dog.getEquipmentHolder()
                          .setStack(
                              DogEquipmentSlot.COSTUME, new ItemStack(ModCostumeItems.of(costume)));
                      context.runAtTick(
                          STAT_SETTLE_TICKS,
                          () -> {
                            context.assertTrue(
                                STATS.stream().map(dog::getAttributeValue).toList().equals(bare),
                                costume + " changed the dog's stats");
                            context.complete();
                          });
                    }))
        .toList();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void costumeSlotTakesTaggedCostumesAndNothingElse(final TestContext context) {
    final ItemStack costume = new ItemStack(COSTUME);
    context.assertTrue(
        DogEquipmentSlot.COSTUME.canHold(costume), "A tagged costume should fit the costume slot");
    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      if (slot != DogEquipmentSlot.COSTUME) {
        context.assertFalse(slot.canHold(costume), "A costume must not fit the " + slot + " slot");
      }
    }
    context.assertFalse(
        DogEquipmentSlot.COSTUME.canHold(new ItemStack(Items.WOLF_ARMOR)),
        "The costume slot must reject armour");
    context.assertFalse(
        DogEquipmentSlot.COSTUME.canHold(new ItemStack(Items.CARVED_PUMPKIN)),
        "The costume slot must reject cosmetics");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void ownerPutsACostumeOnByRightClickAndGetsTheOldOneBack(final TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    final ItemStack old = new ItemStack(COSTUME);
    old.set(DataComponentTypes.CUSTOM_NAME, Text.literal("old"));
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSTUME, old);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(COSTUME, 2));

    dog.interactMob(owner, Hand.MAIN_HAND);

    final ItemStack worn = dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME);
    context.assertTrue(
        worn.isOf(COSTUME) && !worn.contains(DataComponentTypes.CUSTOM_NAME),
        "Right-clicking with a costume should put the held one on");
    context.assertEquals(
        owner.getStackInHand(Hand.MAIN_HAND).getCount(), 1, "Exactly one costume should be used");
    context.assertTrue(
        owner.getInventory().main.stream()
            .anyMatch(
                stack -> stack.isOf(COSTUME) && stack.contains(DataComponentTypes.CUSTOM_NAME)),
        "The costume the dog wore should be handed back");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void puppiesCanWearACostume(final TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final UnleashedDogEntity puppy = spawnOwnedDog(context, owner);
    puppy.setBaby(true);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(COSTUME));

    puppy.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        puppy.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME).isOf(COSTUME),
        "A puppy should take a costume");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void strangerCannotPutACostumeOn(final TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    final ServerPlayerEntity stranger = survivalPlayerAt(context, STRANGER_POS);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(COSTUME));

    dog.interactMob(stranger, Hand.MAIN_HAND);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME).isEmpty(),
        "A stranger must not dress someone else's dog");
    context.assertTrue(
        stranger.getStackInHand(Hand.MAIN_HAND).isOf(COSTUME),
        "The stranger should keep the costume");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void strangerCannotTakeACostumeOff(final TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSTUME, new ItemStack(COSTUME));
    final ServerPlayerEntity stranger = survivalPlayerAt(context, STRANGER_POS);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(COSTUME));

    dog.interactMob(stranger, Hand.MAIN_HAND);
    dog.interactMob(stranger, Hand.OFF_HAND);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME).isOf(COSTUME),
        "The dog should keep its costume");
    context.assertEquals(
        stranger.getStackInHand(Hand.MAIN_HAND).getCount(),
        1,
        "A stranger must not swap the costume for their own");
    context.assertFalse(
        new DogEquipmentScreenHandler(1, stranger.getInventory(), dog).canUse(stranger),
        "A stranger must not reach the costume through the gear screen");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void costumeSurvivesAReload(final TestContext context) {
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSTUME, new ItemStack(COSTUME));

    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);
    final UnleashedDogEntity reloaded =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    reloaded.setAiDisabled(true);
    reloaded.readCustomDataFromNbt(nbt);

    context.assertTrue(
        reloaded.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME).isOf(COSTUME),
        "The costume should still be worn after a reload");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = DEATH_TICK_LIMIT)
  public void costumeDropsWhenTheDogDies(final TestContext context) {
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSTUME, new ItemStack(COSTUME));

    dog.kill();

    context.runAtTick(
        DEATH_ASSERT_TICK,
        () -> {
          context.assertEquals(
              DogTestHelper.droppedStacksOf(context, COSTUME).size(),
              1,
              "Exactly one costume should drop on death");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sheepCostumeTakesDyeLikeLeatherArmour(final TestContext context) {
    final ItemStack dyed =
        craft(
            context.getWorld(),
            List.of(
                ModCostumeItems.of(DogCostume.SHEEP),
                Items.RED_DYE,
                NONE,
                NONE,
                NONE,
                NONE,
                NONE,
                NONE,
                NONE));
    context.assertTrue(
        dyed.isOf(ModCostumeItems.of(DogCostume.SHEEP))
            && DyedColorComponent.getColor(dyed, DogCostume.SHEEP.undyedColor())
                == ColorHelper.Argb.fullAlpha(RED_DYE_COLOR),
        "A sheep costume and red dye should craft a red sheep costume but made " + dyed);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void onlyTheSheepCostumeTakesDye(final TestContext context) {
    for (final DogCostume costume : DogCostume.values()) {
      context.assertTrue(
          new ItemStack(ModCostumeItems.of(costume)).isIn(ItemTags.DYEABLE) == costume.isDyeable(),
          costume + (costume.isDyeable() ? " should" : " should not") + " take dye");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aWaterCauldronWashesTheDyeOutOfASheepCostume(final TestContext context) {
    final BlockPos cauldron = new BlockPos(1, 2, 1);
    context.setBlockState(
        cauldron, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final ItemStack dyed = new ItemStack(ModCostumeItems.of(DogCostume.SHEEP));
    dyed.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(RED_DYE_COLOR, true));
    owner.setStackInHand(Hand.MAIN_HAND, dyed);

    context.useBlock(cauldron, owner);

    context.assertFalse(
        owner.getStackInHand(Hand.MAIN_HAND).contains(DataComponentTypes.DYED_COLOR),
        "Washing should clear the sheep costume's dye");
    context.assertEquals(
        context.getBlockState(cauldron).get(LeveledCauldronBlock.LEVEL),
        2,
        "Washing should use one level of water");
    context.complete();
  }

  private static ItemStack craft(final ServerWorld world, final List<Item> grid) {
    final CraftingRecipeInput input =
        CraftingRecipeInput.create(
            GRID_WIDTH, grid.size() / GRID_WIDTH, grid.stream().map(ItemStack::new).toList());
    return world
        .getRecipeManager()
        .getFirstMatch(RecipeType.CRAFTING, input, world)
        .map(entry -> entry.value().craft(input, world.getRegistryManager()))
        .orElse(ItemStack.EMPTY);
  }

  private static UnleashedDogEntity spawnOwnedDog(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS, owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }

  private static ServerPlayerEntity survivalPlayerAt(
      final TestContext context, final BlockPos relativePos) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.changeGameMode(GameMode.SURVIVAL);
    final BlockPos absolutePos = context.getAbsolutePos(relativePos);
    player.refreshPositionAndAngles(
        absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0f, 0f);
    return player;
  }
}
