import glob
import math
import os
import unittest

import bake
import suit
from testutil import remove, write_geo, write_sheet

ASSETS = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "..", "..", "src", "client", "resources", "assets", "dogs-unleashed")
RIGS = sorted(os.path.basename(p)[:-len(".json")] for p in glob.glob(os.path.join(ASSETS, "rig_profiles", "*.json")))


def shipped(rig):
    geo = os.path.join(ASSETS, "geo", f"{rig}.geo.json")
    return geo, bake.load_profile(os.path.join(ASSETS, "rig_profiles", f"{rig}.json"))


def rects(regions):
    return sorted((x, y, w, h) for _, x, y, w, h in regions)


def label(slot, face):
    return (suit.SLOTS.index(slot), suit.FACES.index(face), 1, 255)


def labelled_sheet():
    pixels = [[(0, 0, 0, 0)] * suit.WIDTH for _ in range(suit.HEIGHT)]
    for slot in suit.SLOTS:
        for face in suit.FACES:
            cx, cy, cw, ch = suit.cell_of(slot, face)
            for y in range(cy, cy + ch):
                for x in range(cx, cx + cw):
                    pixels[y][x] = label(slot, face)
    return suit.WIDTH, suit.HEIGHT, pixels


def overlap(a, b):
    ax, ay, aw, ah = a
    bx, by, bw, bh = b
    return ax < bx + bw and bx < ax + aw and ay < by + bh and by < ay + ah


def box_uv_faces(geo, bone_names):
    description, _, bones = bake.load_geo(geo)
    atlas_w, atlas_h = description["texture_width"], description["texture_height"]
    faces = []
    for name in bone_names:
        for cube in bones.get(name, {}).get("cubes", []):
            w, h, d = (math.floor(s) for s in cube["size"])
            u, v = cube["uv"]
            for fu, fv, fw, fh in ((u + d, v + d, w, h), (u + d + w + d, v + d, w, h), (u, v + d, d, h),
                                   (u + d + w, v + d, d, h), (u + d, v, w, d), (u + d + w, v, w, d)):
                x, y = max(0, int(fu)), max(0, int(fv))
                right, bottom = min(atlas_w - 1, int(fu + fw) - 1), min(atlas_h - 1, int(fv + fh) - 1)
                if right >= x and bottom >= y:
                    faces.append((x, y, right - x + 1, bottom - y + 1))
    return sorted(faces)


