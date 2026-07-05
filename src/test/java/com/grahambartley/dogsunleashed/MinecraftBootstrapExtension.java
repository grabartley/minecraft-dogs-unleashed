package com.grahambartley.dogsunleashed;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that brings up just enough Minecraft static state for tests to reach {@code
 * ModSounds.HUSKY_HOWL}, {@code ModBlocks.DOG_BED}, {@code ModEntities.HUSKY}, {@code Items.BONE},
 * and the vanilla registries.
 *
 * <p>Apply with {@code @ExtendWith(MinecraftBootstrapExtension.class)} on any test class that
 * touches mod content or registry-backed vanilla content. The extension is idempotent and safe to
 * apply to multiple test classes in the same run.
 *
 * <p>The Yarn-mapped Minecraft jars on the unit-test classpath produce verification errors mid- way
 * through {@link Bootstrap#initialize()} (subclass receivers on protected invokevirtuals into
 * {@code MobEntity} from {@code EntityType}'s static initializer) that the full Loom production
 * runtime avoids via deeper bytecode access widening. The {@code Bootstrap.initialize()} call is
 * therefore guarded: it loads everything Loom can load before the verifier rejects further static
 * init. By the time it stops, {@code SharedConstants}, the registry plumbing, and all sound / block
 * / item / entity statics that we touch from tests have already been populated, so the remaining
 * mod-content class loads complete normally.
 *
 * <p>Each {@code initialize()} call is itself a no-op (sounds / blocks / components register via
 * static field init when the class is first touched); the calls exist as load-trigger points so
 * field initialization actually runs before the first test queries the registry.
 */
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
      // Yarn-mapped vanilla bootstrap can throw VerifyError mid-init under the JUnit classloader.
      // See class javadoc.
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
