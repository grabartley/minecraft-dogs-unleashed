# Dogs Unleashed: Product Requirements

## How To Read This Document

This document says what Dogs Unleashed is: what a player can do with it, who may do what, which side decides each rule, and what the mod deliberately does not do. Every requirement has an identifier such as `TAME-3`, so issues, commits and reviews can cite it.

It holds no status. Nothing here says whether a requirement is built, in progress or waiting. Progress lives on the project board. A requirement belongs here whether it shipped long ago or has not been started, because both are equally part of what the mod is. If merging a pull request would mean editing this document, the document has status in it, and that line should come out.

It changes only when a requirement changes or turns out to be unclear. The reasoning behind architectural choices lives in the decision records in [`docs/adr/`](adr/). The engineering baseline every change is held to lives in [`docs/standards.md`](standards.md). Acceptance criteria live on each issue.

## 1. Product Summary

### The Problem

Vanilla Minecraft has one dog, the wolf. It follows, sits and fights, and that is all. It all looks the same, it gets lost when you travel, and when it dies it is gone. Players who love their dogs have nothing to do with them and no way to keep them close.

### What The Mod Is

Dogs Unleashed adds real dog breeds to Minecraft, each with its own look, coats, voice, size and home biome. You find one in the wild, win it over, name it, and then live with it: give it commands, play with it, put it to bed, kit it out, groom it, breed it, and bring it back if it dies. The dogs act like dogs, with body language, habits, personalities and a bond with their owner that grows over time. The rarest breed, the dire wolf, grows from a pup into a mount you can ride.

### Experience It Aims For

- **A dog, not a mob.** Dogs show what they feel through body language and sound, never through numbers on screen.
- **Learnable in seconds.** Every feature starts from a gesture a player already knows: right-click, sneak, a lead, a crafting grid. Depth comes from doing, not from reading.
- **Never lost.** A player can always find, call and bring back their dogs.
- **Premium look and sound.** Every model, animation, coat and sound is hand-made and judged against vanilla. Real recordings, never synthesised voices.
- **Vanilla at heart.** Recipes, foods and behaviours echo what vanilla already teaches: dogs dig, foxes steal, creepers fear cats.

### What Makes It Different

| Claim | Why it holds |
|---|---|
| Breeds feel distinct | Each breed has its own rig, animation set, painted coats, voice, size, stats and home biome (ADR 0003, 0005) |
| Dogs are never lost | Dogs follow through portals, a whistle calls one from anywhere, and the Pet Manager lists and summons every dog (FIND-1 to FIND-12) |
| Death has a second act | Graves, a lightning ritual, an undead life and a cure (DEATH-1 to DEATH-14) |
| A breed that grows into a mount | The dire wolf grows through five stages, earns its alpha form in battle and can be ridden (DIRE-1 to DIRE-13, RIDE-1 to RIDE-9) |
| Dogs with a nose | A dog tracks a scent to a mob, an ore, a dig site or a treasure map's X (NOSE-1 to NOSE-12). No other dog mod tracks structures or map targets |
| Point and command | Look at something and send your dog to it, with no menu (POINT-1 to POINT-8) |
| Learning by doing | Dogs learn tricks the way real dogs do, by being rewarded for what they already do (TRICK-1 to TRICK-8) |
| Sled teams | Up to six dogs pull a sled, and team size and surface decide speed (SLED-1 to SLED-11) |
| Real inheritance | Coats, eyes and personality come from genes with dominant and carried alleles (FAM-9 to FAM-15) |

### Non-Goals

- **Not a general pet mod.** It is about dogs. Cats, birds and other pets are out.
- **Not a mount mod.** Only the dire wolf grows big enough to carry a rider, and it is ridden bareback. No saddles, and no riding for other breeds.
- **Not a stats game.** No experience bars, levels or numeric bond meters on screen. Inspect cards show words and bars, never grinding targets.
- **Not a replacement for the vanilla wolf.** Vanilla wolves stay as they are (GEAR-7).
- **Not a shared skeleton.** Breeds are never made from one blended mesh or one shared texture layout, because the rigs differ too much (21 to 66 cubes) to blend.

## 2. Release Scope

This document specifies the Dogs Unleashed alpha line for Minecraft 1.21.1 on Fabric: every behaviour the mod offers, including those not yet built.

| An alpha release accepts | An alpha release refuses |
|---|---|
| Features that are thin, as long as what is there works | Any item, block or dog that can be duplicated or lost by a game mechanic |
| Settings that need a world restart to apply, if the setting says so | A client being able to change anything the server decides |
| Missing optional integrations | A crash, or a server that will not start, from a bad config file |
| Art still being refined, if it reads clearly in game | Any visual regression to a breed that already ships |
| Fewer breeds than planned | Any untranslated text in a shipped language |

### Deferred

| Out | Why |
|---|---|
| Generated fur | It never reached the quality the mod needs. Fur is hand made per rig (ADR 0005) |
| Projecting armour from a shared sheet | Armour is painted onto each breed's own torso cubes instead (ADR 0003) |
| A separate zombie model per breed | The undead look is one overlay projected onto every rig (ADR 0006) |
| Separate puppy geometry | Puppies are the adult model scaled down |
| Tracking players by scent | Left out of the first scent design |
| A keybind for pointing | The whistle is the input, so no new key is needed |
| Changing how vanilla items despawn | Not needed: an unloaded chunk already stops items ageing |
| Dogs dying of old age | Aging is meant to be gentle, so seniors never die of age |
| Making new maps by scent | Scent reads existing map targets only |
| Personality changes that stop a dog obeying | Traits colour behaviour but never override a command |

### Product Decisions

Decisions the product owner has made where the sources could be read more than one way. Each points at the requirement that states it.

| Question | Decision | Requirement |
|---|---|---|
| Can a stranger dye, heal, cure or breed someone else's dog? | Only the owner dyes or breeds. Anyone may heal or start a cure, as a kindness | GEAR-10, FAM-1, PERM-4 |
| What do shears do on an armoured dog? | Always trim fur. Armour comes off only through the gear screen | GEAR-9 |
| Which dogs come with you through portals and long teleports? | Follow and Heel dogs, from anywhere. Everyone else stays | FIND-2 |
| Do dire wolves sleep? | They nap on the ground, about 3 minutes by day and 3 by night, only when idle and safe | SLEEP-14 |
| Does the resurrection ritual need a storm? | No. Any lightning that strikes the rod works, including a Channeling trident | DEATH-7 |
| Does a dog lost for good leave a grave? | Yes, as a memorial that says the dog cannot come back | DEATH-12 |
| Can adult dire wolves from spawn eggs be tamed? | No. Only pups can be won, whatever their source | TAME-4 |
| Is the "Winter is Coming" advancement title kept? | Yes | DIRE-13 |
| Which breed comes next? | The one players ask for most, then the next most asked for | BREED-1 |
| Do mob costumes change how mobs treat the dog? | No. Costumes and mob heads are cosmetic only | COSTUME-9 |
| Can a costume be worn with armour? | Yes. Armour covers it, and it shows again when the armour comes off | COSTUME-5 |
| Where does the command wheel get room for new actions? | One Actions sector opens a second ring. Existing sectors keep their places | CMD-14 |
| Where the Dog Rig Profiles plan and a decision record disagree, which wins? | The decision record: fur is hand made, armour is painted per torso | LOOK-5 |

## 3. Actors

| Actor | What they need |
|---|---|
| **New player** | To find a dog, understand how to win it over, and learn the rest by doing |
| **Owner** | To command, keep, care for, find and enjoy their dogs, and never lose them for good by accident |
| **Other player** | To look at someone else's dog without being able to take, change or harm their ownership of it |
| **Delivery recipient** | To receive items a messenger dog brings, and nothing more |
| **Server operator** | To configure every option from the command line, look up and summon any player's dogs, and trust that clients cannot cheat |
| **Pack author** | To tune spawn rates, packs and growth, and change data-driven content (scent rules, loot tables, tags) without code |
| **Artist** | To add a breed through a preset entry, a rig, coats and a profile, and a coat through data and assets, with no per-breed code in any feature (LOOK-7) |
| **Wild dogs and packs** | Not people, but they act on their own: wild dire wolf packs hunt, guard pups and hold grudges |
| **Vanilla mobs** | Foxes raid caches, raiders trigger alarms, warded mobs are turned away. They behave as vanilla does unless a requirement says otherwise |
| **A vanilla client** | Cannot join: the mod adds entities, blocks and items, so the client must have the mod installed. Every rule is still enforced by the server, so a client that lies is no better off |
| **A modified client** | Assumed hostile. It can send any packet with any value |
| **Reduced settings** | A player on Minimal particles or with sound off still understands what a dog is doing from its pose and its geometry |

## 4. Domain Model

