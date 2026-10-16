package com.grahambartley.dogsunleashed.screen;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

final class DogsUnleashedConfigList extends ElementListWidget<DogsUnleashedConfigList.Row> {
  private final int rowWidth;

  DogsUnleashedConfigList(
      final MinecraftClient client,
      final int width,
      final ConfigScreenLayout layout,
      final int rowHeight,
      final int rowWidth) {
    super(client, width, layout.listHeight(), layout.listTop(), rowHeight);
    this.rowWidth = rowWidth;
  }

  void addRow(final ClickableWidget widget) {
    addEntry(new Row(widget));
  }

  @Override
  public int getRowWidth() {
    return this.rowWidth;
  }

  static final class Row extends ElementListWidget.Entry<Row> {
    private final ClickableWidget widget;

    Row(final ClickableWidget widget) {
      this.widget = widget;
    }

    @Override
    public void render(
        final DrawContext context,
        final int index,
        final int y,
        final int x,
        final int entryWidth,
        final int entryHeight,
        final int mouseX,
        final int mouseY,
        final boolean hovered,
        final float tickDelta) {
      this.widget.setX(x + (entryWidth - this.widget.getWidth()) / 2);
      this.widget.setY(y + (entryHeight - this.widget.getHeight()) / 2);
      this.widget.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public List<? extends Element> children() {
      return List.of(this.widget);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
      return List.of(this.widget);
    }
  }
}
