package com.grahambartley.dogsunleashed.command;

import com.grahambartley.dogsunleashed.config.FurSettings;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class FurConfigCommand {

  private FurConfigCommand() {}

  static void addTo(final LiteralArgumentBuilder<ServerCommandSource> config) {
    config
        .then(
            ConfigSettingNodes.toggle(
                "furgrowth",
                (current, value) -> current.withFur(current.fur().withFurGrowthEnabled(value))))
        .then(
            ConfigSettingNodes.intSetting(
                "furgrowthrate",
                "percent",
                FurSettings.PERCENT_MIN,
                FurSettings.PERCENT_MAX,
                (current, value) ->
                    current.withFur(current.fur().withFurGrowthRateMultiplierPercent(value)),
                value -> value + "%"))
        .then(
            ConfigSettingNodes.intSetting(
                "badhairdaychance",
                "percent",
                FurSettings.PERCENT_MIN,
                FurSettings.PERCENT_MAX,
                (current, value) ->
                    current.withFur(current.fur().withBadHairDayChanceMultiplierPercent(value)),
                value -> value + "%"));
  }

  static List<Text> helpLines() {
    return List.of(
        Text.translatable("command.dogs-unleashed.help.furgrowth"),
        Text.translatable(
            "command.dogs-unleashed.help.furgrowthrate",
            FurSettings.PERCENT_MIN,
            FurSettings.PERCENT_MAX),
        Text.translatable(
            "command.dogs-unleashed.help.badhairdaychance",
            FurSettings.PERCENT_MIN,
            FurSettings.PERCENT_MAX));
  }

  static List<Text> statusLines(final FurSettings settings) {
    return List.of(
        Text.translatable("command.dogs-unleashed.status.furgrowth", settings.furGrowthEnabled()),
        Text.translatable(
            "command.dogs-unleashed.status.furgrowthrate",
            settings.furGrowthRateMultiplierPercent()),
        Text.translatable(
            "command.dogs-unleashed.status.badhairdaychance",
            settings.badHairDayChanceMultiplierPercent()));
  }
}
