package com.grahambartley.dogsunleashed.network.handler;

public final class PetNameSanitizer {

  private PetNameSanitizer() {}

  public static String stripControlChars(final String input) {
    final StringBuilder sb = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
      final char c = input.charAt(i);
      if (c >= 0x20 && c != 0x7f) {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
