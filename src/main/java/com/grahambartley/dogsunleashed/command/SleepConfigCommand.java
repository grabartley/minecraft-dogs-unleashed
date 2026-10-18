package com.grahambartley.dogsunleashed.command;

import com.grahambartley.dogsunleashed.config.SleepSettings;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class SleepConfigCommand {

  private SleepConfigCommand() {}

  static void addTo(final LiteralArgumentBuilder<ServerCommandSource> config) {
    config
        .then(
            ConfigSettingNodes.toggle(
                "autosleep",
                (current, value) -> current.withSleep(current.sleep().withAutoSleepEnabled(value)),
                false))
        .then(
            ConfigSettingNodes.intSetting(
                "autosleeprange",
                "blocks",
                SleepSettings.RANGE_MIN,
                SleepSettings.RANGE_MAX,
                (current, value) ->
                    current.withSleep(current.sleep().withAutoSleepRangeBlocks(value)),
                Integer::toString,
                false));
  }

  static List<Text> helpLines() {
    return List.of(
        Text.translatable("command.dogs-unleashed.help.autosleep"),
        Text.translatable(
            "command.dogs-unleashed.help.autosleeprange",
            SleepSettings.RANGE_MIN,
            SleepSettings.RANGE_MAX));
  }

  static List<Text> statusLines(final SleepSettings settings) {
    return List.of(
        Text.translatable("command.dogs-unleashed.status.autosleep", settings.autoSleepEnabled()),
        Text.translatable(
            "command.dogs-unleashed.status.autosleeprange", settings.autoSleepRangeBlocks()));
  }
}