| Term | Definition |
|---|---|
| Dog | A Dogs Unleashed creature of any breed, living or undead |
| Breed | One of husky, dachshund, beagle, golden retriever, shiba inu, pinscher, shih tzu, caramelo, border collie, chihuahua, labrador, yorkshire terrier, pomeranian, german shepherd, dire wolf, or a cross. A breed sets the rig, coats, voice, size, stats and home biomes |
| Cross | A dog whose genome mixes more than one founding breed. It renders on the rig of its largest share |
| Founding breed | A breed that spawns naturally, in the wild or as a village stray, and may cross: every breed but the dire wolf. The dire wolf breeds only with dire wolves |
| Genome | A dog's breed shares, blended stats (health, speed, damage, fur growth), voice and genes |
| Gene | A pair of alleles for one trait (a coat locus, eye colour, or a personality trait). One allele is shown, the other may be carried |
| Coat | A painted coat variant of a breed, with a rarity of common, uncommon, rare or epic |
| Rarity | Derived from the larger of a coat's natural and breeding chances: common 25% or more, uncommon 10% or more, rare 3% or more, else epic |
| Owner | The player a tamed dog belongs to. Exactly one per tamed dog |
| Wild | Untamed |
| Pack | Dire wolves spawned together. They share a pack id for life |
| Grown | A dire wolf at adult stage or above |
| Command | The dog's standing order: Follow, Heel, Stay, Sit, Hunt, Guard or Free Roam |
| Anchor | The block position, and its dimension, that a Stay or Guard dog keeps to |
| Play mode | A fetch session between one owner and one dog |
| Bed | A dog bed or dog house assigned to one dog |
| Sleep window | The time of day a dog sleeps: 13000 to 23000 for adults, 11000 to 24000 for puppies, inverted for undead dogs |
| Gear | A dog's armour, pendant, cosmetic, costume and saddlebags |
| Cosmetic | A small thing worn on the head or neck, such as a bandana, a hat or a vanilla mob head |
| Costume | A suit worn over the body that dresses the dog up as a mob |
| Pet record | The world-level record of a tamed dog: name, owner, breed, life state, last known place, stats and family. It outlives the entity |
| Life state | Living, undead, deceased (can be brought back), or lost (gone for good) |
| Bond | How close a dog is to its owner, in five named steps from Stranger to Devoted |
| Trust | A wild dire wolf pup's trust in one player, 0 to 100 |
| Trait | A personality trait: brave, timid, greedy, lazy, clingy or playful |
| Scent | An item a dog has been given to track |
| Cache | A buried item, its position and the dog that buried it |
| Away | A dog out of the world on an expedition or a delivery |
| Reach | The vanilla interaction distance between a player and an entity |
| In-game day | 24000 ticks, 20 real minutes |

### Who Can Cross

| In | Out | Why not |
|---|---|---|
| Any two tamed founding breeds or crosses | A dire wolf with anything but a dire wolf | Dire wolves breed only with their own kind |
| Two dire wolves | An undead dog | Undead dogs cannot breed |
| | A sitting partner | A sitting dog is not looking for a mate |

### Who Follows Through Portals

| In | Out | Why not |
|---|---|---|
| Follow and Heel dogs, from anywhere | Sit, Stay and Guard dogs | They were told to stay |
| | Hunt and Free Roam dogs | They are busy where they are |
| | A dog sleeping in a bed | It is asleep at home |
| | A dog already within 12 blocks | It is already with you |

## 5. Use Cases

### 5.1 Find A Dog

**Actor:** New player.

Each breed lives in its home biomes and spawns there in small groups. Dire wolves spawn only as whole packs in the coldest places, always with a pup and an older wolf, both in new chunks and near players. Strays turn up in villages, and the wandering trader now and then sells a puppy in a basket.

| Breed | Home | Group |
|---|---|---|
| Husky | Snowy taiga, snowy plains, ice spikes, frozen peaks, snowy slopes, grove | 2 to 4 |
| Dachshund | Plains, sunflower plains, meadow | 1 to 2 |
| Beagle | Forest, flower forest, birch forest, old growth birch forest, meadow | 1 to 3 |
| Golden retriever | Beach | 1 to 3 |
| Shiba inu | Cherry grove | 1 to 2 |
| Pinscher | Badlands, wooded badlands | 1 to 2 |
| Shih tzu | Bamboo jungle | 1 to 2 |
| Caramelo | Villages of every biome, as a stray only | 1 |
| Border collie | Meadow, plains, windswept hills | 1 to 3 |
| Chihuahua | Desert | 1 to 2 |
| Labrador | Swamp, river banks | 1 to 3 |
| Yorkshire terrier | Dark forest | 1 to 2 |
| Pomeranian | Taiga, old growth spruce taiga | 1 to 2 |
| German shepherd | Sparse jungle, savanna, windswept hills | 1 to 3 |
| Dire wolf | Ice spikes, frozen peaks, jagged peaks, snowy slopes, snowy taiga | One leader that brings a whole pack, 5 to 10 by default |

| ID | Requirement |
|---|---|
| SPAWN-1 | Each naturally spawning breed, the dire wolf included, spawns in its home biomes when chunks generate and while the creature cap has room, on ground its breed allows, at a weight scaled by the global and per-breed spawn rate settings |
| SPAWN-2 | Golden retrievers spawn on beach sand |
| SPAWN-3 | Dire wolves spawn only as whole packs. A wild dire wolf spawned by any natural path brings the rest of its pack, placed within 3 blocks and a 2 block step of it in the same chunk. It spawns only where its smallest pack fits, and vanilla spawning places at most one pack leader per chunk at a time. Spawn eggs and commands give a single wolf |
| SPAWN-4 | A pack's leader is a prime one time in five, otherwise a yearling or adult. The second member is always a pup. Later members are pups, yearlings, adults and, rarely, primes. No wild pack has an alpha |
| SPAWN-5 | When Spawn Near Players is on, an extra spawner places wild groups of every breed near players even when the mob cap is full. Every 60 to 120 seconds it tries spots 24 to 48 blocks from a random player, and it waits while 2 or more wild groups are within 64 blocks. A breed's spawn rate scales how many groups it places, so 200% is about twice as many. An untamed dog with no group counts as a group of its own |
| SPAWN-6 | Dogs from the extra spawner may despawn while wild, like vanilla animals. Taming stops that |
| SPAWN-7 | Coat, eye colour (huskies) and fur length are rolled at spawn from the breed's natural tables |
| SPAWN-8 | Villages get a stray now and then, never more than 2 per village. One stray in three is a caramelo. The rest are the breed that fits the village biome (snowy: husky, plains: dachshund, taiga: beagle, desert or savanna: shiba inu, others: golden retriever), and only near a village with more than 4 occupied homes within 48 blocks |
| SPAWN-9 | The wandering trader rarely offers a Puppy Basket for emeralds. Using it releases a random founding breed's puppy, already tamed to the user |
| SPAWN-10 | Village Strays and Trader Puppies are settings, both on by default |

**Not supported:** dogs in the Nether or the End; cross-breeds spawning wild; wild alphas; caramelos in the wild.

#### Breed Roster

| ID | Requirement |
|---|---|
| BREED-1 | New breeds are added in order of how often players ask for them: pinscher, shih tzu, caramelo, border collie, chihuahua, labrador, yorkshire terrier, pomeranian, then german shepherd |
| BREED-2 | Every new breed is a founding breed. It tames with a taming food (TAME-2), crosses with the other founding breeds (FAM-2) and is listed in the Pet Manager's breed filter |
| BREED-3 | Every new breed is made the way the shipped ones are (LOOK-1, LOOK-7): its own rig, animation set, painted coats, collar mask, fur and rig profile, and a voice from real recordings of that breed (LOOK-12) |
| BREED-4 | Each breed's size and stats match the real dog: the pinscher, chihuahua, yorkshire terrier and pomeranian are toy sized, smaller than the dachshund, and the labrador and german shepherd are about the size of the golden retriever |
| BREED-5 | The caramelo is a short-coated mixed street dog in shades of tan and cream. It spawns only as a village stray (SPAWN-8) |

**Enforcement:** server. Spawning, rolls and trades are decided on the server.

### 5.2 Win A Dog Over

**Actor:** New player.

Most breeds are tamed with meat or a bone. A dire wolf can only be won as a pup, by earning its trust while its pack watches. A stray tames through trust too, but quickly and gently.

| Gesture | Result |
|---|---|
| Right-click a wild founding breed with a taming food | 1 in 3 chance to tame. Hearts on success, smoke on failure. The food is used either way |
| Right-click a wild dire wolf pup with meat or a bone | Adds trust if the pup will take it |
| Sneak and right-click a wild dog | Opens a read-only inspect card |

