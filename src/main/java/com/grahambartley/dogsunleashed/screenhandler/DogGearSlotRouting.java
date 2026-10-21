package com.grahambartley.dogsunleashed.screenhandler;

import java.util.List;

public final class DogGearSlotRouting {

  public enum Section {
    GEAR,
    MAIN,
    HOTBAR,
    BAGS
  }

  public enum Target {
    PLAYER,
    GEAR,
    BAGS,
    HOTBAR,
    MAIN
  }

  private final int gearSlots;
  private final int mainSlots;
  private final int hotbarSlots;
  private final int bagSlots;

  public DogGearSlotRouting(
      final int gearSlots, final int mainSlots, final int hotbarSlots, final int bagSlots) {
    this.gearSlots = gearSlots;
    this.mainSlots = mainSlots;
    this.hotbarSlots = hotbarSlots;
    this.bagSlots = bagSlots;
  }

  public int mainStart() {
    return this.gearSlots;
  }

  public int hotbarStart() {
    return this.mainStart() + this.mainSlots;
  }

  public int bagStart() {
    return this.hotbarStart() + this.hotbarSlots;
  }

  public int total() {
    return this.bagStart() + this.bagSlots;
  }

  public Section sectionOf(final int index) {
    if (index < this.mainStart()) {
      return Section.GEAR;
    }
    if (index < this.hotbarStart()) {
      return Section.MAIN;
    }
    if (index < this.bagStart()) {
      return Section.HOTBAR;
    }
    return Section.BAGS;
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
