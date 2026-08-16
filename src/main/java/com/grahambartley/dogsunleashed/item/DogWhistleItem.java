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
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Recalls the owner's pets without opening the Pet Manager. The stack remembers which pet it is
 * pointed at, and sneaking cycles that target through the pack.
 */
public class DogWhistleItem extends Item implements GeoItem {

  public static final int BLOW_COOLDOWN_TICKS = 20;
  public static final int CYCLE_COOLDOWN_TICKS = 4;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public DogWhistleItem(final Settings settings) {
    super(settings);
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
    return player.isSneaking() ? cycleTarget(player, stack, pets) : blow(player, stack, pets);
  }

  private TypedActionResult<ItemStack> blow(
      final ServerPlayerEntity player, final ItemStack stack, final List<PetData> pets) {
    final UUID target =
        WhistleTargetCycle.resolveTarget(
            pets,
            stack.get(ModComponents.WHISTLE_TARGET_PET),
            player.getWorld().getRegistryKey().getValue().toString(),
            player.getBlockPos());

    playWhistle(player, 1.0f, 1.0f);
    player.getItemCooldownManager().set(this, BLOW_COOLDOWN_TICKS);

    if (target == null) {
      player.sendMessage(
          Text.translatable("message.dogs-unleashed.whistle.no_target").formatted(Formatting.GRAY),
          true);
      return TypedActionResult.success(stack, false);
    }

    stack.set(ModComponents.WHISTLE_TARGET_PET, target);
    final PetData petData = findPet(pets, target);
    if (petData != null) {
      PetLocationService.loadAndSummon(player.getServer(), petData, player);
    }
    return TypedActionResult.success(stack, false);
  }

  private TypedActionResult<ItemStack> cycleTarget(
      final ServerPlayerEntity player, final ItemStack stack, final List<PetData> pets) {
    final UUID next =
        WhistleTargetCycle.nextTarget(pets, stack.get(ModComponents.WHISTLE_TARGET_PET));
    player.getItemCooldownManager().set(this, CYCLE_COOLDOWN_TICKS);

    if (next == null) {
      player.sendMessage(
          Text.translatable("message.dogs-unleashed.whistle.no_target").formatted(Formatting.GRAY),
          true);
      return TypedActionResult.success(stack, false);
    }

    stack.set(ModComponents.WHISTLE_TARGET_PET, next);
    playWhistle(player, 0.4f, 1.5f);
    final PetData petData = findPet(pets, next);
    player.sendMessage(
        Text.translatable(
            "message.dogs-unleashed.whistle.target_changed",
            petData == null ? "" : petData.getName()),
        true);
    return TypedActionResult.success(stack, false);
  }

  private static void playWhistle(
      final ServerPlayerEntity player, final float volume, final float pitch) {
    player
        .getWorld()
        .playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            ModSounds.DOG_WHISTLE_BLOW,
            SoundCategory.PLAYERS,
            volume,
            pitch);
  }

  private static @Nullable PetData findPet(final List<PetData> pets, final UUID petId) {
    return pets.stream().filter(pet -> petId.equals(pet.getPetId())).findFirst().orElse(null);
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    tooltip.add(
        Text.translatable("item.dogs-unleashed.dog_whistle.tooltip").formatted(Formatting.GRAY));
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {}

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
