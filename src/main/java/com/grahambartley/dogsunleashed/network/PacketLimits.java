package com.grahambartley.dogsunleashed.network;

public final class PacketLimits {
  public static final int SET_PET_NAME_NAME_MAX_LENGTH = 32;
  public static final int REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH = 64;
  public static final int CONNECTIONS_LIST_MAX_SIZE = 32;
  public static final int OWNER_NAME_MAX_LENGTH = 16;
  public static final int BREED_COMPOSITION_MAX_SIZE = 8;
  public static final int GENES_MAX_SIZE = 8;
  public static final int CARRIED_ALLELES_MAX_SIZE = 2;
  public static final int PARENTS_MAX_SIZE = 2;
  public static final int PARENT_ID_MAX_LENGTH = 36;
  public static final int DELIVERY_RECIPIENTS_MAX_SIZE = 64;

  private PacketLimits() {}
}
