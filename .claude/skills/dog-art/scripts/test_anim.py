import unittest

import anim


def key(channel, time, value, interpolation="linear"):
    return {"channel": channel, "time": time, "interpolation": interpolation,
            "data_points": [{"x": str(value[0]), "y": str(value[1]), "z": str(value[2])}]}


def project(keyframes, version="5.0", loop="loop", length=1.0):
    return {"meta": {"format_version": version},
            "animations": [{"name": "clip", "loop": loop, "length": length,
                            "animators": {"id": {"name": "tail", "type": "bone", "keyframes": keyframes}}}]}


class AnimationExport(unittest.TestCase):
    def test_v5_projects_flip_rotation_x_y_and_position_x(self):
        out = anim.export_animations(project([key("rotation", 0, [10, 20, 30]), key("position", 0, [1, 2, 3]),
                                              key("scale", 0, [1.1, 1.2, 1.3])]))
        tail = out["animations"]["clip"]["bones"]["tail"]
        self.assertEqual(tail["rotation"]["0.0"]["vector"], [-10, -20, 30])
        self.assertEqual(tail["position"]["0.0"]["vector"], [-1, 2, 3])
        self.assertEqual(tail["scale"]["0.0"]["vector"], [1.1, 1.2, 1.3])

    def test_flipped_zeros_are_written_without_a_sign(self):
        for channel in ("rotation", "position"):
            vector = anim.export_animations(project([key(channel, 0, [0, 0, 0])]))["animations"]["clip"]["bones"]["tail"][channel]["0.0"]["vector"]
            self.assertEqual([str(v) for v in vector], ["0.0", "0.0", "0.0"])

    def test_legacy_projects_export_as_stored(self):
        out = anim.export_animations(project([key("rotation", 0, [10, 20, 30])], version="4.10"))
        self.assertEqual(out["animations"]["clip"]["bones"]["tail"]["rotation"]["0.0"]["vector"], [10, 20, 30])

    def test_loop_modes_map_to_geckolib(self):
        frames = [key("rotation", 0, [0, 0, 0])]
        self.assertIs(anim.export_animations(project(frames, loop="loop"))["animations"]["clip"]["loop"], True)
        self.assertEqual(anim.export_animations(project(frames, loop="hold"))["animations"]["clip"]["loop"], "hold_on_last_frame")
        self.assertNotIn("loop", anim.export_animations(project(frames, loop="once"))["animations"]["clip"])
        self.assertEqual(anim.export_animations(project(frames, length=2.5))["animations"]["clip"]["animation_length"], 2.5)

    def test_linear_keys_are_written_unchanged(self):
        out = anim.export_animations(project([key("rotation", 0, [0, 0, 0]), key("rotation", 0.5, [0, 0, 40])]))
        self.assertEqual(sorted(out["animations"]["clip"]["bones"]["tail"]["rotation"]), ["0.0", "0.5"])

    def test_catmullrom_is_baked_through_every_key_at_the_step(self):
        frames = [key("rotation", t, [0, 0, z], "catmullrom") for t, z in ((0, 0), (0.25, 10), (0.5, 0), (0.75, -10), (1.0, 0))]
        rotation = anim.export_animations(project(frames), step=0.05, tolerance=0)["animations"]["clip"]["bones"]["tail"]["rotation"]
        self.assertEqual(len(rotation), 21)
        self.assertEqual(rotation["0.25"]["vector"], [0, 0, 10])
        self.assertEqual(rotation["0.75"]["vector"], [0, 0, -10])
        self.assertAlmostEqual(rotation["0.35"]["vector"][2], 7.44, places=3)
        self.assertGreater(rotation["0.2"]["vector"][2], 10 * 0.2 / 0.25)

    def test_simplify_keeps_every_baked_sample_within_tolerance(self):
        frames = [key("rotation", t, [0, 0, z], "catmullrom") for t, z in ((0, 0), (0.25, 10), (0.5, 0), (0.75, -10), (1.0, 0))]
        dense = anim.export_animations(project(frames), tolerance=0)["animations"]["clip"]["bones"]["tail"]["rotation"]
        sparse = anim.export_animations(project(frames), tolerance=0.2)["animations"]["clip"]["bones"]["tail"]["rotation"]
        self.assertLess(len(sparse), len(dense))
        keys = sorted((float(t), v["vector"]) for t, v in sparse.items())
        for time, value in ((float(t), v["vector"]) for t, v in dense.items()):
            rebuilt = anim.sample([(t, v, "linear") for t, v in keys], time)
            self.assertLessEqual(abs(rebuilt[2] - value[2]), 0.2 + 1e-9)

    def test_simplify_collapses_a_straight_line(self):
        line = [(t / 10, [t, 0, 0]) for t in range(11)]
        self.assertEqual(anim.simplify(line, 0.01), [line[0], line[-1]])

    def test_catmull_rom_passes_through_its_middle_points(self):
        self.assertAlmostEqual(anim.catmull_rom(0, 3, 7, 9, 0.0), 3)
        self.assertAlmostEqual(anim.catmull_rom(0, 3, 7, 9, 1.0), 7)
        self.assertAlmostEqual(anim.catmull_rom(0, 1, 2, 3, 0.5), 1.5)


