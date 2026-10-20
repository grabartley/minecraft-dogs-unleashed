package com.grahambartley.dogsunleashed.mixin;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FoxEntity.class)
public interface FoxEntityAccessor {

  @Invoker("getTrustedUuids")
  List<UUID> dogsUnleashed$getTrustedUuids();
}
