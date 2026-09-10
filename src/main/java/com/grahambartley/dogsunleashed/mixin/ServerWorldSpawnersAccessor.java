package com.grahambartley.dogsunleashed.mixin;

import java.util.List;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.spawner.SpecialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerWorld.class)
public interface ServerWorldSpawnersAccessor {

  @Accessor("spawners")
  List<SpecialSpawner> dogsUnleashed$getSpawners();

  @Mutable
  @Accessor("spawners")
  void dogsUnleashed$setSpawners(List<SpecialSpawner> spawners);
}
