package com.grahambartley.dogsunleashed.sound;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

record OggVorbisInfo(int channels, int sampleRate, long totalSamples) {
  private static final byte[] CAPTURE_PATTERN = "OggS".getBytes(StandardCharsets.US_ASCII);
  private static final int PAGE_HEADER_LENGTH = 27;
  private static final int GRANULE_OFFSET = 6;
  private static final int SEGMENT_COUNT_OFFSET = 26;

  static OggVorbisInfo read(final Path file) throws IOException {
    final ByteBuffer bytes =
        ByteBuffer.wrap(Files.readAllBytes(file)).order(ByteOrder.LITTLE_ENDIAN);
    if (!startsWithCapturePattern(bytes, 0)) {
      throw new IOException(file + " is not an Ogg file");
    }
    final int segmentCount = Byte.toUnsignedInt(bytes.get(SEGMENT_COUNT_OFFSET));
    final int packet = PAGE_HEADER_LENGTH + segmentCount;
    if (bytes.get(packet) != 1
        || !"vorbis".equals(new String(bytes.array(), packet + 1, 6, StandardCharsets.US_ASCII))) {
      throw new IOException(file + " does not start with a Vorbis identification header");
    }
    final int channels = Byte.toUnsignedInt(bytes.get(packet + 11));
    final int sampleRate = bytes.getInt(packet + 12);
    return new OggVorbisInfo(
        channels, sampleRate, bytes.getLong(lastPageStart(bytes) + GRANULE_OFFSET));
  }

  double durationSeconds() {
    return (double) totalSamples / sampleRate;
  }

  private static int lastPageStart(final ByteBuffer bytes) throws IOException {
    for (int offset = bytes.limit() - PAGE_HEADER_LENGTH; offset >= 0; offset--) {
      if (startsWithCapturePattern(bytes, offset)) {
        return offset;
      }
    }
    throw new IOException("no Ogg page found");
  }

  private static boolean startsWithCapturePattern(final ByteBuffer bytes, final int offset) {
    for (int i = 0; i < CAPTURE_PATTERN.length; i++) {
      if (bytes.get(offset + i) != CAPTURE_PATTERN[i]) {
        return false;
      }
    }
    return true;
  }
}
