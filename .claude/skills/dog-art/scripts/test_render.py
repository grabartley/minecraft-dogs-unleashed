import unittest

import bake
import pngio
import render
from testutil import remove, write_geo, write_sheet


class Renderer(unittest.TestCase):
    def setUp(self):
        self.geo = write_geo([{"origin": [-2, 0, -2], "size": [4, 4, 4], "uv": [0, 0]}], texture=16)
        self.sheet = write_sheet(16, 16, fill=(255, 255, 255, 255))

    def tearDown(self):
        remove(self.geo, self.sheet)

    def centre(self, view):
        _, cubes = bake.cubes_of(self.geo)
        w, h, image = render.render_view(cubes, pngio.read_png(self.sheet), 16, 16, *render.view_angles(view), 8)
        return image[h // 2][w // 2]

    def test_top_face_is_lit_and_underside_is_half(self):
        self.assertEqual(self.centre("top"), (255, 255, 255, 255))
        _, cubes = bake.cubes_of(self.geo)
        w, h, image = render.render_view(cubes, pngio.read_png(self.sheet), 16, 16, 0, -89, 8)
        self.assertEqual(image[h // 2][w // 2], (127, 127, 127, 255))

    def test_front_view_sees_the_north_face_at_its_ramp(self):
        self.assertEqual(self.centre("front"), (204, 204, 204, 255))

    def test_a_frame_fixes_the_canvas_whatever_else_is_drawn(self):
        _, small = bake.cubes_of(self.geo)
        big_geo = write_geo([{"origin": [-6, 0, -2], "size": [12, 4, 4], "uv": [0, 0]}], texture=16)
        try:
            _, big = bake.cubes_of(big_geo)
            sheet = pngio.read_png(self.sheet)
            framed = render.render_view(big, sheet, 16, 16, *render.view_angles("front"), 8, frame=small)
            alone = render.render_view(small, sheet, 16, 16, *render.view_angles("front"), 8)
            self.assertEqual(framed[:2], alone[:2])
        finally:
            remove(big_geo)

    def test_unknown_view_is_a_clear_error(self):
        with self.assertRaises(SystemExit):
            render.view_angles("behind")


if __name__ == "__main__":
    unittest.main()
