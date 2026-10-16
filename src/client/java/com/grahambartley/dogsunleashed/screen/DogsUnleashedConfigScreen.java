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
  private static final int WIDGET_WIDTH = 200;
  private static final int WIDGET_HEIGHT = 20;
  private static final int DONE_CANCEL_WIDTH = 97;
  private static final int DONE_CANCEL_GAP = 6;
  private static final int TITLE_Y = 16;
  private static final int BANNER_Y = 30;
  private static final int BANNER_COLOR = 0xFFAA00;
  private static final int HINT_COLOR = 0xBBBBBB;

  private final Screen parent;

  private boolean enableNaturalSpawning;
  private int spawnRateMultiplierPercent;
  private final Map<String, Integer> breedSpawnRateMultipliersPercent;
  private boolean capIndependentSpawningEnabled;
  private boolean gravesEnabled;
  private boolean dropLeashOnPlayMode;
  private boolean autoSleepEnabled;
  private int autoSleepRangeBlocks;
  private float barkVolume;
  private float howlVolume;
  private boolean showDogNames;

  public DogsUnleashedConfigScreen(@Nullable Screen parent) {
    super(Text.translatable("screen.dogs-unleashed.settings.title"));
    this.parent = parent;
    final DogsUnleashedConfig current =
        resolveAccess() == EditAccess.NO_WORLD
            ? DogsUnleashedConfig.defaults()
            : DogsUnleashed.SERVER_CONFIG;
    this.enableNaturalSpawning = current.enableNaturalSpawning();
    this.spawnRateMultiplierPercent = current.spawnRateMultiplierPercent();
    this.breedSpawnRateMultipliersPercent =
        new LinkedHashMap<>(current.breedSpawnRateMultipliersPercent());
    this.capIndependentSpawningEnabled = current.capIndependentSpawningEnabled();
    this.gravesEnabled = current.gravesEnabled();
    this.dropLeashOnPlayMode = current.dropLeashOnPlayMode();
    this.autoSleepEnabled = current.autoSleepEnabled();
    this.autoSleepRangeBlocks = current.autoSleepRangeBlocks();
    this.barkVolume = current.barkVolume();
    this.howlVolume = current.howlVolume();
    this.showDogNames = current.showDogNames();
  }

  @Override
  protected void init() {
    final boolean canEdit = resolveAccess() == EditAccess.EDITABLE;
    final ConfigScreenLayout layout = ConfigScreenLayout.of(this.height, !canEdit);
    final DogsUnleashedConfigList list =
        new DogsUnleashedConfigList(this.client, this.width, layout, ROW_HEIGHT, CONTENT_WIDTH);

    list.addRow(sectionHeader("screen.dogs-unleashed.settings.gameplay"));
    list.addRow(
        booleanRow(
            canEdit,
            "screen.dogs-unleashed.settings.spawn",
            "screen.dogs-unleashed.settings.spawn.tooltip",
            this.enableNaturalSpawning,
            value -> this.enableNaturalSpawning = value));
    list.addRow(
        booleanRow(
            canEdit,
            "screen.dogs-unleashed.settings.graves",
            "screen.dogs-unleashed.settings.graves.tooltip",
            this.gravesEnabled,
            value -> this.gravesEnabled = value));
    list.addRow(
        booleanRow(
            canEdit,
            "screen.dogs-unleashed.settings.dropleashonplay",
            "screen.dogs-unleashed.settings.dropleashonplay.tooltip",
            this.dropLeashOnPlayMode,
            value -> this.dropLeashOnPlayMode = value));
    list.addRow(
        booleanRow(
            canEdit,
            "screen.dogs-unleashed.settings.shownames",
            "screen.dogs-unleashed.settings.shownames.tooltip",
            this.showDogNames,
            value -> this.showDogNames = value));

    list.addRow(sectionHeader("screen.dogs-unleashed.settings.spawning"));
    list.addRow(
        percentSliderRow(
            canEdit,
            Text.translatable("screen.dogs-unleashed.settings.spawnrate"),
            Text.translatable("screen.dogs-unleashed.settings.spawnrate.tooltip"),
            this.spawnRateMultiplierPercent,
            value -> this.spawnRateMultiplierPercent = value));
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      if (!breed.isNaturallySpawning()) {
        continue;
      }
      list.addRow(
          percentSliderRow(
              canEdit,
              Text.translatable(
                  "screen.dogs-unleashed.settings.spawnrate.breed",
                  Text.translatable(breed.translationKey())),
              Text.translatable("screen.dogs-unleashed.settings.spawnrate.breed.tooltip"),
              this.breedSpawnRateMultipliersPercent.get(breed.serializedId()),
              value -> this.breedSpawnRateMultipliersPercent.put(breed.serializedId(), value)));
    }
    list.addRow(
        booleanRow(
            canEdit,
            "screen.dogs-unleashed.settings.capindependentspawning",
            "screen.dogs-unleashed.settings.capindependentspawning.tooltip",
            this.capIndependentSpawningEnabled,
            value -> this.capIndependentSpawningEnabled = value));

    list.addRow(sectionHeader("screen.dogs-unleashed.settings.sleep"));
    list.addRow(
        booleanRow(
            canEdit,
            "screen.dogs-unleashed.settings.autosleep",
            "screen.dogs-unleashed.settings.autosleep.tooltip",
            this.autoSleepEnabled,
            value -> this.autoSleepEnabled = value));
    list.addRow(
        intSliderRow(
            canEdit,
            "screen.dogs-unleashed.settings.autosleeprange",
            "screen.dogs-unleashed.settings.autosleeprange.tooltip",
            DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN,
            DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX,
            this.autoSleepRangeBlocks,
            value -> this.autoSleepRangeBlocks = value));

    list.addRow(sectionHeader("screen.dogs-unleashed.settings.sounds"));
    list.addRow(
        floatSliderRow(
            canEdit,
            "screen.dogs-unleashed.settings.barkvolume",
            "screen.dogs-unleashed.settings.barkvolume.tooltip",
            DogsUnleashedConfig.VOLUME_MIN,
            DogsUnleashedConfig.VOLUME_MAX,
            this.barkVolume,
            value -> this.barkVolume = value));
    list.addRow(
        floatSliderRow(
            canEdit,
            "screen.dogs-unleashed.settings.howlvolume",
            "screen.dogs-unleashed.settings.howlvolume.tooltip",
            DogsUnleashedConfig.VOLUME_MIN,
            DogsUnleashedConfig.VOLUME_MAX,
            this.howlVolume,
            value -> this.howlVolume = value));
    addDrawableChild(list);

    final int doneLeft = (this.width - (DONE_CANCEL_WIDTH * 2 + DONE_CANCEL_GAP)) / 2;
    final int cancelLeft = doneLeft + DONE_CANCEL_WIDTH + DONE_CANCEL_GAP;
    final ButtonWidget doneButton =
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.settings.done"), button -> saveAndClose())
            .dimensions(
                doneLeft, layout.buttonsY(), DONE_CANCEL_WIDTH, ConfigScreenLayout.BUTTON_HEIGHT)
            .build();
    doneButton.active = canEdit;
    addDrawableChild(doneButton);

    addDrawableChild(
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.settings.cancel"), button -> close())
            .dimensions(
                cancelLeft, layout.buttonsY(), DONE_CANCEL_WIDTH, ConfigScreenLayout.BUTTON_HEIGHT)
            .build());
  }

  private ClickableWidget sectionHeader(final String key) {
    return new SectionHeaderWidget(0, 0, CONTENT_WIDTH, 12, Text.translatable(key), this);
  }

  private static ClickableWidget booleanRow(
      final boolean canEdit,
      final String labelKey,
      final String tooltipKey,
      final boolean initial,
      final Consumer<Boolean> sink) {
    final CyclingButtonWidget<Boolean> widget =
        CyclingButtonWidget.onOffBuilder(initial)
            .build(
                0,
                0,
                WIDGET_WIDTH,
                WIDGET_HEIGHT,
                Text.translatable(labelKey),
                (button, value) -> sink.accept(value));
    return withTooltip(widget, Text.translatable(tooltipKey), canEdit);
  }

  private static ClickableWidget intSliderRow(
      final boolean canEdit,
      final String labelKey,
      final String tooltipKey,
      final int min,
      final int max,
      final int initial,
      final Consumer<Integer> sink) {
    final IntSliderWidget widget =
        new IntSliderWidget(
            0,
            0,
            WIDGET_WIDTH,
            WIDGET_HEIGHT,
            Text.translatable(labelKey),
            "",
            min,
            max,
            initial,
            sink);
    return withTooltip(widget, Text.translatable(tooltipKey), canEdit);
  }

  private static ClickableWidget percentSliderRow(
      final boolean canEdit,
      final Text label,
      final Text tooltip,
      final int initial,
      final Consumer<Integer> sink) {
    final IntSliderWidget widget =
        new IntSliderWidget(
            0,
            0,
            WIDGET_WIDTH,
            WIDGET_HEIGHT,
            label,
            "%",
            DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MIN,
            DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MAX,
            initial,
            sink);
    return withTooltip(widget, tooltip, canEdit);
  }

  private static ClickableWidget floatSliderRow(
      final boolean canEdit,
      final String labelKey,
      final String tooltipKey,
      final float min,
      final float max,
      final float initial,
      final Consumer<Float> sink) {
    final FloatSliderWidget widget =
        new FloatSliderWidget(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, labelKey, min, max, initial, sink);
    return withTooltip(widget, Text.translatable(tooltipKey), canEdit);
  }

  private static ClickableWidget withTooltip(
      final ClickableWidget widget, final Text tooltip, final boolean canEdit) {
    widget.setTooltip(Tooltip.of(tooltip));
    widget.active = canEdit;
    return widget;
  }

  private void saveAndClose() {
    if (resolveAccess() != EditAccess.EDITABLE) {
      close();
      return;
    }
    final DogsUnleashedConfig updated =
        new DogsUnleashedConfig(
            this.enableNaturalSpawning,
            this.spawnRateMultiplierPercent,
            this.breedSpawnRateMultipliersPercent,
            this.capIndependentSpawningEnabled,
            this.gravesEnabled,
            this.dropLeashOnPlayMode,
            this.autoSleepEnabled,
            this.autoSleepRangeBlocks,
            this.barkVolume,
            this.howlVolume,
            this.showDogNames);
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
    final EditAccess access = resolveAccess();
    if (access != EditAccess.EDITABLE) {
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable(access.bannerKey()),
          this.width / 2,
          BANNER_Y,
          BANNER_COLOR);
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable(access.hintKey()),
          this.width / 2,
          BANNER_Y + 12,
          HINT_COLOR);
    }
  }

  private static EditAccess resolveAccess() {
    final ClientPlayerEntity player = MinecraftClient.getInstance().player;
    return EditAccess.resolve(
        player != null,
        player != null && player.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL));
  }

  public enum EditAccess {
    EDITABLE("", ""),
    READ_ONLY(
        "screen.dogs-unleashed.settings.readonly_banner",
        "screen.dogs-unleashed.settings.readonly_hint"),
    NO_WORLD(
        "screen.dogs-unleashed.settings.noworld_banner",
        "screen.dogs-unleashed.settings.noworld_hint");

    private final String bannerKey;
    private final String hintKey;

    EditAccess(final String bannerKey, final String hintKey) {
      this.bannerKey = bannerKey;
      this.hintKey = hintKey;
    }

    public static EditAccess resolve(final boolean inWorld, final boolean hasOperatorPermission) {
      if (!inWorld) {
        return NO_WORLD;
      }
      return hasOperatorPermission ? EDITABLE : READ_ONLY;
    }

    public String bannerKey() {
      return bannerKey;
    }

    public String hintKey() {
      return hintKey;
    }
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
        final net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {}
  }
}
