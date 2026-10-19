package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record PointWithWhistlePayload(boolean mainHand, int entityId, BlockPos blockPos)
    implements CustomPayload {

  public static final int NO_ENTITY = -1;

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "point_with_whistle");
  public static final CustomPayload.Id<PointWithWhistlePayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, PointWithWhistlePayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.BOOL,
          PointWithWhistlePayload::mainHand,
          PacketCodecs.VAR_INT,
          PointWithWhistlePayload::entityId,
          BlockPos.PACKET_CODEC,
          PointWithWhistlePayload::blockPos,
          PointWithWhistlePayload::new);

  public static PointWithWhistlePayload atBlock(final Hand hand, final BlockPos pos) {
    return new PointWithWhistlePayload(hand == Hand.MAIN_HAND, NO_ENTITY, pos);
  }

  public static PointWithWhistlePayload atEntity(final Hand hand, final int entityId) {
    return new PointWithWhistlePayload(hand == Hand.MAIN_HAND, entityId, BlockPos.ORIGIN);
  }

  public Hand hand() {
    return this.mainHand ? Hand.MAIN_HAND : Hand.OFF_HAND;
  }

  public boolean pointsAtEntity() {
    return this.entityId != NO_ENTITY;
  }

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
