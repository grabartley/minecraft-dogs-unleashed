package com.grahambartley.dogsunleashed.command;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.config.DireWolfSettings;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class DireWolfConfigCommand {

  private DireWolfConfigCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("direwolf")
        .then(
            setting(
                "growthrate",
                "percent",
                DireWolfSettings.GROWTH_RATE_MIN,
                DireWolfSettings.GROWTH_RATE_MAX,
                DireWolfSettings::withGrowthRateMultiplierPercent,
                "%",
                (settings, value) -> Optional.empty()))
        .then(
            setting(
                "battles",
                "count",
                DireWolfSettings.BATTLES_MIN,
                DireWolfSettings.BATTLES_MAX,
                DireWolfSettings::withBattlesToEvolve,
                "",
                (settings, value) -> Optional.empty()))
        .then(
            setting(
                "smallestpack",
                "size",
                DireWolfSettings.PACK_MIN,
                DireWolfSettings.PACK_MAX,
                DireWolfSettings::withSmallestPack,
                "",
                (settings, value) -> packOrder(value, settings.largestPack())))
        .then(
            setting(
                "largestpack",
                "size",
                DireWolfSettings.PACK_MIN,
                DireWolfSettings.PACK_MAX,
                DireWolfSettings::withLargestPack,
                "",
                (settings, value) -> packOrder(settings.smallestPack(), value)));
  }

  static List<Text> helpLines() {
    return List.of(
        Text.translatable(
            "command.dogs-unleashed.help.direwolf.growthrate",
            DireWolfSettings.GROWTH_RATE_MIN,
            DireWolfSettings.GROWTH_RATE_MAX),
        Text.translatable(
            "command.dogs-unleashed.help.direwolf.battles",
            DireWolfSettings.BATTLES_MIN,
            DireWolfSettings.BATTLES_MAX),
        Text.translatable(
            "command.dogs-unleashed.help.direwolf.smallestpack",
            DireWolfSettings.PACK_MIN,
            DireWolfSettings.PACK_MAX),
        Text.translatable(
            "command.dogs-unleashed.help.direwolf.largestpack",
            DireWolfSettings.PACK_MIN,
            DireWolfSettings.PACK_MAX));
  }

  static List<Text> statusLines(final DireWolfSettings settings) {
    return List.of(
        Text.translatable(
            "command.dogs-unleashed.status.direwolf.growthrate",
            settings.growthRateMultiplierPercent()),
        Text.translatable(
            "command.dogs-unleashed.status.direwolf.battles", settings.battlesToEvolve()),
        Text.translatable(
            "command.dogs-unleashed.status.direwolf.packsize",
            settings.smallestPack(),
            settings.largestPack()));
  }

  static Optional<Text> packOrder(final int smallest, final int largest) {
    return smallest > largest
        ? Optional.of(
            Text.translatable("command.dogs-unleashed.direwolf.pack_order", smallest, largest))
        : Optional.empty();
  }

  private static LiteralArgumentBuilder<ServerCommandSource> setting(
      final String name,
      final String argument,
      final int min,
      final int max,
      final BiFunction<DireWolfSettings, Integer, DireWolfSettings> update,
      final String unit,
      final BiFunction<DireWolfSettings, Integer, Optional<Text>> refusal) {
    return CommandManager.literal(name)
        .then(
            CommandManager.argument(argument, IntegerArgumentType.integer(min, max))
                .executes(
                    ctx ->
                        apply(
                            ctx,
                            "direwolf " + name,
                            update,
                            IntegerArgumentType.getInteger(ctx, argument),
                            unit,
                            refusal)));
  }

  private static int apply(
      final CommandContext<ServerCommandSource> ctx,
      final String label,
      final BiFunction<DireWolfSettings, Integer, DireWolfSettings> update,
      final int value,
      final String unit,
      final BiFunction<DireWolfSettings, Integer, Optional<Text>> refusal) {
    final DireWolfSettings current = DogsUnleashed.SERVER_CONFIG.direWolf();
    final Optional<Text> refused = refusal.apply(current, value);
    if (refused.isPresent()) {
      ctx.getSource().sendError(refused.get());
      return 0;
    }
    final DireWolfSettings updated = update.apply(current, value);
    return DogsUnleashedCommand.applyUpdate(
        ctx, DogsUnleashed.SERVER_CONFIG.withDireWolf(updated), label, value + unit, false);
  }
}
