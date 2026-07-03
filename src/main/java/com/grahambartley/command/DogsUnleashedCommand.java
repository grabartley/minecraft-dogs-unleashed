package com.grahambartley.command;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.config.DogsUnleashedConfig;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.pet.PetData;
import com.grahambartley.pet.PetLocationService;
import com.grahambartley.pet.PetManager;
import com.grahambartley.server.ServerConfigService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class DogsUnleashedCommand {
  private DogsUnleashedCommand() {}

  public static void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("dogsunleashed")
            .executes(DogsUnleashedCommand::help)
            .then(
                CommandManager.literal("status")
                    .requires(DogsUnleashedCommand::isOp)
                    .executes(DogsUnleashedCommand::status))
            .then(
                CommandManager.literal("config")
                    .requires(DogsUnleashedCommand::isOp)
                    .then(
                        CommandManager.literal("spawn")
                            .then(
                                CommandManager.argument("enabled", BoolArgumentType.bool())
                                    .executes(
                                        ctx ->
                                            setSpawn(
                                                ctx, BoolArgumentType.getBool(ctx, "enabled")))))
                    .then(
                        CommandManager.literal("graves")
                            .then(
                                CommandManager.argument("enabled", BoolArgumentType.bool())
                                    .executes(
                                        ctx ->
                                            setGraves(
                                                ctx, BoolArgumentType.getBool(ctx, "enabled")))))
                    .then(
                        CommandManager.literal("autosleep")
                            .then(
                                CommandManager.argument("enabled", BoolArgumentType.bool())
                                    .executes(
                                        ctx ->
                                            setAutoSleepEnabled(
                                                ctx, BoolArgumentType.getBool(ctx, "enabled")))))
                    .then(
                        CommandManager.literal("autosleeprange")
                            .then(
                                CommandManager.argument(
                                        "blocks",
                                        IntegerArgumentType.integer(
                                            DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN,
                                            DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX))
                                    .executes(
                                        ctx ->
                                            setAutoSleepRange(
                                                ctx,
                                                IntegerArgumentType.getInteger(ctx, "blocks")))))
                    .then(
                        CommandManager.literal("barkvolume")
                            .then(
                                CommandManager.argument(
                                        "volume",
                                        FloatArgumentType.floatArg(
                                            DogsUnleashedConfig.VOLUME_MIN,
                                            DogsUnleashedConfig.VOLUME_MAX))
                                    .executes(
                                        ctx ->
                                            setBarkVolume(
                                                ctx, FloatArgumentType.getFloat(ctx, "volume")))))
                    .then(
                        CommandManager.literal("howlvolume")
                            .then(
                                CommandManager.argument(
                                        "volume",
                                        FloatArgumentType.floatArg(
                                            DogsUnleashedConfig.VOLUME_MIN,
                                            DogsUnleashedConfig.VOLUME_MAX))
                                    .executes(
                                        ctx ->
                                            setHowlVolume(
                                                ctx, FloatArgumentType.getFloat(ctx, "volume")))))
                    .then(CommandManager.literal("reset").executes(DogsUnleashedCommand::resetAll)))
            .then(
                CommandManager.literal("list")
                    .requires(DogsUnleashedCommand::isOp)
                    .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                            .executes(DogsUnleashedCommand::listPets)))
            .then(
                CommandManager.literal("summon")
                    .requires(DogsUnleashedCommand::isOp)
                    .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                            .then(
                                CommandManager.argument("petId", UuidArgumentType.uuid())
                                    .executes(DogsUnleashedCommand::summonPet))))
            .then(
                CommandManager.literal("find")
                    .requires(DogsUnleashedCommand::isOp)
                    .then(
                        CommandManager.argument("petId", UuidArgumentType.uuid())
                            .executes(DogsUnleashedCommand::findPet))));
  }

  static boolean isOp(final ServerCommandSource source) {
    return source.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL);
  }

  private static int help(final CommandContext<ServerCommandSource> ctx) {
    final ServerCommandSource source = ctx.getSource();
    for (final Text line : helpLines()) {
      source.sendFeedback(() -> line, false);
    }
    return 1;
  }

  // Package-private only so the help/status output can be asserted as List<Text> in tests;
  // production code calls these from help() / status() exclusively.
  static List<Text> helpLines() {
    return List.of(
        Text.translatable("command.dogs-unleashed.help.header"),
        Text.translatable("command.dogs-unleashed.help.status"),
        Text.translatable("command.dogs-unleashed.help.spawn"),
        Text.translatable("command.dogs-unleashed.help.graves"),
        Text.translatable("command.dogs-unleashed.help.autosleep"),
        Text.translatable(
            "command.dogs-unleashed.help.autosleeprange",
            DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN,
            DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX),
        Text.translatable(
            "command.dogs-unleashed.help.barkvolume",
            DogsUnleashedConfig.VOLUME_MIN,
            DogsUnleashedConfig.VOLUME_MAX),
        Text.translatable(
            "command.dogs-unleashed.help.howlvolume",
            DogsUnleashedConfig.VOLUME_MIN,
            DogsUnleashedConfig.VOLUME_MAX),
        Text.translatable("command.dogs-unleashed.help.reset"),
        Text.translatable("command.dogs-unleashed.help.list"),
        Text.translatable("command.dogs-unleashed.help.summon"),
        Text.translatable("command.dogs-unleashed.help.find"));
  }

  private static int status(final CommandContext<ServerCommandSource> ctx) {
    final ServerCommandSource source = ctx.getSource();
    for (final Text line : statusLines(DogsUnleashed.SERVER_CONFIG)) {
      source.sendFeedback(() -> line, false);
    }
    return 1;
  }

  // Package-private only so the help/status output can be asserted as List<Text> in tests;
  // production code calls these from help() / status() exclusively.
  static List<Text> statusLines(final DogsUnleashedConfig config) {
    return List.of(
        Text.translatable("command.dogs-unleashed.status.header"),
        Text.translatable("command.dogs-unleashed.status.spawn", config.enableNaturalSpawning()),
        Text.translatable("command.dogs-unleashed.status.graves", config.gravesEnabled()),
        Text.translatable("command.dogs-unleashed.status.autosleep", config.autoSleepEnabled()),
        Text.translatable(
            "command.dogs-unleashed.status.autosleeprange", config.autoSleepRangeBlocks()),
        Text.translatable(
            "command.dogs-unleashed.status.barkvolume",
            String.format(Locale.ROOT, "%.2f", config.barkVolume())),
        Text.translatable(
            "command.dogs-unleashed.status.howlvolume",
            String.format(Locale.ROOT, "%.2f", config.howlVolume())));
  }

  private static int setSpawn(final CommandContext<ServerCommandSource> ctx, final boolean value) {
    return applyUpdate(
        ctx,
        DogsUnleashed.SERVER_CONFIG.withEnableNaturalSpawning(value),
        "spawn",
        Boolean.toString(value),
        true);
  }

  private static int setGraves(final CommandContext<ServerCommandSource> ctx, final boolean value) {
    return applyUpdate(
        ctx,
        DogsUnleashed.SERVER_CONFIG.withGravesEnabled(value),
        "graves",
        Boolean.toString(value),
        false);
  }

  private static int setAutoSleepEnabled(
      final CommandContext<ServerCommandSource> ctx, final boolean value) {
    return applyUpdate(
        ctx,
        DogsUnleashed.SERVER_CONFIG.withAutoSleepEnabled(value),
        "autosleep",
        Boolean.toString(value),
        false);
  }

  private static int setAutoSleepRange(
      final CommandContext<ServerCommandSource> ctx, final int value) {
    return applyUpdate(
        ctx,
        DogsUnleashed.SERVER_CONFIG.withAutoSleepRangeBlocks(value),
        "autosleeprange",
        Integer.toString(value),
        false);
  }

  private static int setBarkVolume(
      final CommandContext<ServerCommandSource> ctx, final float value) {
    return applyUpdate(
        ctx,
        DogsUnleashed.SERVER_CONFIG.withBarkVolume(value),
        "barkvolume",
        String.format("%.2f", value),
        false);
  }

  private static int setHowlVolume(
      final CommandContext<ServerCommandSource> ctx, final float value) {
    return applyUpdate(
        ctx,
        DogsUnleashed.SERVER_CONFIG.withHowlVolume(value),
        "howlvolume",
        String.format("%.2f", value),
        false);
  }

  private static int resetAll(final CommandContext<ServerCommandSource> ctx) {
    return applyUpdate(ctx, DogsUnleashedConfig.defaults(), "all", "defaults", true);
  }

  private static int applyUpdate(
      final CommandContext<ServerCommandSource> ctx,
      final DogsUnleashedConfig updated,
      final String label,
      final String displayValue,
      final boolean restartNote) {
    final ServerCommandSource source = ctx.getSource();
    final boolean ok = ServerConfigService.update(source.getServer(), updated);
    if (!ok) {
      source.sendError(Text.translatable("command.dogs-unleashed.update.failed"));
      return 0;
    }
    source.sendFeedback(
        () -> Text.translatable("command.dogs-unleashed.update.success", label, displayValue),
        true);
    if (restartNote) {
      source.sendFeedback(
          () -> Text.translatable("command.dogs-unleashed.update.restart_note"), false);
    }
    return 1;
  }

  private static int listPets(final CommandContext<ServerCommandSource> ctx)
      throws CommandSyntaxException {
    final ServerCommandSource source = ctx.getSource();
    final ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
    final List<PetData> pets = PetManager.get(source.getServer()).getPetsByOwner(target.getUuid());
    for (final Text line : listLines(target.getGameProfile().getName(), pets)) {
      source.sendFeedback(() -> line, false);
    }
    return 1;
  }

  private static int summonPet(final CommandContext<ServerCommandSource> ctx)
      throws CommandSyntaxException {
    final ServerCommandSource source = ctx.getSource();
    final ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
    final UUID petId = UuidArgumentType.getUuid(ctx, "petId");
    final MinecraftServer server = source.getServer();
    final PetData pet = PetManager.get(server).getPet(target.getUuid(), petId);
    if (pet == null) {
      source.sendError(
          Text.translatable(
              "command.dogs-unleashed.summon.notfound",
              target.getGameProfile().getName(),
              petId.toString()));
      return 0;
    }
    PetLocationService.loadAndSummon(server, pet, target);
    source.sendFeedback(
        () ->
            Text.translatable(
                "command.dogs-unleashed.summon.success",
                pet.getName(),
                target.getGameProfile().getName()),
        true);
    return 1;
  }

  private static int findPet(final CommandContext<ServerCommandSource> ctx) {
    final ServerCommandSource source = ctx.getSource();
    final UUID petId = UuidArgumentType.getUuid(ctx, "petId");
    final MinecraftServer server = source.getServer();
    final PetData pet = PetManager.get(server).getPetByEntityId(petId);
    if (pet == null) {
      source.sendError(Text.translatable("command.dogs-unleashed.find.notfound", petId.toString()));
      return 0;
    }
    final UnleashedDogEntity liveDog = PetLocationService.findDog(server, pet);
    final String dimension =
        liveDog != null
            ? liveDog.getWorld().getRegistryKey().getValue().toString()
            : pet.getDimension();
    final BlockPos position = liveDog != null ? liveDog.getBlockPos() : pet.getLastKnownPosition();
    source.sendFeedback(() -> findLine(pet, dimension, position), false);
    return 1;
  }

  // Public only so the list/find output can be asserted from the gametest package; constructing a
  // PetData class-loads UnleashedDogEntity, which forces these formatting seams into the gametest
  // suite rather than same-package JUnit. Production code calls them from listPets()/findPet()
  // only.
  public static List<Text> listLines(final String playerName, final List<PetData> pets) {
    if (pets.isEmpty()) {
      return List.of(Text.translatable("command.dogs-unleashed.list.empty", playerName));
    }
    final List<Text> lines = new ArrayList<>();
    lines.add(Text.translatable("command.dogs-unleashed.list.header", playerName, pets.size()));
    for (final PetData pet : pets) {
      lines.add(
          petDescriptor(
              "command.dogs-unleashed.list.entry",
              pet,
              pet.getDimension(),
              pet.getLastKnownPosition()));
    }
    return lines;
  }

  public static Text findLine(
      final PetData pet, @Nullable final String dimension, @Nullable final BlockPos position) {
    return petDescriptor("command.dogs-unleashed.find.result", pet, dimension, position);
  }

  private static Text petDescriptor(
      final String key,
      final PetData pet,
      @Nullable final String dimension,
      @Nullable final BlockPos position) {
    return Text.translatable(
        key,
        pet.getName(),
        pet.getBreedType(),
        pet.getPetId().toString(),
        Text.translatable(
            pet.isAlive() ? "command.dogs-unleashed.pet.alive" : "command.dogs-unleashed.pet.dead"),
        dimension == null || dimension.isEmpty() ? "unknown" : dimension,
        formatPos(position));
  }

  public static String formatPos(@Nullable final BlockPos pos) {
    if (pos == null) {
      return "unknown";
    }
    return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
  }
}
