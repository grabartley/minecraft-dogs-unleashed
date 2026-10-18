package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.config.DireWolfSettings;
import com.grahambartley.dogsunleashed.config.DireWolfStaminaSettings;

final class DireWolfConfigSection {
  private static final String KEY = "screen.dogs-unleashed.settings.direwolf.";

  private DireWolfSettings settings;
  private ConfigIntSlider smallestPackSlider;
  private ConfigIntSlider largestPackSlider;

  DireWolfConfigSection(final DireWolfSettings settings) {
    this.settings = settings;
  }

  void addTo(final DogsUnleashedConfigList list, final ConfigRows rows) {
    list.addRow(rows.header("screen.dogs-unleashed.settings.direwolf"));
    list.addRow(
        rows.percentSlider(
            KEY + "growthrate",
            DireWolfSettings.GROWTH_RATE_MIN,
            DireWolfSettings.GROWTH_RATE_MAX,
            this.settings.growthRateMultiplierPercent(),
            value -> this.settings = this.settings.withGrowthRateMultiplierPercent(value)));
    list.addRow(
        rows.intSlider(
            KEY + "battles",
            KEY + "battles.tooltip",
            DireWolfSettings.BATTLES_MIN,
            DireWolfSettings.BATTLES_MAX,
            this.settings.battlesToEvolve(),
            value -> this.settings = this.settings.withBattlesToEvolve(value)));
    this.smallestPackSlider =
        rows.intSlider(
            KEY + "smallestpack",
            KEY + "smallestpack.tooltip",
            DireWolfSettings.PACK_MIN,
            DireWolfSettings.PACK_MAX,
            this.settings.smallestPack(),
            value -> {
              this.settings = this.settings.withSmallestPack(value);
              this.largestPackSlider.show(this.settings.largestPack());
            });
    this.largestPackSlider =
        rows.intSlider(
            KEY + "largestpack",
            KEY + "largestpack.tooltip",
            DireWolfSettings.PACK_MIN,
            DireWolfSettings.PACK_MAX,
            this.settings.largestPack(),
            value -> {
              this.settings = this.settings.withLargestPack(value);
              this.smallestPackSlider.show(this.settings.smallestPack());
            });
    list.addRow(this.smallestPackSlider);
    list.addRow(this.largestPackSlider);
    list.addRow(
        rows.toggle(
            KEY + "packhunting",
            KEY + "packhunting.tooltip",
            this.settings.packs().hunt(),
            value ->
                this.settings = this.settings.withPacks(this.settings.packs().withHunt(value))));
    list.addRow(
        rows.toggle(
            KEY + "packdefence",
            KEY + "packdefence.tooltip",
            this.settings.packs().defend(),
            value ->
                this.settings = this.settings.withPacks(this.settings.packs().withDefend(value))));
    list.addRow(
        rows.toggle(
            KEY + "staminadrain",
            KEY + "staminadrain.tooltip",
            this.settings.stamina().drains(),
            value ->
                this.settings =
                    this.settings.withStamina(this.settings.stamina().withDrains(value))));
    list.addRow(
        rows.percentSlider(
            KEY + "staminacapacity",
            DireWolfStaminaSettings.PERCENT_MIN,
            DireWolfStaminaSettings.PERCENT_MAX,
            this.settings.stamina().capacityPercent(),
            value ->
                this.settings =
                    this.settings.withStamina(this.settings.stamina().withCapacityPercent(value))));
    list.addRow(
        rows.percentSlider(
            KEY + "staminarecovery",
            DireWolfStaminaSettings.PERCENT_MIN,
            DireWolfStaminaSettings.PERCENT_MAX,
            this.settings.stamina().recoveryPercent(),
            value ->
                this.settings =
                    this.settings.withStamina(this.settings.stamina().withRecoveryPercent(value))));
  }

  DireWolfSettings settings() {
    return this.settings;
  }
}
