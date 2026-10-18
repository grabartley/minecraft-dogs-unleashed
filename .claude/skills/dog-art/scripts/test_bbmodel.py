import json
import unittest

import bbmodel
from testutil import remove, temp_path, write_geo, write_sheet


class BlockbenchProject(unittest.TestCase):
    def test_faces_match_what_the_bbmodel_mcp_lays_out(self):
        plain = bbmodel.blockbench_faces({"size": [6, 4, 4], "uv": [22, 8]}, False)
        self.assertEqual({f: v["uv"] for f, v in plain.items()},
                         {"north": [26, 12, 32, 16], "east": [22, 12, 26, 16], "south": [36, 12, 42, 16], "west": [32, 12, 36, 16], "up": [32, 12, 26, 8], "down": [38, 8, 32, 12]})
        mirrored = bbmodel.blockbench_faces({"size": [5, 5, 5], "uv": [65, 84]}, True)
        self.assertEqual({f: v["uv"] for f, v in mirrored.items()},
                         {"north": [75, 89, 70, 94], "east": [80, 89, 75, 94], "south": [85, 89, 80, 94], "west": [70, 89, 65, 94], "up": [70, 89, 75, 84], "down": [75, 84, 80, 89]})

    def test_project_mirrors_x_and_negates_x_and_y_angles(self):
        geo = write_geo([{"origin": [1, 2, 3], "size": [4, 5, 6], "uv": [0, 0], "rotation": [10, 20, 30]}], {"pivot": [1, 2, 3], "rotation": [-32.5, 0, 0]})
        sheet = write_sheet(16, 16)
        try:
            project = bbmodel.project_from_geo(geo, sheet, "t")
        finally:
            remove(geo, sheet)
        element, group = project["elements"][0], project["groups"][0]
        self.assertEqual((element["from"], element["to"]), ([-5, 2, 3], [-1, 7, 9]))
        self.assertEqual((element["rotation"], element["origin"]), ([-10, -20, 30], [0, 0, 0]))
        self.assertEqual((group["origin"], group["rotation"]), ([-1, 2, 3], [32.5, 0, 0]))
        self.assertEqual(project["outliner"][0]["children"], [element["uuid"]])
        self.assertTrue(project["textures"][0]["source"].startswith("data:image/png;base64,"))


class GeoCompare(unittest.TestCase):
    def test_equal_rigs_have_no_mismatches_and_a_moved_cube_is_named(self):
        a = write_geo([{"origin": [1, 2, 3], "size": [4, 5, 6], "uv": [0, 0]}], {"inflate": 0.5})
        b = write_geo([{"origin": [1, 2, 3], "size": [4, 5, 6], "uv": [0, 0], "inflate": 0.5}])
        c = write_geo([{"origin": [1, 2, 3.5], "size": [4, 5, 6], "uv": [0, 1]}])
        try:
            self.assertEqual(bbmodel.compare_geo(a, b), [])
            mismatches = bbmodel.compare_geo(a, c)
        finally:
            remove(a, b, c)
        self.assertEqual(len(mismatches), 3)
        self.assertTrue(any("b[0].origin" in m for m in mismatches))
        self.assertTrue(any("b[0].uv" in m for m in mismatches))
        self.assertTrue(any("b[0].inflate" in m for m in mismatches))

    def test_a_size_just_under_a_whole_number_is_a_box_uv_change(self):
        a = write_geo([{"origin": [0, 0, 0], "size": [8, 7, 8], "uv": [0, 0]}])
        b = write_geo([{"origin": [0, 0, 0], "size": [8, 7, 7.999999999999999], "uv": [0, 0]}])
        try:
            mismatches = bbmodel.compare_geo(a, b)
        finally:
            remove(a, b)
        self.assertEqual(mismatches, ["b[0].box_uv_size [8, 7, 8] vs [8, 7, 7]"])


class TidyGeo(unittest.TestCase):
    def test_float_noise_from_an_export_is_rounded_away(self):
        noisy = write_geo([{"origin": [-4.270000000000001, 0.20000000000000018, 3.4000000000000004],
                            "size": [8, 7, 7.999999999999999], "inflate": -3.27, "uv": [149, 217]}])
        out = temp_path(".geo.json")
        try:
            bbmodel.tidy_geo(noisy, out)
            with open(out) as handle:
                cube = json.load(handle)["minecraft:geometry"][0]["bones"][0]["cubes"][0]
        finally:
            remove(noisy, out)
        self.assertEqual(cube["size"], [8, 7, 8])
        self.assertEqual(cube["origin"], [-4.27, 0.2, 3.4])
        self.assertEqual(cube["inflate"], -3.27)


if __name__ == "__main__":
    unittest.main()
