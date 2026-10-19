package com.grahambartley.dogsunleashed.command;

import com.grahambartley.dogsunleashed.config.GameplaySettings;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class GameplayConfigCommand {

  private GameplayConfigCommand() {}

  static void addTo(final LiteralArgumentBuilder<ServerCommandSource> config) {
    config
        .then(
            ConfigSettingNodes.toggle(
                "graves",
                (current, value) ->
                    current.withGameplay(current.gameplay().withGravesEnabled(value))))
        .then(
            ConfigSettingNodes.toggle(
                "dropleashonplay",
                (current, value) ->
                    current.withGameplay(current.gameplay().withDropLeashOnPlayMode(value))))
        .then(
            ConfigSettingNodes.toggle(
                "shownames",
                (current, value) ->
                    current.withGameplay(current.gameplay().withShowDogNames(value))));
  }

  static List<Text> helpLines() {
    return List.of(
        Text.translatable("command.dogs-unleashed.help.graves"),
        Text.translatable("command.dogs-unleashed.help.dropleashonplay"),
        Text.translatable("command.dogs-unleashed.help.shownames"));
  }

  static List<Text> statusLines(final GameplaySettings settings) {
    return List.of(
        Text.translatable("command.dogs-unleashed.status.graves", settings.gravesEnabled()),
        Text.translatable(
            "command.dogs-unleashed.status.dropleashonplay", settings.dropLeashOnPlayMode()),
        Text.translatable("command.dogs-unleashed.status.shownames", settings.showDogNames()));
  }
}
