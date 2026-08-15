package com.grahambartley.dogsunleashed.network;

public final class PacketLimits {
  public static final int SET_PET_NAME_NAME_MAX_LENGTH = 32;
  public static final int REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH = 64;
  public static final int CONNECTIONS_LIST_MAX_SIZE = 32;
  public static final int OWNER_NAME_MAX_LENGTH = 16;
  public static final int BREED_COMPOSITION_MAX_SIZE = 8;

  private PacketLimits() {}
}
