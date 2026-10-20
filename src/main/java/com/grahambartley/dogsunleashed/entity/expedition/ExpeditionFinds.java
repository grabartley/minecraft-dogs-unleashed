package com.grahambartley.dogsunleashed.entity.expedition;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ExpeditionFinds {

  public static final TagKey<Item> RARE_FINDS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(DogsUnleashed.MOD_ID, "expedition_rare_finds"));
  public static final String MAP_NAME_KEY = "filled_map.dogs-unleashed.expedition";
  private static final byte MAP_SCALE = 1;

  private ExpeditionFinds() {}

  public static RegistryKey<LootTable> lootTable(final ExpeditionBiomeGroup group) {
    return RegistryKey.of(
        RegistryKeys.LOOT_TABLE, Identifier.of(DogsUnleashed.MOD_ID, group.lootTablePath()));
  }

  public static List<ItemStack> roll(final ServerWorld world, final BlockPos spot) {
    final ExpeditionBiomeGroup group =
        ExpeditionBiomes.groupOf(ExpeditionBiomes.biomeAt(world, spot.getX(), spot.getZ()));
    return world
        .getServer()
        .getReloadableRegistries()
        .getLootTable(lootTable(group))
        .generateLoot(
            new LootContextParameterSet.Builder(world)
                .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(spot))
                .build(LootContextTypes.CHEST));
  }

  public static boolean isRare(final List<ItemStack> finds) {
    return finds.stream().anyMatch(stack -> stack.isIn(RARE_FINDS));
  }

  public static ItemStack map(final ServerWorld world, final BlockPos spot) {
    final ItemStack map =
        FilledMapItem.createMap(world, spot.getX(), spot.getZ(), MAP_SCALE, true, true);
    FilledMapItem.fillExplorationMap(world, map);
    MapState.addDecorationsNbt(map, spot, "+", MapDecorationTypes.TARGET_X);
    map.set(DataComponentTypes.ITEM_NAME, Text.translatable(MAP_NAME_KEY));
    return map;
  }
}
