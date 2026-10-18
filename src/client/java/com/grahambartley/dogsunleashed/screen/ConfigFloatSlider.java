package com.grahambartley.dogsunleashed.screen;

import java.util.function.Consumer;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

final class ConfigFloatSlider extends SliderWidget {
  private final String labelKey;
  private final SliderScale scale;
  private final Consumer<Float> sink;
  private float currentValue;

  ConfigFloatSlider(
      final int width,
      final int height,
      final String labelKey,
      final float min,
      final float max,
      final float initial,
      final Consumer<Float> sink) {
    super(0, 0, width, height, Text.empty(), new SliderScale(min, max).fractionOf(initial));
    this.labelKey = labelKey;
    this.scale = new SliderScale(min, max);
    this.currentValue = (float) this.scale.clamp(initial);
    this.sink = sink;
    updateMessage();
  }

  @Override
  protected void updateMessage() {
    setMessage(
        Text.translatable(labelKey).append(Text.literal(String.format(": %.2f", currentValue))));
  }

  @Override
  protected void applyValue() {
    currentValue = (float) scale.valueAt(this.value);
    sink.accept(currentValue);
  }
}
