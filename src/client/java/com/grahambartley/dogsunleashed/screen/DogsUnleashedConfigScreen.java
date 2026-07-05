package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.EditServerConfigC2SPayload;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class DogsUnleashedConfigScreen extends Screen {
  private static final int CONTENT_WIDTH = 220;
  private static final int ROW_HEIGHT = 24;
  private static final int SECTION_GAP = 16;
  private static final int WIDGET_WIDTH = 200;
  private static final int WIDGET_HEIGHT = 20;
  private static final int BUTTON_ROW_HEIGHT = 20;
  private static final int DONE_CANCEL_WIDTH = 97;
  private static final int DONE_CANCEL_GAP = 6;
  private static final int TITLE_Y = 16;
  private static final int BANNER_Y = 30;

  private final Screen parent;

  private boolean enableNaturalSpawning;
  private int spawnRateMultiplierPercent;
  private final Map<String, Integer> breedSpawnRateMultipliersPercent;
  private boolean gravesEnabled;
  private boolean autoSleepEnabled;
  private int autoSleepRangeBlocks;
  private float barkVolume;
  private float howlVolume;

  public DogsUnleashedConfigScreen(@Nullable Screen parent) {
    super(Text.translatable("screen.dogs-unleashed.settings.title"));
    this.parent = parent;
    final DogsUnleashedConfig current = DogsUnleashed.SERVER_CONFIG;
    this.enableNaturalSpawning = current.enableNaturalSpawning();
    this.spawnRateMultiplierPercent = current.spawnRateMultiplierPercent();
    this.breedSpawnRateMultipliersPercent =
        new LinkedHashMap<>(current.breedSpawnRateMultipliersPercent());
    this.gravesEnabled = current.gravesEnabled();
    this.autoSleepEnabled = current.autoSleepEnabled();
    this.autoSleepRangeBlocks = current.autoSleepRangeBlocks();
    this.barkVolume = current.barkVolume();
    this.howlVolume = current.howlVolume();
  }

  @Override
  protected void init() {
    final boolean canEdit = clientHasOperatorPermission();

    final int left = (this.width - CONTENT_WIDTH) / 2;
    int y = BANNER_Y + (canEdit ? 24 : 36);

    y = addSectionHeader("screen.dogs-unleashed.settings.gameplay", y);
    addBooleanRow(
        left,
        y,
        canEdit,
        "screen.dogs-unleashed.settings.spawn",
        "screen.dogs-unleashed.settings.spawn.tooltip",
        this.enableNaturalSpawning,
        value -> this.enableNaturalSpawning = value);
    y += ROW_HEIGHT;
    addBooleanRow(
        left,
        y,
        canEdit,
        "screen.dogs-unleashed.settings.graves",
        "screen.dogs-unleashed.settings.graves.tooltip",
        this.gravesEnabled,
        value -> this.gravesEnabled = value);
    y += ROW_HEIGHT + SECTION_GAP;

    y = addSectionHeader("screen.dogs-unleashed.settings.spawning", y);
    addPercentSliderRow(
        left,
        y,
        canEdit,
        Text.translatable("screen.dogs-unleashed.settings.spawnrate"),
        Text.translatable("screen.dogs-unleashed.settings.spawnrate.tooltip"),
        this.spawnRateMultiplierPercent,
        value -> this.spawnRateMultiplierPercent = value);
    y += ROW_HEIGHT;
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      addPercentSliderRow(
          left,
          y,
          canEdit,
          Text.translatable(
              "screen.dogs-unleashed.settings.spawnrate.breed",
              Text.translatable(breed.translationKey())),
          Text.translatable("screen.dogs-unleashed.settings.spawnrate.breed.tooltip"),
          this.breedSpawnRateMultipliersPercent.get(breed.serializedId()),
          value -> this.breedSpawnRateMultipliersPercent.put(breed.serializedId(), value));
      y += ROW_HEIGHT;
    }
    y += SECTION_GAP;

    y = addSectionHeader("screen.dogs-unleashed.settings.sleep", y);
    addBooleanRow(
        left,
        y,
        canEdit,
        "screen.dogs-unleashed.settings.autosleep",
        "screen.dogs-unleashed.settings.autosleep.tooltip",
        this.autoSleepEnabled,
        value -> this.autoSleepEnabled = value);
    y += ROW_HEIGHT;
    addIntSliderRow(
        left,
        y,
        canEdit,
        "screen.dogs-unleashed.settings.autosleeprange",
        "screen.dogs-unleashed.settings.autosleeprange.tooltip",
        DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN,
        DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX,
        this.autoSleepRangeBlocks,
        value -> this.autoSleepRangeBlocks = value);
    y += ROW_HEIGHT + SECTION_GAP;

    y = addSectionHeader("screen.dogs-unleashed.settings.sounds", y);
    addFloatSliderRow(
        left,
        y,
        canEdit,
        "screen.dogs-unleashed.settings.barkvolume",
        "screen.dogs-unleashed.settings.barkvolume.tooltip",
        DogsUnleashedConfig.VOLUME_MIN,
        DogsUnleashedConfig.VOLUME_MAX,
        this.barkVolume,
        value -> this.barkVolume = value);
    y += ROW_HEIGHT;
    addFloatSliderRow(
        left,
        y,
        canEdit,
        "screen.dogs-unleashed.settings.howlvolume",
        "screen.dogs-unleashed.settings.howlvolume.tooltip",
        DogsUnleashedConfig.VOLUME_MIN,
        DogsUnleashedConfig.VOLUME_MAX,
        this.howlVolume,
        value -> this.howlVolume = value);
    y += ROW_HEIGHT + SECTION_GAP;

    final int doneLeft = (this.width - (DONE_CANCEL_WIDTH * 2 + DONE_CANCEL_GAP)) / 2;
    final int cancelLeft = doneLeft + DONE_CANCEL_WIDTH + DONE_CANCEL_GAP;
    final ButtonWidget doneButton =
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.settings.done"), button -> saveAndClose())
            .dimensions(doneLeft, y, DONE_CANCEL_WIDTH, BUTTON_ROW_HEIGHT)
            .build();
    doneButton.active = canEdit;
    addDrawableChild(doneButton);

    addDrawableChild(
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.settings.cancel"), button -> close())
            .dimensions(cancelLeft, y, DONE_CANCEL_WIDTH, BUTTON_ROW_HEIGHT)
            .build());
  }

  private int addSectionHeader(final String key, final int y) {
    final SectionHeaderWidget header =
        new SectionHeaderWidget(
            (this.width - CONTENT_WIDTH) / 2, y, CONTENT_WIDTH, 12, Text.translatable(key), this);
    addDrawableChild(header);
    return y + 14;
  }

  private void addBooleanRow(
      final int left,
      final int y,
      final boolean canEdit,
      final String labelKey,
      final String tooltipKey,
      final boolean initial,
      final Consumer<Boolean> sink) {
    final CyclingButtonWidget<Boolean> widget =
        CyclingButtonWidget.onOffBuilder(initial)
            .build(
                left + (CONTENT_WIDTH - WIDGET_WIDTH) / 2,
                y,
                WIDGET_WIDTH,
                WIDGET_HEIGHT,
                Text.translatable(labelKey),
                (button, value) -> sink.accept(value));
    widget.setTooltip(Tooltip.of(Text.translatable(tooltipKey)));
    widget.active = canEdit;
    addDrawableChild(widget);
  }

  private void addIntSliderRow(
      final int left,
      final int y,
      final boolean canEdit,
      final String labelKey,
      final String tooltipKey,
      final int min,
      final int max,
      final int initial,
      final Consumer<Integer> sink) {
    final IntSliderWidget widget =
        new IntSliderWidget(
            left + (CONTENT_WIDTH - WIDGET_WIDTH) / 2,
            y,
            WIDGET_WIDTH,
            WIDGET_HEIGHT,
            Text.translatable(labelKey),
            "",
            min,
            max,
            initial,
            sink);
    widget.setTooltip(Tooltip.of(Text.translatable(tooltipKey)));
    widget.active = canEdit;
    addDrawableChild(widget);
  }

  private void addPercentSliderRow(
      final int left,
      final int y,
      final boolean canEdit,
      final Text label,
      final Text tooltip,
      final int initial,
      final Consumer<Integer> sink) {
    final IntSliderWidget widget =
        new IntSliderWidget(
            left + (CONTENT_WIDTH - WIDGET_WIDTH) / 2,
            y,
            WIDGET_WIDTH,
            WIDGET_HEIGHT,
            label,
            "%",
            DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MIN,
            DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MAX,
            initial,
            sink);
    widget.setTooltip(Tooltip.of(tooltip));
    widget.active = canEdit;
    addDrawableChild(widget);
  }

  private void addFloatSliderRow(
      final int left,
      final int y,
      final boolean canEdit,
      final String labelKey,
      final String tooltipKey,
      final float min,
      final float max,
      final float initial,
      final Consumer<Float> sink) {
    final FloatSliderWidget widget =
        new FloatSliderWidget(
            left + (CONTENT_WIDTH - WIDGET_WIDTH) / 2,
            y,
            WIDGET_WIDTH,
            WIDGET_HEIGHT,
            labelKey,
            min,
            max,
            initial,
            sink);
    widget.setTooltip(Tooltip.of(Text.translatable(tooltipKey)));
    widget.active = canEdit;
    addDrawableChild(widget);
  }

  private void saveAndClose() {
    if (!clientHasOperatorPermission()) {
      close();
      return;
    }
    final DogsUnleashedConfig updated =
        new DogsUnleashedConfig(
            this.enableNaturalSpawning,
            this.spawnRateMultiplierPercent,
            this.breedSpawnRateMultipliersPercent,
            this.gravesEnabled,
            this.autoSleepEnabled,
            this.autoSleepRangeBlocks,
            this.barkVolume,
            this.howlVolume);
    ClientPlayNetworking.send(new EditServerConfigC2SPayload(updated));
    close();
  }

  @Override
  public void close() {
    MinecraftClient.getInstance().setScreen(this.parent);
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);
    context.drawCenteredTextWithShadow(
        this.textRenderer, this.title, this.width / 2, TITLE_Y, 0xFFFFFF);
    if (!clientHasOperatorPermission()) {
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.settings.readonly_banner"),
          this.width / 2,
          BANNER_Y,
          0xFFAA00);
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.settings.readonly_hint"),
          this.width / 2,
          BANNER_Y + 12,
          0xBBBBBB);
    }
  }

  private static boolean clientHasOperatorPermission() {
    final ClientPlayerEntity player = MinecraftClient.getInstance().player;
    return player != null && player.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL);
  }

  private static final class IntSliderWidget extends SliderWidget {
    private final Text label;
    private final String valueSuffix;
    private final int min;
    private final int max;
    private final Consumer<Integer> sink;
    private int currentValue;

    IntSliderWidget(
        final int x,
        final int y,
        final int width,
        final int height,
        final Text label,
        final String valueSuffix,
        final int min,
        final int max,
        final int initial,
        final Consumer<Integer> sink) {
      super(x, y, width, height, Text.empty(), normalize(initial, min, max));
      this.label = label;
      this.valueSuffix = valueSuffix;
      this.min = min;
      this.max = max;
      this.currentValue = clamp(initial, min, max);
      this.sink = sink;
      updateMessage();
    }

    private static double normalize(final int value, final int min, final int max) {
      return (double) (clamp(value, min, max) - min) / (double) (max - min);
    }

    private static int clamp(final int value, final int min, final int max) {
      return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void updateMessage() {
      setMessage(label.copy().append(Text.literal(": " + currentValue + valueSuffix)));
    }

    @Override
    protected void applyValue() {
      currentValue = (int) Math.round(min + (max - min) * this.value);
      sink.accept(currentValue);
    }
  }

  private static final class FloatSliderWidget extends SliderWidget {
    private final String labelKey;
    private final float min;
    private final float max;
    private final Consumer<Float> sink;
    private float currentValue;

    FloatSliderWidget(
        final int x,
        final int y,
        final int width,
        final int height,
        final String labelKey,
        final float min,
        final float max,
        final float initial,
        final Consumer<Float> sink) {
      super(x, y, width, height, Text.empty(), normalize(initial, min, max));
      this.labelKey = labelKey;
      this.min = min;
      this.max = max;
      this.currentValue = clamp(initial, min, max);
      this.sink = sink;
      updateMessage();
    }

    private static double normalize(final float value, final float min, final float max) {
      return (clamp(value, min, max) - min) / (double) (max - min);
    }

    private static float clamp(final float value, final float min, final float max) {
      return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void updateMessage() {
      setMessage(
          Text.translatable(labelKey).append(Text.literal(String.format(": %.2f", currentValue))));
    }

    @Override
    protected void applyValue() {
      currentValue = (float) (min + (max - min) * this.value);
      sink.accept(currentValue);
    }
  }

  private static final class SectionHeaderWidget extends ClickableWidget {
    private final DogsUnleashedConfigScreen screen;

    SectionHeaderWidget(
        final int x,
        final int y,
        final int width,
        final int height,
        final Text message,
        final DogsUnleashedConfigScreen screen) {
      super(x, y, width, height, message);
      this.screen = screen;
      this.active = false;
    }

    @Override
    protected void renderWidget(
        final DrawContext context, final int mouseX, final int mouseY, final float delta) {
      context.drawCenteredTextWithShadow(
          screen.textRenderer,
          this.getMessage(),
          this.getX() + this.getWidth() / 2,
          this.getY() + 2,
          0xFFD27F);
    }

    @Override
    protected void appendClickableNarrations(
        final net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
      // Decorative header — narration is not interactive.
    }
  }
}
