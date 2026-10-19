package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.item.DogWhistleItem;
import java.util.UUID;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class PetManagerSummon {

  private PetManagerSummon() {}

  public static void summon(final ServerPlayerEntity player, final UUID petId) {
    final MinecraftServer server = player.getServer();
    final PetData petData = PetManager.get(server).getPet(player.getUuid(), petId);
    if (petData == null) {
      DogsUnleashed.log.warn(
          "[PetSummon] Summon requested for unknown pet {} by {}", petId, player.getUuid());
      return;
    }
    if (!petData.isAlive()) {
      player.sendMessage(
          Text.translatable("message.dogs-unleashed.summon_deceased", petData.getName()), true);
      return;
    }
    final ItemCooldownManager cooldowns = player.getItemCooldownManager();
    if (cooldowns.isCoolingDown(ModItems.DOG_WHISTLE)) {
      return;
    }
    cooldowns.set(ModItems.DOG_WHISTLE, DogWhistleItem.BLOW_COOLDOWN_TICKS);
    PetLocationService.loadAndSummon(server, petData, player);
  }
}