class AnimationImport(unittest.TestCase):
    def geckolib(self):
        return {"animations": {
            "idle": {"loop": True, "animation_length": 2.0, "bones": {
                "tail": {"rotation": {"0.0": {"vector": [10, 20, 30]}, "1.0": [0, 0, 5]},
                         "position": {"vector": [1, 2, 3]},
                         "scale": {"0.5": {"post": [1.1, 1.2, 1.3], "lerp_mode": "catmullrom"}}},
                "missing": {"position": [0, 1, 0]}}},
            "sleep": {"loop": "hold_on_last_frame", "bones": {}}}}

    def target(self, version="5.0"):
        return {"meta": {"format_version": version}, "groups": [{"name": "tail", "uuid": "id"}]}

    def test_round_trips_through_export(self):
        target = self.target()
        anim.import_animations(target, self.geckolib())
        tail = anim.export_animations(target)["animations"]["idle"]["bones"]["tail"]
        self.assertEqual(tail["rotation"], {"0.0": {"vector": [10, 20, 30]}, "1.0": {"vector": [0, 0, 5]}})
        self.assertEqual(tail["position"], {"0.0": {"vector": [1, 2, 3]}})

    def test_v5_projects_store_rotation_x_y_and_position_x_flipped(self):
        target = self.target()
        anim.import_animations(target, self.geckolib())
        keys = target["animations"][0]["animators"]["id"]["keyframes"]
        stored = {(k["channel"], k["time"]): k["data_points"][0] for k in keys}
        self.assertEqual(stored[("rotation", 0.0)], {"x": "-10", "y": "-20", "z": "30"})
        self.assertEqual(stored[("position", 0.0)], {"x": "-1", "y": "2", "z": "3"})
        self.assertEqual(stored[("scale", 0.5)], {"x": "1.1", "y": "1.2", "z": "1.3"})

    def test_keeps_catmullrom_and_loop_modes(self):
        target = self.target()
        anim.import_animations(target, self.geckolib())
        idle, sleep = target["animations"]
        scale = [k for k in idle["animators"]["id"]["keyframes"] if k["channel"] == "scale"]
        self.assertEqual(scale[0]["interpolation"], "catmullrom")
        self.assertEqual(anim.import_animations(self.target(), self.geckolib())[1], ["idle tail"])
        self.assertEqual((idle["loop"], idle["length"]), ("loop", 2.0))
        self.assertEqual(sleep["loop"], "hold")

    def test_reports_bones_the_project_lacks(self):
        self.assertEqual(anim.import_animations(self.target(), self.geckolib())[0], ["missing"])

    def test_keeps_existing_clips_unless_told_to_replace_them(self):
        target = self.target()
        target["animations"] = [{"name": "walk"}]
        with self.assertRaises(ValueError):
            anim.import_animations(target, self.geckolib())
        self.assertEqual(target["animations"], [{"name": "walk"}])
        anim.import_animations(target, self.geckolib(), replace=True)
        self.assertEqual([clip["name"] for clip in target["animations"]], ["idle", "sleep"])


if __name__ == "__main__":
    unittest.main()
