package com.grahambartley.gametest;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.HuskyEntity;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public final class HuskyEntityGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void huskySpawnsCorrectly(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);

    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.runAtTick(
        1,
        () -> {
          context.assertTrue(!husky.isRemoved(), "Husky should be alive and present in the world");
          context.assertTrue(
              world.getEntitiesByType(ModEntities.HUSKY, entity -> true).contains(husky),
              "Husky should be in the world's entity list");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyIsTameable(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);

    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.assertTrue(husky instanceof TameableEntity, "Husky must be a TameableEntity");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyCanBeTamed(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);

    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          context.assertFalse(husky.isTamed(), "Husky should not be tamed initially");

          husky.setTamed(true, true);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(husky.isTamed(), "Husky should be tamed after setTamed");
                context.complete();
              });
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyHasCorrectDimensions(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);

    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    final float expectedWidth = 0.8f;
    final float expectedHeight = 1.1f;

    context.assertTrue(
        Math.abs(husky.getWidth() - expectedWidth) < 0.01f,
        "Husky width should be " + expectedWidth);
    context.assertTrue(
        Math.abs(husky.getHeight() - expectedHeight) < 0.01f,
        "Husky height should be " + expectedHeight);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyHasAnimatableInstanceCache(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);

    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.assertTrue(
        husky.getAnimatableInstanceCache() != null,
        "Husky should have AnimatableInstanceCache for GeckoLib");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void untamedHuskyHasDefaultCollarColor(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.assertFalse(husky.isTamed(), "Husky should not be tamed initially");
    context.assertTrue(
        husky.getCollarColor() == DyeColor.RED, "Untamed Husky should have RED collar color");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedHuskyCollarColorCanBeChanged(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setTamed(true, true);
          context.assertTrue(husky.isTamed(), "Husky should be tamed");
          context.assertTrue(
              husky.getCollarColor() == DyeColor.RED, "Tamed Husky should start with RED collar");

          husky.setCollarColor(DyeColor.BLUE);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(
                    husky.getCollarColor() == DyeColor.BLUE, "Collar color should change to BLUE");
                context.complete();
              });
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void collarColorPersistsInNbt(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setTamed(true, true);
          husky.setCollarColor(DyeColor.LIME);

          final NbtCompound nbt = new NbtCompound();
          husky.writeCustomDataToNbt(nbt);

          context.assertTrue(nbt.contains("CollarColor"), "NBT should contain CollarColor data");
          context.assertTrue(
              nbt.getInt("CollarColor") == DyeColor.LIME.getId(), "NBT should store LIME color ID");

          final HuskyEntity newHusky = new HuskyEntity(ModEntities.HUSKY, world);
          newHusky.readCustomDataFromNbt(nbt);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(
                    newHusky.getCollarColor() == DyeColor.LIME,
                    "Collar color should persist after NBT load");
                context.complete();
              });
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void babyHuskyHasCollarWhenTamed(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity babyHusky = new HuskyEntity(ModEntities.HUSKY, world);
    babyHusky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    babyHusky.setBaby(true);
    world.spawnEntity(babyHusky);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(babyHusky.isBaby(), "Husky should be a baby");
          context.assertFalse(babyHusky.isTamed(), "Baby should not be tamed initially");

          babyHusky.setTamed(true, true);
          babyHusky.setCollarColor(DyeColor.PINK);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(babyHusky.isTamed(), "Baby should be tamed");
                context.assertTrue(
                    babyHusky.getCollarColor() == DyeColor.PINK, "Baby should have PINK collar");
                context.complete();
              });
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void allDyeColorsWorkOnCollar(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setTamed(true, true);

          final DyeColor[] testColors = {
            DyeColor.RED, DyeColor.BLUE, DyeColor.GREEN, DyeColor.YELLOW,
            DyeColor.ORANGE, DyeColor.PURPLE, DyeColor.CYAN, DyeColor.MAGENTA,
            DyeColor.WHITE, DyeColor.BLACK, DyeColor.GRAY, DyeColor.LIGHT_GRAY,
            DyeColor.LIME, DyeColor.PINK, DyeColor.LIGHT_BLUE, DyeColor.BROWN
          };

          for (DyeColor color : testColors) {
            husky.setCollarColor(color);
            context.assertTrue(
                husky.getCollarColor() == color,
                "Collar should accept " + color.getName() + " color");
          }

          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void bredBabyInheritsParentTamedStatus(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity parent = new HuskyEntity(ModEntities.HUSKY, world);
    parent.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(parent);

    context.runAtTick(
        10,
        () -> {
          parent.setTamed(true, true);
          parent.setCollarColor(DyeColor.YELLOW);

          final HuskyEntity otherParent = new HuskyEntity(ModEntities.HUSKY, world);
          otherParent.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
          otherParent.setTamed(true, true);
          world.spawnEntity(otherParent);

          final HuskyEntity baby = (HuskyEntity) parent.createChild(world, otherParent);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(baby != null, "Baby should be created from breeding");
                context.assertTrue(baby.isTamed(), "Baby should inherit tamed status");
                context.assertTrue(baby.isBaby(), "Created entity should be a baby");
                context.complete();
              });
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void boneIsTamingItem(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    final ItemStack bone = new ItemStack(Items.BONE);

    context.assertTrue(husky.isTamingItem(bone), "Bone should be a taming item");
    context.assertFalse(husky.isBreedingItem(bone), "Bone should not be a breeding item");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void meatItemsAreBothTamingAndBreeding(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    final ItemStack chicken = new ItemStack(Items.CHICKEN);

    context.assertTrue(husky.isTamingItem(chicken), "Chicken should be a taming item");
    context.assertTrue(husky.isBreedingItem(chicken), "Chicken should be a breeding item");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedStatusPersistsAfterNbtReload(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          final UUID ownerUuid = UUID.randomUUID();
          husky.setOwnerUuid(ownerUuid);
          husky.setTamed(true, true);
          husky.setCollarColor(DyeColor.GREEN);

          final NbtCompound nbt = new NbtCompound();
          husky.writeCustomDataToNbt(nbt);

          final HuskyEntity reloadedHusky = new HuskyEntity(ModEntities.HUSKY, world);
          reloadedHusky.readCustomDataFromNbt(nbt);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(
                    reloadedHusky.isTamed(), "Tamed status should persist after NBT reload");
                context.assertTrue(
                    reloadedHusky.getOwnerUuid() != null,
                    "Owner UUID should persist after NBT reload");
                context.assertTrue(
                    reloadedHusky.getOwnerUuid().equals(ownerUuid),
                    "Owner UUID should match original owner");
                context.assertTrue(
                    reloadedHusky.getCollarColor() == DyeColor.GREEN,
                    "Collar color should persist after NBT reload");
                context.complete();
              });
        });
  }
}
