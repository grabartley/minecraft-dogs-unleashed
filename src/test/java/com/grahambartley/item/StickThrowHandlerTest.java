package com.grahambartley.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StickThrowHandlerTest {
  private static final Path STICK_THROW_HANDLER_SOURCE =
      Path.of("src/main/java/com/grahambartley/item/StickThrowHandler.java");

  @Test
  @DisplayName("stick throw handler should register a vanilla-stick use callback")
  void stickThrowHandlerShouldRegisterVanillaStickUseCallback() throws IOException {
    String source = Files.readString(STICK_THROW_HANDLER_SOURCE);

    assertTrue(source.contains("UseItemCallback.EVENT.register(StickThrowHandler::use);"));
    assertTrue(source.contains("itemStack.isOf(Items.STICK)"));
    assertTrue(source.contains("player.isSneaking()"));
  }

  @Test
  @DisplayName(
      "stick throw handler should spawn the stick projectile and consume the vanilla stick")
  void stickThrowHandlerShouldThrowVanillaStick() throws IOException {
    String source = Files.readString(STICK_THROW_HANDLER_SOURCE);

    assertTrue(
        source.contains("StickProjectileEntity stick = new StickProjectileEntity(world, player);"));
    assertTrue(source.contains("world.spawnEntity(stick);"));
    assertTrue(source.contains("itemStack.decrementUnlessCreative(1, player);"));
  }
}
