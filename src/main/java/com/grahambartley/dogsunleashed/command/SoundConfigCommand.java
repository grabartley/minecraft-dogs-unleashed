package com.grahambartley.dogsunleashed.command;

import com.grahambartley.dogsunleashed.config.SoundSettings;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import java.util.Locale;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class SoundConfigCommand {

  private SoundConfigCommand() {}

  static void addTo(final LiteralArgumentBuilder<ServerCommandSource> config) {
    config
        .then(
            ConfigSettingNodes.volumeSetting(
                "barkvolume",
                SoundSettings.VOLUME_MIN,
                SoundSettings.VOLUME_MAX,
                (current, value) -> current.withSound(current.sound().withBarkVolume(value))))
        .then(
            ConfigSettingNodes.volumeSetting(
                "howlvolume",
                SoundSettings.VOLUME_MIN,
                SoundSettings.VOLUME_MAX,
                (current, value) -> current.withSound(current.sound().withHowlVolume(value))));
  }

  static List<Text> helpLines() {
    return List.of(
        Text.translatable(
            "command.dogs-unleashed.help.barkvolume",
            SoundSettings.VOLUME_MIN,
            SoundSettings.VOLUME_MAX),
        Text.translatable(
            "command.dogs-unleashed.help.howlvolume",
            SoundSettings.VOLUME_MIN,
            SoundSettings.VOLUME_MAX));
  }

  static List<Text> statusLines(final SoundSettings settings) {
    return List.of(
        Text.translatable(
            "command.dogs-unleashed.status.barkvolume",
            String.format(Locale.ROOT, "%.2f", settings.barkVolume())),
        Text.translatable(
            "command.dogs-unleashed.status.howlvolume",
            String.format(Locale.ROOT, "%.2f", settings.howlVolume())));
  }
}
