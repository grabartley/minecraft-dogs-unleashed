package com.grahambartley.command;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.config.DogsUnleashedConfig;
import com.grahambartley.server.ServerConfigService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
                    .then(
                        CommandManager.literal("reset").executes(DogsUnleashedCommand::resetAll))));
  }

  static boolean isOp(final ServerCommandSource source) {
    return source.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL);
  }

  private static int help(final CommandContext<ServerCommandSource> ctx) {
    final ServerCommandSource source = ctx.getSource();
    source.sendFeedback(() -> Text.literal("Dogs Unleashed commands (operators only):"), false);
    source.sendFeedback(() -> Text.literal("- /dogsunleashed status"), false);
    source.sendFeedback(() -> Text.literal("- /dogsunleashed config spawn <true|false>"), false);
    source.sendFeedback(() -> Text.literal("- /dogsunleashed config graves <true|false>"), false);
    source.sendFeedback(
        () -> Text.literal("- /dogsunleashed config autosleep <true|false>"), false);
    source.sendFeedback(
        () ->
            Text.literal(
                "- /dogsunleashed config autosleeprange <"
                    + DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN
                    + ".."
                    + DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX
                    + ">"),
        false);
    source.sendFeedback(
        () ->
            Text.literal(
                "- /dogsunleashed config barkvolume <"
                    + DogsUnleashedConfig.VOLUME_MIN
                    + ".."
                    + DogsUnleashedConfig.VOLUME_MAX
                    + ">"),
        false);
    source.sendFeedback(
        () ->
            Text.literal(
                "- /dogsunleashed config howlvolume <"
                    + DogsUnleashedConfig.VOLUME_MIN
                    + ".."
                    + DogsUnleashedConfig.VOLUME_MAX
                    + ">"),
        false);
    source.sendFeedback(() -> Text.literal("- /dogsunleashed config reset"), false);
    return 1;
  }

  private static int status(final CommandContext<ServerCommandSource> ctx) {
    final ServerCommandSource source = ctx.getSource();
    final DogsUnleashedConfig config = DogsUnleashed.SERVER_CONFIG;
    source.sendFeedback(() -> Text.literal("Dogs Unleashed settings:"), false);
    source.sendFeedback(() -> Text.literal("- spawn=" + config.enableNaturalSpawning()), false);
    source.sendFeedback(() -> Text.literal("- graves=" + config.gravesEnabled()), false);
    source.sendFeedback(() -> Text.literal("- autosleep=" + config.autoSleepEnabled()), false);
    source.sendFeedback(
        () -> Text.literal("- autosleeprange=" + config.autoSleepRangeBlocks()), false);
    source.sendFeedback(
        () -> Text.literal("- barkvolume=" + String.format("%.2f", config.barkVolume())), false);
    source.sendFeedback(
        () -> Text.literal("- howlvolume=" + String.format("%.2f", config.howlVolume())), false);
    return 1;
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
      source.sendError(Text.literal("Failed to persist Dogs Unleashed config update."));
      return 0;
    }
    source.sendFeedback(() -> Text.literal(label + " set to " + displayValue), true);
    if (restartNote) {
      source.sendFeedback(
          () -> Text.literal("Note: spawn changes apply on next world load."), false);
    }
    return 1;
  }
}
