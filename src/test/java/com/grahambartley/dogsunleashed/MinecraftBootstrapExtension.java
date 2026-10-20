package com.grahambartley.dogsunleashed;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class MinecraftBootstrapExtension implements BeforeAllCallback {

  private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

  @Override
  public void beforeAll(final ExtensionContext context) {
    if (!INITIALIZED.compareAndSet(false, true)) {
      return;
    }
    SharedConstants.createGameVersion();
    Bootstrap.initialize();
    ModSounds.initialize();
    ModComponents.initialize();
    ModBlocks.initialize();
    ModStatueBlocks.initialize();
    ModBlockEntities.initialize();
    ModPointsOfInterest.initialize();
    ModEntities.initialize();
    ModItems.initialize();
  }
}
