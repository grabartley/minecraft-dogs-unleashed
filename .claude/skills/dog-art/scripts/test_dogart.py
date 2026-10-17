import json
import os
import tempfile
import unittest

import dogart


def geo_with(cubes, bones=None):
    bone = {"name": "b", "pivot": [0, 0, 0], "cubes": cubes}
    bone.update(bones or {})
    data = {"format_version": "1.12.0", "minecraft:geometry": [{"description": {"identifier": "geometry.t", "texture_width": 64, "texture_height": 64}, "bones": [bone]}]}
    handle = tempfile.NamedTemporaryFile("w", suffix=".geo.json", delete=False)
    json.dump(data, handle)
    handle.close()
    return handle.name


def patches_of(cube):
    path = geo_with([cube])
    try:
        _, _, cubes = dogart.cubes_of(path)
    finally:
        os.unlink(path)
    return {face: patch for face, _, patch in cubes[0]["faces"]}, cubes[0]


class BoxUvFollowsGeckoLib(unittest.TestCase):
    def test_plain_cube_patches_match_baked_model_factory(self):
        patches, _ = patches_of({"origin": [1, 2, 3], "size": [4, 5, 6], "uv": [10, 20]})
        self.assertEqual(patches["east"], (10, 26, 6, 5))
        self.assertEqual(patches["north"], (16, 26, 4, 5))
        self.assertEqual(patches["west"], (20, 26, 6, 5))
        self.assertEqual(patches["south"], (26, 26, 4, 5))
        self.assertEqual(patches["up"], (16, 20, 4, 6))
        self.assertEqual(patches["down"], (20, 20, 4, 6))

    def test_mirrored_cube_swaps_the_side_patches(self):
        patches, cube = patches_of({"origin": [1, 2, 3], "size": [4, 5, 6], "uv": [10, 20], "mirror": True})
        self.assertEqual(patches["west"], (10, 26, 6, 5))
        self.assertEqual(patches["east"], (20, 26, 6, 5))
        self.assertTrue(cube["mirror"])

    def test_fractional_sizes_are_floored_for_uv_only(self):
        patches, cube = patches_of({"origin": [0, 0, 0], "size": [2.5, 4, 1.5], "uv": [0, 0]})
        self.assertEqual(patches["east"], (0, 1, 1, 4))
        self.assertEqual(patches["north"], (1, 1, 2, 4))
        self.assertEqual(patches["west"], (3, 1, 1, 4))
        self.assertEqual(cube["visual"], (2.5, 4, 1.5))


class GeometryFollowsGeckoLib(unittest.TestCase):
    def test_x_is_mirrored_and_inflate_grows_every_side(self):
        _, cube = patches_of({"origin": [1, 2, 3], "size": [4, 5, 6], "uv": [0, 0], "inflate": 0.5})
        north = dict((f, c) for f, c, _ in cube["faces"])["north"]
        xs = sorted({round(p[0], 6) for p in north})
        ys = sorted({round(p[1], 6) for p in north})
        self.assertEqual(xs, [-5.5, -0.5])
        self.assertEqual(ys, [1.5, 7.5])
        self.assertTrue(all(abs(p[2] - 2.5) < 1e-9 for p in north))

    def test_face_normals_point_outward(self):
        _, cube = patches_of({"origin": [0, 0, 0], "size": [4, 4, 4], "uv": [0, 0]})
        for face, corners, _ in cube["faces"]:
            normal = dogart.normalize(dogart.cross(dogart.sub(corners[3], corners[0]), dogart.sub(corners[1], corners[0])))
            expected = {"up": (0, 1, 0), "down": (0, -1, 0), "north": (0, 0, -1), "south": (0, 0, 1), "east": (1, 0, 0), "west": (-1, 0, 0)}[face]
            self.assertEqual(tuple(round(n) for n in normal), expected, face)

    def test_bone_rotation_negates_x_and_y_like_the_bake(self):
        path = geo_with([{"origin": [0, 0, 0], "size": [2, 2, 2], "uv": [0, 0]}], {"pivot": [0, 0, 0], "rotation": [90, 0, 0]})
        try:
            _, _, cubes = dogart.cubes_of(path)
        finally:
            os.unlink(path)
        up = dict((f, c) for f, c, _ in cubes[0]["faces"])["up"]
        self.assertTrue(all(p[2] < -1.9 for p in up), "a +90 x rotation in the file tips the top face toward -z, as the husky neck shows")

    def test_down_face_runs_front_to_back_on_the_sheet(self):
        _, cube = patches_of({"origin": [0, 0, 0], "size": [2, 2, 2], "uv": [0, 0]})
        down = dict((f, c) for f, c, _ in cube["faces"])["down"]
        self.assertGreater(down[0][2], down[1][2], "the first down corner sits at the back and maps to the top of the patch")


if __name__ == "__main__":
    unittest.main()
