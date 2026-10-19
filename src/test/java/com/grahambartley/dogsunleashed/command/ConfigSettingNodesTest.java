package com.grahambartley.dogsunleashed.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfigSettingNodesTest {

  @Test
  @DisplayName("a toggle takes one true or false argument")
  void toggleTakesABoolean() {
    final CommandNode<ServerCommandSource> node =
        ConfigSettingNodes.toggle("graves", (config, value) -> config).build();
    assertEquals("graves", node.getName());
    assertInstanceOf(BoolArgumentType.class, onlyArgument(node).getType());
  }

  @Test
  @DisplayName("a whole number setting refuses values outside its bounds")
  void intSettingKeepsItsBounds() {
    final CommandNode<ServerCommandSource> node =
        ConfigSettingNodes.intSetting(
                "autosleeprange", "blocks", 4, 128, (config, value) -> config, Integer::toString)
            .build();
    final ArgumentCommandNode<ServerCommandSource, ?> argument = onlyArgument(node);
    assertEquals("blocks", argument.getName());
    assertEquals(IntegerArgumentType.integer(4, 128), argument.getType());
  }

  @Test
  @DisplayName("a volume setting refuses values outside its bounds")
  void volumeSettingKeepsItsBounds() {
    final CommandNode<ServerCommandSource> node =
        ConfigSettingNodes.volumeSetting("barkvolume", 0.0f, 2.0f, (config, value) -> config)
            .build();
    final ArgumentCommandNode<ServerCommandSource, ?> argument = onlyArgument(node);
    assertEquals("volume", argument.getName());
    assertEquals(FloatArgumentType.floatArg(0.0f, 2.0f), argument.getType());
  }

  private static ArgumentCommandNode<ServerCommandSource, ?> onlyArgument(
      final CommandNode<ServerCommandSource> node) {
    assertEquals(1, node.getChildren().size());
    return (ArgumentCommandNode<ServerCommandSource, ?>) node.getChildren().iterator().next();
  }
}
