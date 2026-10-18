package com.grahambartley.dogsunleashed.screen;

import java.util.function.Consumer;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

final class ConfigIntSlider extends SliderWidget {
  private final Text label;
  private final String valueSuffix;
  private final SliderScale scale;
  private final Consumer<Integer> sink;
  private int currentValue;

  ConfigIntSlider(
      final int width,
      final int height,
      final Text label,
      final String valueSuffix,
      final int min,
      final int max,
      final int initial,
      final Consumer<Integer> sink) {
    super(0, 0, width, height, Text.empty(), new SliderScale(min, max).fractionOf(initial));
    this.label = label;
    this.valueSuffix = valueSuffix;
    this.scale = new SliderScale(min, max);
    this.currentValue = (int) this.scale.clamp(initial);
    this.sink = sink;
    updateMessage();
  }

  @Override
  protected void updateMessage() {
    setMessage(label.copy().append(Text.literal(": " + currentValue + valueSuffix)));
  }

  void show(final int shown) {
    currentValue = (int) scale.clamp(shown);
    this.value = scale.fractionOf(currentValue);
    updateMessage();
  }

  @Override
  protected void applyValue() {
    currentValue = (int) Math.round(scale.valueAt(this.value));
    sink.accept(currentValue);
  }
}
