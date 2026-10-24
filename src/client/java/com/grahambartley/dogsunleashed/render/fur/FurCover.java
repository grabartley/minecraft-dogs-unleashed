package com.grahambartley.dogsunleashed.render.fur;

import com.grahambartley.dogsunleashed.entity.DogSaddlebags;
import com.grahambartley.dogsunleashed.entity.DogSledHarness;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.render.cosmetic.DrawnCosmetic;
import com.grahambartley.dogsunleashed.render.costume.WornCostume;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public record FurCover(Set<Slot> covered) {

  public static final FurCover NONE = new FurCover(EnumSet.noneOf(Slot.class));

  public static FurCover on(final UnleashedDogEntity dog) {
    return new FurCover(
        slotsCovered(
            !dog.getArmour().worn().isEmpty(),
            dog.getComponents().get(DogSaddlebags.class).isWorn(),
            dog.getComponents().get(DogSledHarness.class).isHitched(),
            WornCostume.on(dog).isPresent(),
            DrawnCosmetic.on(dog).map(DogCosmetic::mount),
            DrawnCosmetic.mobHeadOn(dog).isPresent(),
            dog.getPendants().worn() != null,
            dog.isTamed() && !dog.isInvisible()));
  }

  public static Set<Slot> slotsCovered(
      final boolean armour,
      final boolean saddlebags,
      final boolean hitched,
      final boolean costume,
      final Optional<DogCosmetic.Mount> cosmetic,
      final boolean mobHead,
      final boolean pendant,
      final boolean collar) {
    final EnumSet<Slot> covered = EnumSet.noneOf(Slot.class);
    if (costume) {
      Arrays.stream(Slot.values()).filter(slot -> !slot.isFur()).forEach(covered::add);
      return covered;
    }
    if (armour || saddlebags || hitched) {
      covered.add(Slot.TORSO);
    }
    if (mobHead || cosmetic.filter(DogCosmetic.Mount.HEAD::equals).isPresent()) {
      covered.add(Slot.HEAD);
    }
    if (pendant || collar || cosmetic.filter(DogCosmetic.Mount.COLLAR::equals).isPresent()) {
      covered.add(Slot.NECK);
    }
    return covered;
  }

  public boolean hides(final RigProfile profile, final String bone, final @Nullable String parent) {
    if (parent == null || this.covered.isEmpty() || !FurLook.isFur(profile, bone)) {
      return false;
    }
    return this.covered.stream().anyMatch(slot -> profile.bonesIn(slot).contains(parent));
  }
}
