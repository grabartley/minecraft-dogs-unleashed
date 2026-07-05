package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.command.DogsUnleashedCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.PetData;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Locks the operator command output contract for {@code /dogsunleashed list} and {@code
 * /dogsunleashed find}: {@link DogsUnleashedCommand#listLines}, {@link
 * DogsUnleashedCommand#findLine}, and {@link DogsUnleashedCommand#formatPos}. These are the pure
 * formatting seams the three op-only subcommands render through, so pinning them here keeps the
 * translated feedback stable without standing up a live command dispatcher.
 *
 * <p>Lives in the gametest suite rather than {@code src/test/java} for the same reason as {@link
 * PetManagerFilterGameTest}: constructing a {@link PetData} class-loads {@code UnleashedDogEntity}
 * for its persisted default constants, and that class only passes bytecode verification on the
 * access-widened runtime classpath. No world ticking is required, so each case runs in {@code
 * EMPTY_STRUCTURE} and completes immediately after asserting.
 */
public final class DogsUnleashedCommandGameTest implements FabricGameTest {

  private static final UUID OWNER =
      UUID.nameUUIDFromBytes("dogsunleashed-command-owner".getBytes());
  private static final UUID PET_ID = UUID.nameUUIDFromBytes("dogsunleashed-command-pet".getBytes());

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void formatPosRendersNullAsUnknown(final TestContext context) {
    context.assertTrue(
        "unknown".equals(DogsUnleashedCommand.formatPos(null)),
        "A null position must render as 'unknown'");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void formatPosRendersCoordinates(final TestContext context) {
    context.assertTrue(
        "1, 2, 3".equals(DogsUnleashedCommand.formatPos(new BlockPos(1, 2, 3))),
        "A position must render as comma-separated coordinates");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void listLinesForEmptyRosterReportsNoPets(final TestContext context) {
    final List<Text> lines = DogsUnleashedCommand.listLines("Steve", List.of());
    context.assertTrue(lines.size() == 1, "An empty roster must produce a single line");
    assertTranslatable(
        context, lines.get(0), "command.dogs-unleashed.list.empty", new Object[] {"Steve"});
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void listLinesRendersHeaderThenEntryPerPet(final TestContext context) {
    final PetData pet = pet("Rex", true, new BlockPos(10, 64, -20), "minecraft:overworld");
    final List<Text> lines = DogsUnleashedCommand.listLines("Alex", List.of(pet));

    context.assertTrue(lines.size() == 2, "A one-pet roster must produce a header plus one entry");
    assertTranslatable(
        context, lines.get(0), "command.dogs-unleashed.list.header", new Object[] {"Alex", 1});
    assertTranslatable(
        context,
        lines.get(1),
        "command.dogs-unleashed.list.entry",
        new Object[] {
          "Rex",
          "husky",
          PET_ID.toString(),
          Text.translatable("command.dogs-unleashed.pet.alive"),
          "minecraft:overworld",
          "10, 64, -20"
        });
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void findLineRendersDeceasedPetDescriptor(final TestContext context) {
    final PetData pet = pet("Bella", false, new BlockPos(0, 70, 0), "minecraft:the_nether");
    final Text line =
        DogsUnleashedCommand.findLine(pet, "minecraft:the_nether", new BlockPos(5, 71, 9));

    assertTranslatable(
        context,
        line,
        "command.dogs-unleashed.find.result",
        new Object[] {
          "Bella",
          "husky",
          PET_ID.toString(),
          Text.translatable("command.dogs-unleashed.pet.dead"),
          "minecraft:the_nether",
          "5, 71, 9"
        });
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void findLineRendersMissingLocationAsUnknown(final TestContext context) {
    final PetData pet = pet("Max", true, new BlockPos(3, 3, 3), "minecraft:overworld");
    final Text line = DogsUnleashedCommand.findLine(pet, null, null);

    assertTranslatable(
        context,
        line,
        "command.dogs-unleashed.find.result",
        new Object[] {
          "Max",
          "husky",
          PET_ID.toString(),
          Text.translatable("command.dogs-unleashed.pet.alive"),
          "unknown",
          "unknown"
        });
    context.complete();
  }

  private static void assertTranslatable(
      final TestContext context,
      final Text text,
      final String expectedKey,
      final Object[] expectedArgs) {
    context.assertTrue(
        text.getContent() instanceof TranslatableTextContent,
        "Expected a translatable text for key " + expectedKey);
    final TranslatableTextContent content = (TranslatableTextContent) text.getContent();
    context.assertTrue(
        expectedKey.equals(content.getKey()),
        "Expected translation key " + expectedKey + " but got " + content.getKey());
    final Object[] actualArgs = content.getArgs();
    context.assertTrue(
        actualArgs.length == expectedArgs.length,
        "Expected "
            + expectedArgs.length
            + " args for "
            + expectedKey
            + " but got "
            + actualArgs.length);
    for (int i = 0; i < expectedArgs.length; i++) {
      final Object expected = expectedArgs[i];
      final Object actual = actualArgs[i];
      context.assertTrue(
          expected.equals(actual),
          "Arg " + i + " for " + expectedKey + " expected " + expected + " but got " + actual);
    }
  }

  private static PetData pet(
      final String name,
      final boolean alive,
      @Nullable final BlockPos pos,
      final String dimension) {
    return new PetData(
        PET_ID, OWNER, UnleashedDogBreed.HUSKY, name, 20.0f, 20.0f, pos, dimension, alive);
  }
}
