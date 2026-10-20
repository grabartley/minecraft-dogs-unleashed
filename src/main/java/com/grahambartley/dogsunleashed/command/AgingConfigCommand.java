package com.grahambartley.dogsunleashed.command;

import com.grahambartley.dogsunleashed.config.AgingSettings;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class AgingConfigCommand {

  private AgingConfigCommand() {}

  static void addTo(final LiteralArgumentBuilder<ServerCommandSource> config) {
    config
        .then(
            ConfigSettingNodes.toggle(
                "dogaging",
                (current, value) -> current.withAging(current.aging().withDogAgingEnabled(value))))
        .then(
            ConfigSettingNodes.intSetting(
                "senioragedays",
                "days",
                AgingSettings.DAYS_MIN,
                AgingSettings.DAYS_MAX,
                (current, value) -> current.withAging(current.aging().withSeniorAfterDays(value)),
                Integer::toString));
  }

  static List<Text> helpLines() {
    return List.of(
        Text.translatable("command.dogs-unleashed.help.dogaging"),
        Text.translatable(
            "command.dogs-unleashed.help.senioragedays",
            AgingSettings.DAYS_MIN,
            AgingSettings.DAYS_MAX));
  }

  static List<Text> statusLines(final AgingSettings settings) {
    return List.of(
        Text.translatable("command.dogs-unleashed.status.dogaging", settings.dogAgingEnabled()),
        Text.translatable(
            "command.dogs-unleashed.status.senioragedays", settings.seniorAfterDays()));
  }
}
