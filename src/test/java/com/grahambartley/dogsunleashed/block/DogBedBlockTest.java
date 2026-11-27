package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import com.grahambartley.dogsunleashed.wood.WoodenBlock;
import java.util.UUID;
import net.minecraft.block.WoodType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogBedBlockTest {

  @Test
  @DisplayName("dog bed hardness matches oak wood (2.0) so it breaks in a comparable time")
  void hardnessMatchesOakWood() {
    assertEquals(2.0f, ModBlocks.DOG_BED.getHardness(), 0.0f);
  }

  @Test
  @DisplayName("pendingAssignment maps the player UUID to the dog UUID and clears on first consume")
  void pendingAssignmentRoundTrip() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID dogUuid = UUID.randomUUID();

    DogBedBlock.setPendingAssignment(playerUuid, dogUuid);

    assertEquals(dogUuid, DogBedBlock.consumePendingAssignment(playerUuid));
    assertNull(
        DogBedBlock.consumePendingAssignment(playerUuid),
        "Second consume should return null because the assignment is single-use");
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"oak", "spruce", "acacia", "crimson", "bamboo"})
  @DisplayName("the block reports the wood its block entity was built from")
  void woodComesFromTheBlockEntity(final String name) {
    final WoodType wood =
        WoodType.stream().filter(type -> type.name().equals(name)).findFirst().orElseThrow();
    final BlockPos origin = new BlockPos(5, 64, 5);
    final DogBedBlockEntity entity =
        new DogBedBlockEntity(origin, ModBlocks.DOG_BED.getDefaultState());
    entity.setWood(wood);
    final BlockView world = mock(BlockView.class);
    when(world.getBlockEntity(origin)).thenReturn(entity);

    assertSame(
        wood,
        ((WoodenBlock) ModBlocks.DOG_BED)
            .woodAt(world, origin, ModBlocks.DOG_BED.getDefaultState()));
  }

  @Test
  @DisplayName("with no block entity to ask, the block counts as oak")
  void missingBlockEntityIsOak() {
    final BlockPos pos = new BlockPos(5, 64, 5);
    assertSame(
        WoodType.OAK,
        ((WoodenBlock) ModBlocks.DOG_BED)
            .woodAt(mock(BlockView.class), pos, ModBlocks.DOG_BED.getDefaultState()));
  }
}
