package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.DogGraveBlock;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;

public final class DogGraveGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_GRAVE = new BlockPos(3, 1, 3);
  private static final BlockPos REL_GRAVE_UPPER = new BlockPos(3, 2, 3);

  @GameTest(templateName = ARENA)
  public void dogGraveCanBePlaced(final TestContext context) {
    final BlockPos relGravePos = REL_GRAVE;
    final ServerWorld world = context.getWorld();

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    final BlockState placedState = world.getBlockState(gravePos);
    context.assertTrue(placedState.isOf(ModBlocks.DOG_GRAVE), "Dog grave should be placed");
    context.complete();
  }

  @GameTest(templateName = ARENA)
  public void dogGraveHasBlockEntity(final TestContext context) {
    final BlockPos relGravePos = REL_GRAVE;
    final ServerWorld world = context.getWorld();

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    final BlockEntity blockEntity = world.getBlockEntity(gravePos);
    context.assertTrue(
        blockEntity instanceof DogGraveBlockEntity, "Dog grave should have DogGraveBlockEntity");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 100)
  public void dogGraveStoresDogData(final TestContext context) {
    final BlockPos relGravePos = REL_GRAVE;
    final ServerWorld world = context.getWorld();

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    context.runAtTick(
        5,
        () -> {
          final BlockEntity blockEntity = world.getBlockEntity(gravePos);
          context.assertTrue(
              blockEntity instanceof DogGraveBlockEntity,
              "Block entity should be DogGraveBlockEntity");
          final DogGraveBlockEntity graveEntity = (DogGraveBlockEntity) blockEntity;

          final UUID dogUuid = UUID.randomUUID();
          final String dogName = "Good Boy";
          final DyeColor flowerColor = DyeColor.RED;

          graveEntity.setDogUuid(dogUuid);
          graveEntity.setDogName(dogName);
          graveEntity.setFlowerColor(flowerColor);

          context.assertTrue(graveEntity.getDogUuid().equals(dogUuid), "Dog UUID should match");
          context.assertTrue(graveEntity.getDogName().equals(dogName), "Dog name should match");
          context.assertTrue(
              graveEntity.getFlowerColor() == flowerColor, "Flower color should match");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 100)
  public void dogGraveItemRetainsData(final TestContext context) {
    final UUID dogUuid = UUID.randomUUID();
    final String dogName = "Memorial";
    final DyeColor flowerColor = DyeColor.PINK;

    final ItemStack graveStack = new ItemStack(ModBlocks.DOG_GRAVE);
    graveStack.set(ModComponents.DOG_GRAVE_UUID, dogUuid);
    graveStack.set(ModComponents.DOG_GRAVE_NAME, dogName);
    graveStack.set(ModComponents.DOG_GRAVE_FLOWER_COLOR, flowerColor);

    context.runAtTick(
        5,
        () -> {
          context.assertTrue(
              graveStack.get(ModComponents.DOG_GRAVE_UUID).equals(dogUuid),
              "Item should retain dog UUID");
          context.assertTrue(
              graveStack.get(ModComponents.DOG_GRAVE_NAME).equals(dogName),
              "Item should retain dog name");
          context.assertTrue(
              graveStack.get(ModComponents.DOG_GRAVE_FLOWER_COLOR) == flowerColor,
              "Item should retain flower color");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA)
  public void dogGraveRequiresPickaxe(final TestContext context) {
    final BlockPos relGravePos = REL_GRAVE;
    final ServerWorld world = context.getWorld();

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    final float handSpeed =
        ModBlocks.DOG_GRAVE
            .getDefaultState()
            .calcBlockBreakingDelta(context.createMockPlayer(GameMode.SURVIVAL), world, gravePos);

    context.assertTrue(handSpeed > 0, "Grave should be breakable by hand (slowly)");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 100)
  public void dogGraveRequiresPickaxeToBreak(final TestContext context) {
    final BlockPos relGravePos = REL_GRAVE;
    final ServerWorld world = context.getWorld();

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    final BlockState state = world.getBlockState(gravePos);

    // Test that block requires a tool (pickaxe) to harvest
    context.assertTrue(state.isToolRequired(), "Grave should require a tool to harvest");

    // Test that the block is in the pickaxe mineable tag
    final var player = context.createMockPlayer(GameMode.SURVIVAL);
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));

    final float pickaxeSpeed = state.calcBlockBreakingDelta(player, world, gravePos);
    context.assertTrue(pickaxeSpeed > 0, "Pickaxe should be able to break grave");

    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
    final float handSpeed = state.calcBlockBreakingDelta(player, world, gravePos);
    context.assertTrue(
        handSpeed < pickaxeSpeed, "Hand should be slower than pickaxe at breaking grave");

    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 100)
  public void dogGravePickaxeDropsWithData(final TestContext context) {
    final BlockPos relGravePos = REL_GRAVE;
    final ServerWorld world = context.getWorld();

    final UUID dogUuid = UUID.randomUUID();
    final String dogName = "PickaxeTest";
    final DyeColor flowerColor = DyeColor.BLUE;

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    context.runAtTick(
        5,
        () -> {
          final DogGraveBlockEntity grave = (DogGraveBlockEntity) world.getBlockEntity(gravePos);
          grave.setDogUuid(dogUuid);
          grave.setDogName(dogName);
          grave.setFlowerColor(flowerColor);

          // Test getPickStack immediately (used for creative mode middle-click)
          final BlockState state = world.getBlockState(gravePos);
          final ItemStack stack = ModBlocks.DOG_GRAVE.getPickStack(world, gravePos, state);

          context.assertTrue(stack.isOf(ModItems.DOG_GRAVE), "Pick stack should be dog grave item");
          context.assertTrue(
              dogUuid.equals(stack.get(ModComponents.DOG_GRAVE_UUID)), "UUID should transfer");
          context.assertTrue(
              dogName.equals(stack.get(ModComponents.DOG_GRAVE_NAME)), "Name should transfer");
          context.assertTrue(
              flowerColor.equals(stack.get(ModComponents.DOG_GRAVE_FLOWER_COLOR)),
              "Flower color should transfer");

          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 100)
  public void dogGravePlacementRetainsData(final TestContext context) {
    final BlockPos relGravePos = REL_GRAVE;
    final ServerWorld world = context.getWorld();

    final UUID dogUuid = UUID.randomUUID();
    final String dogName = "PersistTest";
    final DyeColor flowerColor = DyeColor.GREEN;

    context.runAtTick(
        10,
        () -> {
          // Place block and set data directly (matches dog death spawn pattern)
          context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
          final BlockPos gravePos = context.getAbsolutePos(relGravePos);

          final DogGraveBlockEntity grave = (DogGraveBlockEntity) world.getBlockEntity(gravePos);
          grave.setDogUuid(dogUuid);
          grave.setDogName(dogName);
          grave.setFlowerColor(flowerColor);
        });

    context.runAtTick(
        15,
        () -> {
          final BlockPos gravePos = context.getAbsolutePos(relGravePos);
          // Verify data persisted across ticks
          final DogGraveBlockEntity grave = (DogGraveBlockEntity) world.getBlockEntity(gravePos);

          context.assertTrue(dogUuid.equals(grave.getDogUuid()), "UUID should persist");
          context.assertTrue(dogName.equals(grave.getDogName()), "Name should persist");
          context.assertTrue(
              flowerColor.equals(grave.getFlowerColor()), "Flower color should persist");

          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 100)
  public void spawnedGravesStandTwoBlocksTall(final TestContext context) {
    context.setBlockState(REL_GRAVE, ModBlocks.DOG_GRAVE.getDefaultState());
    context.setBlockState(
        REL_GRAVE_UPPER,
        ModBlocks.DOG_GRAVE.getDefaultState().with(DogGraveBlock.HALF, DoubleBlockHalf.UPPER));

    final BlockState upper =
        context.getWorld().getBlockState(context.getAbsolutePos(REL_GRAVE_UPPER));
    context.assertTrue(
        upper.isOf(ModBlocks.DOG_GRAVE) && upper.get(DogGraveBlock.HALF) == DoubleBlockHalf.UPPER,
        "The grave should carry an upper half above its base");
    context.assertTrue(
        context.getBlockEntity(REL_GRAVE) instanceof DogGraveBlockEntity,
        "The base half should hold the block entity");
    context.assertTrue(
        context.getWorld().getBlockEntity(context.getAbsolutePos(REL_GRAVE_UPPER)) == null,
        "The upper half should hold no block entity of its own");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 100)
  public void aLegacyGraveHealsItsMissingUpperHalf(final TestContext context) {
    context.setBlockState(REL_GRAVE, ModBlocks.DOG_GRAVE.getDefaultState());

    context.runAtTick(
        10,
        () -> {
          final BlockState upper =
              context.getWorld().getBlockState(context.getAbsolutePos(REL_GRAVE_UPPER));
          context.assertTrue(
              upper.isOf(ModBlocks.DOG_GRAVE)
                  && upper.get(DogGraveBlock.HALF) == DoubleBlockHalf.UPPER,
              "A lone base from an old world should grow its upper half after loading");
          context.complete();
        });
  }

  /**
   * The original hitbox bug: the headstone is a block and a half tall, but a ray at head height
   * used to sail through the air cell where its top half stands, so labels flickered and a rod
   * could not be placed on top unless aimed from above.
   */
  @GameTest(templateName = ARENA, tickLimit = 100)
  public void aRayAtHeadstoneTopHeightHitsTheGrave(final TestContext context) {
    context.setBlockState(REL_GRAVE, ModBlocks.DOG_GRAVE.getDefaultState());
    context.setBlockState(
        REL_GRAVE_UPPER,
        ModBlocks.DOG_GRAVE.getDefaultState().with(DogGraveBlock.HALF, DoubleBlockHalf.UPPER));
    final var player = context.createMockPlayer(GameMode.SURVIVAL);

    final Vec3d from = context.getAbsolute(new Vec3d(3.5, 2.3, 0.2));
    final Vec3d to = context.getAbsolute(new Vec3d(3.5, 2.3, 3.5));
    final BlockHitResult hit =
        context
            .getWorld()
            .raycast(
                new RaycastContext(
                    from,
                    to,
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE,
                    player));

    context.assertTrue(
        hit.getType() == HitResult.Type.BLOCK
            && hit.getBlockPos().equals(context.getAbsolutePos(REL_GRAVE_UPPER)),
        "A level ray at the top of the headstone should hit the grave's upper half, but hit "
            + hit.getType()
            + " at "
            + hit.getBlockPos());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 100)
  public void aRodPlacedOnTopOfTheGraveLandsAboveIt(final TestContext context) {
    context.setBlockState(REL_GRAVE, ModBlocks.DOG_GRAVE.getDefaultState());
    context.setBlockState(
        REL_GRAVE_UPPER,
        ModBlocks.DOG_GRAVE.getDefaultState().with(DogGraveBlock.HALF, DoubleBlockHalf.UPPER));
    final var player = context.createMockPlayer(GameMode.SURVIVAL);

    context.useStackOnBlock(
        player, new ItemStack(Items.LIGHTNING_ROD), REL_GRAVE_UPPER, Direction.UP);

    context.expectBlock(Blocks.LIGHTNING_ROD, REL_GRAVE_UPPER.up());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 100)
  public void breakingTheUpperHalfTakesTheWholeGraveAndFreesItsTotem(final TestContext context) {
    context.setBlockState(REL_GRAVE, ModBlocks.DOG_GRAVE.getDefaultState());
    context.setBlockState(
        REL_GRAVE_UPPER,
        ModBlocks.DOG_GRAVE.getDefaultState().with(DogGraveBlock.HALF, DoubleBlockHalf.UPPER));
    final DogGraveBlockEntity grave = context.getBlockEntity(REL_GRAVE);
    grave.installTotem(UUID.randomUUID());
    final var player = context.createMockPlayer(GameMode.SURVIVAL);

    ModBlocks.DOG_GRAVE.onBreak(
        context.getWorld(),
        context.getAbsolutePos(REL_GRAVE_UPPER),
        context.getWorld().getBlockState(context.getAbsolutePos(REL_GRAVE_UPPER)),
        player);
    context.getWorld().removeBlock(context.getAbsolutePos(REL_GRAVE_UPPER), false);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              context.getWorld().getBlockState(context.getAbsolutePos(REL_GRAVE)).isAir(),
              "Breaking the upper half should take the base with it");
          context.expectEntityAround(EntityType.ITEM, REL_GRAVE, 3.0);
          context.complete();
        });
  }
}