| ID | Requirement |
|---|---|
| TAME-1 | Taming foods are raw and cooked chicken, beef, porkchop, mutton and rabbit, rotten flesh, and bones |
| TAME-2 | A founding breed tames on a 1 in 3 roll per taming food |
| TAME-3 | On taming, the dog becomes the player's, sits, gets a pet record with a random name, and the player is offered a naming screen. Names are 1 to 32 characters with control characters removed |
| TAME-4 | A dire wolf can be won only as a wild pup, and never on a random roll. Adults cannot be tamed, whatever their source |
| TAME-5 | Each pup keeps a trust value per player from 0 to 100. Raw meat adds 20, a bone or cooked meat adds 10, with a 2 second cooldown per pup. Rotten flesh and anything else is refused and kept |
| TAME-6 | Killing a hostile mob within 16 blocks of a pup adds 20 trust, and killing a rabbit, chicken, fox or sheep adds 15, with a 5 second cooldown |
| TAME-7 | Spooking the pack costs 15 trust. Trust fades by 1 every 10 seconds while the player is offline or more than 24 blocks away |
| TAME-8 | At 25 trust the pup starts following the player at a distance. Hearts show at 25, 50 and 75. At 100 it is tamed through the normal path (TAME-3) and all trust for that pup is cleared |
| TAME-9 | Feeding a grown wild dire wolf makes it growl, and the food is kept |
| TAME-10 | A stray tames through trust with gentler numbers than a dire wolf pup: feeding builds trust quickly and nothing spooks it |
| TAME-11 | The inspect card shows breed, coat, rarity, fur, age, owner or wild, and stats. For a dire wolf pup it shows that player's own trust as a bar. A grown wild dire wolf's card says it is too wild to tame. A pack that holds a grudge against the player says it will never trust them |

**Not supported:** taming an adult dire wolf by any means; buying adult dogs.

**Enforcement:** server. Trust is kept on the server per player, and each player is sent only their own.

### 5.3 Give Commands

**Actor:** Owner.

Right-clicking your own dog opens the command wheel. The dog takes a standing command, or a one-shot action.

| Wheel sector | What it does |
|---|---|
| Follow | Follows, keeping 2 to 10 blocks |
| Heel | Follows closely, 1.5 to 4 blocks |
| Stay | Stays near where it stands, returning if more than 8 blocks away |
| Sit | Sits until told otherwise |
| Hunt | Follows and attacks animals and hostile mobs nearby |
| Guard | Holds its spot and attacks hostile mobs within 12 blocks of it |
| Free Roam | Wanders freely |
| Go to Bed | Goes to its bed if it is sleepy |
| Equipment | Opens the gear screen |
| Ride | Mounts a prime or alpha dire wolf |
| Actions | Opens a second ring with one-shot actions: learned tricks, Explore, Deliver |

| ID | Requirement |
|---|---|
| CMD-1 | Right-clicking your own tamed dog, not sneaking, with an item that nothing else handles, opens the command wheel |
| CMD-2 | Picking a command applies it, wakes a sleeping dog, and acknowledges with a wag, a bark and an action bar message |
| CMD-3 | Picking the command the dog already has just closes the wheel |
| CMD-4 | Number keys pick sectors |
| CMD-5 | A sector that cannot be used is greyed out and says why: Go to Bed with no bed, Ride for the wrong breed, too young, too far or busy |
| CMD-6 | Follow and Heel do not pull a leashed dog |
| CMD-7 | Stay and Guard set an anchor where the dog stands. The dog wanders within 5 blocks of it and walks back when more than 8 blocks away |
| CMD-8 | Hunt targets hostile mobs and animals within 16 blocks, never named mobs, tamed animals, tamed horses, other dogs, villagers or iron golems |
| CMD-9 | Guard targets hostile mobs within 12 blocks of the anchor, never named mobs |
| CMD-10 | A sitting dog that is hurt stands up and switches to Follow |
| CMD-11 | A dog flees danger unless it is on Guard or Hunt. Every dog flees fire |
| CMD-12 | A dog never attacks another dog, a villager or an iron golem, even with its owner |
| CMD-13 | Puppies never take targets |
| CMD-14 | The Actions sector opens a second ring in the same screen. It is hidden when the dog has no actions in it, and its actions can be greyed out with a reason |
| CMD-15 | The wheel closes itself if the dog leaves the world |

**Not supported:** commanding a dog that is not yours; commanding several dogs at once from the wheel.

**Enforcement:** server. The server checks that the sender owns the dog, that it is alive and in the sender's world, before applying any action. Ride also needs reach.

### 5.4 Point To Command

**Actor:** Owner holding a whistle bound to a dog.

| Gesture | Result |
|---|---|
| Sneak and use the whistle while looking at a block | The dog runs there and stays |
| Sneak and use the whistle while looking at a mob | The dog attacks it |
| Sneak and use the whistle while looking at a dropped item | The dog fetches it to you |
| Use the whistle without sneaking | Summons the dog (FIND-6) |

| ID | Requirement |
|---|---|
| POINT-1 | Pointing works up to 64 blocks, at the first block or entity under the crosshair |
| POINT-2 | Pointing at a block sets the dog's anchor there, like Stay |
| POINT-3 | Pointing at a hostile or neutral mob makes the dog attack it and keep that target |
| POINT-4 | Pointing at a dropped item makes the dog pick it up and bring it back |
| POINT-5 | Pointing at your own pets, villagers, iron golems, named or leashed animals or any player does nothing |
| POINT-6 | A point plays a whistle note different from the summon, and briefly marks the target for the owner only |
| POINT-7 | If the bound dog is not loaded, sitting, sleeping or ridden, the action bar says why |
| POINT-8 | The server re-checks ownership, range and line of sight. It never trusts the client's choice of target |
| POINT-9 | The first point a dog obeys earns "Over There!" |

**Not supported:** pointing for other players' dogs; pointing without a whistle.

**Enforcement:** server. The client only says what it is looking at.

### 5.5 Play Fetch

**Actor:** Owner.

| Gesture | Result |
|---|---|
| Sneak and right-click your dog with a tennis ball, stick or frisbee | Starts or stops play mode |
| Throw the toy | The dog chases it, picks it up and brings it back |

| ID | Requirement |
|---|---|
| FETCH-1 | Tennis balls and frisbees are thrown on use. Sticks are thrown only while the player is in play mode |
| FETCH-2 | One dog per player plays at a time. Starting play with a dog ends it with any other |
| FETCH-3 | Starting play turns Sit into Follow. With Drop Leash on Play Mode on, it also drops the lead. With it off, the dog will not chase while leashed |
| FETCH-4 | The dog chases a toy its play partner threw within 128 blocks, picks it up where it landed, and brings it back to within 3 blocks of the owner |
| FETCH-5 | A toy lands as a placed block in a free, dry space. If it hits water, lava or a solid face, it drops as an item and play ends |
| FETCH-6 | A frisbee keeps its colour in flight, on the ground and in the dog's mouth. Frisbees come in all 16 dye colours |
| FETCH-7 | A placed toy always drops itself when broken, by a player or by anything else |
| FETCH-8 | Play ends if the dog dies, leaves, or the owner is gone while it returns |
| FETCH-9 | Dogs follow a player holding a fetch toy, except while that player is already playing |

**Not supported:** fetching for other players; fetching other items.

**Enforcement:** server. The server holds the play sessions. The client may predict a stick throw only from synced state.

### 5.6 Put A Dog To Bed

**Actor:** Owner.

| Gesture | Result |
|---|---|
| Sneak and right-click your dog, then right-click a dog bed or house | Assigns the bed |
| Right-click an assigned bed | Sends the dog to bed, or wakes it |
| Sneak and right-click an assigned bed with an empty hand | Unassigns it |
| Right-click a bed or house with a dye | Recolours it |

| ID | Requirement |
|---|---|
| SLEEP-1 | Dog beds are crafted in any carpet colour. Dog houses are 2 by 2 by 2 and crafted with a cushion of any carpet colour |
| SLEEP-2 | A bed or house holds one dog. Assigning a new dog clears the old dog's link |
| SLEEP-3 | Only the owner can assign their dog |
| SLEEP-4 | A dog with a bed goes to it by itself in its sleep window when within the Auto-Sleep Range, unless leashed, sitting, or woken by hand earlier in the same window |
| SLEEP-5 | Living dogs don't sleep in rain or thunder. Undead dogs sleep by day, whatever the weather |
| SLEEP-6 | Being leashed, hurt or given a command wakes a sleeping dog |
| SLEEP-7 | Waking in a dog house gives 30 seconds of Regeneration I. A dog bed gives nothing extra |
| SLEEP-8 | A dog waking in a house steps out to a safe spot by the door |
| SLEEP-9 | Breaking a bed or house, or the dog's death, clears the assignment |
| SLEEP-10 | A bed or house drops itself with its colour. A broken house drops once |
| SLEEP-11 | Any player may recolour a bed or house with a dye |
| SLEEP-12 | A bed remembers its dimension. A dog can be assigned only to a bed in its own dimension, and a dog in another dimension from its bed does not try to sleep there |
| SLEEP-13 | A dog picks one of its breed's sleep poses and keeps it for that nap, so a sleeping pack doesn't look cloned |
| SLEEP-14 | Dire wolves can't be given beds. A tamed dire wolf naps on the ground for about 3 in-game minutes in the day and about 3 at night, only when idle and safe: on Follow, Stay, Guard or Free Roam with nothing to do, no hostile mob within 16 blocks, not hurt in the last 30 seconds, not ridden, leashed or playing, on solid ground. If it cannot nap safely it skips the nap. A command, damage, a hostile coming near or its owner mounting it wakes it |

**Not supported:** two dogs in one bed; beds that work across dimensions.

