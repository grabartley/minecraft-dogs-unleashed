package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.scent.ScentPhase;
import com.grahambartley.dogsunleashed.entity.scent.ScentRange;
import com.grahambartley.dogsunleashed.entity.scent.ScentRule;
import com.grahambartley.dogsunleashed.entity.scent.ScentRules;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class DogScent extends DogComponent {

  private final UnleashedDogEntity dog;
  private final DogScentCues cues;

  private @Nullable Identifier ruleId;
  private @Nullable Item scentItem;
  private int generation;
  private boolean settled;

  DogScent(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.cues = new DogScentCues(dog);
  }

  public boolean isTracking() {
    return this.ruleId != null;
  }

  public boolean isTrackingItem(final ItemStack stack) {
    return this.isTracking() && stack.isOf(this.scentItem);
  }

  public int generation() {
    return this.generation;
  }

  public DogScentCues cues() {
    return this.cues;
  }

  public @Nullable ScentRule rule() {
    if (this.ruleId == null) {
      return null;
    }
    return ScentRules.byId(this.dog.getWorld(), this.ruleId).map(RegistryEntry::value).orElse(null);
  }

  public int range() {
    final DogGenome genome = this.dog.getGenome();
    return ScentRange.of(this.dog.getBreed(), genome == null ? List.of() : genome.composition());
  }

  public ScentPhase phase() {
    return ScentPhase.fromId(this.dog.getDataTracker().get(UnleashedDogEntity.SCENT_PHASE));
  }

  public void setPhase(final ScentPhase phase) {
    this.dog.getDataTracker().set(UnleashedDogEntity.SCENT_PHASE, phase.id());
  }

  void start(final Identifier rule, final Item item) {
    this.ruleId = rule;
    this.scentItem = item;
    this.settled = false;
    this.generation++;
  }

  public void settle() {
    this.settled = true;
  }

  public void stop() {
    this.ruleId = null;
    this.scentItem = null;
    this.setPhase(ScentPhase.NONE);
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    if (this.ruleId != null && this.scentItem != null && !this.settled) {
      nbt.putString(ModNbtKeys.SCENT_RULE, this.ruleId.toString());
      nbt.putString(ModNbtKeys.SCENT_ITEM, Registries.ITEM.getId(this.scentItem).toString());
    }
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    this.stop();
    final Identifier rule = savedId(nbt, ModNbtKeys.SCENT_RULE);
    final Identifier item = savedId(nbt, ModNbtKeys.SCENT_ITEM);
    if (rule == null || item == null || !Registries.ITEM.containsId(item)) {
      return;
    }
    this.start(rule, Registries.ITEM.get(item));
  }

  static @Nullable Identifier savedId(final NbtCompound nbt, final String key) {
    return nbt.contains(key, NbtElement.STRING_TYPE)
        ? Identifier.tryParse(nbt.getString(key))
        : null;
  }
}
