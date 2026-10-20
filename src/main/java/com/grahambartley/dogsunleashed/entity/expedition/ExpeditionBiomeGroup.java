package com.grahambartley.dogsunleashed.entity.expedition;

import java.util.Locale;

public enum ExpeditionBiomeGroup {
  PLAINS,
  FOREST,
  TAIGA,
  SNOWY,
  ARID,
  JUNGLE,
  SWAMP,
  MOUNTAIN,
  COAST,
  MUSHROOM,
  NETHER,
  END;

  public static final String LOOT_TABLE_FOLDER = "gameplay/expedition/";

  public String lootTablePath() {
    return LOOT_TABLE_FOLDER + this.name().toLowerCase(Locale.ROOT);
  }
}
