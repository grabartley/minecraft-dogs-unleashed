package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.scent.ScentPhase;
import com.grahambartley.dogsunleashed.entity.scent.ScentRange;
import com.grahambartley.dogsunleashed.entity.scent.ScentRule;
import com.grahambartley.dogsunleashed.entity.scent.ScentRules;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class DogScent extends DogComponent {

  private static final float LOOK_SPEED = 30.0F;

  private final UnleashedDogEntity dog;

  private @Nullable Identifier ruleId;
  private @Nullable Item scentItem;
  private int generation;

  DogScent(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public boolean isTracking() {
    return this.ruleId != null;
  }

  public int generation() {
    return this.generation;
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

  ActionResult offer(final PlayerEntity owner, final ItemStack stack) {
    if (this.isTracking() && stack.isOf(this.scentItem)) {
      this.stop();
      this.tell(owner, "message.dogs-unleashed.scent.stop");
      return ActionResult.SUCCESS;
    }
    final Optional<RegistryEntry.Reference<ScentRule>> rule =
        ScentRules.forItem(this.dog.getWorld(), stack);
    if (rule.isEmpty()) {
      this.giveUp();
      return ActionResult.SUCCESS;
    }
    if (this.dog.getCommand() == DogCommand.SIT) {
      this.dog.getCommandController().apply(DogCommand.FOLLOW);
    }
    this.dog.getComponents().get(DogPointedOrders.class).clear();
    this.dog.getNavigation().stop();
    this.start(rule.get().registryKey().getValue(), stack.getItem());
    this.dog.playSound(
        SoundEvents.ENTITY_FOX_SNIFF, 1.0F, this.dog.getVocalization().getBarkPitch());
    this.tell(owner, "message.dogs-unleashed.scent.start");
    return ActionResult.SUCCESS;
  }

  public void stop() {
    this.ruleId = null;
    this.scentItem = null;
    this.setPhase(ScentPhase.NONE);
  }

  public void giveUp() {
    this.stop();
    this.dog.playSound(
        SoundEvents.ENTITY_WOLF_WHINE, 1.0F, this.dog.getVocalization().getBarkPitch());
    final LivingEntity owner = this.dog.getOwner();
    if (owner != null) {
      this.dog.getLookControl().lookAt(owner, LOOK_SPEED, LOOK_SPEED);
    }
    if (owner instanceof PlayerEntity player) {
      this.tell(player, "message.dogs-unleashed.scent.nothing");
    }
  }

  public void announceFound() {
    this.dog.getVocalization().forceBark(this.dog.getVocalization().getBarkPitch());
    if (this.dog.getOwner() instanceof PlayerEntity player) {
      this.tell(player, "message.dogs-unleashed.scent.found");
    }
  }

  private void start(final Identifier rule, final Item item) {
    this.ruleId = rule;
    this.scentItem = item;
    this.generation++;
    this.setPhase(ScentPhase.SNIFFING);
  }

  private void tell(final PlayerEntity player, final String translationKey) {
    player.sendMessage(Text.translatable(translationKey, PetNames.tamedNameOf(this.dog)), true);
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    if (this.ruleId != null && this.scentItem != null) {
      nbt.putString(ModNbtKeys.SCENT_RULE, this.ruleId.toString());
      nbt.putString(ModNbtKeys.SCENT_ITEM, Registries.ITEM.getId(this.scentItem).toString());
    }
  }

  @Override
  void readNbt(final NbtCompound nbt) {
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
