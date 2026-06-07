package com.grahambartley.network;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ModNetworkingTest {

  @Test
  @DisplayName("stripControlChars should keep normal text unchanged")
  void keepsNormalText() {
    assertEquals("hello", ModNetworking.stripControlChars("hello"));
    assertEquals("Hello World 123", ModNetworking.stripControlChars("Hello World 123"));
    assertEquals("abcDEF", ModNetworking.stripControlChars("abcDEF"));
  }

  @Test
  @DisplayName("stripControlChars should remove null bytes")
  void removesNullBytes() {
    assertEquals("ab", ModNetworking.stripControlChars("a\0b"));
    assertEquals("hello", ModNetworking.stripControlChars("hello\0"));
    assertEquals("hello", ModNetworking.stripControlChars("\0hello"));
  }

  @Test
  @DisplayName("stripControlChars should remove control characters below 0x20")
  void removesLowControlChars() {
    assertEquals("ab", ModNetworking.stripControlChars("a\nb"));
    assertEquals("ab", ModNetworking.stripControlChars("a\tb"));
    assertEquals("ab", ModNetworking.stripControlChars("a\rb"));
    assertEquals("hello", ModNetworking.stripControlChars("\u0001hello\u0002"));
  }

  @Test
  @DisplayName("stripControlChars should remove DEL character (0x7f)")
  void removesDelCharacter() {
    assertEquals("ab", ModNetworking.stripControlChars("a\u007fb"));
    assertEquals("hello", ModNetworking.stripControlChars("hello\u007f"));
  }

  @Test
  @DisplayName("stripControlChars should handle empty string")
  void handlesEmptyString() {
    assertEquals("", ModNetworking.stripControlChars(""));
  }

  @Test
  @DisplayName("stripControlChars should return empty string when all chars are control chars")
  void handlesAllControlChars() {
    assertEquals("", ModNetworking.stripControlChars("\0\n\r\t\u0001\u007f"));
  }

  @ParameterizedTest
  @DisplayName("stripControlChars should preserve printable ASCII characters")
  @CsvSource({
    " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~",
    " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
  })
  void preservesPrintableAscii(final String input) {
    assertEquals(input, ModNetworking.stripControlChars(input));
  }

  @Test
  @DisplayName("stripControlChars should preserve printable non-ASCII characters")
  void preservesNonAscii() {
    assertEquals(
        "\u00e9\u00e0\u00fc\u00f1", ModNetworking.stripControlChars("\u00e9\u00e0\u00fc\u00f1"));
    assertEquals("\u4e2d\u56fd", ModNetworking.stripControlChars("\u4e2d\u56fd"));
  }

  @Test
  @DisplayName("stripControlChars should strip mixed control and normal chars")
  void stripsMixedContent() {
    assertEquals("abcXYZ123", ModNetworking.stripControlChars("a\0b\0c\tX\nY\rZ\u0001123\u007f"));
  }
}
