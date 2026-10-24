package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModPortraits;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.grahambartley.dogsunleashed.entity.DogAging;
import com.grahambartley.dogsunleashed.entity.DogCollar;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogMud;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import com.grahambartley.dogsunleashed.item.BlankCanvasItem;
import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import com.grahambartley.dogsunleashed.portrait.DogPortraitEntity;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfits;
import com.grahambartley.dogsunleashed.portrait.PortraitSize;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.decoration.painting.PaintingVariants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class DogPortraitGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final BlockPos REL_WALL = new BlockPos(1, 2, 1);
  private static final Vec3d REL_PLAYER = new Vec3d(4.5, 2, 4.5);

  private static final Map<PortraitSize, RegistryKey<PaintingVariant>> VANILLA_PAINTINGS =
      Map.of(
          PortraitSize.SMALL, PaintingVariants.KEBAB,
          PortraitSize.WIDE, PaintingVariants.POOL,
          PortraitSize.LARGE, PaintingVariants.BUST);

  private static DogPortrait huskyPortrait(final PortraitSize size) {
    return DogPortrait.painted(
        new DogStatueLook(UnleashedDogBreed.HUSKY, false, 1, 0, 2, 0, null, null),
        "Rex",
        size,
        DogPortraitOutfit.NONE);
  }

  private static ServerPlayerEntity holdingCanvas(
      final TestContext context, final PortraitSize size) {
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final ItemStack canvas = new ItemStack(ModPortraits.BLANK_CANVAS);
    canvas.set(ModComponents.PORTRAIT_SIZE, size);
    player.setStackInHand(Hand.MAIN_HAND, canvas);
    return player;
  }

  private static ItemStack portraitIn(final ServerPlayerEntity player) {
    return player.getInventory().main.stream()
        .filter(stack -> stack.isOf(ModPortraits.DOG_PORTRAIT))
        .findFirst()
        .orElse(ItemStack.EMPTY);
  }

  private static void buildWall(final TestContext context) {
    for (final BlockPos pos : BlockPos.iterate(REL_WALL, REL_WALL.add(1, 1, 0))) {
      context.setBlockState(pos.toImmutable(), Blocks.STONE);
    }
  }

  private static DogPortraitEntity hang(
      final TestContext context, final ServerPlayerEntity player, final DogPortrait portrait) {
    buildWall(context);
    final ItemStack stack = ModPortraits.portraitStack(portrait);
    player.setStackInHand(Hand.MAIN_HAND, stack);
    final BlockPos absWall = context.getAbsolutePos(REL_WALL);
    final BlockHitResult hit =
        new BlockHitResult(Vec3d.ofCenter(absWall), Direction.SOUTH, absWall, false);
    stack.useOnBlock(new ItemUsageContext(context.getWorld(), player, Hand.MAIN_HAND, stack, hit));
    final List<DogPortraitEntity> hung =
        context
            .getWorld()
            .getEntitiesByClass(
                DogPortraitEntity.class, context.getTestBox().expand(1.0), entity -> true);
    context.assertEquals(hung.size(), 1, "hung portraits");
    return hung.get(0);
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void usingACanvasOnYourDogPaintsItsPortrait(final TestContext context) {
    final ServerPlayerEntity player = holdingCanvas(context, PortraitSize.WIDE);
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, REL_DOG, player.getUuid());
    dog.setAiDisabled(true);

    dog.interactMob(player, Hand.MAIN_HAND);

    final DogPortrait portrait = portraitIn(player).get(ModComponents.DOG_PORTRAIT);
    final boolean canvasLeft =
        player.getInventory().contains(new ItemStack(ModPortraits.BLANK_CANVAS));
    SurvivalTestPlayer.leave(context, player);
    context.assertTrue(portrait != null, "Using a canvas on your dog should paint its portrait");
    context.assertEquals(portrait.look(), DogStatueLook.of(dog), "painted look");
    context.assertEquals(portrait.size(), PortraitSize.WIDE, "painted size");
    context.assertFalse(canvasLeft, "Painting should use up the blank canvas");
    context.complete();
  }

  private static void dressUp(final UnleashedDogEntity dog) {
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR));
    dog.getEquipmentHolder()
        .setStack(
            DogEquipmentSlot.COSMETIC,
            new ItemStack(Registries.ITEM.get(Identifier.of(DogsUnleashed.MOD_ID, "party_hat"))));
    dog.getEquipmentHolder()
        .setStack(DogEquipmentSlot.SADDLEBAGS, new ItemStack(ModItems.SADDLEBAGS));
    dog.getEquipmentHolder()
        .setStack(
            DogEquipmentSlot.PENDANT,
            new ItemStack(
                Registries.ITEM.get(
                    Identifier.of(DogsUnleashed.MOD_ID, "warding_undead_pendant"))));
    dog.getEquipmentHolder()
        .setStack(
            DogEquipmentSlot.COSTUME,
            new ItemStack(
                Registries.ITEM.get(Identifier.of(DogsUnleashed.MOD_ID, "spider_costume"))));
    dog.getComponents().get(DogCollar.class).setColor(DyeColor.LIME);
    dog.getComponents().get(DogMud.class).makeMuddy();
    dog.getComponents().get(DogAging.class).showSeniorCoat(true);
    dog.getFetchCarry().setActiveType(FetchTypes.TENNIS_BALL);
    dog.getFetchCarry().setCarrying(true);
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void thePortraitKeepsWhatTheDogWasWearing(final TestContext context) {
    final ServerPlayerEntity player = holdingCanvas(context, PortraitSize.SMALL);
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, REL_DOG, player.getUuid());
    dog.setAiDisabled(true);
    dressUp(dog);

    dog.interactMob(player, Hand.MAIN_HAND);

    final DogPortrait portrait = portraitIn(player).get(ModComponents.DOG_PORTRAIT);
    SurvivalTestPlayer.leave(context, player);
    context.assertTrue(portrait != null, "the portrait");
    final DogPortraitOutfit outfit = portrait.outfit();
    context.assertTrue(
        outfit.worn(DogEquipmentSlot.ARMOUR).isOf(Items.WOLF_ARMOR), "painted armour");
    context.assertTrue(!outfit.worn(DogEquipmentSlot.COSMETIC).isEmpty(), "painted cosmetic");
    context.assertTrue(
        outfit.worn(DogEquipmentSlot.SADDLEBAGS).isOf(ModItems.SADDLEBAGS), "painted saddlebags");
    context.assertEquals(outfit.collar().orElse(null), DyeColor.LIME, "painted collar");
    context.assertTrue(outfit.muddy(), "painted mud");
    context.assertTrue(!outfit.worn(DogEquipmentSlot.PENDANT).isEmpty(), "painted pendant");
    context.assertTrue(!outfit.worn(DogEquipmentSlot.COSTUME).isEmpty(), "painted costume");
    context.assertTrue(outfit.senior(), "painted senior coat");
    context.assertEquals(
        outfit.fetchType().orElse(null), FetchTypes.TENNIS_BALL.id(), "painted fetch toy");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void aPaintedOutfitDressesAnotherDogTheSameWay(final TestContext context) {
    final UnleashedDogEntity source =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, REL_DOG, UUID.randomUUID());
    source.setAiDisabled(true);
    dressUp(source);
    final DogPortraitOutfit outfit = DogPortraitOutfits.of(source);

    final UnleashedDogEntity model =
        context.spawnEntity(ModEntities.getDogEntityType(UnleashedDogBreed.HUSKY), REL_DOG.east());
    model.setAiDisabled(true);
    DogPortraitOutfits.dress(model, outfit);

    context.assertEquals(
        model.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).getItem(),
        Items.WOLF_ARMOR,
        "dressed armour");
    context.assertTrue(
        model.getEquipmentHolder().getStack(DogEquipmentSlot.SADDLEBAGS).isOf(ModItems.SADDLEBAGS),
        "dressed saddlebags");
    context.assertEquals(
        model.getComponents().get(DogCollar.class).getColor(), DyeColor.LIME, "dressed collar");
    context.assertTrue(model.isTamed(), "a collared model is tamed so the collar shows");
    context.assertTrue(model.getComponents().get(DogMud.class).isMuddy(), "dressed mud");
    context.assertTrue(model.getComponents().get(DogAging.class).isSenior(), "dressed senior coat");
    context.assertTrue(model.getFetchCarry().isCarrying(), "dressed carrying");
    context.assertEquals(
        model.getFetchCarry().activeType(), FetchTypes.TENNIS_BALL, "dressed fetch toy");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void thePortraitIsTitledWithTheDogsName(final TestContext context) {
    final ServerPlayerEntity player = holdingCanvas(context, PortraitSize.SMALL);
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, REL_DOG, player.getUuid());
    dog.setAiDisabled(true);
    dog.setCustomName(Text.literal("Rex"));

    dog.interactMob(player, Hand.MAIN_HAND);

    final DogPortrait portrait = portraitIn(player).get(ModComponents.DOG_PORTRAIT);
    SurvivalTestPlayer.leave(context, player);
    context.assertTrue(portrait != null, "the portrait");
    context.assertEquals(portrait.name(), "Rex", "portrait name");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void anotherPlayersDogIsNeverPainted(final TestContext context) {
    final ServerPlayerEntity player = holdingCanvas(context, PortraitSize.SMALL);
    DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, REL_DOG, UUID.randomUUID())
        .interactMob(player, Hand.MAIN_HAND);

    final boolean painted = !portraitIn(player).isEmpty();
    final boolean canvasKept = player.getMainHandStack().isOf(ModPortraits.BLANK_CANVAS);
    SurvivalTestPlayer.leave(context, player);
    context.assertFalse(painted, "Only the player's own dogs can be painted");
    context.assertTrue(canvasKept, "The canvas stays blank");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void sneakUsingACanvasStepsThroughTheSizes(final TestContext context) {
    final ServerPlayerEntity player = holdingCanvas(context, PortraitSize.SMALL);
    player.setSneaking(true);
    final ItemStack canvas = player.getMainHandStack();

    for (final PortraitSize expected :
        List.of(PortraitSize.WIDE, PortraitSize.LARGE, PortraitSize.SMALL)) {
      canvas.use(context.getWorld(), player, Hand.MAIN_HAND);
      context.assertEquals(BlankCanvasItem.sizeOf(canvas), expected, "canvas size");
    }
    SurvivalTestPlayer.leave(context, player);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void aHungPortraitKeepsItsDogAcrossAReload(final TestContext context) {
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final DogPortrait portrait = huskyPortrait(PortraitSize.LARGE);
    final DogPortraitEntity hung = hang(context, player, portrait);
    SurvivalTestPlayer.leave(context, player);

    final NbtCompound saved = new NbtCompound();
    context.assertTrue(hung.saveNbt(saved), "the portrait saves");
    final DogPortraitEntity reloaded =
        (DogPortraitEntity) EntityType.getEntityFromNbt(saved, context.getWorld()).orElseThrow();

    context.assertEquals(reloaded.portrait().orElse(null), portrait, "portrait after reload");
    context.assertEquals(reloaded.getBoundingBox(), hung.getBoundingBox(), "bounds after reload");
    context.assertEquals(reloaded.getHorizontalFacing(), Direction.SOUTH, "facing after reload");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void aPortraitOutlivesTheDogItWasPaintedFrom(final TestContext context) {
    final ServerPlayerEntity player = holdingCanvas(context, PortraitSize.SMALL);
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(
            context, DogTestData.GOLDEN_RETRIEVER, REL_DOG, player.getUuid());
    dog.setAiDisabled(true);
    final DogStatueLook look = DogStatueLook.of(dog);
    dog.interactMob(player, Hand.MAIN_HAND);
    final DogPortrait portrait = portraitIn(player).get(ModComponents.DOG_PORTRAIT);
    dog.kill();

    final DogPortraitEntity hung = hang(context, player, portrait);
    SurvivalTestPlayer.leave(context, player);

    context.assertFalse(dog.isAlive(), "the dog is gone");
    context.assertEquals(hung.portrait().map(DogPortrait::look).orElse(null), look, "hung look");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void usingAHungPortraitWithAnEmptyHandStepsThroughThePoses(final TestContext context) {
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final DogPortrait portrait = huskyPortrait(PortraitSize.SMALL);
    final DogPortraitEntity hung = hang(context, player, portrait);
    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

    for (final DogStatuePose expected :
        List.of(DogStatuePose.STAND, DogStatuePose.HOWL, DogStatuePose.SIT)) {
      hung.interact(player, Hand.MAIN_HAND);
      context.assertEquals(
          hung.portrait().map(DogPortrait::pose).orElse(null), expected, "hung pose");
    }
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModPortraits.BLANK_CANVAS));
    hung.interact(player, Hand.MAIN_HAND);
    SurvivalTestPlayer.leave(context, player);
    context.assertEquals(
        hung.portrait().orElse(null), portrait, "a full hand leaves the pose alone");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void breakingAPortraitDropsItWithTheSameDog(final TestContext context) {
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final DogPortrait portrait = huskyPortrait(PortraitSize.WIDE);
    final DogPortraitEntity hung = hang(context, player, portrait);

    hung.damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f);

    final List<ItemStack> drops = DogTestHelper.droppedStacksOf(context, ModPortraits.DOG_PORTRAIT);
    SurvivalTestPlayer.leave(context, player);
    context.assertFalse(hung.isAlive(), "the portrait comes off the wall");
    context.assertEquals(drops.size(), 1, "dropped portraits");
    context.assertEquals(
        drops.get(0).get(ModComponents.DOG_PORTRAIT), portrait, "dropped portrait");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "portrait")
  public void eachSizeHangsExactlyWhereAVanillaPaintingOfThatSizeWould(final TestContext context) {
    final BlockPos pos = context.getAbsolutePos(REL_WALL.south());
    for (final PortraitSize size : PortraitSize.values()) {
      for (final Direction facing : Direction.Type.HORIZONTAL) {
        final Box ours =
            new DogPortraitEntity(context.getWorld(), pos, facing, huskyPortrait(size))
                .getBoundingBox();
        final Box vanilla =
            new PaintingEntity(
                    context.getWorld(),
                    pos,
                    facing,
                    context
                        .getWorld()
                        .getRegistryManager()
                        .get(RegistryKeys.PAINTING_VARIANT)
                        .entryOf(VANILLA_PAINTINGS.get(size)))
                .getBoundingBox();
        context.assertEquals(ours, vanilla, size + " facing " + facing);
      }
    }
    context.complete();
  }
}
