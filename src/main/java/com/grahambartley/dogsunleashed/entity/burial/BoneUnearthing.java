package com.grahambartley.dogsunleashed.entity.burial;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class BoneUnearthing {

  public static final RegistryKey<LootTable> DOG_DIG_LOOT =
      RegistryKey.of(
          RegistryKeys.LOOT_TABLE, Identifier.of(DogsUnleashed.MOD_ID, "gameplay/dog_dig"));

  private BoneUnearthing() {}

  public static ItemStack bone() {
    return new ItemStack(Items.BONE);
  }

  public static void popBonus(final ServerWorld world, final BlockPos ground) {
    if (!BoneCacheRules.rollsBonus(world.getRandom().nextInt(BoneCacheRules.BONUS_ODDS))) {
      return;
    }
    final Vec3d origin = Vec3d.ofCenter(ground.up());
    final List<ItemStack> bonus =
        world
            .getServer()
            .getReloadableRegistries()
            .getLootTable(DOG_DIG_LOOT)
            .generateLoot(
                new LootContextParameterSet.Builder(world)
                    .add(LootContextParameters.ORIGIN, origin)
                    .build(LootContextTypes.CHEST));
    bonus.forEach(stack -> ItemScatterer.spawn(world, origin.x, origin.y, origin.z, stack));
  }
}
