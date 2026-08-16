<p align="center">
<img src="https://github.com/grabartley/minecraft-dogs-unleashed/raw/main/docs/banner.png" alt="Dogs Unleashed banner" width="800">
</p>

<a href="https://modrinth.com/mod/dogs-unleashed"><img src="https://img.shields.io/modrinth/dt/dogs-unleashed?logo=modrinth&label=Modrinth%20downloads&color=00AF5C" alt="Modrinth downloads"></a>
<a href="https://github.com/grabartley/minecraft-dogs-unleashed/stargazers"><img src="https://img.shields.io/github/stars/grabartley/minecraft-dogs-unleashed?logo=github&label=Stars&color=4078c0" alt="GitHub stars"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-yellow.svg" alt="License: MIT"></a>
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support_Dogs_Unleashed-009078?logo=ko-fi&logoColor=white)](https://ko-fi.com/grahambartley)

[![Watch the video](https://img.youtube.com/vi/vmh4vTChO-0/maxresdefault.jpg)](https://www.youtube.com/watch?v=vmh4vTChO-0)
<p align="center">
<a title="Fabric API" href="https://modrinth.com/mod/fabric-api" target="_blank">
	<img src="https://i.imgur.com/Ol1Tcf8.png" width="180" height="60" alt="Fabric API requirement button">
</a>
<a title="GeckoLib" href="https://modrinth.com/mod/geckolib" target="_blank">
	<img src="https://cdn.modrinth.com/data/cached_images/8e403ea8e76541bc7afe882f8e04ac08e2ae3bbf.png" width="180" height="60" alt="GeckoLib requirement button">
</a>
</p>

Good dogs. Big adventures. Dogs Unleashed is an open-source Fabric mod that adds five new dog breeds, a fetch system,
dog beds, graves, a full advancement tab, and more. Every dog is unique. Every bark has its own personality. Your
wolves have been carrying the team long enough.

Time to expand the pack.

## Alpha Release

This mod is currently in **alpha** testing and we would love to hear your feedback! Any feedback that you have for us,
please [open an issue](https://github.com/grabartley/minecraft-dogs-unleashed/issues/new) on the GitHub repository.

## Who's This For

**Builders** who want a Shiba Inu curled up on their cherry-wood porch while they landscape.

**Explorers** who want a Husky howling at the full moon from a frozen peak.

**Survival veterans** who want a proper fetch system, dog beds their pets actually sleep in, and a grave marker when the
worst happens.

**Anyone who has ever looked at a vanilla wolf and thought "this is great but what if it was a wiener dog?"**

## What You Get

### Five New Breeds

Each breed has its own stats, spawn biomes, coat variants, and bark sounds. They are not reskins. They are fully
animated with GeckoLib, each with idle, walk, sit, sleep, shake, head tilt, and tail wag animations.

| Breed                | Health | Speed | Damage | Biome                                                                    | Coats                                     |
|----------------------|--------|-------|--------|--------------------------------------------------------------------------|-------------------------------------------|
| **Husky**            | 25     | 0.30  | 5.0    | Snowy Taiga, Snowy Plains, Ice Spikes, Frozen Peaks, Snowy Slopes, Grove | 6 coats, 4 eye color combos               |
| **Dachshund**        | 10     | 0.25  | 2.0    | Plains, Sunflower Plains, Meadow                                         | 10 coats, our first wiener dog            |
| **Beagle**           | 17     | 0.29  | 3.0    | Flower Forest, Forest, Birch Forest, Old Growth Birch Forest, Meadow     | 12 coats, the most colourful of the bunch |
| **Golden Retriever** | 24     | 0.30  | 4.0    | Beach                                                                    | The classic golden look                   |
| **Shiba Inu**        | 18     | 0.32  | 3.5    | Cherry Grove                                                             | 3 coats: Red, Black, Sesame               |

#### Husky

<p align="center">
<img src="https://raw.githubusercontent.com/grabartley/minecraft-dogs-unleashed/main/docs/all%20huskies.png" alt="Husky coat variants" width="800">
</p>

<p align="center">
<img src="https://raw.githubusercontent.com/grabartley/minecraft-dogs-unleashed/main/docs/New%20Project.png" alt="Husky eye variants" width="600">
</p>

#### Dachshund

<p align="center">
<img src="https://raw.githubusercontent.com/grabartley/minecraft-dogs-unleashed/main/docs/allsal.png" alt="Dachshund coat variants" width="800">
</p>

#### Beagle

<p align="center">
<img src="https://raw.githubusercontent.com/grabartley/minecraft-dogs-unleashed/main/docs/allbeagles.png" alt="Beagle coat variants" width="800">
</p>

#### Golden Retriever

<p align="center">
<img src="https://raw.githubusercontent.com/grabartley/minecraft-dogs-unleashed/main/docs/goldenretriever.png" alt="Golden Retriever coat" width="400">
</p>

#### Shiba Inu

<p align="center">
<img src="https://raw.githubusercontent.com/grabartley/minecraft-dogs-unleashed/main/docs/allshibas.png" alt="Shiba Inu coat variants" width="800">
</p>

### The Command Wheel

Vanilla wolves have one move: sit. Our dogs take real commands. Right-click any of your tamed dogs to open a radial
command wheel with eight options:

| Command       | What your dog does                                                                                     |
|---------------|--------------------------------------------------------------------------------------------------------|
| **Follow**    | The default. Follows you around and teleports to you when left behind.                                  |
| **Heel**      | Sticks close, within a few blocks, instead of the usual loose follow.                                   |
| **Stay**      | Holds an area around the spot where you gave the command, without sitting. Still uses a nearby bed at night. |
| **Sit**       | The classic. Sits until told otherwise.                                                                 |
| **Hunt**      | Follows you and actively attacks hostile mobs and unnamed wild animals nearby. Name-tagged animals, tamed pets, villagers, golems, and other dogs are always safe. |
| **Guard**     | Holds an area like Stay and attacks hostile mobs that come near it.                                     |
| **Free Roam** | Wanders freely without following, but still comes along when you change dimension so it is never lost.  |
| **Go to Bed** | Sends the dog to its assigned bed to sleep. Greyed out until a bed is assigned.                         |

Hover a sector to see its name, click to command. The active command is highlighted, the world keeps running behind the
wheel, and number keys 1-8 work as shortcuts. Your dog barks and wags to acknowledge, and commands persist across
saves, restarts, and dimension changes.

### Fetch (Yes, Actual Fetch)

Sneak-right-click your dog with a fetch item to enter play mode, then throw it and watch your dog:

1. **Chase** the projectile as it arcs through the air
2. **Retrieve** the item from where it lands
3. **Return** it to you and drop it at your feet

Full chase AI, carry animation, the works. Three fetch items are supported:

| Item            | How to get                                         | Glide         | Notes                                        |
|-----------------|----------------------------------------------------|---------------|----------------------------------------------|
| **Tennis Ball** | Craft with slimeball + lime/white dye              | Standard      | Stackable (16)                               |
| **Stick**       | Vanilla sticks                                     | Standard      | No crafting needed                           |
| **Frisbee**     | Craft with 4× honeycomb + matching dye (16 colors) | Long and flat | Dyeable, name reflects color ("Red Frisbee") |

The frisbee glides roughly three times further than the tennis ball before landing, making it the best fetch item for
open terrain.

### Leads That Behave

Clip a vanilla lead onto your dog and the AI gets out of the way:

- **No tug-of-war**: follow and heel AI pause while leashed, the lead is the walk
- **No mid-walk naps**: a leashed dog never auto-sleeps, and one that was snoozing in bed wakes up the moment the lead
clips on
- **Fetch handoff**: sneak-right-click a leashed dog with a fetch item and the lead drops before play mode starts (turn
off *Drop Leash on Play Mode* to keep the lead on instead, which pauses fetching until the lead comes off)
- **Collar-matched rope**: the lead rope tints to your dog's collar color, so walking the whole pack stays readable

### Dog Beds They Actually Use

16 dye colors. Craft one, place it, and assign it to your dog by sneak-right-clicking the dog then right-clicking the
bed. Your dog will:

- **Auto-sleep** at night when within 32 blocks (clear weather required)
- **Stay asleep** until you wake them with a right-click of the bed or a new command from the wheel
- **Refuse to auto-sleep again** if you wake them before morning (respect)
- **Unassign** with a sneak-right-click of the bed if your dog gets too big for it

Every breed uses its curled "sleep" pose in bed. Tiny loaf, giant loaf, same nap energy.

### Husky Howling (Full Moon Only)

Huskies do not bark. Instead, they howl. And only on nights with a **full moon** (moon phase 0). The howl lasts 4.5
seconds with a 30-second cooldown. Stand on a frozen peak at midnight with your Husky and watch the animation trigger on
its own. It is the most atmospheric thing in the mod.

### Dog Graves

When a tamed dog dies, a grave appears near the death location with the dog's name and a flower matching their collar
color. Mine it with a pickaxe to preserve the grave as an item with full NBT data (dog name, UUID, collar color). Place
it anywhere as a memorial.

The grave avoids spawning on top of their bed. Because even in death, they want to be near home.

### Advancements

The Dogs Unleashed advancement tab tracks the full pack experience: taming your first dog, collecting every breed,
hearing a Husky howl, finishing a fetch return, seeing a dog sleep in bed, preserving a grave, and taming a Shiba Inu.

### Collar Dyeing

Right-click any of your tamed dogs with any dye to change their collar color. All 16 Minecraft dyes are supported.
Coordinated squad, colour-coded pack, rainbow chaos — your call.

### Taming, Breeding & Naming

Tame dogs with raw or cooked meats and bones (1-in-3 chance per feed). On tame, a naming screen opens so your new friend
gets a proper name immediately. Over 200 built-in name suggestions or type your own.

Any two tamed dogs can breed. Both parents must be tamed and not sitting. Puppies inherit their owner's UUID (preferring
the player who clicked). Breeding roll tables include rare coat variants you will not find in the wild.

Same-breed pairs produce pure puppies, exactly as before. Mixed pairs produce true cross-breeds: each one carries a
genome recording its breed composition (a 50/50 first cross, exact recursive splits from there), stats blended from its
ancestry with a small mutation jitter and a rare throwback toward one ancestor, a coat rolled from its dominant breed's
breeding table, and the bark (or Husky howl) of one parent's line. They show up as "Husky-Beagle Mix" style names (or
"Mixed Breed" for wilder blends), get their own "Mixed" filter in the Pet Manager, and their real percentages appear
across the details, inspect, and family tree screens. Cross-breeds can themselves breed with anything, recursively.

Golden Retriever and Beagle crosses now genuinely look mixed rather than rendering as whichever parent dominates. Both
breeds share one skeleton, so a puppy's proportions interpolate between its ancestors, and its coat is composited at
runtime from layered art: pigment colours blend by ancestry, pattern markings like the Beagle's saddle are inherited
whole rather than averaged into mush, and eyes and nose are stamped last so they stay crisp. Ear shape is inherited
whole from one ancestor, because a floppy Beagle ear and a feathered Golden ear are different shapes rather than two
ends of a slider. The remaining three breeds keep their own rigs and coats until their art is normalised the same way.

### Loyalty Across Any Teleport

Dimension hop and your dogs follow. Portal, end portal, weird command magic, whatever: every alive tamed dog comes with
you, unless it is sitting, sleeping in bed, or holding a Stay or Guard command. Long-distance teleports within a dimension (16 blocks or more) bring
them along too, and they always arrive on safe ground beside you instead of inside the floor. Flying? They land on the
ground beneath you. If you teleport somewhere with no safe footing at all, like into solid terrain, they stay safely
where they were. Pet Manager summons are different: an explicit summon always delivers the dog, using your exact
position as a last resort.

### Pet Manager

Press the configured keybind to open the Pet Manager screen. Filter by breed, alive or deceased status, or search by
name. Each living dog's row carries a Summon button that brings them to you from anywhere, even across dimensions: the
system loads their chunk, finds them, and delivers them. Clicking a row opens that dog's Pet Details screen, with a
large 3D portrait, their breed makeup as percentages, their named coat variant and how rare it is (Common through
Epic, with the actual roll odds), stat bars comparing their breed's max health, speed, and attack against the best in the pack, their
current health and last known location, a preview of their closest family, and the door into the full family tree. A
footer link at the bottom of the screen opens the controls screen so you can bind the keybind without digging through
menus.

### Family Trees & Ancestry

Every puppy permanently remembers both parents, and that ancestry survives death, despawns, and dimension hops. From
any dog's Pet Details screen, open their interactive family tree: generations laid out like a human family tree, with
mates side by side and litters hanging under their parents, every dog rendered with its live 3D model. Drag to pan,
scroll to zoom, and click any relative to expand their side of the family: grandparents, cousins, nieces and nephews,
the whole dynasty. Deceased ancestors stay in the tree, and you can re-focus the tree on any relative, even one owned
by another player. Dogs bred before this feature recover what their entity still remembers (at most one parent), so
old bloodlines show up as best they can.

### Inspecting Any Dog

Sneak and right-click any dog that is not yours, wild or another player's, and a floating details card pops up over
the world: their portrait, name, owner (or Wild), breed makeup, named coat and its rarity, health, age, and breed
stat bars. Handy
for scouting a rare coat before you spend the meat taming it, or checking out a dog on someone else's leash. Click
anywhere or press Escape to dismiss it.

### Breed-Specific Barking

Every breed has its own bark sound set. Dachshunds have 14 different bark sound files — the most vocal of the pack.
Beagles have 2, Goldens have 5, Shibas have 1. Dogs bark when you hold food nearby, when their health drops below 30%,
when they have a target, or just because (1 in 7200 chance per tick, roughly once every six minutes).

### Little Details

- **Head tilting** when you hold taming food nearby. It is impossible not to smile at.
- **Tail wagging** when you hold food or just because they love you.
- **Shaking** after leaving water — 20 tick delay, 22 tick animation, splash particles.
- **Floof.** So much floof.

## Quick Start

1. Install Dogs Unleashed, Fabric API, and GeckoLib into your `mods` folder.
2. Launch the game, join a world, and find your first dog in its spawn biome.
3. Tame it with meat, name it, grab a tennis ball, and try fetch.

That is it. No config files to edit. No server setup required.

## Controls

| Keybind          | Default | What it does                                                         |
|------------------|---------|----------------------------------------------------------------------|
| Open Pet Manager | Unbound | Opens the Pet Manager screen (set under `Controls > Dogs Unleashed`) |

Leash interactions use vanilla leads with no extra keybinds: clip a lead on to walk a dog (its follow and auto-sleep AI
pause while leashed), and sneak-right-click a leashed dog with a fetch item to drop the lead and start fetch in one
motion (see *Drop Leash on Play Mode* under Settings).

The keybind ships unbound so it never steals a key you already use. The first time you launch the game with the mod
installed, a one-time toast reminds you to bind it, and the Pet Manager screen carries a footer link that jumps straight
to the controls screen. The reminder is tracked in `config/dogs-unleashed/client-state.json`, so it only ever shows once
per installation.

## Settings

Open the settings screen via the cog button in the top-right of the Pet Manager, or via Mod Menu (if installed). All
settings are server-authoritative and editable in singleplayer or by operators on a multiplayer server. Operators can
also edit via `/dogsunleashed config`.

| Setting                   | Default | Range      | Effect                                                           |
|---------------------------|---------|------------|------------------------------------------------------------------|
| Spawn Dogs in the Wild    | On      | On / Off   | Natural biome spawning. Requires world restart to take effect.   |
| Spawn Rate                | 100%    | 0% to 500% | Spawn rate for all breeds, relative to the mod's defaults. 0% disables natural spawning. Requires world restart. |
| Spawn Rate (per breed)    | 100%    | 0% to 500% | Per-breed spawn rate, combined with the global rate. 0% disables that breed. Requires world restart. |
| Cap-Independent Spawner   | On      | On / Off   | Dedicated spawner that spawns wild dog packs near players even when the creature cap is full. Takes effect immediately. |
| Dog Graves on Death       | On      | On / Off   | Whether tamed dogs leave a grave block. Off drops loot normally. |
| Drop Leash on Play Mode   | On      | On / Off   | Whether starting fetch with a leashed dog drops the leash first. Off keeps the leash on and pauses fetching while leashed. |
| Auto-Sleep at Night       | On      | On / Off   | Whether dogs auto-sleep in their assigned bed at night.          |
| Auto-Sleep Range (blocks) | 32      | 4 to 128   | How far a dog can be from its bed and still return at night.     |
| Bark Volume               | 1.0     | 0.0 to 2.0 | Multiplier on bark sound volume (0.0 mutes).                     |
| Husky Howl Volume         | 1.5     | 0.0 to 2.0 | Multiplier on Husky howl sound volume (0.0 mutes).               |

Spawn rates are percentages of the mod's default spawn weights, not absolute spawn chances. The actual encounter rate
also depends on what else spawns in each biome's creature pool, which varies by modpack.

Vanilla passive spawning is gated by a shared creature cap that stays full in explored terrain, so dogs would normally
only spawn when new chunks generate, and never at all alongside mods like Cobblemon whose creatures keep the cap
saturated. The Cap-Independent Spawner, on by default, periodically spawns a small wild pack near a random player in a
matching biome, in the overworld only, honoring the spawn rate multipliers, the `doMobSpawning` gamerule, and its own
cap of 4 untamed dogs within 64 blocks of the player. Untamed dogs it spawns despawn again when no player is near, so
the world doesn't fill up; taming makes them permanent as usual. Disable it to limit dogs to vanilla natural spawning.

The config file lives at `<world-save>/dogs-unleashed/server-config.json` and can be hand-edited while the server is
offline.

### Command

```
/dogsunleashed status
/dogsunleashed config spawn <true|false>
/dogsunleashed config spawnrate <0..500>
/dogsunleashed config spawnrate <breed> <0..500>
/dogsunleashed config capindependentspawning <true|false>
/dogsunleashed config graves <true|false>
/dogsunleashed config autosleep <true|false>
/dogsunleashed config autosleeprange <4..128>
/dogsunleashed config barkvolume <0.0..2.0>
/dogsunleashed config howlvolume <0.0..2.0>
/dogsunleashed config reset
/dogsunleashed list <player>
/dogsunleashed summon <player> <petId>
/dogsunleashed find <petId>
```

The pet management subcommands are for operators inspecting and recalling a specific player's dogs server-side:

| Subcommand                         | What it does                                                                        |
| ---------------------------------- | ----------------------------------------------------------------------------------- |
| `list <player>`                    | Lists every pet the player owns: id, breed, name, alive/dead, dimension, last coords. |
| `summon <player> <petId>`          | Recalls that specific pet to the target player's current location.                   |
| `find <petId>`                     | Prints the dimension and coordinates of a pet UUID across all loaded worlds.          |

All subcommands require operator permission level 2.

### Mod Menu (optional)

If [Mod Menu](https://modrinth.com/mod/modmenu) is installed, Dogs Unleashed appears in the Mods list with the same
settings screen. Mod Menu is not bundled and not required.

The mod tile links out to Modrinth, the GitHub source and issue tracker, the releases page, and Ko-fi. The **Configure**
button opens the settings screen described above:

| Where you open it from       | What you see                                                                 |
|------------------------------|------------------------------------------------------------------------------|
| Title screen (no world)      | Default values, read-only, with a prompt to join a world first                |
| In a world, operator         | The world's live values, fully editable                                       |
| In a world, non-operator     | The world's live values, read-only                                            |

### Recipe Viewers (optional)

Dogs Unleashed ships plugins for both [JEI](https://modrinth.com/mod/jei) and
[EMI](https://modrinth.com/mod/emi). Install either one (or neither, or both) and every mod item gains an info page
next to its crafting recipe:

| Info page                      | What it covers                                                                    |
|--------------------------------|-----------------------------------------------------------------------------------|
| Tennis Ball, Stick, Frisbee    | How to start play mode, and how far each fetch item glides                         |
| Dog Bed                        | Assigning a dog, auto-sleep rules, and how to unassign                             |
| Dog Grave                      | How graves appear on death and how to preserve one with a pickaxe                  |
| Taming and breeding foods      | Which vanilla foods tame, which breed, and the 1-in-3 tame chance                  |
| Each breed's spawn egg         | Breed flavour plus the biomes it spawns in and its health, attack and speed        |

Spawn biomes and stats are read straight off the breed definitions, so the pages stay accurate when a breed is
retuned. Neither viewer is bundled or required, and the mod loads identically with both absent.

## Languages

Dogs Unleashed ships with translations for breed names, screens, commands, and chat feedback in:

| Locale  | Language                         |
|---------|----------------------------------|
| `en_us` | English (US)                     |
| `zh_cn` | Simplified Chinese (简体中文)        |
| `de_de` | German (Deutsch)                 |
| `fr_fr` | French (Français)                |
| `pt_br` | Portuguese (Português do Brasil) |
| `ru_ru` | Russian (Русский)                |
| `es_es` | Spanish (Español)                |

Want your language in the pack? **Translation PRs are very welcome!** Copy
[`en_us.json`](src/main/resources/assets/dogs-unleashed/lang/en_us.json), translate the values (keep the keys and
`%s` placeholders intact), and open a PR. Spotted a typo or an awkward phrasing in an existing translation? Please
[open an issue](https://github.com/grabartley/minecraft-dogs-unleashed/issues/new) so we can fix it.

A few things to leave untranslated in the `command.dogs-unleashed.*` strings, since operators type them
literally: command names (`/dogsunleashed`, `status`, `config`, `list`, `summon`, `find`), config option identifiers
(`spawn`, `graves`, `autosleeprange`, `barkvolume`, ...), literal argument tokens (`<true|false>`, `<player>`,
`<petId>`, range markers like `<4..128>`), and the pet descriptor field labels (`id=`, `dim=`, `pos=`).
Translate only the surrounding prose. The longest, most idiom-heavy strings are the settings tooltips, so
phrasing-polish PRs there are especially appreciated.

## Compatibility

- **Minecraft:** `1.21.1`
- **Loader:** Fabric `0.16.5+`
- **Fabric API:** `0.107.0+1.21.1` minimum
- **GeckoLib:** `4.7+`
- **Java:** `21`
- **Environments:** Dedicated server and integrated server, fully multiplayer

## Dependencies

| Dependency    | Version            | Required | Reason                      |
|---------------|--------------------|----------|-----------------------------|
| Fabric Loader | `>=0.16.5`         | Yes      | Mod loader                  |
| Fabric API    | `>=0.107.0+1.21.1` | Yes      | Fabric hooks and APIs       |
| GeckoLib      | `>=4.7`            | Yes      | Entity and block animations |
| Mod Menu      | any                | No       | Settings screen in the mods list |
| JEI           | any                | No       | Recipe and info pages       |
| EMI           | any                | No       | Recipe and info pages       |

## Open Source

Dogs Unleashed is open source under the MIT license. Built with love and treats ❤️

If Dogs Unleashed made you smile on a long survival night, a star on GitHub or a coffee on Ko-fi means the world.

<a href="https://ko-fi.com/grahambartley" rel="noopener nofollow ugc" target="_blank">
<img src="https://i.imgur.com/FSNi7zk.png" alt="Support me Ko-fi">
</a>
