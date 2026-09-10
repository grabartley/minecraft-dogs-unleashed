package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModSounds;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DogWhistleItem extends Item implements GeoItem {

  public static final int BLOW_COOLDOWN_TICKS = 20;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public DogWhistleItem(final Settings settings) {
    super(settings);
  }

  public static void bind(final ItemStack whistle, final UUID petId, final String dogName) {
    whistle.set(ModComponents.WHISTLE_TARGET_PET, petId);
    whistle.set(ModComponents.WHISTLE_TARGET_NAME, dogName);
  }

  @Override
  public TypedActionResult<ItemStack> use(
      final World world, final PlayerEntity user, final Hand hand) {
    final ItemStack stack = user.getStackInHand(hand);
    if (!(user instanceof ServerPlayerEntity player)) {
      return TypedActionResult.success(stack, world.isClient);
    }

    final MinecraftServer server = player.getServer();
    if (server == null) {
      return TypedActionResult.pass(stack);
    }

    final List<PetData> pets = PetManager.get(server).getPetsByOwner(player.getUuid());
    final PetData bound =
        WhistleBinding.boundPet(pets, stack.get(ModComponents.WHISTLE_TARGET_PET));

    playWhistle(player);
    player.getItemCooldownManager().set(this, BLOW_COOLDOWN_TICKS);

    if (bound == null) {
      player.sendMessage(
          Text.translatable("message.dogs-unleashed.whistle.unbound").formatted(Formatting.GRAY),
          true);
      return TypedActionResult.success(stack, false);
    }

    stack.set(ModComponents.WHISTLE_TARGET_NAME, bound.getName());
    PetLocationService.loadAndSummon(server, bound, player);
    return TypedActionResult.success(stack, false);
  }

  private static void playWhistle(final ServerPlayerEntity player) {
    player
        .getWorld()
        .playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            ModSounds.DOG_WHISTLE_BLOW,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f);
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    final String boundName = stack.get(ModComponents.WHISTLE_TARGET_NAME);
    if (boundName == null || boundName.isBlank()) {
      tooltip.add(
          Text.translatable("item.dogs-unleashed.dog_whistle.unbound").formatted(Formatting.GRAY));
      return;
    }
    tooltip.add(
        Text.translatable("item.dogs-unleashed.dog_whistle.bound", boundName)
            .formatted(Formatting.GRAY));
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {}

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
