package com.grahambartley.dogsunleashed.entity.fetch;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public record FetchItemType(
    Identifier id,
    Item item,
    EntityType<?> projectileType,
    Block landedBlock,
    BlockEntityType<? extends BlockEntity> landedBlockEntityType) {}
