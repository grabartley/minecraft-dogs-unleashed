import unittest

import layer

BASE = {"animation_length": 1.0, "loop": True, "bones": {
    "tail": {"rotation": {"0.0": [0, 0, 0], "0.5": [0, 20, 0], "1.0": [0, 0, 0]}},
    "leg": {"rotation": [10, 0, 0], "position": [0, -1, 0]},
    "head": {"rotation": "math.sin(query.anim_time * 90) * 2"}}}


class Layer(unittest.TestCase):
    def test_a_clip_inherits_exactly_the_base_channels_it_leaves_unkeyed(self):
        for clip_bones, expected in (
                ({}, [("head", "rotation"), ("leg", "position"), ("leg", "rotation"), ("tail", "rotation")]),
                ({"leg": {"rotation": [0, 0, 0]}}, [("head", "rotation"), ("leg", "position"), ("tail", "rotation")]),
                ({"leg": {"rotation": [0, 0, 0], "position": [0, 0, 0]}, "tail": {"rotation": [0, 0, 0]}, "head": {"rotation": [0, 0, 0]}}, []),
                ({"leg": {"scale": [1, 1, 1]}}, [("head", "rotation"), ("leg", "position"), ("leg", "rotation"), ("tail", "rotation")])):
            self.assertEqual(layer.inherited({"bones": clip_bones}, BASE), expected)

    def test_layered_keeps_the_clips_own_channels_and_adds_the_base_ones(self):
        clip = {"animation_length": 2.0, "bones": {"leg": {"rotation": {"0.0": [0, 0, 0], "2.0": [90, 0, 0]}}}}
        composite = layer.layered(clip, BASE)
        self.assertEqual(composite["animation_length"], 2.0)
        self.assertEqual(composite["bones"]["leg"]["rotation"], clip["bones"]["leg"]["rotation"])
        self.assertEqual(composite["bones"]["leg"]["position"], [0, -1, 0])
        self.assertEqual(composite["bones"]["head"]["rotation"], BASE["bones"]["head"]["rotation"])
        self.assertNotIn("tail", clip["bones"])

    def test_a_looping_base_track_repeats_across_the_whole_clip(self):
        for length, times in ((1.0, ["0", "0.5", "1"]), (2.0, ["0", "0.5", "1", "1.5", "2"]), (1.2, ["0", "0.5", "1"])):
            tiled = layer.tile(BASE["bones"]["tail"]["rotation"], 1.0, length)
            self.assertEqual(list(tiled), times)
            self.assertEqual(tiled["0.5"], [0, 20, 0])

    def test_constant_and_molang_channels_are_copied_as_they_are(self):
        for channel in ([1, 2, 3], "math.sin(query.anim_time)", 5):
            self.assertEqual(layer.tile(channel, 1.0, 3.0), channel)

    def test_clip_length_falls_back_to_the_last_key(self):
        for clip, length in (({"animation_length": 2.4, "bones": {}}, 2.4), ({"bones": {"a": {"rotation": {"0": [0, 0, 0], "1.5": [0, 0, 0]}}}}, 1.5), ({"bones": {}}, 0.0)):
            self.assertEqual(layer.clip_length(clip), length)

    def test_composites_are_named_clip_at_base(self):
        animations = {"sit": BASE, "idle": BASE, "beg": {"animation_length": 2.0, "bones": {}}}
        self.assertEqual(list(layer.composites(animations, ["beg"], ["sit", "idle"])), ["beg@sit", "beg@idle"])


if __name__ == "__main__":
    unittest.main()
