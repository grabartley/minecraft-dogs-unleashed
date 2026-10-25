package com.grahambartley.dogsunleashed.screenhandler;

import java.util.List;

public final class DogGearSlotRouting {

  public enum Section {
    BAGS,
    GEAR,
    MAIN,
    HOTBAR
  }

  public enum Target {
    PLAYER,
    GEAR,
    BAGS,
    HOTBAR,
    MAIN
  }

  private final int bagSlots;
  private final int gearSlots;
  private final int mainSlots;
  private final int hotbarSlots;

  public DogGearSlotRouting(
      final int bagSlots, final int gearSlots, final int mainSlots, final int hotbarSlots) {
    this.bagSlots = bagSlots;
    this.gearSlots = gearSlots;
    this.mainSlots = mainSlots;
    this.hotbarSlots = hotbarSlots;
  }

  public int bagStart() {
    return 0;
  }

  public int gearStart() {
    return this.bagStart() + this.bagSlots;
  }

  public int mainStart() {
    return this.gearStart() + this.gearSlots;
  }

  public int hotbarStart() {
    return this.mainStart() + this.mainSlots;
  }

  public int total() {
    return this.hotbarStart() + this.hotbarSlots;
  }

  public Section sectionOf(final int index) {
    if (index < this.gearStart()) {
      return Section.BAGS;
    }
    if (index < this.mainStart()) {
      return Section.GEAR;
    }
    if (index < this.hotbarStart()) {
      return Section.MAIN;
    }
    return Section.HOTBAR;
  }

  public static List<Target> targetsFor(
      final Section from, final boolean owner, final boolean bagsOpen) {
    return switch (from) {
      case GEAR, BAGS -> List.of(Target.PLAYER);
      case MAIN -> playerTargets(owner, bagsOpen, Target.HOTBAR);
      case HOTBAR -> playerTargets(owner, bagsOpen, Target.MAIN);
    };
  }

  private static List<Target> playerTargets(
      final boolean owner, final boolean bagsOpen, final Target otherHalf) {
    if (!owner) {
      return List.of(otherHalf);
    }
    return bagsOpen
        ? List.of(Target.GEAR, Target.BAGS, otherHalf)
        : List.of(Target.GEAR, otherHalf);
  }
}
