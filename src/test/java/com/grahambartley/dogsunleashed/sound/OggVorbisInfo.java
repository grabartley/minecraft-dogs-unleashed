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
    final int packet = PAGE_HEADER_LENGTH + segmentCount(bytes, 0);
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
    int page = 0;
    while (true) {
      final int segments = segmentCount(bytes, page);
      int bodyLength = 0;
      for (int i = 0; i < segments; i++) {
        bodyLength += Byte.toUnsignedInt(bytes.get(page + PAGE_HEADER_LENGTH + i));
      }
      final int next = page + PAGE_HEADER_LENGTH + segments + bodyLength;
      if (next >= bytes.limit()) {
        return page;
      }
      page = next;
    }
  }

  private static int segmentCount(final ByteBuffer bytes, final int page) throws IOException {
    for (int i = 0; i < CAPTURE_PATTERN.length; i++) {
      if (bytes.get(page + i) != CAPTURE_PATTERN[i]) {
        throw new IOException("no Ogg page at byte " + page);
      }
    }
    return Byte.toUnsignedInt(bytes.get(page + SEGMENT_COUNT_OFFSET));
  }
}