**Enforcement:** server.

### 5.7 Kit A Dog Out

**Actor:** Owner.

The gear screen, opened from the wheel, has slots for armour, a pendant, a cosmetic, a costume and saddlebags.

| Gesture | Result |
|---|---|
| Right-click your dog with armour or a pendant | Equips it, handing back what it wore |
| Right-click your dog with the armour's material | Repairs it |
| Right-click your dog with a dye | Dyes its collar |
| Feed your dog a Dog Treat | A short boost |

| ID | Requirement |
|---|---|
| GEAR-1 | Dog armour comes in leather, iron, gold, diamond and netherite. Vanilla wolf armour also fits. Defence is vanilla wolf armour's plus 0, 4, 4, 8 and 9. Diamond and netherite add toughness, and netherite adds knockback resistance |
| GEAR-2 | Durability is vanilla wolf armour's times 1, 2, 1.5, 4 and 5. Leather dyes and washes like leather armour. Netherite is a smithing upgrade from diamond |
| GEAR-3 | While armour is worn it takes the whole hit, except damage that bypasses wolf armour. It cracks visibly as it wears, with vanilla's crack sound |
| GEAR-4 | Right-clicking with the armour's material restores 12.5% of its durability per item |
| GEAR-5 | Only grown dogs can wear armour |
| GEAR-6 | Curse of Binding keeps armour on outside creative mode |
| GEAR-7 | Tamed vanilla wolves can wear dog armour, following the same rules |
| GEAR-8 | Armour, pendant, cosmetic, costume and saddlebags all drop when the dog dies |
| GEAR-9 | Shears always trim fur (GROOM-4). Armour comes off only through the gear screen |
| GEAR-10 | The collar is red by default. The owner can dye it any colour. The leash takes the collar's colour |
| GEAR-11 | A Dog Treat (wheat, bone meal and sugar) gives 60 seconds of +10% speed and +1 attack damage, with a wag, hearts and a bark. Another treat resets the timer |
| GEAR-12 | Pendants are crafted from a blank pendant (gold nuggets around leather) and one ingredient |
| GEAR-13 | Warding pendants turn away undead (bone block), arthropods (cobweb), illagers with ravagers and witches (ominous banner) or creepers (cod). Warded mobs within 16 blocks can't target the dog or its owner, and creepers flee the dog |
| GEAR-14 | Guardian pendants stop fire (magma cream), fall (phantom membrane) or drowning (pufferfish) damage to the dog. The fire guardian also stops it burning |
| GEAR-15 | The vitality pendant (glistering melon) regenerates an owner within 8 blocks who hasn't been hurt for 10 seconds |
| GEAR-16 | The loyalty pendant (totem of undying) saves the dog from one death with a totem's effects, then is used up |
| GEAR-17 | Warding and vitality do not stack across several dogs |
| GEAR-18 | The cosmetic slot takes bandanas, bows, a party hat, a straw hat and a rosette. Bandanas and bows take dye. Bandanas take banner patterns on a loom |
| GEAR-19 | Cosmetics render on the dog in every pose, clear of armour, pendants and ears. A hat hides any pointed ear that would stand up through it |
| GEAR-20 | Saddlebags (leather and a chest) add 9 item slots to the gear screen and show on the dog |
| GEAR-21 | The gear screen closes if the owner moves out of 8 blocks or the dog dies |
| GEAR-22 | The gear screen looks like vanilla's horse inventory: the vanilla container background and slot art, a live view of the dog in the preview box, and greyed empty-slot icons for the pendant, cosmetic and costume slots. Hovering an empty dog slot names it and says what it holds |
| GEAR-23 | Putting any gear on your dog, by hand or in the gear screen, earns "Dressed to Impress". Dyeing its collar earns "Collar Couture" |

#### Costumes

A dog can be dressed up as a Minecraft mob. A vanilla mob head goes in the cosmetic slot, and a mob costume goes in the costume slot, so a dog can wear a head and its matching suit together.

| Costume | Look | Crafted from |
|---|---|---|
| Spider | A dark fuzzy suit with eight legs sticking out of its sides and red eyes on the hood | String, a spider eye and black wool |
| Zombie | A torn blue shirt and green paws | Rotten flesh and blue wool |
| Skeleton | A bone print suit | Bones and white wool |
| Enderman | A black suit with purple eyes on the hood | An ender pearl and black wool |
| Ender dragon | A black scaled suit with wings | Dragon's breath, phantom membrane and black wool |
| Creeper | A green pixel hoodie | Gunpowder and lime wool |
| Wither | A dark suit with a small skull on each shoulder | A wither rose and black wool |
| Warden | A dark suit with glowing sculk patches | An echo shard and sculk |
| Bee | A yellow and black striped vest with wings and an antenna headband | Honeycomb, yellow wool and black wool |
| Sheep | A fluffy wool coat | White wool |
| Mooshroom | A red spotted coat with mushrooms on the back | Red mushrooms and red wool |
| Chicken | A feathered vest with a comb on the hood | Feathers and white wool |
| Axolotl | A pink suit with frilly gills on the hood | A tropical fish and pink wool |
| Frog | A green hood with big eyes | Slimeballs and green wool |
| Panda | A black and white suit | Bamboo, black wool and white wool |
| Turtle | A shell on the back | A turtle scute |
| Allay | Small blue wings | An amethyst shard and light blue wool |

| ID | Requirement |
|---|---|
| COSTUME-1 | The cosmetic slot also takes the vanilla mob heads (zombie, skeleton, wither skeleton, creeper, piglin and dragon) and a carved pumpkin. The dragon head's jaw opens when the dog barks |
| COSTUME-2 | The costume slot takes the costumes in the table above, and nothing else |
| COSTUME-3 | Each costume is crafted from the ingredients in the table. The sheep costume takes dye like leather armour |
| COSTUME-4 | The wandering trader sometimes offers one costume for emeralds, picked at random from the table |
| COSTUME-5 | A costume renders over the dog's body in every pose. Armour covers it while worn, and it shows again when the armour comes off. Cosmetics, pendants, saddlebags and collars render over it |
| COSTUME-6 | Every costume and mob head fits every breed, adults and puppies, crosses, undead dogs and the dire wolf at every growth stage, from the rig profile's roles (LOOK-5). Nothing is placed per breed in code |
| COSTUME-7 | Costumes move with the dog: the spider's legs step with its walk, the ender dragon's, chicken's and allay's wings flap when it jumps, the bee's wings buzz while it runs, the wither's side skulls look around, the warden's patches pulse when it barks, and the axolotl's gills wiggle in water |
| COSTUME-8 | Only the owner can put on or take off a costume, through the gear screen or by right-clicking their dog with it |
| COSTUME-9 | Costumes and mob heads are cosmetic only. They change no stats, and mobs treat a dog in costume exactly as they treat any other dog |

**Not supported:** gear on other players' dogs; stacking several pendants; wild dogs spawning in costume; more than one costume at a time.

**Enforcement:** server. Equipping, slots and effects are server decisions. Only the owner may open the gear screen or change a costume.

### 5.8 Groom A Dog

**Actor:** Owner.

Coats grow out over time. A long coat slows a dog down, and shears trim it.

| ID | Requirement |
|---|---|
| GROOM-1 | Fur grows through trimmed, natural, shaggy and overgrown, up to the breed's maximum (beagles, dachshunds and shiba inus stop at natural, pinschers at shaggy) |
| GROOM-2 | Trimmed to natural takes 2 to 3 hours of play depending on breed. Natural to shaggy takes 1.5 times that and shaggy to overgrown twice that. A botched trim grows back in half that. All of it is scaled by Fur Growth Speed. Puppies don't grow fur. With Fur Growth off, or at 0%, nothing grows |
| GROOM-3 | An overgrown dog is 10% slower |
| GROOM-4 | The owner trims with shears, getting dog fur: one share from natural, two from shaggy, three from overgrown, times the breed's yield. Puppies and already-trimmed dogs refuse |
| GROOM-5 | A trim goes wrong 5% of the time from natural, 12% from shaggy and 20% from overgrown, scaled by Bad Haircut Chance. A botched dog whines, earns "Bad Hair Day", gives double fur and looks botched until it grows back |
| GROOM-6 | Wild adults spawn natural 70%, shaggy 25% and overgrown 5% of the time, up to their breed's maximum |
| GROOM-7 | Each fur stage is clearly visible on every breed through hand-made fur, never generated |
| GROOM-8 | A dog gets muddy walking on mud, through swamp water, or in rain on dirt, grass, podzol or farmland. Muddy dogs show mud and leave paw prints that fade in 30 seconds |
| GROOM-9 | Using a water cauldron on a muddy dog cleans it and uses one level of water. Swimming in clean water cleans it over time, and mud wears off in a few days |
| GROOM-10 | Muddy Dogs is a setting, on by default |

**Not supported:** trimming someone else's dog; dyeing a dog's coat.

**Enforcement:** server. The client draws mud and prints from synced state, and prints never change blocks.

### 5.9 Never Lose A Dog

**Actor:** Owner.

