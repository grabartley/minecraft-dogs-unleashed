package com.grahambartley;

import com.grahambartley.screen.DogsUnleashedConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class DogsUnleashedModMenu implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return DogsUnleashedConfigScreen::new;
  }
}
