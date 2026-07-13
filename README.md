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

### Dog Beds They Actually Use

16 dye colors. Craft one, place it, and assign it to your dog by sneak-right-clicking the dog then right-clicking the
bed. Your dog will:

- **Auto-sleep** at night when within 32 blocks (clear weather required)
- **Stay asleep** until you wake them with a right-click
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

Breeding is same-breed only. Both parents must be tamed and not sitting. Puppies inherit their owner's UUID (preferring
the player who clicked). Breeding roll tables include rare coat variants you will not find in the wild.

### Loyalty Across Any Teleport

Dimension hop and your dogs follow. Portal, end portal, weird command magic, whatever: every alive tamed dog comes with
you, unless they are sitting or sleeping in bed. Long-distance teleports within a dimension (16 blocks or more) bring
them along too, and they always arrive on safe ground beside you instead of inside the floor. Flying? They land on the
ground beneath you. If you teleport somewhere with no safe footing at all, like into solid terrain, they stay safely
where they were. Pet Manager summons are different: an explicit summon always delivers the dog, using your exact
position as a last resort.

### Pet Manager

Press the configured keybind to open the Pet Manager screen. Filter by breed, alive or deceased status, or search by
name. Summon any alive dog from anywhere, even across dimensions. The system loads their chunk, finds them, and brings
them to you.

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

## Settings

Open the settings screen via the cog button in the top-right of the Pet Manager, or via Mod Menu (if installed). All
settings are server-authoritative and editable in singleplayer or by operators on a multiplayer server. Operators can
also edit via `/dogsunleashed config`.

| Setting                   | Default | Range      | Effect                                                           |
|---------------------------|---------|------------|------------------------------------------------------------------|
| Spawn Dogs in the Wild    | On      | On / Off   | Natural biome spawning. Requires world restart to take effect.   |
| Spawn Rate                | 100%    | 0% to 500% | Spawn rate for all breeds, relative to the mod's defaults. 0% disables natural spawning. Requires world restart. |
| Spawn Rate (per breed)    | 100%    | 0% to 500% | Per-breed spawn rate, combined with the global rate. 0% disables that breed. Requires world restart. |
| Dog Graves on Death       | On      | On / Off   | Whether tamed dogs leave a grave block. Off drops loot normally. |
| Auto-Sleep at Night       | On      | On / Off   | Whether dogs auto-sleep in their assigned bed at night.          |
| Auto-Sleep Range (blocks) | 32      | 4 to 128   | How far a dog can be from its bed and still return at night.     |
| Bark Volume               | 1.0     | 0.0 to 2.0 | Multiplier on bark sound volume (0.0 mutes).                     |
| Husky Howl Volume         | 1.5     | 0.0 to 2.0 | Multiplier on Husky howl sound volume (0.0 mutes).               |

Spawn rates are percentages of the mod's default spawn weights, not absolute spawn chances. The actual encounter rate
also depends on what else spawns in each biome's creature pool, which varies by modpack.

The config file lives at `<world-save>/dogs-unleashed/server-config.json` and can be hand-edited while the server is
offline.

### Command

```
/dogsunleashed status
/dogsunleashed config spawn <true|false>
/dogsunleashed config spawnrate <0..500>
/dogsunleashed config spawnrate <breed> <0..500>
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

## Open Source

Dogs Unleashed is open source under the MIT license. Built with love and treats ❤️

If Dogs Unleashed made you smile on a long survival night, a star on GitHub or a coffee on Ko-fi means the world.

<a href="https://ko-fi.com/grahambartley" rel="noopener nofollow ugc" target="_blank">
<img src="https://i.imgur.com/FSNi7zk.png" alt="Support me Ko-fi">
</a>