| ID | Requirement |
|---|---|
| FIND-1 | Every tamed dog has a pet record that outlives the entity: name, breed, owner, life state, last known place and stats |
| FIND-2 | After a portal trip, or a teleport of 16 blocks or more in the same world, the owner's Follow and Heel dogs join them from anywhere, unless asleep in a bed or already within 12 blocks (Section 4) |
| FIND-3 | A dog that follows (FIND-2) lands on a safe spot near the player. If there is none, it stays where it was |
| FIND-4 | A dog whistle (gold, string and wool) binds to one of your dogs by right-clicking it |
| FIND-5 | A whistle only works for its binder. Another player using it does nothing |
| FIND-6 | Using a bound whistle summons the dog from anywhere, loading it if needed, with a 1 second cooldown. A summon, from the whistle, the Pet Manager or an operator, wakes the dog, sets it to Follow and places it on a safe spot beside the player, or on the player's own spot if there is none |
| FIND-7 | The Pet Manager lists every dog the player owns, with a portrait, search, breed filter and life state filter, 4 per page. Filters are remembered per player |
| FIND-8 | Pet details show breed, coat, rarity, fur, owner, life state, health, age, riding and stamina (dire wolves), last known place, stats and family |
| FIND-9 | The Pet Manager can summon (FIND-6) any living or undead dog, sharing the whistle's 1 second cooldown |
| FIND-10 | The Pet Manager shows saved data for dogs that aren't loaded. It never loads chunks to fill the list |
| FIND-11 | The Pet Manager key is unbound by default, and a one-time toast suggests binding it |
| FIND-12 | When a player joins, their tamed dogs within 32 blocks wag and show hearts, at most once a minute per dog |

**Not supported:** summoning a deceased or lost dog; seeing other players' dogs in the Pet Manager.

**Enforcement:** server. Only the owner's own records are ever sent.

### 5.10 Raise A Family

**Actor:** Owner.

| ID | Requirement |
|---|---|
| FAM-1 | Two tamed living dogs breed with a breeding food (the taming foods without the bone). Only a dog's owner may start breeding it |
| FAM-2 | Two of the same breed give that breed. Anything else gives a cross. Dire wolves breed only with dire wolves |
| FAM-3 | A puppy belongs to the player who fed a parent, if that player owns it, otherwise to a parent's owner. It never belongs to a stranger |
| FAM-4 | A cross's breed shares are the average of its parents'. One time in 20, one ancestor's share is nudged up by 15% (a throwback) |
| FAM-5 | A cross's health, speed, damage and fur growth are the share-weighted average of its breeds, each varied by up to 5% |
| FAM-6 | A cross takes its voice from one parent and renders on the rig of its largest share |
| FAM-7 | A cross looks like a mix of its parents: coat tinted toward the other parents' colours by share, one parent's ear shape inherited whole, and proportions scaled by share. Pure breeds show no blending |
| FAM-8 | Puppies follow a living parent, starting 3 to 16 blocks away and giving up beyond 32 |
| FAM-9 | Every dog carries two alleles per gene: each coat locus of its breed, eye colour, and each trait |
| FAM-10 | A puppy takes one random allele from each parent per gene, with about a 1 in 32 mutation chance |
| FAM-11 | The dominant allele shows, or the recessive one when both alleles match. A rare coat can be carried for generations |
| FAM-12 | A dog saved without genes gets genes matching how it looks, so nothing about it changes |
| FAM-13 | The family tree shows parents, mates, siblings and children (32 each at most), with pan, zoom, expand and focus |
| FAM-14 | The family tree shows each dog's visible genes, and marks the genes it carries once breeding has revealed them |
| FAM-15 | A player may view the family tree of a dog they own, or of a dog related to one they own |
| FAM-16 | The player who started the breeding earns "Puppy Love" when any puppy is born and "Mixed Heritage" when a cross is born |

**Not supported:** breeding undead dogs; crosses with a dire wolf; choosing a puppy's coat.

**Enforcement:** server. Genes and family data are decided and stored on the server.

### 5.11 Bring A Dog Back

**Actor:** Owner, or any player who helps.

| ID | Requirement |
|---|---|
| DEATH-1 | A tamed dog that dies leaves a grave near where it died, if Dog Graves on Death is on and there is room. Its gear drops either way |
| DEATH-2 | The grave keeps the dog's name and grows flowers in its collar colour. It needs a pickaxe to break and keeps its data as an item |
| DEATH-3 | Any player can place a totem of undying in a grave or take it out |
| DEATH-4 | A lightning rod on top of a grave with a totem, struck by lightning, brings the dog back undead |
| DEATH-5 | The undead dog keeps its name, owner, breed, genome, coat, eyes, collar, family, age, growth and fur. It returns on Follow at full health, with a short burst of fire resistance and absorption |
| DEATH-6 | The ritual uses up the totem, the grave and the rod. The player who placed the totem earns "Second Leash of Life" |
| DEATH-7 | Any lightning works: a storm, or a Channeling trident |
| DEATH-8 | Undead dogs have half their living health and damage, dire wolves included |
| DEATH-9 | Undead dogs burn in daylight unless they have shade, armour, or a fire guardian pendant. Lightning doesn't hurt them |
| DEATH-10 | Any player can cure an undead dog the vanilla way: Weakness, then a golden apple. It takes 3 to 5 minutes. The curer earns "Best Friend Again" |
| DEATH-11 | A dog that dies while undead is lost for good, unless it wears a Pendant of Loyalty |
| DEATH-12 | A lost dog still leaves a grave, as a memorial. Its tooltip says it can't come back, and a ritual on it fizzles and says why |
| DEATH-13 | An undead dog looks undead on every breed and coat through one shared overlay. Curing restores exactly its living look |
| DEATH-14 | When an owner dies, their Follow and Heel dogs within 32 blocks go to the spot, lie down and attack hostile mobs within 8 blocks. They bark or howl when the owner respawns, and keep watch until the owner comes within 8 blocks or gives a command |

**Not supported:** reviving a lost dog; reviving without a totem.

**Enforcement:** server.

### 5.12 Raise A Dire Wolf

**Actor:** Owner.

| Stage | Size (wide by tall) | Health | Speed | Damage | Armour | Jumps |
|---|---|---|---|---|---|---|
| Pup | 0.4 by 0.55 | 20 | 0.30 | 2 | 0 | 1 block |
| Yearling | 0.8 by 1.1 | 30 | 0.32 | 6 | 2 | 1 block |
| Adult | 1.15 by 1.5 | 40 | 0.34 | 8 | 3 | 1 block |
| Prime | 1.6 by 2.55 | 50 | 0.36 | 10 | 4 | 1.5 blocks and fences |
| Alpha | 2.0 by 3.2 | 60 | 0.38 | 12 | 6 | 2 blocks |

| ID | Requirement |
|---|---|
| DIRE-1 | A pup, wild or bred, grows into a yearling, a yearling into an adult after 20 minutes, an adult into a prime after 40, all scaled by Dire Wolf Growth Speed. At 0% no stage grows, pups included |
| DIRE-2 | The owner is told each time their wolf grows |
| DIRE-3 | A prime that wins Alpha Battles fights (default 20) beside its owner, then eats an Enchanted Golden Apple from them, becomes an alpha with a howl. A battle is a kill of a hostile mob while the owner is within 16 blocks |
| DIRE-4 | The wolf refuses the apple, saying why, if it is already an alpha, not a prime, or short of battles |
| DIRE-5 | Prime and alpha wolves jump and path over the heights in the table, fences included for both |
| DIRE-6 | Wild grown pack members hunt animals, skeletons and zombies within 16 blocks, resting 2 minutes after a kill. They leave alone tamed, named or leashed animals, and animals within 4 blocks of a fence, gate or wall |
| DIRE-7 | When a wild member is hurt, grown members within 24 blocks attack the attacker for 30 seconds and young ones keep away. Creative and spectator players are ignored |
| DIRE-8 | A survival player who hurts any member is remembered by that pack for good. Its grown members attack them on sight while pack defence is on. Its young keep away, and its pups never trust them |
| DIRE-9 | Grown members watch players within 12 blocks of a pup. Sprinting, holding a weapon (a data tag, so modded weapons count) or coming within 3 blocks while not sneaking makes the nearest one growl and step between. Sneaking is always tolerated |
| DIRE-10 | Wild young members never attack and keep 12 blocks from threats |
| DIRE-11 | Pack hunting and pack defence are settings, both on by default |
| DIRE-12 | Dire wolves howl and bark, and growl when angry |
| DIRE-13 | Tame one of every dire wolf coat to earn "Winter is Coming" |
| RIDE-1 | The owner rides a prime or alpha from the wheel's Ride sector when it is within reach, alive, not sitting, sleeping, playing or leashed, and not already carrying someone |
| RIDE-2 | Steer with the camera and movement keys. Speed eases in and out. Turning slows at a gallop. Sneak to get off |
| RIDE-3 | Holding sprint runs 1.6 times as fast while stamina lasts, never in water |
| RIDE-4 | Holding jump charges a leap for up to half a second |
| RIDE-5 | Stamina is 100 for a prime and 140 for an alpha, scaled by Riding Stamina. Sprinting costs 0.4 a tick. It recovers 0.25 a tick while ridden and 1 a tick at rest, scaled by Stamina Recovery |
| RIDE-6 | A spent wolf cannot sprint again until it is back above 25% |
| RIDE-7 | Stamina shows in place of the experience bar, using vanilla's mount bar art, greyed out when spent |
| RIDE-8 | While ridden, the wolf takes no targets and its own goals pause. Its command is unchanged when you get off |
| RIDE-9 | Riding Stamina Drain, Riding Stamina and Stamina Recovery are settings |

