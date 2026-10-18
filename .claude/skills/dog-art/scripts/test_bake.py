import json
import unittest

import bake
from testutil import remove, temp_path, write_geo


def bake_one(cube, bone=None, profile=None, hidden=(), poses=None):
    path = write_geo([cube], bone)
    try:
        _, cubes = bake.cubes_of(path, profile, hidden, poses)
    finally:
        remove(path)
    return cubes


def faces_of(cube):
    return {face: (corners, patch) for face, corners, patch in cube["faces"]}


class BoxUvFollowsGeckoLib(unittest.TestCase):
    def test_patch_rectangles_match_baked_model_factory(self):
        cases = {
            "plain": ({"origin": [1, 2, 3], "size": [4, 5, 6], "uv": [10, 20]},
                      {"east": (10, 26, 6, 5), "north": (16, 26, 4, 5), "west": (20, 26, 6, 5), "south": (26, 26, 4, 5), "up": (16, 20, 4, 6), "down": (20, 20, 4, 6)}),
            "mirrored": ({"origin": [1, 2, 3], "size": [4, 5, 6], "uv": [10, 20], "mirror": True},
                         {"west": (10, 26, 6, 5), "north": (16, 26, 4, 5), "east": (20, 26, 6, 5), "south": (26, 26, 4, 5), "up": (16, 20, 4, 6), "down": (20, 20, 4, 6)}),
            "fractional": ({"origin": [0, 0, 0], "size": [2.5, 4, 1.5], "uv": [0, 0]},
                           {"east": (0, 1, 1, 4), "north": (1, 1, 2, 4), "west": (3, 1, 1, 4), "south": (4, 1, 2, 4), "up": (1, 0, 2, 1), "down": (3, 0, 2, 1)}),
        }
        for name, (cube, expected) in cases.items():
            with self.subTest(name):
                faces = faces_of(bake_one(cube)[0])
                self.assertEqual({face: patch for face, (_, patch) in faces.items()}, expected)

    def test_per_corner_uvs_match_geoquad_build(self):
        u, v, x, y, z = 10, 20, 4, 5, 6
        cube = {"origin": [0, 0, 0], "size": [x, y, z], "uv": [u, v]}
        xmin, xmax = -x, 0
        expected = {
            (False, "north", (xmin, y, 0)): (u + z + x, v + z),
            (False, "north", (xmax, 0, 0)): (u + z, v + z + y),
            (False, "west", (xmin, y, z)): (u + 2 * z + x, v + z),
            (False, "east", (xmax, y, 0)): (u + z, v + z),
            (False, "up", (xmin, y, z)): (u + z + x, v),
            (False, "up", (xmax, y, 0)): (u + z, v + z),
            (False, "down", (xmax, 0, 0)): (u + z + x, v + z),
            (False, "down", (xmin, 0, z)): (u + z + 2 * x, v),
            (True, "north", (xmin, y, 0)): (u + z, v + z),
            (True, "down", (xmax, 0, 0)): (u + z + 2 * x, v + z),
            (True, "east", (xmax, y, 0)): (u + z + x, v + z),
        }
        for (mirror, face, model_point), uv in expected.items():
            with self.subTest(mirror=mirror, face=face, corner=model_point):
                cube["mirror"] = mirror
                corners, patch = faces_of(bake_one(cube)[0])[face]
                index = [tuple(round(c, 6) for c in corner) for corner in corners].index(model_point)
                self.assertEqual(bake.face_uvs(face, patch, mirror)[index], uv)

    def test_per_face_uv_is_refused(self):
        with self.assertRaises(SystemExit):
            bake_one({"origin": [0, 0, 0], "size": [1, 1, 1], "uv": {"north": {"uv": [0, 0]}}})


