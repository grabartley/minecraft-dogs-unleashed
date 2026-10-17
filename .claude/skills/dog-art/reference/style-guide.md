# Dog Art Style Guide

Every number here was measured on `main` with `dogart.py` on 2026-10-17. Re-measure before
quoting it in an issue, and update this file when a breed changes.

## Sheets And Density

| Breed | Sheet | Geo UV grid | Texels per visible unit | Cubes | Render scale |
|---|---|---|---|---|---|
| Beagle | 128x128 | 128 | 5.0 (2.7 to 10) | 21 | 1.5 |
| Dachshund | 128x128 | 128 | 2.5 (1.25 to 12.5) | 52 | 1.3, faces +z |
| Golden retriever | 128x128 | 128 | 5.0 (2.3 to 16.7) | 66 | 1.7 |
| Husky | 64x64 | 64 | 1.0 (0.9 to 2.5) | 26 | 1.3 |
| Shiba inu | 128x128 | 128 | 5.0 (2.9 to 10) | 33 | 1.5 |

Density is `size / (size + 2 * inflate)` times the sheet scale. The outliers are small detail
cubes (paws, tufts) that carry more texels than the body. The husky is the odd one out: native
scale, no inflate trick, a quarter of the density. It still reads well because its markings are
large shapes. Match the breed you are painting on; do not mix densities inside one rig.

## Rest-Pose Proportions

Visible units after inflate, before the render scale. Width is across the dog, length is nose to
tail.

| Breed | Whole dog (w h l) | Head cube | Torso | Front leg | Tail |
|---|---|---|---|---|---|
| Beagle | 3.6 7.0 11.3 | 2.2 | 2.8 x 2.8 x 6.1 | 0.8 x 3.4 x 1.6 | 0.6 x 3.0 x 2.9 |
| Dachshund | 3.2 5.9 12.7 | 2.0 | 2.0 x 2.2 x 7.9 | 0.9 x 3.0 x 1.1 | 0.4 x 1.2 x 2.8 |
| Golden retriever | 4.2 9.5 13.9 | 2.6 | 4.2 x 4.5 x 7.6 | 1.0 x 3.6 x 1.8 | 1.0 x 4.3 x 4.2 |
| Husky | 7.7 17.3 18.7 | 5.4 x 4.4 x 3.7 | 7.1 x 6.4 x 10.9 | 3.0 x 10.1 x 3.2 | 3.0 x 7.9 x 5.0 |
| Shiba inu | 4.4 8.2 10.8 | 2.0 (4.4 with ears) | 2.8 x 2.8 x 6.1 | 0.8 x 3.4 x 1.6 | 0.8 x 3.4 x 2.6 |

The beagle and shiba share torso and leg numbers because one rig was the starting point for the
other. Head to torso length sits near 1 to 3 on every small rig. Tails are raised in rest pose.
Breed character is carried by a few cubes: beagle ears hang, shiba ears stand and its tail curls,
the dachshund is all torso, the golden has a hand-built chest ruff of small rotated mirrored cubes.

## Colour

The shipped coats are not limited-palette pixel art. They are soft-shaded paintings in one or two
hue families, at a density where gradients survive.

| Texture | Distinct colours | Opaque texels | Hue families |
|---|---|---|---|
| beagle_tri1 | 1481 | 3317 | warm brown, grey, a little red |
| goldenretriever | 825 | 3508 | red-brown and orange |
| shibainu_red | 1315 | 4573 | red-brown, orange, grey |
| husky_blackwhite_blueblue | 357 | 1775 | grey, blue eyes |
| dachshund_red | 95 | 5833 | red-brown |

Rules that follow:

- Paint tonal variation inside a narrow hue band. No dithering, no three-tone ramps, no outlines.
  A flat fill with one shadow tone looks like a different mod next to these.
- The dachshund is the flattest sheet and the one to bring up, not the one to copy.
- Markings follow the real breed standard: blaze, saddle, mask, points, ticking. Reference photos
  before painting, and name the coat after the real coat.
- Underside, inner legs and under the tail go darker and cooler; spine, top of head and the
  outside of the ears go lighter and warmer. The engine adds the hard directional shading, so
  painted shading stays soft.
- Eyes and nose are the only places for high contrast. They are what the portrait reads from.
- Run `palette` on the sibling coat you are matching and stay inside its tonal range unless the
  coat is meant to be lighter or darker overall.

## Known Debt On Main

Recorded so nobody treats it as the standard. Counts are from `check` with GeckoLib's floored UV.

| Sheet | Holes | Unpainted faces | Stray texels | Off the sheet |
|---|---|---|---|---|
| beagle_tri1 | 5 | 1 | 1 | 0 |
| dachshund_red | 4 | 1 | 0 | 0 |
| goldenretriever | 27 | 92 | 0 | 0 |
| husky_blackwhite_blueblue | 2 | 10 | 0 | 0 |
| shibainu_red | 20 | 46 | 399 | 4 |

The golden retriever's unpainted faces are mostly shared tuft patches and hidden inner faces. The
shiba inu's stray paint sits left of the torso patch and below the head patch, and its four
off-sheet faces belong to the tail fluff cube at uv 96,109, which runs one row past a 128 sheet.