**Not supported:** riding a wild wolf, another player's wolf or any other breed.

**Enforcement:** server. The rider's client moves the mount, as in vanilla, and sends only whether sprint is held. Stamina is decided on the server.

### 5.13 Track A Scent

**Actor:** Owner.

| Gesture | Result |
|---|---|
| Right-click your dog with a scent item | The dog sniffs it and starts tracking |
| Right-click your dog with a map that has a target | The dog leads you to the target |

| ID | Requirement |
|---|---|
| NOSE-1 | Scent items are a data tag, and scent rules map an item to a target, so packs can add more |
| NOSE-2 | A mob drop leads to the nearest living mob that drops it |
| NOSE-3 | A raw ore leads to the nearest matching ore block |
| NOSE-4 | A brush leads to the nearest suspicious sand or gravel |
| NOSE-5 | A map with a target marker leads to that marker |
| NOSE-6 | A pottery sherd leads to the nearest trail ruins |
| NOSE-7 | The dog walks ahead with its nose down, waits if the owner falls more than 12 blocks behind, and barks on arrival. At a dig site or map target it digs |
| NOSE-8 | Each breed has a scent range. Beagles track furthest, about twice the others. A cross takes a share-weighted range |
| NOSE-9 | With nothing in range, the dog sniffs, looks up and whines |
| NOSE-10 | Giving the item again, any command, or 2 minutes without progress stops tracking |
| NOSE-11 | Only the owner can give a scent. Wild dogs and other players' dogs ignore scent items |
| NOSE-12 | Any other item still opens the wheel, and food still feeds |
| NOSE-13 | A dog tracking a scent to its target earns its owner "On the Scent" |

**Not supported:** tracking players; making maps.

**Enforcement:** server. Search costs are budgeted (PERF-2).

### 5.14 Teach Tricks

**Actor:** Owner.

| ID | Requirement |
|---|---|
| TRICK-1 | An idle tamed dog near its owner now and then does an untrained trick on its own: beg, roll over or play dead |
| TRICK-2 | Feeding a Dog Treat within 5 seconds rewards that trick |
| TRICK-3 | Five rewards teach the trick. The dog barks happily and the owner is told |
| TRICK-4 | Learned tricks appear in the Actions ring. Picking one makes the dog do it |
| TRICK-5 | Learned tricks are never forgotten, through death and resurrection |
| TRICK-6 | How fast a pup learns depends on its parents (AGE-3) |
| TRICK-7 | Advancements reward a first trick and all three |
| TRICK-8 | A dog stays put until its trick ends. A busy dog, such as one pulling a sled, ridden, fighting, fetching or asleep, never starts a trick on its own, and the Actions ring says it is busy |

**Not supported:** teaching another player's dog; tricks on demand without learning them.

**Enforcement:** server.

### 5.15 Grow Close To A Dog

**Actor:** Owner.

| ID | Requirement |
|---|---|
| BOND-1 | Every tamed dog has a bond with its owner in five named steps from Stranger to Devoted. Pet details show the step's name, never a number |
| BOND-2 | Bond grows from walks together, play, fights won together, treats and grooming. Each source has a daily cap |
| BOND-3 | Bond never drops from neglect. It resets only if the dog changes owner |
| BOND-4 | Bond shows in body language: tail wag speed, a play bow when the owner returns after a while, and following closer |
| BOND-5 | A closer bond gives a longer follow and teleport range and faster responses. At Devoted the dog comes to its owner when they are hurt |
| BOND-6 | Bond survives death, resurrection and unloading |
| TRAIT-1 | Every dog has one or two traits, shown in pet details and on the inspect card |
| TRAIT-2 | Brave: growls and steps between its owner and a hostile mob, never flees |
| TRAIT-3 | Timid: cowers, stays behind its owner in fights, flees at higher health |
| TRAIT-4 | Greedy: sometimes takes one food from an open chest or the ground near its owner and eats it |
| TRAIT-5 | Lazy: naps more in the day and walks a little slower when not following |
| TRAIT-6 | Clingy: follows closer, whines on a long Stay, greets with extra hearts |
| TRAIT-7 | Playful: starts games more often and brings toys more |
| TRAIT-8 | Traits come from genes (FAM-9). Wild dogs roll them at spawn |
| TRAIT-9 | Traits never stop a dog obeying a command |

**Not supported:** a numeric bond meter; changing a dog's traits.

**Enforcement:** server.

### 5.16 Grow Old Together

**Actor:** Owner, on a world with Dog Aging on.

| ID | Requirement |
|---|---|
| AGE-1 | Dog Aging is off by default. With it off, nothing ages |
| AGE-2 | An adult becomes a senior after a set number of in-game days (100 by default). Seniors have a grey muzzle, walk a little slower and nap more. Pet details and the inspect card show a senior as one |
| AGE-3 | Pups of a senior parent learn tricks twice as fast and start one bond step higher |
| AGE-4 | Seniors never die of age |
| AGE-5 | Dire wolves age only after their adult stage, and aging never changes their size |

**Enforcement:** server.

### 5.17 Run A Sled Team

**Actor:** Owner.

| ID | Requirement |
|---|---|
| SLED-1 | A sled is crafted and placed like a boat |
| SLED-2 | A lead hitches one of your huskies, or a dire wolf at adult stage or above, to the sled. Up to six dogs, in pairs |
| SLED-3 | The player rides the sled and steers with the movement keys. The team pulls |
| SLED-4 | Speed grows with team size and the surface: fastest on ice, fast on snow, slow on grass and dirt, crawling on sand |
| SLED-5 | Each dog has stamina while pulling, using the riding rules (RIDE-5). Sprint costs stamina, and a bigger team shares the load |
| SLED-6 | A spent team slows to a trot |
| SLED-7 | Hitched dogs wear a harness. Huskies run at speed |
| SLED-8 | Sneak and use the sled to unhitch. Dogs keep their commands for when they are unhitched |
| SLED-9 | Advancements reward 1000 blocks by sled in snowy biomes, and a full team of six |
| SLED-10 | Only the owner can hitch their own dogs |
| SLED-11 | Harness lines are drawn from the sled to each dog |

**Not supported:** other breeds pulling; sleds pulled by players.

**Enforcement:** server. Sled movement follows the vanilla vehicle model, with speed and stamina decided on the server.

### 5.18 Everyday Dog Life

**Actor:** Owner, and nearby dogs.

| ID | Requirement |
|---|---|
| LIFE-1 | A free-roaming or staying dog given a bone buries it within 16 blocks in grass, dirt, podzol, sand or snow |
| LIFE-2 | A few days later it digs it up and brings it back. One time in five it finds a bonus from a data loot table |
| LIFE-3 | Caches survive restarts. If the dog is gone, a shovel digs the cache up |
| LIFE-4 | At night a wild fox within 24 blocks of a cache may dig it up and run off with the bone in its mouth, dropping it if hurt |
| LIFE-5 | A dog within 16 blocks of a raided cache barks and chases the fox off, never killing it |
| LIFE-6 | Two idle free-roaming dogs within 10 blocks may play bow and chase each other for 10 to 20 seconds, swapping roles. Wild dire wolf pups in one pack play too |
| LIFE-7 | Any command, danger, a hurt dog or the sleep window stops a game |
| LIFE-8 | A rope toy starts tug of war: hold use on your dog. A strength bar shows who is winning. Tap in rhythm to pull |
| LIFE-9 | The dog's pull comes from breed size, bond and a little chance. Win and it lets go and wags. Lose and it runs off with the toy |
| LIFE-10 | In the Nether dogs whine now and then, cower and follow closer, unless wearing the fire guardian pendant |
| LIFE-11 | In the End, dogs on Hunt chase endermites and bark at endermen without provoking them |
| LIFE-12 | Dogs shake off after rain or a swim, splashing |
| LIFE-13 | Dogs tilt their heads at a nearby player holding a taming food or a treat |
| LIFE-14 | Dogs wag at food and treats, and now and then for no reason |
| LIFE-15 | Huskies and dire wolves, and crosses with their voice, howl at the full moon. An owner within 64 blocks earns "Bark at the Moon" |
| LIFE-16 | Dogs bark when hungry for attention, hurt, low on health, fighting, or at random, at most every 6 seconds. Puppies bark higher. Huskies howl but never bark |
| LIFE-17 | Bark Volume and Howl Volume are settings |

**Enforcement:** server decides every behaviour. Sounds and particles are played to nearby clients.

### 5.19 Home And Keepsakes

