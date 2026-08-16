package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {

  public static final ScreenHandlerType<DogEquipmentScreenHandler> DOG_EQUIPMENT =
      Registry.register(
          Registries.SCREEN_HANDLER,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_equipment"),
          new ExtendedScreenHandlerType<>(DogEquipmentScreenHandler::new, PacketCodecs.VAR_INT));

  public static void initialize() {}
}
