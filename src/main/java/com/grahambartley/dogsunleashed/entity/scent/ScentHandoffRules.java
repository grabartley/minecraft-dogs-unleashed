package com.grahambartley.dogsunleashed.entity.scent;

public final class ScentHandoffRules {

  public enum Handoff {
    IGNORE,
    FOLLOW_MAP,
    FOLLOW_SCENT,
    CALL_OFF
  }

  private ScentHandoffRules() {}

  public static Handoff decide(
      final boolean map,
      final boolean scentItem,
      final boolean tamingItem,
      final boolean trackingThisItem,
      final boolean markFound,
      final boolean sameMark) {
    if ((!map && !scentItem) || tamingItem) {
      return Handoff.IGNORE;
    }
    if (map) {
      if (!markFound) {
        return Handoff.IGNORE;
      }
      return trackingThisItem && sameMark ? Handoff.CALL_OFF : Handoff.FOLLOW_MAP;
    }
    return trackingThisItem ? Handoff.CALL_OFF : Handoff.FOLLOW_SCENT;
  }
}
