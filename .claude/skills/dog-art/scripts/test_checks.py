import unittest

import bake
import checks
import pngio
from testutil import remove, temp_path, write_geo, write_sheet


def block(text):
    path = temp_path(".txt")
    with open(path, "w") as handle:
        handle.write(text)
    return path


class Patch(unittest.TestCase):
    def test_rows_after_the_first_pixel_row_are_all_pixel_rows(self):
        path = block("x=#FF00FF\n# a note\nxx\n  \nxx\n##\n")
        try:
            palette, rows = checks.parse_block(path)
        finally:
            remove(path)
        self.assertEqual(rows, ["xx", "  ", "xx", "##"])
        pixels = [[(0, 0, 0, 0)] * 4 for _ in range(4)]
        with self.assertRaises(SystemExit):
            checks.apply_block(pixels, 4, 4, palette, rows, 0, 0)
        palette["#"] = (1, 2, 3)
        self.assertEqual(checks.apply_block(pixels, 4, 4, palette, rows, 0, 0), 6)
        self.assertEqual(pixels[1][0], (0, 0, 0, 0))
        self.assertEqual(pixels[3][1], (1, 2, 3, 255))

    def test_bad_palette_value_is_refused(self):
        path = block("x=purple\nx\n")
        try:
            with self.assertRaises(SystemExit):
                checks.parse_block(path)
        finally:
            remove(path)


class Diff(unittest.TestCase):
    def test_clear_texels_never_count_and_visible_ones_do(self):
        a = (2, 1, [[(1, 2, 3, 0), (9, 9, 9, 255)]])
        b = (2, 1, [[(7, 7, 7, 0), (9, 9, 8, 255)]])
        changed, image = checks.diff_sheets(a, b)
        self.assertEqual(changed, 1)
        self.assertEqual(image[0][1], (255, 0, 255, 255))


class CheckSheet(unittest.TestCase):
    def report(self, clear=(), size=(2, 2, 2), palette=None, texture=16):
        geo = write_geo([{"origin": [0, 0, 0], "size": list(size), "uv": [0, 0]}], texture=texture)
        sheet = write_sheet(16, 16, clear=clear)
        try:
            _, cubes = bake.cubes_of(geo)
            return checks.check_sheet(cubes, texture, texture, pngio.read_png(sheet), palette)
        finally:
            remove(geo, sheet)

    def test_clean_sheet_has_only_stray_paint_outside_the_patch(self):
        report = self.report()
        self.assertEqual((report["holes"], report["unpainted"], report["off_sheet"]), ([], [], []))
        self.assertEqual(report["stray"], 16 * 16 - 24)
        self.assertEqual(checks.problem_groups(report), 0)
        self.assertEqual(checks.problem_groups(report, strict=True), 1)

    def test_a_partly_cleared_face_is_a_hole_and_a_cleared_face_is_unpainted(self):
        report = self.report(clear={(2, 2)} | {(x, y) for x in range(4, 6) for y in range(2, 4)})
        self.assertEqual(len(report["holes"]), 1)
        self.assertIn("b[0].north", report["holes"][0])
        self.assertEqual(len(report["unpainted"]), 1)
        self.assertIn("b[0].west", report["unpainted"][0])
        self.assertEqual(checks.problem_groups(report), 0)
        self.assertEqual(checks.problem_groups(report, fail_unpainted=True), 1)

    def test_faces_past_the_sheet_edge_are_reported(self):
        report = self.report(size=(8, 8, 8))
        self.assertEqual(len(report["off_sheet"]), 3)

    def test_palette_drift_lists_colours_outside_the_palette(self):
        report = self.report(palette={(1, 1, 1)})
        self.assertEqual(report["drift"], {(200, 120, 60): 256})
        self.assertEqual(checks.problem_groups(report), 1)


class Palette(unittest.TestCase):
    def test_hex_tokens_only_and_ramps_sort_by_tone(self):
        path = temp_path(".txt")
        with open(path, "w") as handle:
            handle.write("# approved coat\n#FFFFFF #000000 notacolour #12\n")
        try:
            self.assertEqual(checks.read_palette(path), {(255, 255, 255), (0, 0, 0)})
        finally:
            remove(path)
        ramps = checks.palette_ramps([[(10, 10, 10, 255), (200, 200, 200, 255), (200, 50, 50, 255), (0, 0, 0, 10)]])
        self.assertEqual([rgb for rgb, _ in ramps["grey"]], [(10, 10, 10), (200, 200, 200)])
        self.assertEqual(list(ramps["hue000"]), [((200, 50, 50), 1)])


if __name__ == "__main__":
    unittest.main()
