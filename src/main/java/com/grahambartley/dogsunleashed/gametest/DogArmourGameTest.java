package com.grahambartley.dogsunleashed.gametest;

import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.ARENA;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.DOG_POS;
import static com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper.OWNER_POS;

import com.grahambartley.dogsunleashed.ModArmorItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.armor.DogArmorTier;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public final class DogArmourGameTest implements FabricGameTest {

  private static final String BATCH = "dog-armour";
  private static final int TICK_LIMIT = 20;
  private static final int FIRST_HOTBAR_SLOT_INDEX = DogEquipmentScreenHandler.DOG_SLOT_COUNT + 27;
  private static final float HIT = 4.0f;
  private static final float BIG_HIT = 20.0f;
  private static final int ARMOUR_APPLIED_TICK = 2;
  private static final int ARMOUR_SLOT_INDEX = DogEquipmentSlot.ARMOUR.ordinal();

  private record Armour(String id, Item item, Item repairItem, int mendedPerItem) {}

  private static Stream<Armour> everyCanineArmour() {
    return Stream.of(
        tier(DogArmorTier.LEATHER, 8),
        tier(DogArmorTier.IRON, 16),
        tier(DogArmorTier.GOLD, 12),
        tier(DogArmorTier.DIAMOND, 32),
        tier(DogArmorTier.NETHERITE, 40),
        new Armour("wolf", Items.WOLF_ARMOR, Items.ARMADILLO_SCUTE, 8));
  }

  private static Armour tier(final DogArmorTier tier, final int mendedPerItem) {
    return new Armour(tier.id(), ModArmorItems.of(tier), tier.repairItem(), mendedPerItem);
  }

  private static Collection<TestFunction> perArmour(
      final String name, final BiConsumer<TestContext, Armour> body) {
    return everyCanineArmour()
        .map(
            armour ->
                GeneratedGameTest.of(
                    BATCH,
                    "dogarmourgametest." + name + "." + armour.id(),
                    ARENA,
                    TICK_LIMIT,
                    context -> body.accept(context, armour)))
        .toList();
  }

  @CustomTestProvider
  public Collection<TestFunction> armourTakesTheHitInsteadOfTheDog() {
    return perArmour("absorbs", DogArmourGameTest::testAbsorbs);
  }

  private static void testAbsorbs(final TestContext context, final Armour armour) {
    final UnleashedDogEntity dog = armouredDog(context, armour.item());

    dog.damage(context.getWorld().getDamageSources().generic(), HIT);

    context.assertEquals(dog.getHealth(), dog.getMaxHealth(), "The dog should take no damage");
    context.assertTrue(
        dog.getArmour().worn().getDamage() > 0, armour.id() + " should wear from the hit");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void betterArmourWearsMoreSlowly(final TestContext context) {
    final List<UnleashedDogEntity> dogs =
        Arrays.stream(DogArmorTier.values())
            .map(tier -> armouredDog(context, ModArmorItems.of(tier)))
            .toList();

    context.runAtTick(
        ARMOUR_APPLIED_TICK,
        () -> {
          dogs.forEach(dog -> dog.damage(context.getWorld().getDamageSources().cactus(), BIG_HIT));
          for (int i = 1; i < dogs.size(); i++) {
            context.assertTrue(
                wear(dogs.get(i)) <= wear(dogs.get(i - 1)),
                DogArmorTier.values()[i].id() + " should wear no faster than the tier below");
          }
          context.assertTrue(
              wear(dogs.get(DogArmorTier.DIAMOND.ordinal()))
                  < wear(dogs.get(DogArmorTier.LEATHER.ordinal())),
              "Diamond should lose less durability than leather from the same hit");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void hitsThatIgnoreArmourWearItInFull(final TestContext context) {
    final UnleashedDogEntity dog = armouredDog(context, ModArmorItems.of(DogArmorTier.DIAMOND));

    context.runAtTick(
        ARMOUR_APPLIED_TICK,
        () -> {
          dog.damage(context.getWorld().getDamageSources().generic(), BIG_HIT);
          context.assertEquals(
              wear(dog), (int) BIG_HIT, "Protection does not soften a hit that ignores armour");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aHarmlessHitDoesNotWearTheArmour(final TestContext context) {
    final UnleashedDogEntity dog = armouredDog(context, ModArmorItems.of(DogArmorTier.LEATHER));

    dog.damage(context.getWorld().getDamageSources().cactus(), 0.0f);

    context.assertEquals(wear(dog), 0, "A hit that deals nothing should not wear the armour");
    context.complete();
  }

  private static int wear(final UnleashedDogEntity dog) {
    return dog.getArmour().worn().getDamage();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aHitAPendantBlocksLeavesTheArmourAlone(final TestContext context) {
    final UnleashedDogEntity dog = armouredDog(context, ModArmorItems.of(DogArmorTier.IRON));
    PendantTestHelper.wear(dog, PendantType.GUARDIAN_FIRE);

    dog.damage(context.getWorld().getDamageSources().onFire(), HIT);

    context.assertEquals(
        dog.getArmour().worn().getDamage(), 0, "A fire guardian should spare the armour too");
    context.complete();
  }

  @CustomTestProvider
  public Collection<TestFunction> equipmentScreenTakesEveryArmour() {
    return perArmour("screen", DogArmourGameTest::testEquipmentScreen);
  }

  private static void testEquipmentScreen(final TestContext context, final Armour armour) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = PendantTestHelper.dogWearing(context, null, owner, DOG_POS);
    owner.getInventory().setStack(0, new ItemStack(armour.item()));
    final DogEquipmentScreenHandler handler =
        new DogEquipmentScreenHandler(1, owner.getInventory(), dog);

    handler.quickMove(owner, FIRST_HOTBAR_SLOT_INDEX);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isOf(armour.item()),
        "Shift-clicking " + armour.id() + " in the equipment screen should put it on the dog");
    context.assertTrue(owner.getInventory().getStack(0).isEmpty(), "It should leave the hotbar");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPuppyRefusesArmourFromTheHand(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity puppy = PendantTestHelper.dogWearing(context, null, owner, DOG_POS);
    puppy.setBaby(true);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOLF_ARMOR));

    puppy.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(puppy.getArmour().worn().isEmpty(), "A puppy is too small for armour");
    context.assertEquals(
        owner.getStackInHand(Hand.MAIN_HAND).getCount(), 1, "The armour stays in the hand");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPuppyRefusesArmourInTheGearScreen(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity puppy = PendantTestHelper.dogWearing(context, null, owner, DOG_POS);
    puppy.setBaby(true);
    owner.getInventory().setStack(0, new ItemStack(Items.WOLF_ARMOR));
    final DogEquipmentScreenHandler handler = openHandler(owner, puppy);

    handler.onSlotClick(FIRST_HOTBAR_SLOT_INDEX, 0, SlotActionType.QUICK_MOVE, owner);
    context.assertTrue(puppy.getArmour().worn().isEmpty(), "Shift-click cannot fit a puppy");

    owner.getInventory().setStack(0, new ItemStack(Items.WOLF_ARMOR));
    handler.onSlotClick(FIRST_HOTBAR_SLOT_INDEX, 0, SlotActionType.PICKUP, owner);
    handler.onSlotClick(ARMOUR_SLOT_INDEX, 0, SlotActionType.PICKUP, owner);

    context.assertTrue(puppy.getArmour().worn().isEmpty(), "Placing cannot fit a puppy");
    context.assertTrue(
        handler.getCursorStack().isOf(Items.WOLF_ARMOR), "The armour stays with the owner");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void bindingKeepsArmourOnInTheGearScreen(final TestContext context) {
    final PlayerEntity owner = survivalPlayerAt(context);
    final UnleashedDogEntity dog = PendantTestHelper.dogWearing(context, null, owner, DOG_POS);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, bound(context, Items.WOLF_ARMOR));
    owner.getInventory().setStack(0, new ItemStack(ModArmorItems.of(DogArmorTier.IRON)));
    final DogEquipmentScreenHandler handler = openHandler(owner, dog);

    for (final SlotActionType click :
        List.of(
            SlotActionType.QUICK_MOVE,
            SlotActionType.PICKUP,
            SlotActionType.THROW,
            SlotActionType.SWAP)) {
      handler.onSlotClick(ARMOUR_SLOT_INDEX, 0, click, owner);
      context.assertTrue(
          dog.getArmour().worn().isOf(Items.WOLF_ARMOR), "Cursed armour survives " + click);
    }
    handler.onSlotClick(FIRST_HOTBAR_SLOT_INDEX, 0, SlotActionType.PICKUP, owner);
    handler.onSlotClick(ARMOUR_SLOT_INDEX, 0, SlotActionType.PICKUP, owner);

    context.assertTrue(
        dog.getArmour().worn().isOf(Items.WOLF_ARMOR), "Cursed armour survives a swap");
    context.assertTrue(
        handler.getCursorStack().isOf(ModArmorItems.of(DogArmorTier.IRON)),
        "Cursed armour cannot be swapped out");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void creativeModeTakesCursedArmourOff(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity dog = PendantTestHelper.dogWearing(context, null, owner, DOG_POS);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, bound(context, Items.WOLF_ARMOR));
    final DogEquipmentScreenHandler handler = openHandler(owner, dog);

    handler.onSlotClick(ARMOUR_SLOT_INDEX, 0, SlotActionType.PICKUP, owner);

    context.assertTrue(dog.getArmour().worn().isEmpty(), "Creative mode ignores the curse");
    context.assertTrue(handler.getCursorStack().isOf(Items.WOLF_ARMOR), "It lands on the cursor");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void bindingStopsArmourBeingSwappedFromTheHand(final TestContext context) {
    final PlayerEntity owner = survivalPlayerAt(context);
    final UnleashedDogEntity dog = PendantTestHelper.dogWearing(context, null, owner, DOG_POS);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, bound(context, Items.WOLF_ARMOR));
    final Item iron = ModArmorItems.of(DogArmorTier.IRON);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(iron));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        dog.getArmour().worn().isOf(Items.WOLF_ARMOR), "Cursed armour stays on the dog");
    context.assertTrue(owner.getStackInHand(Hand.MAIN_HAND).isOf(iron), "The swap is refused");
    context.complete();
  }

  private static PlayerEntity survivalPlayerAt(final TestContext context) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    PendantTestHelper.placeAt(context, player, OWNER_POS);
    return player;
  }

  private static DogEquipmentScreenHandler openHandler(
      final PlayerEntity owner, final UnleashedDogEntity dog) {
    return new DogEquipmentScreenHandler(1, owner.getInventory(), dog);
  }

  private static ItemStack bound(final TestContext context, final Item armour) {
    final ItemStack stack = new ItemStack(armour);
    stack.addEnchantment(
        context
            .getWorld()
            .getRegistryManager()
            .get(RegistryKeys.ENCHANTMENT)
            .entryOf(Enchantments.BINDING_CURSE),
        1);
    return stack;
  }

  @CustomTestProvider
  public Collection<TestFunction> repairMaterialMendsTheArmour() {
    return perArmour("repair", DogArmourGameTest::testRepair);
  }

  private static void testRepair(final TestContext context, final Armour armour) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = armouredDog(context, owner, armour.item());
    final ItemStack worn = dog.getArmour().worn();
    final int damaged = worn.getMaxDamage() / 2;
    worn.setDamage(damaged);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(armour.repairItem(), 2));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertEquals(
        dog.getArmour().worn().getDamage(),
        damaged - armour.mendedPerItem(),
        armour.id() + " should mend by " + armour.mendedPerItem());
    context.assertEquals(
        owner.getStackInHand(Hand.MAIN_HAND).getCount(), 1, "One repair item should be used");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void undamagedArmourIgnoresItsRepairMaterial(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = armouredDog(context, owner, ModArmorItems.of(DogArmorTier.IRON));
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 2));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertEquals(
        owner.getStackInHand(Hand.MAIN_HAND).getCount(), 2, "Nothing to mend, nothing used");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aStrangerCannotMendSomeoneElsesDogArmour(final TestContext context) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger =
        PendantTestHelper.survivalOwnerAt(context, new BlockPos(2, 2, 3));
    final UnleashedDogEntity dog = armouredDog(context, owner, ModArmorItems.of(DogArmorTier.IRON));
    dog.getArmour().worn().setDamage(10);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 2));

    dog.interactMob(stranger, Hand.MAIN_HAND);

    context.assertEquals(dog.getArmour().worn().getDamage(), 10, "Only the owner mends armour");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void damageThatBypassesWolfArmourHitsTheDog(final TestContext context) {
    final UnleashedDogEntity dog = armouredDog(context, ModArmorItems.of(DogArmorTier.DIAMOND));

    dog.damage(context.getWorld().getDamageSources().magic(), HIT);

    context.assertTrue(dog.getHealth() < dog.getMaxHealth(), "Magic should hurt the dog");
    context.assertEquals(dog.getArmour().worn().getDamage(), 0, "Magic should skip the armour");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void wornOutArmourShattersAndTheDogTakesTheNextHit(final TestContext context) {
    final UnleashedDogEntity dog = armouredDog(context, ModArmorItems.of(DogArmorTier.LEATHER));
    final ItemStack worn = dog.getArmour().worn();
    worn.setDamage(worn.getMaxDamage() - 1);

    dog.damage(context.getWorld().getDamageSources().generic(), HIT);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isEmpty(),
        "Worn out armour should shatter and leave the slot empty");
    context.assertEquals(dog.getHealth(), dog.getMaxHealth(), "The shattering hit is absorbed");

    dog.timeUntilRegen = 0;
    dog.damage(context.getWorld().getDamageSources().generic(), HIT);

    context.assertTrue(dog.getHealth() < dog.getMaxHealth(), "With no armour the dog is hurt");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void registeredMaterialsGiveEachTierItsProtection(final TestContext context) {
    final int armadillo = ArmorMaterials.ARMADILLO.value().getProtection(ArmorItem.Type.BODY);
    for (final DogArmorTier tier : DogArmorTier.values()) {
      context.assertEquals(
          ModArmorItems.of(tier).getMaterial().value().getProtection(ArmorItem.Type.BODY),
          tier.defense(armadillo),
          tier.id() + " should protect the body slot by its tier value");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void onlyLeatherTakesDye(final TestContext context) {
    for (final DogArmorTier tier : DogArmorTier.values()) {
      context.assertTrue(
          new ItemStack(ModArmorItems.of(tier)).isIn(ItemTags.DYEABLE)
              == (tier == DogArmorTier.LEATHER),
          tier.id() + (tier == DogArmorTier.LEATHER ? " should" : " should not") + " be dyeable");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyDyeGivesLeatherDogArmourItsOwnColour(final TestContext context) {
    final Set<Integer> colours = new HashSet<>();
    for (final DyeColor dye : DyeColor.values()) {
      final ItemStack dyed =
          craftSmall(
              context.getWorld(),
              List.of(
                  new ItemStack(ModArmorItems.of(DogArmorTier.LEATHER)),
                  new ItemStack(DyeItem.byColor(dye))));
      context.assertTrue(
          dyed.isOf(ModArmorItems.of(DogArmorTier.LEATHER))
              && dyed.contains(DataComponentTypes.DYED_COLOR),
          dye + " dye should colour leather dog armour, got " + dyed);
      colours.add(dyed.get(DataComponentTypes.DYED_COLOR).rgb());
    }
    context.assertEquals(colours.size(), DyeColor.values().length, "Every dye should differ");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void dyesMixOnLeatherDogArmour(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final ItemStack red =
        craftSmall(
            world,
            List.of(
                new ItemStack(ModArmorItems.of(DogArmorTier.LEATHER)),
                new ItemStack(Items.RED_DYE)));
    final ItemStack mixed = craftSmall(world, List.of(red.copy(), new ItemStack(Items.YELLOW_DYE)));

    context.assertTrue(
        mixed.contains(DataComponentTypes.DYED_COLOR)
            && mixed.get(DataComponentTypes.DYED_COLOR).rgb()
                != red.get(DataComponentTypes.DYED_COLOR).rgb(),
        "Adding yellow to red leather should blend a new colour");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aWaterCauldronWashesTheDyeOut(final TestContext context) {
    final ServerPlayerEntity player = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    final BlockPos cauldron = new BlockPos(2, 2, 3);
    context.setBlockState(
        cauldron, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
    final ItemStack dyed = new ItemStack(ModArmorItems.of(DogArmorTier.LEATHER));
    dyed.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x3C44AA, true));
    player.setStackInHand(Hand.MAIN_HAND, dyed);
    final BlockPos absolute = context.getAbsolutePos(cauldron);

    context
        .getWorld()
        .getBlockState(absolute)
        .onUseWithItem(
            dyed,
            context.getWorld(),
            player,
            Hand.MAIN_HAND,
            new BlockHitResult(Vec3d.ofCenter(absolute), Direction.UP, absolute, false));

    context.assertFalse(
        player.getStackInHand(Hand.MAIN_HAND).contains(DataComponentTypes.DYED_COLOR),
        "The cauldron should wash the dye out");
    context.assertEquals(
        context.getBlockState(cauldron).get(LeveledCauldronBlock.LEVEL), 2, "It uses some water");
    context.complete();
  }

  private static ItemStack craftSmall(final ServerWorld world, final List<ItemStack> items) {
    final CraftingRecipeInput input = CraftingRecipeInput.create(2, 1, items);
    return world
        .getRecipeManager()
        .getFirstMatch(RecipeType.CRAFTING, input, world)
        .map(entry -> entry.value().craft(input, world.getRegistryManager()))
        .orElse(ItemStack.EMPTY);
  }

  @CustomTestProvider
  public Collection<TestFunction> eachTierIsCraftedFromItsMaterialAndLeather() {
    return Arrays.stream(DogArmorTier.values())
        .filter(tier -> tier != DogArmorTier.NETHERITE)
        .map(
            tier ->
                GeneratedGameTest.of(
                    BATCH,
                    "dogarmourgametest.recipe." + tier.id(),
                    ARENA,
                    TICK_LIMIT,
                    context -> testRecipe(context, tier)))
        .toList();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void netheriteIsASmithingUpgradeOfDiamond(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final SmithingRecipeInput input =
        new SmithingRecipeInput(
            new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
            new ItemStack(ModArmorItems.of(DogArmorTier.DIAMOND)),
            new ItemStack(Items.NETHERITE_INGOT));

    final ItemStack result =
        world
            .getRecipeManager()
            .getFirstMatch(RecipeType.SMITHING, input, world)
            .map(entry -> entry.value().craft(input, world.getRegistryManager()))
            .orElse(ItemStack.EMPTY);

    context.assertTrue(
        result.isOf(ModArmorItems.of(DogArmorTier.NETHERITE)),
        "Diamond dog armour, a netherite ingot and the upgrade template should make netherite"
            + " dog armour, got "
            + result);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void netheriteCannotBeCraftedAtATable(final TestContext context) {
    final ServerWorld world = context.getWorld();
    world
        .getRecipeManager()
        .listAllOfType(RecipeType.CRAFTING)
        .forEach(
            entry ->
                context.assertFalse(
                    entry
                        .value()
                        .getResult(world.getRegistryManager())
                        .isOf(ModArmorItems.of(DogArmorTier.NETHERITE)),
                    entry.id() + " should not craft netherite dog armour"));
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyTierUnlocksInTheRecipeBook(final TestContext context) {
    final Set<Identifier> unlocked = new HashSet<>();
    context
        .getWorld()
        .getServer()
        .getAdvancementLoader()
        .getAdvancements()
        .forEach(entry -> unlocked.addAll(entry.value().rewards().recipes()));
    for (final DogArmorTier tier : DogArmorTier.values()) {
      final Identifier recipe = Identifier.of("dogs-unleashed", tier.itemId());
      context.assertTrue(
          context.getWorld().getRecipeManager().get(recipe).isPresent(),
          recipe + " should be a real recipe");
      context.assertTrue(
          unlocked.contains(recipe),
          tier.id() + " armour should unlock in the recipe book from its material");
    }
    context.complete();
  }

  private static void testRecipe(final TestContext context, final DogArmorTier tier) {
    final ItemStack material = new ItemStack(tier.repairItem());
    final List<ItemStack> grid = new ArrayList<>(List.of(material, material, material));
    grid.addAll(List.of(material, new ItemStack(Items.LEATHER), material));

    final ItemStack result = craft(context.getWorld(), grid);

    context.assertTrue(
        result.isOf(ModArmorItems.of(tier)), tier.id() + " recipe made " + result + " instead");
    context.complete();
  }

  private static UnleashedDogEntity armouredDog(final TestContext context, final Item armour) {
    final ServerPlayerEntity owner = PendantTestHelper.survivalOwnerAt(context, OWNER_POS);
    return armouredDog(context, owner, armour);
  }

  private static UnleashedDogEntity armouredDog(
      final TestContext context, final ServerPlayerEntity owner, final Item armour) {
    final UnleashedDogEntity dog = PendantTestHelper.dogWearing(context, null, owner, DOG_POS);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, new ItemStack(armour));
    return dog;
  }

  private static ItemStack craft(final ServerWorld world, final List<ItemStack> ingredients) {
    final CraftingRecipeInput input = CraftingRecipeInput.create(3, 2, ingredients);
    final Optional<RecipeEntry<CraftingRecipe>> match =
        world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, world);
    return match
        .map(entry -> entry.value().craft(input, world.getRegistryManager()))
        .orElse(ItemStack.EMPTY);
  }
}
