package com.grahambartley.dogsunleashed.command;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
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
            .then(configNode())
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

  private static LiteralArgumentBuilder<ServerCommandSource> configNode() {
    final LiteralArgumentBuilder<ServerCommandSource> config =
        CommandManager.literal("config").requires(DogsUnleashedCommand::isOp);
    SpawningConfigCommand.addTo(config);
    GameplayConfigCommand.addTo(config);
    SleepConfigCommand.addTo(config);
    FurConfigCommand.addTo(config);
    SoundConfigCommand.addTo(config);
    return config
        .then(DireWolfConfigCommand.node())
        .then(CommandManager.literal("reset").executes(DogsUnleashedCommand::resetAll));
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

  static List<Text> helpLines() {
    final List<Text> lines = new ArrayList<>();
    lines.add(Text.translatable("command.dogs-unleashed.help.header"));
    lines.add(Text.translatable("command.dogs-unleashed.help.status"));
    lines.addAll(SpawningConfigCommand.helpLines());
    lines.addAll(GameplayConfigCommand.helpLines());
    lines.addAll(SleepConfigCommand.helpLines());
    lines.addAll(FurConfigCommand.helpLines());
    lines.addAll(SoundConfigCommand.helpLines());
    lines.addAll(DireWolfConfigCommand.helpLines());
    lines.addAll(
        List.of(
            Text.translatable("command.dogs-unleashed.help.reset"),
            Text.translatable("command.dogs-unleashed.help.list"),
            Text.translatable("command.dogs-unleashed.help.summon"),
            Text.translatable("command.dogs-unleashed.help.find")));
    return lines;
  }

  private static int status(final CommandContext<ServerCommandSource> ctx) {
    final ServerCommandSource source = ctx.getSource();
    for (final Text line : statusLines(DogsUnleashed.SERVER_CONFIG)) {
      source.sendFeedback(() -> line, false);
    }
    return 1;
  }

  static List<Text> statusLines(final DogsUnleashedConfig config) {
    final List<Text> lines = new ArrayList<>();
    lines.add(Text.translatable("command.dogs-unleashed.status.header"));
    lines.addAll(SpawningConfigCommand.statusLines(config.spawning()));
    lines.addAll(GameplayConfigCommand.statusLines(config.gameplay()));
    lines.addAll(SleepConfigCommand.statusLines(config.sleep()));
    lines.addAll(SoundConfigCommand.statusLines(config.sound()));
    lines.addAll(FurConfigCommand.statusLines(config.fur()));
    lines.addAll(DireWolfConfigCommand.statusLines(config.direWolf()));
    return lines;
  }

  private static int resetAll(final CommandContext<ServerCommandSource> ctx) {
    return ConfigSettingNodes.applyUpdate(ctx, DogsUnleashedConfig.defaults(), "all", "defaults");
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
