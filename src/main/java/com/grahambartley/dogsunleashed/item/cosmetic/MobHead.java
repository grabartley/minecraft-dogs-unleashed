package com.grahambartley.dogsunleashed.item.cosmetic;

import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public enum MobHead {
  ZOMBIE("zombie_head"),
  SKELETON("skeleton_skull"),
  WITHER_SKELETON("wither_skeleton_skull"),
  CREEPER("creeper_head"),
  PIGLIN("piglin_head"),
  DRAGON("dragon_head"),
  CARVED_PUMPKIN("carved_pumpkin");

  private final Identifier itemId;

  MobHead(final String itemPath) {
    this.itemId = Identifier.ofVanilla(itemPath);
  }

  public Identifier itemId() {
    return this.itemId;
  }

  public Item item() {
    return Registries.ITEM.get(this.itemId);
  }

  public boolean isSkull() {
    return this != CARVED_PUMPKIN;
  }

  public boolean stretchesOverTheMuzzle() {
    return this != DRAGON;
  }

  public boolean opensJawOnBark() {
    return this == DRAGON;
  }

  public static Optional<MobHead> of(final ItemStack stack) {
    return byItemId(Registries.ITEM.getId(stack.getItem()));
  }

  public static Optional<MobHead> byItemId(final Identifier itemId) {
    for (final MobHead head : values()) {
      if (head.itemId.equals(itemId)) {
        return Optional.of(head);
      }
    }
    return Optional.empty();
  }
}
