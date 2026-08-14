package com.grahambartley.dogsunleashed.mixin;

import java.util.List;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.spawner.SpecialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Fabric API exposes no hook for the {@code ServerWorld} special-spawner list (a private final list
 * set in the constructor, with only {@code tickSpawners} public), so this accessor pair lets {@code
 * ModSpawns} append the {@code DogSpawner} on world load. Methods carry the mod prefix so they
 * cannot collide with another mod's accessor of the same field.
 */
@Mixin(ServerWorld.class)
public interface ServerWorldSpawnersAccessor {

  @Accessor("spawners")
  List<SpecialSpawner> dogsUnleashed$getSpawners();

  @Mutable
  @Accessor("spawners")
  void dogsUnleashed$setSpawners(List<SpecialSpawner> spawners);
}
