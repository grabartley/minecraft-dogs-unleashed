package com.grahambartley.dogsunleashed.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ModNetworkingTest {

  static Stream<Arguments> stripControlCharsCases() {
    final String printableAscii =
        " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    return Stream.of(
        Arguments.of("normal lowercase", "hello", "hello"),
        Arguments.of("mixed-case sentence", "Hello World 123", "Hello World 123"),
        Arguments.of("camelCase", "abcDEF", "abcDEF"),
        Arguments.of("null byte in middle", "a\0b", "ab"),
        Arguments.of("null byte at end", "hello\0", "hello"),
        Arguments.of("null byte at start", "\0hello", "hello"),
        Arguments.of("newline in middle", "a\nb", "ab"),
        Arguments.of("tab in middle", "a\tb", "ab"),
        Arguments.of("carriage return in middle", "a\rb", "ab"),
        Arguments.of("low control around text", "hello", "hello"),
        Arguments.of("DEL in middle", "ab", "ab"),
        Arguments.of("DEL at end", "hello", "hello"),
        Arguments.of("empty string", "", ""),
        Arguments.of("only control characters", "\0\n\r\t", ""),
        Arguments.of("mixed control and printable", "a\0b\0c\tX\nY\rZ123", "abcXYZ123"),
        Arguments.of("printable ASCII preserved", printableAscii, printableAscii),
        Arguments.of("accented Latin preserved", "éàüñ", "éàüñ"),
        Arguments.of("CJK preserved", "中国", "中国"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("stripControlCharsCases")
  @DisplayName("stripControlChars removes control bytes and keeps printable characters")
  void stripControlCharsRemovesControlBytes(
      final String label, final String input, final String expected) {
    assertEquals(expected, ModNetworking.stripControlChars(input));
  }
}