class GeometryFollowsGeckoLib(unittest.TestCase):
    def test_x_is_mirrored_and_inflate_grows_every_side(self):
        corners, _ = faces_of(bake_one({"origin": [1, 2, 3], "size": [4, 5, 6], "uv": [0, 0], "inflate": 0.5})[0])["north"]
        self.assertEqual(sorted({round(p[0], 6) for p in corners}), [-5.5, -0.5])
        self.assertEqual(sorted({round(p[1], 6) for p in corners}), [1.5, 7.5])
        self.assertTrue(all(abs(p[2] - 2.5) < 1e-9 for p in corners))

    def test_bone_inflate_is_the_fallback(self):
        cube = bake_one({"origin": [0, 0, 0], "size": [2, 2, 2], "uv": [0, 0]}, {"inflate": 1})[0]
        self.assertEqual(cube["visual"], (4, 4, 4))

    def test_bone_rotation_negates_x_like_the_bake(self):
        cube = bake_one({"origin": [0, 0, 0], "size": [2, 2, 2], "uv": [0, 0]}, {"pivot": [0, 0, 0], "rotation": [90, 0, 0]})[0]
        corners, _ = faces_of(cube)["up"]
        self.assertTrue(all(p[2] < -1.9 for p in corners), "a +90 x rotation in the file tips the top face toward -z, as the husky neck shows")

    def test_rotated_cube_without_pivot_turns_about_the_origin(self):
        cube = bake_one({"origin": [2, 0, 0], "size": [2, 2, 2], "uv": [0, 0], "rotation": [0, 0, 90]})[0]
        corners, _ = faces_of(cube)["up"]
        self.assertTrue(all(abs(p[0] + 2) < 1e-6 and p[1] < -1.9 for p in corners))

    def test_pupil_texels_are_not_bones(self):
        profile = {"head": ["skull"], "eyes": ["lid"], "pupils": [[7, 47], [10, 47]]}
        self.assertEqual(bake.slots_by_bone(profile), {"skull": "head", "lid": "eyes"})
        self.assertEqual(bake.pupils(profile), [(7, 47), (10, 47)])
        self.assertEqual(bake.pupils({}), [])

    def test_eyes_and_fur_bones_hide_like_the_client(self):
        profile = {"eyes": ["lid"], "fur_shaggy": ["tuft"], "fur_overgrown": ["mane"]}
        self.assertEqual(bake.hidden_bones(profile), {"lid", "tuft", "mane"})
        self.assertEqual(bake.hidden_bones(profile, asleep=True, fur_stage="shaggy"), {"mane"})
        self.assertEqual(bake.hidden_bones(profile, asleep=True, fur_stage="overgrown"), set())
        self.assertEqual(bake_one({"origin": [0, 0, 0], "size": [1, 1, 1], "uv": [0, 0]}, hidden={"b"}), [])

    def test_a_pose_scales_about_the_pivot_then_moves_in_the_parent_frame(self):
        cube = bake_one({"origin": [0, 0, 0], "size": [2, 2, 2], "uv": [0, 0]}, {"pivot": [1, 1, 1]},
                        poses={"b": {"scale": [2, 2, 2], "position": [1, 0.5, 0]}})[0]
        corners = [c for _, face, _ in cube["faces"] for c in face]
        lo = tuple(round(min(c[i] for c in corners), 6) for i in range(3))
        hi = tuple(round(max(c[i] for c in corners), 6) for i in range(3))
        self.assertEqual((lo, hi), ((-4, -0.5, -1), (0, 3.5, 3)))

    def test_closed_eyes_are_the_poses_the_sleep_clip_holds(self):
        path = temp_path(".animation.json")
        with open(path, "w") as handle:
            json.dump({"animations": {"sleep": {"bones": {
                "lid": {"position": {"0.0": {"vector": [0, 0, 0]}, "1.0": {"vector": [0, 0, -0.5]}}, "scale": [2, 2, 2]},
                "tail": {"position": [0, 1, 0]}}}}}, handle)
        try:
            self.assertEqual(bake.closed_eyes({"eyes": ["lid"]}, path),
                             {"lid": {"position": [0, 0, -0.5], "scale": [2, 2, 2]}})
            self.assertEqual(bake.closed_eyes({"eyes": ["lid"]}, path, clip="idle"), {})
            self.assertEqual(bake.closed_eyes({"eyes": ["lid"]}, None), {})
        finally:
            remove(path)

    def test_density_uses_floored_sizes_and_the_thinnest_axis(self):
        cube = bake_one({"origin": [0, 0, 0], "size": [5.8, 4, 5], "uv": [0, 0]})[0]
        self.assertAlmostEqual(bake.texel_density(cube, 1), 5 / 5.8)


if __name__ == "__main__":
    unittest.main()
