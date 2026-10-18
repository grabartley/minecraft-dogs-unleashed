package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogTrustRules;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public class TrustFollowGoal extends Goal {

  private static final double FOLLOW_SPEED = 1.1;
  private static final int REPATH_TICKS = 10;

  private final UnleashedDogEntity dog;
  private @Nullable PlayerEntity friend;
  private int repathCooldown;

  public TrustFollowGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    this.friend = this.trustedFriend();
    return this.friend != null && this.dog.distanceTo(this.friend) > DogTrustRules.FOLLOW_FARTHEST;
  }

  @Override
  public void start() {
    this.repathCooldown = 0;
  }

  @Override
  public boolean shouldContinue() {
    return this.friend != null
        && this.friend == this.trustedFriend()
        && this.dog.distanceTo(this.friend) > DogTrustRules.FOLLOW_NEAREST;
  }

  @Override
  public void tick() {
    this.dog.getLookControl().lookAt(this.friend, 10.0f, this.dog.getMaxLookPitchChange());
    if (--this.repathCooldown <= 0) {
      this.repathCooldown = this.getTickCount(REPATH_TICKS);
      this.dog.getNavigation().startMovingTo(this.friend, FOLLOW_SPEED);
    }
  }

  @Override
  public void stop() {
    this.friend = null;
    this.dog.getNavigation().stop();
  }

  private @Nullable PlayerEntity trustedFriend() {
    if (!this.dog.getTrust().canBeWon()) {
      return null;
    }
    final UUID friendId = this.dog.getTrust().mostTrusted();
    final PlayerEntity player =
        friendId == null ? null : this.dog.getWorld().getPlayerByUuid(friendId);
    return player != null
            && player.isAlive()
            && player.distanceTo(this.dog) <= DogTrustRules.FOLLOW_RANGE
        ? player
        : null;
  }
}
