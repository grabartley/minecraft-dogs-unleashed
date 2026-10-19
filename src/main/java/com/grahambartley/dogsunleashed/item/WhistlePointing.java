package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModSounds;
import com.grahambartley.dogsunleashed.entity.DogPointedOrders;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.WhistlePointRules.Unavailable;
import com.grahambartley.dogsunleashed.network.payload.PointWithWhistlePayload;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.Optional;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class WhistlePointing {

  private static final int MARKER_PARTICLES = 8;
  private static final double MARKER_SPREAD = 0.2;
  private static final double ENTITY_MARKER_LIFT = 0.5;
  private static final double SPOT_MARKER_LIFT = 0.25;

  private WhistlePointing() {}

  public static void point(final ServerPlayerEntity player, final PointWithWhistlePayload payload) {
    final ItemStack whistle = player.getStackInHand(payload.hand());
    if (!whistle.isOf(ModItems.DOG_WHISTLE)
        || player.getItemCooldownManager().isCoolingDown(ModItems.DOG_WHISTLE)) {
      return;
    }
    final MinecraftServer server = player.getServer();
    if (server == null) {
      return;
    }
    final PetData bound =
        WhistleBinding.boundPet(
            PetManager.get(server).getPetsByOwner(player.getUuid()),
            whistle.get(ModComponents.WHISTLE_TARGET_PET));
    if (bound == null) {
      tell(player, Text.translatable("message.dogs-unleashed.whistle.unbound"));
      return;
    }
    final Optional<WhistlePointTarget> target = WhistlePointTargets.resolve(player, payload);
    if (target.isEmpty()) {
      return;
    }
    final UnleashedDogEntity dog = dogNear(server, bound, player);
    final Optional<Unavailable> unavailable =
        WhistlePointRules.unavailable(
            dog != null,
            dog != null && dog.isInSittingPose(),
            dog != null && dog.getSleepController().isAsleep(),
            dog != null && dog.getRiding().isRidden());
    if (unavailable.isPresent()) {
      tell(player, Text.translatable(unavailable.get().messageKey(), bound.getName()));
      return;
    }
    final Vec3d marker = order(dog, target.get());
    player.getItemCooldownManager().set(ModItems.DOG_WHISTLE, DogWhistleItem.BLOW_COOLDOWN_TICKS);
    playPointNote(player);
    showMarker(player, marker);
    dog.getCommandController().acknowledge();
  }

  public static Vec3d order(final UnleashedDogEntity dog, final WhistlePointTarget target) {
    final DogPointedOrders orders = dog.getComponents().get(DogPointedOrders.class);
    switch (target.order()) {
      case ATTACK -> orders.attack((LivingEntity) target.entity());
      case FETCH -> orders.fetch((ItemEntity) target.entity());
      case GO_THERE -> {
        final BlockPos spot =
            WhistlePointTargets.standingSpot(dog, target.blockPos(), target.side());
        dog.getCommandController().goTo(spot);
        return Vec3d.ofBottomCenter(spot).add(0.0, SPOT_MARKER_LIFT, 0.0);
      }
      case NONE -> {}
    }
    return target.entity() == null
        ? Vec3d.ofCenter(target.blockPos())
        : target.entity().getPos().add(0.0, target.entity().getHeight() + ENTITY_MARKER_LIFT, 0.0);
  }

  private static @Nullable UnleashedDogEntity dogNear(
      final MinecraftServer server, final PetData bound, final ServerPlayerEntity player) {
    final UnleashedDogEntity dog = PetLocationService.findLoadedDog(server, bound);
    if (dog == null || dog.getWorld() != player.getWorld() || !dog.isOwner(player)) {
      return null;
    }
    return dog;
  }

  private static void playPointNote(final ServerPlayerEntity player) {
    player
        .getWorld()
        .playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            ModSounds.DOG_WHISTLE_POINT,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f);
  }

  private static void showMarker(final ServerPlayerEntity player, final Vec3d at) {
    player
        .getServerWorld()
        .spawnParticles(
            player,
            ParticleTypes.HAPPY_VILLAGER,
            true,
            at.x,
            at.y,
            at.z,
            MARKER_PARTICLES,
            MARKER_SPREAD,
            MARKER_SPREAD,
            MARKER_SPREAD,
            0.0);
  }

  private static void tell(final ServerPlayerEntity player, final Text message) {
    player.sendMessage(message.copy().formatted(Formatting.GRAY), true);
  }
}
