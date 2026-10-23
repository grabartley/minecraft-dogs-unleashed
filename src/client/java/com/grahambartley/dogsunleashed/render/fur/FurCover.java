package com.grahambartley.dogsunleashed.render.fur;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogSaddlebags;
import com.grahambartley.dogsunleashed.entity.DogSledHarness;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.render.cosmetic.DrawnCosmetic;
import com.grahambartley.dogsunleashed.render.cosmetic.MobHeadArt;
import com.grahambartley.dogsunleashed.render.costume.WornCostume;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public record FurCover(
    boolean armour,
    boolean saddlebags,
    boolean harness,
    boolean costume,
    boolean headWear,
    boolean collarWear,
    boolean pendant) {

  public static final FurCover NONE = new FurCover(false, false, false, false, false, false, false);

  public static FurCover on(final UnleashedDogEntity dog) {
    final Optional<DogCosmetic.Mount> cosmetic = DrawnCosmetic.on(dog).map(DogCosmetic::mount);
    return new FurCover(
        !dog.getArmour().worn().isEmpty(),
        dog.getComponents().get(DogSaddlebags.class).isWorn(),
        dog.getComponents().get(DogSledHarness.class).isHitched(),
        WornCostume.on(dog).isPresent(),
        cosmetic.filter(DogCosmetic.Mount.HEAD::equals).isPresent()
            || DrawnCosmetic.mobHeadOn(dog).isPresent()
            || MobHeadArt.on(dog).isPresent(),
        cosmetic.filter(DogCosmetic.Mount.COLLAR::equals).isPresent(),
        !dog.getEquipmentHolder().getStack(DogEquipmentSlot.PENDANT).isEmpty());
  }

  public Set<Slot> coveredSlots() {
    if (this.costume) {
      final EnumSet<Slot> all = EnumSet.noneOf(Slot.class);
      Arrays.stream(Slot.values()).filter(slot -> !slot.isFur()).forEach(all::add);
      return all;
    }
    final EnumSet<Slot> covered = EnumSet.noneOf(Slot.class);
    if (this.armour || this.saddlebags || this.harness) {
      covered.add(Slot.TORSO);
    }
    if (this.headWear) {
      covered.add(Slot.HEAD);
    }
    if (this.collarWear || this.pendant) {
      covered.add(Slot.NECK);
    }
    return covered;
  }

  public boolean hides(final RigProfile profile, final String bone, final @Nullable String parent) {
    if (parent == null
        || !(profile.bonesIn(Slot.FUR_SHAGGY).contains(bone)
            || profile.bonesIn(Slot.FUR_OVERGROWN).contains(bone))) {
      return false;
    }
    return this.coveredSlots().stream().anyMatch(slot -> profile.bonesIn(slot).contains(parent));
  }
}
