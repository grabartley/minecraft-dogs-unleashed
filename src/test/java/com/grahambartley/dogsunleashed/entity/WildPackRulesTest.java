package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.config.DireWolfSettings;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class WildPackRulesTest {

  static Stream<Class<?>> prey() {
    return Stream.of(
        SheepEntity.class,
        CowEntity.class,
        RabbitEntity.class,
        FoxEntity.class,
        GoatEntity.class,
        PolarBearEntity.class,
        SkeletonEntity.class,
        StrayEntity.class,
        ZombieEntity.class,
        HuskEntity.class,
        DrownedEntity.class);
  }

  static Stream<Class<?>> notPrey() {
    return Stream.of(
        UnleashedDogEntity.class,
        PlayerEntity.class,
        VillagerEntity.class,
        IronGolemEntity.class,
        CreeperEntity.class,
        SpiderEntity.class);
  }

  @ParameterizedTest
  @MethodSource("prey")
  @DisplayName("animals, skeletons and zombies are prey")
  void preyTypes(final Class<?> type) {
    assertTrue(WildPackRules.isPreyType(type));
  }

  @ParameterizedTest
  @MethodSource("notPrey")
  @DisplayName("dogs, players, villagers, golems and other monsters are never prey")
  void notPreyTypes(final Class<?> type) {
    assertFalse(WildPackRules.isPreyType(type));
  }

  @ParameterizedTest
  @EnumSource(
      value = DireWolfGrowthStage.class,
      names = {"ADULT", "PRIME", "ALPHA"})
  @DisplayName("adults, primes and alphas are grown")
  void grownStages(final DireWolfGrowthStage stage) {
    assertTrue(WildPackRules.isGrownStage(stage));
  }

  @ParameterizedTest
  @EnumSource(
      value = DireWolfGrowthStage.class,
      names = {"PUP", "YEARLING"})
  @DisplayName("pups and yearlings are not grown")
  void youngStages(final DireWolfGrowthStage stage) {
    assertFalse(WildPackRules.isGrownStage(stage));
  }

  @Test
  @DisplayName("a wolf waits two minutes after a kill before it hunts again")
  void huntCooldown() {
    assertEquals(2400, WildPackRules.HUNT_COOLDOWN_TICKS);
    assertFalse(WildPackRules.huntCooldownOver(1000, 1001));
    assertTrue(WildPackRules.huntCooldownOver(1001, 1001));
    assertTrue(WildPackRules.huntCooldownOver(0, 0));
  }

  @Test
  @DisplayName("the pack remembers who hurt it for thirty seconds")
  void threatMemory() {
    assertTrue(WildPackRules.remembersThreat(100, 101));
    assertFalse(WildPackRules.remembersThreat(101, 101));
  }

  @Test
  @DisplayName(
      "a pack spreads over the columns within 3 blocks of its leader, not the leader's own")
  void spreadColumnsCoverTheSquareAroundTheLeader() {
    final BlockPos leader = new BlockPos(8, 64, 8);
    final List<BlockPos> columns = WildPackRules.spreadColumns(leader);
    assertEquals(48, columns.size());
    assertFalse(columns.contains(leader));
    assertTrue(
        columns.stream()
            .allMatch(
                column ->
                    Math.abs(column.getX() - 8) <= WildPackRules.PACK_SPREAD_BLOCKS
                        && Math.abs(column.getZ() - 8) <= WildPackRules.PACK_SPREAD_BLOCKS
                        && column.getY() == 64));
  }

  static Stream<Arguments> chunkEdgeLeaders() {
    return Stream.of(
        Arguments.of(new BlockPos(0, 64, 0), 15),
        Arguments.of(new BlockPos(15, 64, 15), 15),
        Arguments.of(new BlockPos(0, 64, 8), 27),
        Arguments.of(new BlockPos(-1, 64, -1), 15));
  }

  @ParameterizedTest(name = "a leader at {0} has {1} columns")
  @MethodSource("chunkEdgeLeaders")
  @DisplayName("a pack never spreads into another chunk")
  void spreadColumnsStayInTheLeadersChunk(final BlockPos leader, final int expected) {
    final List<BlockPos> columns = WildPackRules.spreadColumns(leader);
    assertEquals(expected, columns.size());
    assertTrue(
        columns.stream().allMatch(column -> new ChunkPos(column).equals(new ChunkPos(leader))));
  }

  @Test
  @DisplayName("even a corner leader has room for the largest allowed pack")
  void everyLeaderHasRoomForTheLargestPack() {
    final int fewestColumns = WildPackRules.spreadColumns(new BlockPos(0, 64, 0)).size();
    assertTrue(WildPackRules.hasRoomForPack(fewestColumns, DireWolfSettings.PACK_MAX));
  }

  static Stream<Arguments> roomCases() {
    return Stream.of(
        Arguments.of(3, 4, true),
        Arguments.of(2, 4, false),
        Arguments.of(4, 5, true),
        Arguments.of(3, 5, false),
        Arguments.of(0, 1, true));
  }

  @ParameterizedTest(name = "{0} free spots, smallest pack {1} -> {2}")
  @MethodSource("roomCases")
  @DisplayName("a leader needs a free spot for every other member of its smallest pack")
  void hasRoomForPackNeedsASpotPerOtherMember(
      final int freeSpots, final int smallestPack, final boolean expected) {
    assertEquals(expected, WildPackRules.hasRoomForPack(freeSpots, smallestPack));
  }

  static Stream<Arguments> memberCases() {
    return Stream.of(
        Arguments.of(5, 48, 4),
        Arguments.of(10, 48, 9),
        Arguments.of(10, 6, 6),
        Arguments.of(5, 0, 0),
        Arguments.of(1, 48, 0));
  }

  @ParameterizedTest(name = "pack of {0} with {1} free spots places {2} members")
  @MethodSource("memberCases")
  @DisplayName("the leader places one member per other pack place, up to the free spots")
  void membersToPlaceFillsThePackUpToTheFreeSpots(
      final int packSize, final int freeSpots, final int expected) {
    assertEquals(expected, WildPackRules.membersToPlace(packSize, freeSpots));
  }
}