**Actor:** Owner.

| ID | Requirement |
|---|---|
| HOME-1 | A food bowl holds up to 16 dog foods. A hurt tamed dog within 16 blocks eats one to heal |
| HOME-2 | A water bowl is filled with a bucket or bottle. Drinking from a full bowl gives a short regeneration, and the bowl empties after a few drinks |
| HOME-3 | Hoppers fill food bowls. Bowls show how full they are |
| HOME-4 | A statue base (stone, gold or copper) copies one of your dogs within 8 blocks, or a dog's grave, in that material. Right-click cycles sit, stand and howl |
| HOME-5 | A statue keeps working after its dog is gone |
| HOME-6 | Copper statues age through vanilla's four stages, wax with honeycomb and scrape with an axe |
| HOME-7 | A blank canvas used on your dog captures its portrait at 1 by 1, 2 by 1 or 2 by 2, chosen while sneaking |
| HOME-8 | A portrait hangs like a painting, shows the dog sitting in a frame with its home biome behind, keeps the look it had, and works after the dog dies |

**Enforcement:** server. Portraits and statues store the captured appearance. The client draws them.

### 5.20 Send A Dog On A Job

**Actor:** Owner.

| ID | Requirement |
|---|---|
| WORK-1 | Explore, in the Actions ring, sends the dog away. The Pet Manager shows it as exploring |
| WORK-2 | It comes back the next morning, or when its owner next joins after that, muddy, with one find and a map |
| WORK-3 | Finds come from data loot tables per biome group, rolled from the biomes within about 1000 blocks of where it was sent, mostly common items and sometimes a rare one |
| WORK-4 | The map is centred on the furthest biome it visited, with a marker at the find |
| WORK-5 | One trip per dog at a time. A sitting, sleeping, ridden or undead dog can't go |
| WORK-6 | Deliver, in the Actions ring, sends a dog wearing saddlebags to an online player chosen from a list. It arrives after a travel time based on distance and waits for them to open its bags, then comes home the same way. Delivery works across dimensions and takes a fixed extra time |
| WORK-7 | A recipient can take items from the bags, but not change the dog's gear or commands |
| WORK-8 | If the recipient leaves mid-trip, the dog returns to its owner with everything |
| WORK-9 | An owned dog barks toward raiders, a patrol or a raid within 48 blocks, with a subtitle that says which way, until told to Sit or Stay |
| WORK-10 | A dog that fights in a raid its owner wins earns "Good Dog of the Village" and a rosette |
| WORK-11 | Advancements reward a first expedition and a rare find |

**Not supported:** sending another player's dog; deliveries to offline players.

**Enforcement:** server. A dog away is saved as a pet record, so nothing depends on chunks staying loaded.

### 5.21 Configure The Mod

**Actor:** Server operator, or the host of a singleplayer or LAN world.

| Setting | Default | Range | Command |
|---|---|---|---|
| Spawn Dogs in the Wild | on | on, off | `config spawn` |
| Spawn Rate | 100% | 0 to 500% | `config spawnrate` |
| Spawn Rate per breed | 100% | 0 to 500% | `config spawnrate <breed>` |
| Spawn Near Players | on | on, off | `config capindependentspawning` |
| Dog Graves on Death | on | on, off | `config graves` |
| Drop Leash on Play Mode | on | on, off | `config dropleashonplay` |
| Show Dog Names | on | on, off | `config shownames` |
| Auto-Sleep at Night | on | on, off | `config autosleep` |
| Auto-Sleep Range | 32 blocks | 4 to 128 | `config autosleeprange` |
| Bark Volume | 1.0 | 0.0 to 2.0 | `config barkvolume` |
| Howl Volume | 1.5 | 0.0 to 2.0 | `config howlvolume` |
| Fur Growth | on | on, off | `config furgrowth` |
| Fur Growth Speed | 100% | 0 to 500% | `config furgrowthrate` |
| Bad Haircut Chance | 100% | 0 to 500% | `config badhairdaychance` |
| Dire Wolf Growth Speed | 100% | 0 to 500% | `config direwolf growthrate` |
| Alpha Battles | 20 | 1 to 200 | `config direwolf battles` |
| Smallest Pack, Largest Pack | 5, 10 | 4 to 16 | `config direwolf smallestpack`, `largestpack` |
| Wild Packs Hunt | on | on, off | `config direwolf packhunting` |
| Packs Defend Each Other | on | on, off | `config direwolf packdefence` |
| Riding Stamina Drain | on | on, off | `config direwolf staminadrain` |
| Riding Stamina | 100% | 25 to 400% | `config direwolf staminacapacity` |
| Stamina Recovery | 100% | 25 to 400% | `config direwolf staminarecovery` |

New features add their own settings (SPAWN-10, GROOM-10, AGE-1 and others) under the same rules.

| ID | Requirement |
|---|---|
| CFG-1 | Every setting is reachable from `/dogsunleashed config`, the settings screen and the config file, through one validation path |
| CFG-2 | Settings are saved per world as JSON in the world folder |
| CFG-3 | A missing file is created with defaults. A broken file is kept aside under a new name and defaults are used |
| CFG-4 | Out-of-range values in the file are clamped on load. Out-of-range values from a command are refused with a message. Values that are not finite numbers are refused everywhere |
| CFG-5 | Smallest Pack can't exceed Largest Pack |
| CFG-6 | Spawn settings apply to the extra spawner at once, and to biome spawns after a world restart. The screen says so |
| CFG-7 | Settings sync to every player, so the screen shows live values |
| CFG-8 | `config reset` restores every default |
| CFG-9 | The settings screen opens from Mod Menu or the Pet Manager. Without permission it is read-only |
| CFG-10 | `/dogsunleashed status` shows the current settings. `list <player>`, `summon <player> <petId>` and `find <petId>` let an operator look up and call any player's dogs |

**Enforcement:** server. See PERM-1.

### 5.22 Work Through The Advancements

**Actor:** Any player.

The mod has its own advancement tab, on a pink background scattered with paw prints. Advancements for a feature are stated with that feature. These are the ones that belong to no single feature.

| ID | Advancement | Earned by |
|---|---|---|
| ADV-1 | Dogs Unleashed | Opening the tab: the root |
| ADV-2 | Best Friend | Taming any breed |
| ADV-3 | The Whole Pack | Taming every founding breed |
| ADV-4 | Cherry Companion | Taming a shiba inu |
| ADV-5 | Fetch! | A dog bringing a toy back |
| ADV-6 | Sweet Dreams | A dog falling asleep in its bed |
| ADV-7 | Forever in Our Hearts | Holding a dog's grave as an item |

The tab as a whole:

| ID | Requirement |
|---|---|
| ADV-8 | The tab branches by theme from Best Friend. Advancements that build on each other chain: The Whole Pack before Winter is Coming, Good Dog! before Show Dog, Mush! before Full Team, Off Exploring before Look What I Found, Puppy Love before Mixed Heritage, and Forever in Our Hearts before Second Leash of Life before Best Friend Again |
| ADV-9 | Bad Hair Day, Second Leash of Life and Best Friend Again stay hidden until earned |
| ADV-10 | Goal advancements give 50 experience and challenge advancements give 100 |

**Enforcement:** server.

## 6. Permissions

The adversary is a modified client that can send any packet with any value. Hiding or greying out a control is never how a rule is enforced: every rule here is checked by the server.

### Capability Matrix

| Capability | Owner | Other player | Operator (level 2) | World host |
|---|---|---|---|---|
| Open the command wheel, give commands | yes | no | no | no |
| Point with a whistle | yes, own whistle and dog | no | no | no |
| Open the gear screen, equip, repair, trim | yes, within 8 blocks | no | no | no |
| Dye the collar | yes | no | no | no |
| Start breeding | yes | no | no | no |
| Heal with food | yes | yes | yes | yes |
| Start a cure | yes | yes | yes | yes |
| Place or take a grave totem, trigger the ritual | yes | yes | yes | yes |
| Inspect a dog | sees the same in pet details (FIND-8) | yes, read-only | yes | yes |
| Rename, summon, list in Pet Manager | yes | no | no | no |
| View a family tree | yes, or a dog related to one they own | if related to a dog they own | same as a player | same as a player |
| Assign or unassign a bed | yes | no | no | no |
| Recolour a bed or house | yes | yes | yes | yes |
| Give a scent, teach tricks, hitch to a sled | yes | no | no | no |
| Take items from a delivery | the sender | the named recipient only | no | no |
| Change settings from the screen | no | no | yes | yes |
| Change settings by command, `status` | no | no | yes | no, unless also an operator |
| `list`, `summon`, `find` any player's dogs | no | no | yes | no, unless also an operator |

### Requirements

| ID | Requirement |
|---|---|
| PERM-1 | `/dogsunleashed config` and `status` need operator level 2. The settings screen accepts operator level 2 or the host of the integrated server. A refused change re-sends the current settings to that player |
| PERM-2 | Every owner-only action checks, on the server, that the sender owns the dog and that the dog is alive and in the sender's world |
| PERM-3 | A dog has exactly one owner. Only taming, trust and breeding (FAM-3) set it |
| PERM-4 | Anything a stranger may do to a tamed dog (heal it, start a cure) never changes its ownership |
| PERM-5 | Family tree access is checked through lineage on the server, visiting at most 512 dogs |
| PERM-6 | The server never accepts a target, position or value from a client that it can work out itself |
| PERM-7 | Wild dogs belong to nobody. Only the server's rules act on them |

