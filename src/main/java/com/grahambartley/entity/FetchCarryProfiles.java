package com.grahambartley.entity;

import com.grahambartley.entity.fetch.FetchItemType;
import java.util.Objects;

public record FetchCarryProfiles(
    CarryProfile tennisBall, CarryProfile stick, CarryProfile frisbee) {
  static final String TENNIS_BALL_PATH = "tennis_ball";
  static final String STICK_PATH = "stick";
  static final String FRISBEE_PATH = "frisbee";

  public FetchCarryProfiles {
    Objects.requireNonNull(tennisBall, "tennisBall");
    Objects.requireNonNull(stick, "stick");
    Objects.requireNonNull(frisbee, "frisbee");
  }

  public CarryProfile forFetchItem(FetchItemType fetchType) {
    if (fetchType == null) {
      return tennisBall;
    }
    return forFetchItemPath(fetchType.id().getPath());
  }

  CarryProfile forFetchItemPath(String path) {
    if (path == null) {
      return tennisBall;
    }
    return switch (path) {
      case TENNIS_BALL_PATH -> tennisBall;
      case STICK_PATH -> stick;
      case FRISBEE_PATH -> frisbee;
      default -> tennisBall;
    };
  }
}
