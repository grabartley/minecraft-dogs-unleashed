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
    try {
      Bootstrap.initialize();
    } catch (final Throwable ignored) {
    }
    try {
      ModSounds.initialize();
    } catch (final Throwable ignored) {
    }
    try {
      ModComponents.initialize();
    } catch (final Throwable ignored) {
    }
    try {
      ModBlocks.initialize();
    } catch (final Throwable ignored) {
    }
    try {
      ModBlockEntities.initialize();
    } catch (final Throwable ignored) {
    }
    try {
      ModEntities.initialize();
    } catch (final Throwable ignored) {
    }
    try {
      ModItems.initialize();
    } catch (final Throwable ignored) {
    }
  }
}