class SheetMatchesOverlaySheet(unittest.TestCase):
    def test_sheet_size(self):
        self.assertEqual((suit.WIDTH, suit.HEIGHT), (192, 144))

    def test_cells_match_overlay_sheet_test(self):
        for slot, face, x, y in (("head", "down", 96, 0), ("head", "up", 112, 0), ("head", "north", 128, 0),
                                 ("head", "south", 96, 24), ("head", "west", 112, 24), ("head", "east", 128, 24),
                                 ("torso", "down", 0, 0), ("leg_front_left", "down", 0, 96), ("leg_back_right", "east", 176, 120)):
            with self.subTest(slot=slot, face=face):
                self.assertEqual(suit.cell_of(slot, face), (x, y, 16, 24))

    def test_fur_has_no_cell(self):
        for slot in ("fur_shaggy", "fur_overgrown"):
            with self.subTest(slot=slot):
                self.assertIsNone(suit.cell_of(slot, "north"))

    def test_cells_sit_in_their_square_and_never_overlap(self):
        cells = [suit.cell_of(slot, face) for slot in suit.SLOTS for face in suit.FACES]
        for index, (x, y, w, h) in enumerate(cells):
            square = index // len(suit.FACES)
            sx, sy = (square % 4) * 48, (square // 4) * 48
            self.assertTrue(sx <= x and x + w <= sx + 48 and sy <= y and y + h <= sy + 48)
            for other in cells[index + 1:]:
                self.assertFalse(overlap((x, y, w, h), other))


class FacingMatchesModelFacing(unittest.TestCase):
    def test_no_turn_keeps_every_face(self):
        for face in suit.FACES:
            for degrees in (0.0, 360.0):
                with self.subTest(face=face, degrees=degrees):
                    self.assertEqual(suit.turned_by(degrees)(face), face)

    def test_turns_match_model_facing_test(self):
        for face, degrees, expected in (("north", 180, "south"), ("south", 180, "north"), ("east", 180, "west"),
                                        ("west", 180, "east"), ("up", 180, "up"), ("down", 180, "down"),
                                        ("east", 90, "north"), ("north", 90, "west"), ("north", -90, "east"),
                                        ("north", 270, "east"), ("north", 89, "west")):
            with self.subTest(face=face, degrees=degrees):
                self.assertEqual(suit.turned_by(degrees)(face), expected)

    def test_facing_breaks_ties_in_direction_order_like_get_facing(self):
        for normal, expected in (((0, 0, 0), "north"), ((1, 1, 0), "up"), ((0, -1, -1), "down"),
                                 ((-1, 0, 1), "south"), ((0.2, 0, -0.9), "north"), ((-0.9, 0.1, 0), "west")):
            with self.subTest(normal=normal):
                self.assertEqual(suit.facing(normal), expected)


class RegionsMatchSlotRegions(unittest.TestCase):
    def test_box_uv_patch_directions_follow_geckolib_quad_normals(self):
        for mirror, expected in ((False, {"east": (10, 26), "west": (20, 26)}), (True, {"west": (10, 26), "east": (20, 26)})):
            cube = {"origin": [1, 2, 3], "size": [4, 5, 6], "uv": [10, 20], "mirror": mirror}
            with self.subTest(mirror=mirror):
                found = {suit.region_of(cube, face, 64, 64)[0]: suit.region_of(cube, face, 64, 64)[1:3] for face in ("west", "east")}
                self.assertEqual(found, expected)
                self.assertEqual(suit.region_of(cube, "north", 64, 64), ("north", 16, 26, 4, 5))
                self.assertEqual(suit.region_of(cube, "down", 64, 64), ("down", 20, 20, 4, 6))

    def test_a_cube_turned_a_quarter_about_y_takes_the_turned_direction(self):
        for rotation, north_becomes in (([0, 90, 0], "east"), ([0, -90, 0], "west"), ([0, 180, 0], "south"), ([90, 0, 0], "down")):
            cube = {"origin": [0, 0, 0], "size": [4, 4, 4], "uv": [0, 0], "rotation": rotation, "pivot": [0, 0, 0]}
            with self.subTest(rotation=rotation):
                self.assertEqual(suit.region_of(cube, "north", 64, 64)[0], north_becomes)

    def test_fractional_and_flat_edges(self):
        self.assertEqual(suit.covered(3.7, 4.5, 64), (3, 8))
        self.assertEqual(suit.covered(10, 2.5, 64), (10, 12))
        self.assertEqual(suit.covered(5.0004, 3, 64), (5, 7))
        low, high = suit.covered(16, 0, 64)
        self.assertLess(high, low)
        self.assertEqual(suit.covered(60, 8, 64), (60, 63))

    def test_beagle_head_matches_its_hand_laid_box_uv(self):
        geo, profile = shipped("beagle")
        self.assertEqual(rects(suit.regions_by_slot(geo, profile)[2]["head"]),
                         [(0, 46, 6, 6), (6, 40, 6, 6), (6, 46, 6, 6), (12, 40, 6, 6), (12, 46, 6, 6), (18, 46, 6, 6)])

    def test_shiba_tail_is_clipped_to_the_atlas(self):
        geo, profile = shipped("shibainu")
        description, _, _ = bake.load_geo(geo)
        self.assertEqual(max(y + h for _, _, y, _, h in suit.regions_by_slot(geo, profile)[2]["tail"]), description["texture_height"])

    def test_every_shipped_slot_is_the_box_uv_of_its_named_cubes(self):
        for rig in RIGS:
            geo, profile = shipped(rig)
            for slot in suit.SLOTS:
                with self.subTest(rig=rig, slot=slot):
                    regions = suit.regions_by_slot(geo, profile)[2][slot]
                    self.assertEqual(rects(regions), box_uv_faces(geo, profile.get(slot, [])))
                    self.assertTrue(regions, f"{rig} {slot} has no texture footprint")


class ProjectionMatchesOverlayProjector(unittest.TestCase):
    def test_a_region_takes_the_cell_for_its_direction(self):
        for face in suit.FACES:
            with self.subTest(face=face):
                atlas = suit.project(labelled_sheet(), 32, 32, suit.turned_by(0), {"head": [(face, 4, 6, 3, 2)]})
                self.assertEqual({atlas[y][x] for y in (6, 7) for x in (4, 5, 6)}, {label("head", face)})

    def test_a_turned_rig_takes_the_cell_of_the_side_it_is_drawn_on(self):
        atlas = suit.project(labelled_sheet(), 32, 32, suit.turned_by(180), {"head": [("north", 0, 0, 2, 2)]})
        self.assertEqual(atlas[0][0], label("head", "south"))

    def test_corners_land_on_the_cell_corners(self):
        pixels = [[(0, 0, 0, 0)] * suit.WIDTH for _ in range(suit.HEIGHT)]
        cx, cy, cw, ch = suit.cell_of("torso", "north")
        for value, (x, y) in enumerate(((cx, cy), (cx + cw - 1, cy), (cx, cy + ch - 1), (cx + cw - 1, cy + ch - 1)), 1):
            pixels[y][x] = (value, 0, 0, 255)
        atlas = suit.project((suit.WIDTH, suit.HEIGHT, pixels), 32, 48, suit.turned_by(0), {"torso": [("north", 0, 0, 32, 48)]})
        self.assertEqual([atlas[y][x][0] for x, y in ((0, 0), (31, 0), (0, 47), (31, 47))], [1, 2, 3, 4])

    def test_outside_regions_stays_clear_and_off_edge_is_clipped(self):
        atlas = suit.project(labelled_sheet(), 32, 32, suit.turned_by(0), {"head": [("up", 31, 31, 4, 4)]})
        self.assertEqual(atlas[31][31], label("head", "up"))
        self.assertEqual(atlas[2][2], (0, 0, 0, 0))

    def test_an_override_wins_for_its_own_slot_only(self):
        green = [[(0, 255, 0, 255)] * 32 for _ in range(32)]
        regions = {"head": [("north", 0, 0, 2, 2)], "tail": [("north", 10, 10, 2, 2)]}
        atlas = suit.project(labelled_sheet(), 32, 32, suit.turned_by(0), regions, {"head": green})
        self.assertEqual(atlas[1][1], (0, 255, 0, 255))
        self.assertEqual(atlas[11][11], label("tail", "north"))
        self.assertEqual(atlas[5][5], (0, 0, 0, 0))

    def test_every_shipped_rig_paints_every_slot_texel_and_unshared_faces_show_their_cell(self):
        for rig in RIGS:
            geo, profile = shipped(rig)
            description, _, _ = bake.load_geo(geo)
            width, height = description["texture_width"], description["texture_height"]
            regions = suit.regions_by_slot(geo, profile)[2]
            atlas = suit.project(labelled_sheet(), width, height, suit.turned_by(0), regions)
            every = [(slot, region) for slot in suit.SLOTS for region in regions[slot]]
            for slot, region in every:
                face, x, y, w, h = region
                with self.subTest(rig=rig, slot=slot, region=region):
                    self.assertTrue(all(atlas[yy][xx][3] for yy in range(y, y + h) for xx in range(x, x + w)))
                    if not any(other is not region and overlap(other[1:], region[1:]) for _, other in every):
                        self.assertEqual(atlas[y][x], label(slot, face))


class Compositing(unittest.TestCase):
    def test_over_keeps_the_coat_under_clear_texels_and_covers_it_under_opaque_ones(self):
        coat = (100, 50, 25, 255)
        self.assertEqual(suit.over((0, 0, 0, 0), coat), coat)
        self.assertEqual(suit.over((200, 0, 0, 255), coat), (200, 0, 0, 255))
        self.assertEqual(suit.over((200, 100, 50, 128), (0, 0, 0, 0)), (200, 100, 50, 128))
        self.assertEqual(suit.over((255, 255, 255, 51), (0, 0, 0, 255)), (51, 51, 51, 255))

    def test_resizing_repeats_each_atlas_texel(self):
        atlas = [[(1, 0, 0, 255), (2, 0, 0, 255)], [(3, 0, 0, 255), (4, 0, 0, 255)]]
        self.assertEqual([[p[0] for p in row] for row in suit.resized(atlas, 4, 4)], [[1, 1, 2, 2], [1, 1, 2, 2], [3, 3, 4, 4], [3, 3, 4, 4]])


class SuitFiles(unittest.TestCase):
    def test_identifier_and_path_round_trip_under_assets(self):
        sheet = os.path.join(ASSETS, "textures", "entity", "costume", "spider_costume.png")
        self.assertEqual(suit.identifier_of(sheet), "dogs-unleashed:textures/entity/costume/spider_costume.png")
        self.assertEqual(os.path.normpath(suit.path_of("dogs-unleashed:textures/entity/costume/spider_costume.png", sheet)), os.path.normpath(sheet))
        self.assertIsNone(suit.identifier_of("/tmp/loose.png"))

    def test_a_shipped_override_replaces_its_slot(self):
        geo, profile = shipped("dachshund")
        sheet = os.path.join(ASSETS, "textures", "entity", "overlay", "mud.png")
        description, _, _ = bake.load_geo(geo)
        found, skipped = suit.overrides_for(profile, sheet, description["texture_width"], description["texture_height"])
        self.assertEqual(sorted(found), ["leg_back_left", "leg_back_right", "leg_front_left", "leg_front_right"])
        self.assertEqual(skipped, [])

    def test_a_sheet_of_the_wrong_size_is_refused(self):
        geo = write_geo([{"origin": [0, 0, 0], "size": [4, 4, 4], "uv": [0, 0]}])
        sheet = write_sheet(64, 64)
        try:
            with self.assertRaises(SystemExit):
                suit.suit_atlas(geo, {}, sheet)
        finally:
            remove(geo, sheet)

    def test_a_loose_sheet_projects_onto_the_slot_bones(self):
        geo = write_geo([{"origin": [0, 0, 0], "size": [4, 4, 4], "uv": [0, 0]}])
        sheet = write_sheet(suit.WIDTH, suit.HEIGHT, fill=(9, 9, 9, 255))
        try:
            atlas, skipped = suit.suit_atlas(geo, {"torso": ["b"]}, sheet)
            self.assertEqual(atlas[5][5], (9, 9, 9, 255))
            self.assertEqual(atlas[20][20], (0, 0, 0, 0))
            self.assertEqual(skipped, [])
        finally:
            remove(geo, sheet)


if __name__ == "__main__":
    unittest.main()
