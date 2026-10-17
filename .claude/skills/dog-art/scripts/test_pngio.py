import struct
import unittest
import zlib

import pngio
from testutil import remove, temp_path


def encode(rows, filter_type):
    width, channels = len(rows[0]), 4
    raw, previous = bytearray(), bytearray(width * channels)
    for row in rows:
        line = bytearray(b for px in row for b in px)
        out = bytearray([filter_type])
        for i in range(len(line)):
            left = line[i - channels] if i >= channels else 0
            up = previous[i]
            up_left = previous[i - channels] if i >= channels else 0
            pred = {0: 0, 1: left, 2: up, 3: (left + up) >> 1, 4: pngio.paeth(left, up, up_left)}[filter_type]
            out.append((line[i] - pred) & 255)
        raw += out
        previous = line
    path = temp_path(".png")
    with open(path, "wb") as handle:
        handle.write(b"\x89PNG\r\n\x1a\n")
        handle.write(pngio.chunk(b"IHDR", struct.pack(">IIBBBBB", width, len(rows), 8, 6, 0, 0, 0)))
        handle.write(pngio.chunk(b"IDAT", zlib.compress(bytes(raw))))
        handle.write(pngio.chunk(b"IEND", b""))
    return path


class PngCodec(unittest.TestCase):
    rows = [[(10, 20, 30, 255), (200, 100, 0, 128), (5, 250, 125, 0)], [(255, 255, 255, 255), (0, 0, 0, 255), (90, 60, 30, 26)]]

    def test_every_filter_type_decodes_to_the_same_pixels(self):
        for filter_type in range(5):
            with self.subTest(filter_type=filter_type):
                path = encode(self.rows, filter_type)
                try:
                    self.assertEqual(pngio.read_png(path), (3, 2, self.rows))
                finally:
                    remove(path)

    def test_write_then_read_round_trips(self):
        path = temp_path(".png")
        try:
            pngio.write_png(path, 3, 2, self.rows)
            self.assertEqual(pngio.read_png(path), (3, 2, self.rows))
        finally:
            remove(path)

    def test_opaque_uses_the_cutout_threshold_of_the_game(self):
        self.assertTrue(pngio.opaque((0, 0, 0, 26)))
        self.assertFalse(pngio.opaque((0, 0, 0, 25)))


if __name__ == "__main__":
    unittest.main()
