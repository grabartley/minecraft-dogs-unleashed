package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.pet.PetAliveFilter;
import com.grahambartley.dogsunleashed.util.DimensionLabelFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class PetManagerScreen extends Screen {

  private static final int ENTRY_HEIGHT = 60;
  private static final int ENTRY_WIDTH = 280;
  private static final int ENTRIES_PER_PAGE = 4;
  private static final int THUMBNAIL_SIZE = 48;
  private static final long SEARCH_DEBOUNCE_MS = 250L;
  private static final int SEARCH_MAX_LENGTH = 32;
  private static final float LOW_HEALTH_COLOR_THRESHOLD = 0.5f;
  private static final int KEYBIND_HINT_BOTTOM_OFFSET = 20;
  private static final int KEYBIND_HINT_COLOR = 0xFFAAAAAA;
  private static final int KEYBIND_HINT_HOVER_COLOR = 0xFFFFDD66;
  static final int ROW_SUMMON_WIDTH = 56;
  static final int ROW_SUMMON_HEIGHT = 18;
  private static final int ROW_SUMMON_COLOR = 0x80446644;
  private static final int ROW_SUMMON_HOVER_COLOR = 0xB055AA55;
  private static final int ROW_SUMMON_BORDER = 0xFF77AA77;

  private static final List<BreedFilterOption> BREED_OPTIONS = List.of(BreedFilterOption.values());

  private List<PetSyncData> pets = new ArrayList<>();
  private TextFieldWidget searchField;
  private CyclingButtonWidget<BreedFilterOption> breedFilterButton;
  private CyclingButtonWidget<PetAliveFilter> aliveFilterButton;
  private UnleashedDogBreed currentBreedFilter = null;
  private PetAliveFilter currentAliveFilter = PetAliveFilter.ALIVE;
  private int scrollOffset = 0;
  private final DogPortraitRenderer portraits = new DogPortraitRenderer();
  private long nextSearchRefreshTime = -1L;
  private String lastRequestedSearchQuery = "";
  private Text keybindHintText;
  private int keybindHintX;
  private int keybindHintY;
  private int keybindHintWidth;

  public PetManagerScreen() {
    super(Text.translatable("screen.dogs-unleashed.pet_manager.title"));
  }

  @Override
  protected void init() {
    final int centerX = this.width / 2;

    searchField =
        new TextFieldWidget(
            this.textRenderer,
            centerX - 140,
            40,
            245,
            20,
            Text.translatable("screen.dogs-unleashed.pet_manager.search"));
    searchField.setMaxLength(SEARCH_MAX_LENGTH);
    searchField.setChangedListener(this::onSearchChanged);
    addDrawableChild(searchField);

    breedFilterButton =
        addDrawableChild(
            CyclingButtonWidget.<BreedFilterOption>builder(
                    breed ->
                        breed.isAllBreeds()
                            ? Text.translatable("screen.dogs-unleashed.pet_manager.all_breeds")
                            : Text.translatable(breed.breed().translationKey()))
                .values(BREED_OPTIONS)
                .initially(BreedFilterOption.fromBreed(currentBreedFilter))
                .build(
                    centerX - 140,
                    65,
                    120,
                    20,
                    Text.translatable("screen.dogs-unleashed.pet_manager.breed_filter"),
                    (button, value) -> {
                      currentBreedFilter = value.breed();
                      refreshPetsList();
                    }));

    aliveFilterButton =
        addDrawableChild(
            CyclingButtonWidget.<PetAliveFilter>builder(
                    filter ->
                        switch (filter) {
                          case ALL ->
                              Text.translatable("screen.dogs-unleashed.pet_manager.all_status");
                          case ALIVE ->
                              Text.translatable("screen.dogs-unleashed.pet_manager.alive_only");
                          case UNDEAD ->
                              Text.translatable("screen.dogs-unleashed.pet_manager.undead_only");
                          case DECEASED ->
                              Text.translatable("screen.dogs-unleashed.pet_manager.deceased_only");
                        })
                .values(PetAliveFilter.values())
                .initially(currentAliveFilter)
                .build(
                    centerX + 5,
                    65,
                    100,
                    20,
                    Text.translatable("screen.dogs-unleashed.pet_manager.status_filter"),
                    (button, value) -> {
                      currentAliveFilter = value;
                      refreshPetsList();
                    }));

    addDrawableChild(
        ButtonWidget.builder(Text.literal("▲"), button -> scroll(-1))
            .dimensions(centerX + ENTRY_WIDTH / 2 + 10, 95, 20, 20)
            .build());

    addDrawableChild(
        ButtonWidget.builder(Text.literal("▼"), button -> scroll(1))
            .dimensions(centerX + ENTRY_WIDTH / 2 + 10, this.height - 80, 20, 20)
            .build());

    final ButtonWidget settingsButton =
        ButtonWidget.builder(
                Text.literal("⚙"),
                button ->
                    MinecraftClient.getInstance()
                        .setScreen(new DogsUnleashedConfigScreen(PetManagerScreen.this)))
            .dimensions(this.width - 24, 4, 20, 20)
            .build();
    settingsButton.setTooltip(
        Tooltip.of(Text.translatable("screen.dogs-unleashed.pet_manager.settings_tooltip")));
    addDrawableChild(settingsButton);

    keybindHintText = Text.translatable("screen.dogs-unleashed.pet_manager.keybind_hint");
    keybindHintWidth = this.textRenderer.getWidth(keybindHintText);
    keybindHintX = centerX - keybindHintWidth / 2;
    keybindHintY = keybindHintY(this.height);

    setInitialFocus(searchField);
    setFocused(searchField);
    searchField.setFocused(true);
    searchField.setEditable(true);

    ModNetworkingClient.sendRequestPetManagerState();
  }

  private void onSearchChanged(final String query) {
    nextSearchRefreshTime = System.currentTimeMillis() + SEARCH_DEBOUNCE_MS;
  }

  @Override
  public void tick() {
    super.tick();
    if (nextSearchRefreshTime > 0L && System.currentTimeMillis() >= nextSearchRefreshTime) {
      nextSearchRefreshTime = -1L;
      final String currentSearchQuery = searchField != null ? searchField.getText() : "";
      if (!currentSearchQuery.equals(lastRequestedSearchQuery)) {
        refreshPetsList();
      }
    }
  }

  private void refreshPetsList() {
    final String searchQuery = searchField != null ? searchField.getText() : "";
    lastRequestedSearchQuery = searchQuery;
    ModNetworkingClient.sendRequestPets(currentBreedFilter, currentAliveFilter, searchQuery);
  }

  public void applySavedFilters(
      final UnleashedDogBreed breedFilter, final PetAliveFilter aliveFilter) {
    currentBreedFilter = breedFilter;
    currentAliveFilter = aliveFilter;
    if (breedFilterButton != null) {
      breedFilterButton.setValue(BreedFilterOption.fromBreed(currentBreedFilter));
    }
    if (aliveFilterButton != null) {
      aliveFilterButton.setValue(currentAliveFilter);
    }
  }

  public void updatePetsList(final List<PetSyncData> pets) {
    portraits.clear();
    this.pets = new ArrayList<>(pets);
    this.scrollOffset = 0;
  }

  @Override
  public void close() {
    if (searchField != null) {
      searchField.setText("");
    }
    lastRequestedSearchQuery = "";
    nextSearchRefreshTime = -1L;
    super.close();
  }

  @Override
  public void removed() {
    portraits.clear();
    super.removed();
  }

  private void scroll(final int direction) {
    final int maxOffset = Math.max(0, pets.size() - ENTRIES_PER_PAGE);
    scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + direction));
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);

    final int centerX = this.width / 2;

    context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 15, 0xFFFFFF);

    final int listStartY = 95;
    for (int i = 0; i < ENTRIES_PER_PAGE && i + scrollOffset < pets.size(); i++) {
      final PetSyncData pet = pets.get(i + scrollOffset);
      final int entryY = listStartY + i * ENTRY_HEIGHT;
      renderPetEntry(
          context, pet, centerX - ENTRY_WIDTH / 2, entryY, (float) mouseX, (float) mouseY);
    }

    if (pets.isEmpty()) {
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.pet_manager.no_pets"),
          centerX,
          listStartY + 50,
          0x888888);
    }

    renderKeybindHint(context, mouseX, mouseY);
  }

  private void renderKeybindHint(final DrawContext context, final int mouseX, final int mouseY) {
    if (keybindHintText == null) {
      return;
    }
    final int color =
        isOverKeybindHint(mouseX, mouseY) ? KEYBIND_HINT_HOVER_COLOR : KEYBIND_HINT_COLOR;
    context.drawText(this.textRenderer, keybindHintText, keybindHintX, keybindHintY, color, false);
    final int underlineY = keybindHintY + this.textRenderer.fontHeight;
    context.fill(keybindHintX, underlineY, keybindHintX + keybindHintWidth, underlineY + 1, color);
  }

  private boolean isOverKeybindHint(final double mouseX, final double mouseY) {
    return keybindHintText != null
        && isWithinKeybindHint(
            mouseX,
            mouseY,
            keybindHintX,
            keybindHintY,
            keybindHintWidth,
            this.textRenderer.fontHeight);
  }

  static boolean isWithinKeybindHint(
      final double mouseX,
      final double mouseY,
      final int hintX,
      final int hintY,
      final int hintWidth,
      final int hintHeight) {
    return mouseX >= hintX
        && mouseX < hintX + hintWidth
        && mouseY >= hintY
        && mouseY < hintY + hintHeight;
  }

  static int rowSummonButtonX(final int rowX) {
    return rowX + ENTRY_WIDTH - ROW_SUMMON_WIDTH - 8;
  }

  static int rowSummonButtonY(final int rowY) {
    return rowY + (ENTRY_HEIGHT - 5 - ROW_SUMMON_HEIGHT) / 2;
  }

  static boolean isWithinRowSummonButton(
      final double mouseX, final double mouseY, final int rowX, final int rowY) {
    final int buttonX = rowSummonButtonX(rowX);
    final int buttonY = rowSummonButtonY(rowY);
    return mouseX >= buttonX
        && mouseX < buttonX + ROW_SUMMON_WIDTH
        && mouseY >= buttonY
        && mouseY < buttonY + ROW_SUMMON_HEIGHT;
  }

  static int keybindHintY(final int screenHeight) {
    return screenHeight - KEYBIND_HINT_BOTTOM_OFFSET;
  }

  private void renderPetEntry(
      final DrawContext context,
      final PetSyncData pet,
      final int x,
      final int y,
      final float mouseX,
      final float mouseY) {
    final boolean isHovered =
        mouseX >= x && mouseX < x + ENTRY_WIDTH && mouseY >= y && mouseY < y + ENTRY_HEIGHT - 5;

    final int bgColor;
    if (!pet.alive()) {
      bgColor = isHovered ? 0x80404040 : 0x60303030;
    } else if (isHovered) {
      bgColor = 0x60555555;
    } else {
      bgColor = 0x40333333;
    }

    context.fill(x, y, x + ENTRY_WIDTH, y + ENTRY_HEIGHT - 5, bgColor);

    final int imgX = x + 5;
    final int imgY = y + 3;

    portraits.draw(context, this.textRenderer, pet, imgX, imgY, THUMBNAIL_SIZE, mouseX, mouseY);

    if (pet.alive()) {
      renderRowSummonButton(context, x, y, mouseX, mouseY);
    }

    final int textX = x + THUMBNAIL_SIZE + 15;
    final int textMaxWidth = rowSummonButtonX(x) - textX - 6;
    final int nameColor = PetLifeStateDisplay.nameColor(pet.lifeState(), 0xFFFFFF, 0x888888);
    context.drawText(
        this.textRenderer,
        this.textRenderer.trimToWidth(pet.name(), textMaxWidth),
        textX,
        y + 5,
        nameColor,
        true);

    final String breedName = DogBreedNames.displayName(pet.breed(), pet.composition()).getString();
    context.drawText(this.textRenderer, breedName, textX, y + 18, 0xAAAAAA, false);

    final Text statusLabel = PetLifeStateDisplay.statusLabel(pet.lifeState());
    if (pet.alive() && statusLabel != null) {
      context.drawText(
          this.textRenderer,
          statusLabel,
          textX + textMaxWidth - this.textRenderer.getWidth(statusLabel),
          y + 18,
          PetLifeStateDisplay.statusColor(pet.lifeState()),
          false);
    }

    if (pet.alive()) {
      final String healthText = String.format("%.1f / %.1f ❤", pet.health(), pet.maxHealth());
      final int healthColor =
          pet.health() > pet.maxHealth() * LOW_HEALTH_COLOR_THRESHOLD ? 0x55FF55 : 0xFF5555;
      context.drawText(this.textRenderer, healthText, textX, y + 31, healthColor, false);

      final String locationText =
          String.format(
              "%s (%d, %d, %d)",
              DimensionLabelFormatter.format(pet.dimension()), pet.posX(), pet.posY(), pet.posZ());
      context.drawText(
          this.textRenderer,
          this.textRenderer.trimToWidth(locationText, textMaxWidth),
          textX,
          y + 44,
          0x888888,
          false);
    } else if (statusLabel != null) {
      context.drawText(
          this.textRenderer,
          statusLabel,
          textX,
          y + 31,
          PetLifeStateDisplay.statusColor(pet.lifeState()),
          false);
    }
  }

  private void renderRowSummonButton(
      final DrawContext context,
      final int rowX,
      final int rowY,
      final float mouseX,
      final float mouseY) {
    final int buttonX = rowSummonButtonX(rowX);
    final int buttonY = rowSummonButtonY(rowY);
    final boolean hovered = isWithinRowSummonButton(mouseX, mouseY, rowX, rowY);
    context.fill(
        buttonX,
        buttonY,
        buttonX + ROW_SUMMON_WIDTH,
        buttonY + ROW_SUMMON_HEIGHT,
        hovered ? ROW_SUMMON_HOVER_COLOR : ROW_SUMMON_COLOR);
    context.drawBorder(buttonX, buttonY, ROW_SUMMON_WIDTH, ROW_SUMMON_HEIGHT, ROW_SUMMON_BORDER);
    context.drawCenteredTextWithShadow(
        this.textRenderer,
        Text.translatable("screen.dogs-unleashed.pet_manager.row_summon"),
        buttonX + ROW_SUMMON_WIDTH / 2,
        buttonY + (ROW_SUMMON_HEIGHT - this.textRenderer.fontHeight) / 2 + 1,
        0xFFFFFF);
  }

  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (button == 0) {
      if (isOverKeybindHint(mouseX, mouseY)) {
        openKeybindsScreen();
        return true;
      }

      final int centerX = this.width / 2;
      final int listStartY = 95;
      final int listX = centerX - ENTRY_WIDTH / 2;

      for (int i = 0; i < ENTRIES_PER_PAGE && i + scrollOffset < pets.size(); i++) {
        final int entryY = listStartY + i * ENTRY_HEIGHT;
        if (mouseX >= listX
            && mouseX < listX + ENTRY_WIDTH
            && mouseY >= entryY
            && mouseY < entryY + ENTRY_HEIGHT - 5) {
          final PetSyncData pet = pets.get(i + scrollOffset);
          if (pet.alive() && isWithinRowSummonButton(mouseX, mouseY, listX, entryY)) {
            ModNetworkingClient.sendSummonPet(UUID.fromString(pet.petId()));
          } else {
            MinecraftClient.getInstance().setScreen(new PetDetailsScreen(this, pet));
          }
          return true;
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  private void openKeybindsScreen() {
    final MinecraftClient client = MinecraftClient.getInstance();
    client
        .getSoundManager()
        .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    client.setScreen(new KeybindsScreen(this, client.options));
  }

  @Override
  public boolean mouseScrolled(
      final double mouseX,
      final double mouseY,
      final double horizontalAmount,
      final double verticalAmount) {
    scroll(verticalAmount > 0 ? -1 : 1);
    return true;
  }

  @Override
  public boolean shouldPause() {
    return false;
  }

  private enum BreedFilterOption {
    ALL(null),
    HUSKY(UnleashedDogBreed.HUSKY),
    DACHSHUND(UnleashedDogBreed.DACHSHUND),
    BEAGLE(UnleashedDogBreed.BEAGLE),
    GOLDEN_RETRIEVER(UnleashedDogBreed.GOLDEN_RETRIEVER),
    SHIBA_INU(UnleashedDogBreed.SHIBA_INU),
    MIXED(UnleashedDogBreed.CROSS_BREED);

    private final UnleashedDogBreed breed;

    BreedFilterOption(final @Nullable UnleashedDogBreed breed) {
      this.breed = breed;
    }

    public @Nullable UnleashedDogBreed breed() {
      return this.breed;
    }

    public boolean isAllBreeds() {
      return this.breed == null;
    }

    public static BreedFilterOption fromBreed(final @Nullable UnleashedDogBreed breed) {
      if (breed == null) {
        return ALL;
      }

      return switch (breed) {
        case HUSKY -> HUSKY;
        case DACHSHUND -> DACHSHUND;
        case BEAGLE -> BEAGLE;
        case GOLDEN_RETRIEVER -> GOLDEN_RETRIEVER;
        case SHIBA_INU -> SHIBA_INU;
        case CROSS_BREED -> MIXED;
      };
    }
  }
}
