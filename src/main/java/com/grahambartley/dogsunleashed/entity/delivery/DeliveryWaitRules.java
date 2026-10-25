package com.grahambartley.dogsunleashed.entity.delivery;

import java.util.Optional;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class DeliveryWaitRules {

  public static final long WAIT_TICKS = 6000L;
  public static final double RECIPIENT_RANGE = 48.0;

  public enum BagsClick {
    PASS,
    OPEN,
    KEEP_OPEN
  }

  private DeliveryWaitRules() {}

  public static BagsClick recipientClick(
      final boolean sneaking, final boolean waitingFor, final boolean viewingThisDog) {
    if (sneaking || !waitingFor) {
      return BagsClick.PASS;
    }
    return viewingThisDog ? BagsClick.KEEP_OPEN : BagsClick.OPEN;
  }

  public static boolean isChunkUnload(final @Nullable Entity.RemovalReason reason) {
    return reason == null || reason == Entity.RemovalReason.UNLOADED_TO_CHUNK;
  }

  public static boolean isStaleCopy(final boolean tagged, final Optional<DeliveryLeg> leg) {
    return tagged && leg.map(current -> current != DeliveryLeg.WAITING).orElse(true);
  }

  public static boolean headsHome(
      final boolean recipientPresent,
      final boolean sameWorld,
      final double distanceToRecipient,
      final boolean waitOver) {
    return !recipientPresent || !sameWorld || distanceToRecipient > RECIPIENT_RANGE || waitOver;
  }
}
