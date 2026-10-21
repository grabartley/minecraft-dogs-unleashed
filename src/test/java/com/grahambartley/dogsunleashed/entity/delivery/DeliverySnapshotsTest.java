package com.grahambartley.dogsunleashed.entity.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeliverySnapshotsTest {

  private static final String TAG = "dogs-unleashed.delivery_waiting";

  private static NbtCompound tagged(final String tags) {
    final NbtCompound nbt = new NbtCompound();
    final NbtList list = new NbtList();
    if (!tags.isBlank()) {
      Arrays.stream(tags.split(" ")).forEach(tag -> list.add(NbtString.of(tag)));
    }
    nbt.put(DeliverySnapshots.TAGS, list);
    return nbt;
  }

  private static List<String> tagsOf(final NbtCompound nbt) {
    return nbt.getList(DeliverySnapshots.TAGS, NbtElement.STRING_TYPE).stream()
        .map(NbtElement::asString)
        .toList();
  }

  @ParameterizedTest(name = "[{0}] -> [{1}]")
  @CsvSource({
    "'dogs-unleashed.delivery_waiting', ''",
    "'rex dogs-unleashed.delivery_waiting', 'rex'",
    "'rex', 'rex'",
    "'a dogs-unleashed.delivery_waiting b', 'a b'",
    "'', ''"
  })
  @DisplayName("only the waiting tag is taken off the snapshot")
  void stripsOnlyTheWaitingTag(final String before, final String after) {
    final List<String> expected = after.isBlank() ? List.of() : List.of(after.split(" "));
    assertEquals(expected, tagsOf(DeliverySnapshots.withoutTag(tagged(before), TAG)));
  }

  @Test
  @DisplayName("a snapshot with no tags is left as it is")
  void noTagsList() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt("Health", 7);
    assertEquals(nbt.copy(), DeliverySnapshots.withoutTag(nbt, TAG));
    assertFalse(nbt.contains(DeliverySnapshots.TAGS));
  }
}
