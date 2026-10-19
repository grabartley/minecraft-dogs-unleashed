package com.grahambartley.dogsunleashed.command;

import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.config.SpawningSettings;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class SpawningConfigCommand {

  private SpawningConfigCommand() {}

  static void addTo(final LiteralArgumentBuilder<ServerCommandSource> config) {
    config
        .then(
            ConfigSettingNodes.toggle(
                "spawn",
                (current, value) ->
                    current.withSpawning(current.spawning().withEnableNaturalSpawning(value))))
        .then(spawnRateNode())
        .then(
            ConfigSettingNodes.toggle(
                "capindependentspawning",
                (current, value) ->
                    current.withSpawning(
                        current.spawning().withCapIndependentSpawningEnabled(value))));
  }

  static List<Text> helpLines() {
    return List.of(
        Text.translatable("command.dogs-unleashed.help.spawn"),
        Text.translatable(
            "command.dogs-unleashed.help.spawnrate",
            SpawningSettings.RATE_MIN,
            SpawningSettings.RATE_MAX),
        Text.translatable(
            "command.dogs-unleashed.help.spawnratebreed",
            SpawningSettings.RATE_MIN,
            SpawningSettings.RATE_MAX),
        Text.translatable("command.dogs-unleashed.help.capindependentspawning"));
  }

  static List<Text> statusLines(final SpawningSettings settings) {
    final List<Text> lines = new ArrayList<>();
    lines.add(
        Text.translatable("command.dogs-unleashed.status.spawn", settings.enableNaturalSpawning()));
    lines.add(
        Text.translatable(
            "command.dogs-unleashed.status.spawnrate", settings.spawnRateMultiplierPercent()));
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      if (breed.isNaturallySpawning()) {
        lines.add(
            Text.translatable(
                "command.dogs-unleashed.status.spawnrate.breed",
                breed.serializedId(),
                settings.breedSpawnRateMultipliersPercent().get(breed.serializedId())));
      }
    }
    lines.add(
        Text.translatable(
            "command.dogs-unleashed.status.capindependentspawning",
            settings.capIndependentSpawningEnabled()));
    return lines;
  }

  private static LiteralArgumentBuilder<ServerCommandSource> spawnRateNode() {
    final LiteralArgumentBuilder<ServerCommandSource> node = CommandManager.literal("spawnrate");
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      if (breed.isNaturallySpawning()) {
        node.then(
            CommandManager.literal(breed.serializedId())
                .then(
                    ConfigSettingNodes.intArgument(
                        "spawnrate " + breed.serializedId(),
                        "percent",
                        SpawningSettings.RATE_MIN,
                        SpawningSettings.RATE_MAX,
                        (current, value) -> withBreedRate(current, breed, value),
                        value -> value + "%")));
      }
    }
    return node.then(
        ConfigSettingNodes.intArgument(
            "spawnrate",
            "percent",
            SpawningSettings.RATE_MIN,
            SpawningSettings.RATE_MAX,
            (current, value) ->
                current.withSpawning(current.spawning().withSpawnRateMultiplierPercent(value)),
            value -> value + "%"));
  }

  private static DogsUnleashedConfig withBreedRate(
      final DogsUnleashedConfig current, final UnleashedDogBreed breed, final int value) {
    return current.withSpawning(
        current.spawning().withBreedSpawnRateMultiplierPercent(breed.serializedId(), value));
  }
}
