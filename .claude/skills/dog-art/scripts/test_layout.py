import glob
import json
import os
import unittest

import layout

ASSETS = os.path.join(os.path.dirname(__file__), "..", "..", "..", "..", "src", "client", "resources", "assets", "dogs-unleashed")


class Layout(unittest.TestCase):
    def test_each_cube_sits_on_one_line_with_inline_numbers(self):
        geo = {"format_version": "1.12.0", "minecraft:geometry": [{"description": {"identifier": "geometry.t", "visible_bounds_offset": [0, 0.25, 0]},
               "bones": [{"name": "head", "pivot": [0, 5.9, -4.1], "cubes": [{"origin": [-1, 4.9, -4.6], "size": [2, 2, 1], "inflate": -0.5, "uv": [1, 71]}]}]}]}
        text = layout.geo_text(geo)
        self.assertIn('\t\t\t\t\t\t{"origin": [-1, 4.9, -4.6], "size": [2, 2, 1], "inflate": -0.5, "uv": [1, 71]}\n', text)
        self.assertIn('"visible_bounds_offset": [0, 0.25, 0]', text)
        self.assertEqual(json.loads(text), geo)

    def test_every_shipped_geo_and_animation_file_keeps_its_exact_layout(self):
        rigs = [os.path.basename(p)[:-len(".json")] for p in glob.glob(os.path.join(ASSETS, "rig_profiles", "*.json"))]
        self.assertTrue(rigs)
        for folder, suffix, writer in (("geo", ".geo.json", layout.geo_text), ("animations", ".animation.json", layout.animation_text)):
            for path in sorted(os.path.join(ASSETS, folder, rig + suffix) for rig in rigs):
                with open(path) as handle:
                    raw = handle.read()
                self.assertEqual(layout.text_like(path, json.loads(raw), writer), raw, path)

    def test_splicing_replaces_only_the_named_bones(self):
        shipped = {"animations": {"idle": {"bones": {"tail": {"rotation": [1, 0, 0]}, "eyelids": {"position": [0, 1, 0]}}},
                                  "walk": {"bones": {"tail": {"rotation": [2, 0, 0]}}}}}
        exported = {"animations": {"idle": {"bones": {"tail": {"rotation": [9, 9, 9]}, "eyelid_left": {"position": [1, 0, 0]}}},
                                   "walk": {"bones": {"tail": {"rotation": [9, 9, 9]}}}}}
        spliced = layout.splice_tracks(shipped, exported, ["eyelid_left"], ["eyelids"])
        self.assertEqual(spliced["animations"]["idle"]["bones"], {"tail": {"rotation": [1, 0, 0]}, "eyelid_left": {"position": [1, 0, 0]}})
        self.assertEqual(spliced["animations"]["walk"]["bones"], {"tail": {"rotation": [2, 0, 0]}})

    def test_splicing_limited_to_clips_keeps_other_clips_and_bone_order(self):
        for clips, walk_tail in ((["idle"], [2, 0, 0]), (["idle", "walk"], [9, 9, 9])):
            shipped = {"animations": {"idle": {"bones": {"tail": {"rotation": [1, 0, 0]}, "head": {"rotation": [3, 0, 0]}}},
                                      "walk": {"bones": {"tail": {"rotation": [2, 0, 0]}}}}}
            exported = {"animations": {"idle": {"bones": {"tail": {"rotation": [9, 9, 9]}}},
                                       "walk": {"bones": {"tail": {"rotation": [9, 9, 9]}}}}}
            spliced = layout.splice_tracks(shipped, exported, ["tail"], clips=clips)
            self.assertEqual(list(spliced["animations"]["idle"]["bones"]), ["tail", "head"])
            self.assertEqual(spliced["animations"]["idle"]["bones"]["tail"], {"rotation": [9, 9, 9]})
            self.assertEqual(spliced["animations"]["walk"]["bones"]["tail"], {"rotation": walk_tail})

    def test_dropping_with_clips_drops_only_inside_those_clips(self):
        shipped = {"animations": {"idle": {"bones": {"old": {"rotation": [1, 0, 0]}}},
                                  "walk": {"bones": {"old": {"rotation": [2, 0, 0]}}}}}
        exported = {"animations": {"idle": {"bones": {}}, "walk": {"bones": {}}}}
        spliced = layout.splice_tracks(shipped, exported, ["tail"], dropped=["old"], clips=["idle"])
        self.assertEqual(spliced["animations"]["idle"]["bones"], {})
        self.assertEqual(spliced["animations"]["walk"]["bones"], {"old": {"rotation": [2, 0, 0]}})

    def test_a_clip_missing_from_either_file_is_refused(self):
        for shipped_clips, exported_clips in ((["idle"], ["idle", "beg"]), (["idle", "beg"], ["idle"])):
            shipped = {"animations": {name: {"bones": {}} for name in shipped_clips}}
            exported = {"animations": {name: {"bones": {}} for name in exported_clips}}
            with self.assertRaisesRegex(ValueError, "beg"):
                layout.splice_tracks(shipped, exported, ["tail"], clips=["idle", "beg"])


if __name__ == "__main__":
    unittest.main()