## 7. Client And Server

There is no singleplayer mode in this mod's design. A singleplayer world is an integrated server with one client, and runs the same code down the same path as a dedicated server. Every requirement names which side decides, never how many players are present, so a rule can't be skipped because a player seems to be alone.

### The Boundary

| Server owns | Client does |
|---|---|
| Spawning, taming, trust, bond, traits, genes | Renders dogs, coats, overlays, armour, gear and cosmetics |
| Commands, goals, targets, pointing targets | Raycasts what the player looks at and sends it |
| Gear slots and their effects | Draws screens: wheel, gear, Pet Manager, family tree, inspect, settings |
| Fetch sessions, tug results, sled movement rules | Predicts a stick throw from synced play state |
| Growth, riding stamina, sprint lockout | Sends whether sprint is held, and draws the stamina bar |
| Pet records, caches, grudges, away dogs | Holds the Pet Manager key and the first-run toast state |
| Settings and their validation | Shows live settings after sync |
| Raids, expeditions, deliveries | Draws paw prints, portraits and statues from synced data |

### Concurrency

| ID | Requirement |
|---|---|
| CS-1 | Two players acting on one dog in the same tick get one consistent result, and only the owner's actions apply |
| CS-2 | A hopper and a hand filling the same food bowl never duplicate or lose food |
| CS-3 | Two players breaking or using one grave, bed or statue at once never duplicate its item |
| CS-4 | A dog away on a job cannot be summoned, pointed, ridden, bred or hurt until it returns |

### Per-Player State

| ID | Requirement |
|---|---|
| CS-5 | Each player receives only their own pet records, trust values and Pet Manager filters |
| CS-6 | Point markers, the stamina bar and the tug bar are shown only to the player concerned |
| CS-7 | Inspect cards show trust only for the player looking |

### Side Separation

| ID | Requirement |
|---|---|
| CS-8 | A dedicated server starts without loading any client class. A build check enforces it |
| CS-9 | Mod Menu, JEI and EMI classes load only when those mods are present, and only on the client |

## 8. Non-Functional Requirements

### Safety

| ID | Requirement |
|---|---|
| SAFE-1 | No game mechanic duplicates or deletes an item the mod handles: fetch toys, gear, saddlebag contents, deliveries, bowls, caches, totems, graves |
| SAFE-2 | No mechanic duplicates or deletes a dog. A dog moved between worlds keeps its id and leaves no twin |
| SAFE-3 | A bad config file never stops a server starting (CFG-3, CFG-4) |
| SAFE-4 | Every payload is size-limited (names are covered by TAME-3): searches 64, breed lists 8, family lists 32, owner names 16 |

### Performance

| ID | Requirement |
|---|---|
| PERF-1 | Following and summoning load at most the chunk the dog was last seen in, for at most 20 ticks |
| PERF-2 | Scent searches never scan a volume in one tick. They work in budgeted slices, and structure lookups run at most once per scent |
| PERF-3 | The raid scan runs at most once a second, and not at all when no raider is loaded nearby |
| PERF-4 | The extra spawner runs at most once every 60 seconds, with one try per whole multiple of the highest spawn rate |
| PERF-5 | Projected overlays and portraits are built once per look and rebuilt only after a resource reload |

### Persistence

| What | Chunk unload | World reload | Death | Dimension change | Power loss |
|---|---|---|---|---|---|
| Dog (command, gear, fur, growth, trust, stamina, genes, tricks) | kept | kept | gear drops, the rest returns on resurrection except trust, tricks kept | kept, same id | kept to the last save |
| Pet record (name, owner, state, place, family, bond, age) | kept, place updated | kept | kept, state changes | kept, place updated | kept to the last save |
| Play mode | ends | ends | ends | ends | ends |
| Bed assignment | kept | kept | cleared | kept, with its dimension | kept |
| Stay and Guard anchor | kept | kept | cleared | moved to the new place | kept |
| Pack grudges | kept | kept | kept (on the pack) | kept | kept |
| Caches, statues, portraits, bowls | kept | kept | kept | kept | kept |
| A dog away on a job | kept as a pet record | kept | cannot die while away | not applicable | kept, returns on next schedule |
| Settings | not applicable | kept | not applicable | not applicable | written atomically |

### Look And Sound

These follow the Dog Rig Profiles plan. Where that plan and a later decision record differ, the record wins: fur is hand made rather than generated (ADR 0005), and armour is painted onto each breed's torso rather than projected from a sheet (ADR 0003).

| ID | Requirement |
|---|---|
| LOOK-1 | Each breed has its own rig, animation set, painted coats and collar mask. No breed shares a mesh or a texture layout with another |
| LOOK-2 | Each rig has a profile that names its bones by role: head, eyes, mouth, neck, torso, tail, each ear and each leg, plus groups such as fur. Features find a body part only through the profile, never through bone names, cube numbers or pixel coordinates in code. A profile naming a bone the rig lacks fails the build |
| LOOK-3 | The texture region for each role is worked out from the rig's geometry, never measured by hand |
| LOOK-4 | An effect shown on every breed (undead, mud, grey muzzle) is painted once on a shared sheet and projected onto each rig (ADR 0006). A rig may override any role with hand-made art where the projection looks wrong |
| LOOK-5 | Fur is hand made on each rig and named in its profile (ADR 0005). Armour is painted onto each breed's own torso (ADR 0003). A pendant is one model, placed from the collar on every breed (ADR 0002, 0004). Harnesses, saddlebags and cosmetics are placed from profile roles |
| LOOK-6 | Every breed answers to the same animation names. A new animation (sniff, dig, play bow, cower, growl, beg, roll over, play dead, tug, run) is added under one name on every rig that uses it, using rotation rather than position keys where it must hold up across sizes |
| LOOK-7 | Adding a breed takes a breed preset entry, a rig, coats and a profile. Armour, pendants, carried items, fur, overlays and gear then work on it from the profile, with no per-breed code in any feature |
| LOOK-8 | A dire wolf is one rig at every growth stage, grown by scaling its profile roles (ADR 0007) |
| LOOK-9 | A cross (FAM-7) stands on one parent's rig. Ears are inherited whole and never averaged. Colour and shape are inherited separately. Proportions are scaled while drawing, never by changing the model every dog of that breed shares |
| LOOK-10 | A new feature never changes how an existing breed looks or moves unless that is its purpose. Every rendering change is checked against `main` |
| LOOK-11 | Blocks and items look like vanilla ones wherever they are held or shown: GUI, first and third person, item frames and the ground |
| LOOK-12 | Animal voices come from real recordings, never synthesis |
| LOOK-13 | Every breed has a paw raise and more than one sleep pose |

### Accessibility

| ID | Requirement |
|---|---|
| ACC-1 | No state relies on colour alone: rarity names show beside rarity colours, and greyed-out sectors say why |
| ACC-2 | No meaning relies on particles alone. Hearts, smoke and splashes are decoration beside a pose, a sound or a message |
| ACC-3 | Directional barks have subtitles that say which way |
| ACC-4 | Every string is translated into every shipped language |

### Compatibility

| ID | Requirement |
|---|---|
| COMPAT-1 | Minecraft 1.21.1, Java 21, Fabric Loader 0.16.5 or later, Fabric API 0.107.0+1.21.1 or later, GeckoLib 4.7 or later |
| COMPAT-2 | Mod Menu, JEI and EMI are optional. With them, settings and info pages appear. Without them, the mod works fully |
| COMPAT-3 | Data-driven content (scent rules, loot tables, tags, spawn ground) can be changed by data packs |
| COMPAT-4 | Shipped languages: English, German, Spanish, French, Brazilian Portuguese, Russian and Simplified Chinese |
| COMPAT-5 | Every declared dependency is used |

## 9. Release Criteria

- Every use case's requirements hold on a dedicated server with two clients and in a singleplayer world, through the same code.
- No item, dog or gear can be duplicated or lost by any mechanic the mod adds (SAFE-1, SAFE-2).
- A dedicated server starts with no client classes loaded (CS-8).
- Every shipped breed looks and moves exactly as it did in the previous release, apart from changes a release sets out to make.
- Every string is translated in every shipped language.
- Every setting is reachable from the command line.
- A world from the previous release opens with every dog, record and setting intact.

## 10. References

- **Progress:** the [project board](https://github.com/users/grabartley/projects/1). This document holds no status.
- **Acceptance criteria:** each requirement's owning issue on [GitHub](https://github.com/grabartley/minecraft-dogs-unleashed/issues). This document does not repeat them.
- **Reasoning:** the decision records in [`docs/adr/`](adr/).
- **Engineering standards:** [`docs/standards.md`](standards.md).
- **Player-facing summary:** the [README](../README.md).
