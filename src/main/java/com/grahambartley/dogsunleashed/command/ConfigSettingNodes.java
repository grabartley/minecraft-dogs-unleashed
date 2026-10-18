package com.grahambartley.dogsunleashed.command;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class ConfigSettingNodes {

  private ConfigSettingNodes() {}

  static LiteralArgumentBuilder<ServerCommandSource> toggle(
      final String name,
      final BiFunction<DogsUnleashedConfig, Boolean, DogsUnleashedConfig> update,
      final boolean restartNote) {
    return toggle(name, name, update, restartNote);
  }

  static LiteralArgumentBuilder<ServerCommandSource> toggle(
      final String name,
      final String label,
      final BiFunction<DogsUnleashedConfig, Boolean, DogsUnleashedConfig> update,
      final boolean restartNote) {
    return CommandManager.literal(name)
        .then(
            CommandManager.argument("enabled", BoolArgumentType.bool())
                .executes(
                    ctx -> {
                      final boolean value = BoolArgumentType.getBool(ctx, "enabled");
                      return applyUpdate(
                          ctx,
                          update.apply(DogsUnleashed.SERVER_CONFIG, value),
                          label,
                          Boolean.toString(value),
                          restartNote);
                    }));
  }

  static LiteralArgumentBuilder<ServerCommandSource> intSetting(
      final String name,
      final String argument,
      final int min,
      final int max,
      final BiFunction<DogsUnleashedConfig, Integer, DogsUnleashedConfig> update,
      final IntFunction<String> display,
      final boolean restartNote) {
    return CommandManager.literal(name)
        .then(intArgument(name, argument, min, max, update, display, restartNote));
  }

  static RequiredArgumentBuilder<ServerCommandSource, Integer> intArgument(
      final String label,
      final String argument,
      final int min,
      final int max,
      final BiFunction<DogsUnleashedConfig, Integer, DogsUnleashedConfig> update,
      final IntFunction<String> display,
      final boolean restartNote) {
    return CommandManager.argument(argument, IntegerArgumentType.integer(min, max))
        .executes(
            ctx -> {
              final int value = IntegerArgumentType.getInteger(ctx, argument);
              return applyUpdate(
                  ctx,
                  update.apply(DogsUnleashed.SERVER_CONFIG, value),
                  label,
                  display.apply(value),
                  restartNote);
            });
  }

  static LiteralArgumentBuilder<ServerCommandSource> volumeSetting(
      final String name,
      final float min,
      final float max,
      final BiFunction<DogsUnleashedConfig, Float, DogsUnleashedConfig> update) {
    return CommandManager.literal(name)
        .then(
            CommandManager.argument("volume", FloatArgumentType.floatArg(min, max))
                .executes(
                    ctx -> {
                      final float value = FloatArgumentType.getFloat(ctx, "volume");
                      return applyUpdate(
                          ctx,
                          update.apply(DogsUnleashed.SERVER_CONFIG, value),
                          name,
                          String.format("%.2f", value),
                          false);
                    }));
  }

  static int applyUpdate(
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
}
