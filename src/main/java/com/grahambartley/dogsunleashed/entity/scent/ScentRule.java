package com.grahambartley.dogsunleashed.entity.scent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.gen.structure.Structure;

public record ScentRule(
    RegistryEntryList<Item> items,
    Optional<RegistryEntryList<EntityType<?>>> entities,
    Optional<RegistryEntryList<Block>> blocks,
    boolean suspiciousBlocks,
    Optional<TagKey<Structure>> structures) {

  public static final Codec<ScentRule> CODEC =
      RecordCodecBuilder.<ScentRule>create(
              instance ->
                  instance
                      .group(
                          RegistryCodecs.entryList(RegistryKeys.ITEM)
                              .fieldOf("items")
                              .forGetter(ScentRule::items),
                          RegistryCodecs.entryList(RegistryKeys.ENTITY_TYPE)
                              .optionalFieldOf("entities")
                              .forGetter(ScentRule::entities),
                          RegistryCodecs.entryList(RegistryKeys.BLOCK)
                              .optionalFieldOf("blocks")
                              .forGetter(ScentRule::blocks),
                          Codec.BOOL
                              .optionalFieldOf("suspicious_blocks", false)
                              .forGetter(ScentRule::suspiciousBlocks),
                          TagKey.codec(RegistryKeys.STRUCTURE)
                              .optionalFieldOf("structures")
                              .forGetter(ScentRule::structures))
                      .apply(instance, ScentRule::new))
          .validate(ScentRule::validated);

  public ScentTargetKind kind() {
    final ScentTargetKind kind =
        ScentTargetKind.of(
            entities.isPresent(), blocks.isPresent(), suspiciousBlocks, structures.isPresent());
    if (kind == null) {
      throw new IllegalStateException("A scent rule needs exactly one target");
    }
    return kind;
  }

  public boolean matchesItem(final ItemStack stack) {
    return items.contains(stack.getRegistryEntry());
  }

  public boolean matchesEntity(final Entity entity) {
    return entities.map(list -> list.contains(entity.getType().getRegistryEntry())).orElse(false);
  }

  public boolean matchesBlock(final BlockState state) {
    return blocks.map(list -> list.contains(state.getRegistryEntry())).orElse(false);
  }

  private static DataResult<ScentRule> validated(final ScentRule rule) {
    return ScentTargetKind.of(
                rule.entities.isPresent(),
                rule.blocks.isPresent(),
                rule.suspiciousBlocks,
                rule.structures.isPresent())
            == null
        ? DataResult.error(
            () ->
                "A scent rule needs exactly one of entities, blocks, suspicious_blocks or structures")
        : DataResult.success(rule);
  }
}
