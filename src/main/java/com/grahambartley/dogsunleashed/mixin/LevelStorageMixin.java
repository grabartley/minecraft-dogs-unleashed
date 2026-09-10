package com.grahambartley.dogsunleashed.mixin;

import com.grahambartley.dogsunleashed.server.ServerConfigService;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelStorage.class)
public abstract class LevelStorageMixin {

  @Inject(method = "createSession", at = @At("RETURN"))
  private void dogsUnleashed$loadServerConfig(
      String directoryName, CallbackInfoReturnable<LevelStorage.Session> cir) {
    ServerConfigService.loadFromSession(cir.getReturnValue());
  }

  @Inject(method = "createSessionWithoutSymlinkCheck", at = @At("RETURN"))
  private void dogsUnleashed$loadServerConfigWithoutSymlinkCheck(
      String directoryName, CallbackInfoReturnable<LevelStorage.Session> cir) {
    ServerConfigService.loadFromSession(cir.getReturnValue());
  }
}
