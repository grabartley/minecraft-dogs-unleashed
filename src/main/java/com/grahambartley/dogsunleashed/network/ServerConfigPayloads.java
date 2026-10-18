package com.grahambartley.dogsunleashed.network;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.config.DireWolfPackSettings;
import com.grahambartley.dogsunleashed.config.DireWolfSettings;
import com.grahambartley.dogsunleashed.config.DireWolfStaminaSettings;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.config.FurSettings;
import com.grahambartley.dogsunleashed.config.GameplaySettings;
import com.grahambartley.dogsunleashed.config.SleepSettings;
import com.grahambartley.dogsunleashed.config.SoundSettings;
import com.grahambartley.dogsunleashed.config.SpawningSettings;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class ServerConfigPayloads {
  public static final Identifier SYNC_SERVER_CONFIG_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "sync_server_config");
  public static final Identifier EDIT_SERVER_CONFIG_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "edit_server_config");

  private ServerConfigPayloads() {}

  public record SyncServerConfigS2CPayload(DogsUnleashedConfig config) implements CustomPayload {
    public static final CustomPayload.Id<SyncServerConfigS2CPayload> ID =
        new CustomPayload.Id<>(SYNC_SERVER_CONFIG_ID);
    public static final PacketCodec<RegistryByteBuf, SyncServerConfigS2CPayload> CODEC =
        PacketCodec.of(SyncServerConfigS2CPayload::write, SyncServerConfigS2CPayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }

    private void write(final RegistryByteBuf buf) {
      writeConfig(buf, config);
    }

    private static SyncServerConfigS2CPayload read(final RegistryByteBuf buf) {
      return new SyncServerConfigS2CPayload(readConfig(buf));
    }
  }

  public record EditServerConfigC2SPayload(DogsUnleashedConfig config) implements CustomPayload {
    public static final CustomPayload.Id<EditServerConfigC2SPayload> ID =
        new CustomPayload.Id<>(EDIT_SERVER_CONFIG_ID);
    public static final PacketCodec<RegistryByteBuf, EditServerConfigC2SPayload> CODEC =
        PacketCodec.of(EditServerConfigC2SPayload::write, EditServerConfigC2SPayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }

    private void write(final RegistryByteBuf buf) {
      writeConfig(buf, config);
    }

    private static EditServerConfigC2SPayload read(final RegistryByteBuf buf) {
      return new EditServerConfigC2SPayload(readConfig(buf));
    }
  }

  private static void writeConfig(final RegistryByteBuf buf, final DogsUnleashedConfig config) {
    final SpawningSettings spawning = config.spawning();
    buf.writeBoolean(spawning.enableNaturalSpawning());
    buf.writeVarInt(spawning.spawnRateMultiplierPercent());
    buf.writeMap(
        spawning.breedSpawnRateMultipliersPercent(),
        PacketByteBuf::writeString,
        PacketByteBuf::writeVarInt);
    buf.writeBoolean(spawning.capIndependentSpawningEnabled());
    buf.writeBoolean(config.gameplay().gravesEnabled());
    buf.writeBoolean(config.gameplay().dropLeashOnPlayMode());
    buf.writeBoolean(config.gameplay().showDogNames());
    buf.writeBoolean(config.sleep().autoSleepEnabled());
    buf.writeInt(config.sleep().autoSleepRangeBlocks());
    buf.writeBoolean(config.fur().furGrowthEnabled());
    buf.writeVarInt(config.fur().furGrowthRateMultiplierPercent());
    buf.writeVarInt(config.fur().badHairDayChanceMultiplierPercent());
    buf.writeFloat(config.sound().barkVolume());
    buf.writeFloat(config.sound().howlVolume());
    final DireWolfSettings direWolf = config.direWolf();
    buf.writeVarInt(direWolf.growthRateMultiplierPercent());
    buf.writeVarInt(direWolf.battlesToEvolve());
    buf.writeVarInt(direWolf.smallestPack());
    buf.writeVarInt(direWolf.largestPack());
    buf.writeBoolean(direWolf.packs().hunt());
    buf.writeBoolean(direWolf.packs().defend());
    buf.writeBoolean(direWolf.stamina().drains());
    buf.writeVarInt(direWolf.stamina().capacityPercent());
    buf.writeVarInt(direWolf.stamina().recoveryPercent());
  }

  private static DogsUnleashedConfig readConfig(final RegistryByteBuf buf) {
    return new DogsUnleashedConfig(
        new SpawningSettings(
            buf.readBoolean(),
            buf.readVarInt(),
            buf.readMap(PacketByteBuf::readString, PacketByteBuf::readVarInt),
            buf.readBoolean()),
        new GameplaySettings(buf.readBoolean(), buf.readBoolean(), buf.readBoolean()),
        new SleepSettings(buf.readBoolean(), buf.readInt()),
        new FurSettings(buf.readBoolean(), buf.readVarInt(), buf.readVarInt()),
        new SoundSettings(buf.readFloat(), buf.readFloat()),
        new DireWolfSettings(
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            new DireWolfPackSettings(buf.readBoolean(), buf.readBoolean()),
            new DireWolfStaminaSettings(buf.readBoolean(), buf.readVarInt(), buf.readVarInt())));
  }
}
