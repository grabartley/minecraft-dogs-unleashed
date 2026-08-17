package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncDogConnectionsPayload(
    ConnectionDogSyncData self,
    List<BreedShare> focusComposition,
    List<ConnectionDogSyncData> parents,
    List<ConnectionDogSyncData> mates,
    List<ConnectionDogSyncData> siblings,
    List<ConnectionDogSyncData> children,
    boolean truncated)
    implements CustomPayload {

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "sync_dog_connections");
  public static final CustomPayload.Id<SyncDogConnectionsPayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, SyncDogConnectionsPayload> CODEC =
      PacketCodec.of(SyncDogConnectionsPayload::write, SyncDogConnectionsPayload::read);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }

  private void write(final RegistryByteBuf buf) {
    this.self.write(buf);
    BreedShareCodec.writeList(buf, this.focusComposition);
    writeConnectionList(buf, this.parents);
    writeConnectionList(buf, this.mates);
    writeConnectionList(buf, this.siblings);
    writeConnectionList(buf, this.children);
    buf.writeBoolean(this.truncated);
  }

  private static SyncDogConnectionsPayload read(final RegistryByteBuf buf) {
    return new SyncDogConnectionsPayload(
        ConnectionDogSyncData.read(buf),
        BreedShareCodec.readList(buf),
        readConnectionList(buf),
        readConnectionList(buf),
        readConnectionList(buf),
        readConnectionList(buf),
        buf.readBoolean());
  }

  private static void writeConnectionList(
      final RegistryByteBuf buf, final List<ConnectionDogSyncData> connections) {
    buf.writeVarInt(connections.size());
    for (final ConnectionDogSyncData connection : connections) {
      connection.write(buf);
    }
  }

  private static List<ConnectionDogSyncData> readConnectionList(final RegistryByteBuf buf) {
    final int count = buf.readVarInt();
    final List<ConnectionDogSyncData> connections = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      connections.add(ConnectionDogSyncData.read(buf));
    }
    return connections;
  }
}
