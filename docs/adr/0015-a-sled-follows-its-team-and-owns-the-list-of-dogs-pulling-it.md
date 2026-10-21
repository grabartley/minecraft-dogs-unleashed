# ADR 0015: A sled follows its team and owns the list of dogs pulling it

- **Status:** Accepted
- **Date:** 2026-10-21

## Context

Up to six huskies or dire wolves pull a sled with a player on it. Minecraft 1.21.1 leads have one holder each and only pull a mob back towards that holder once it strays, so there is nothing in vanilla that lets several animals drag one entity. A boat or a horse is moved by its rider's client, which works for one mount but would let the client decide where the team goes and how fast, and the dogs walking on the server would fall behind or run ahead of it.

## Decision

The sled is a plain entity moved only by the server. It keeps its pullers as a list of dog and owner ids, saved with the sled, and every tick it finds the dogs that are loaded and drops any that died, changed owner, were put on someone else's lead or ended up far away.

The rider's movement keys, which vanilla already sends to the server for any vehicle, steer a heading and ask for speed. Sprinting has its own small packet, because vanilla only lets a rider sprint on a vehicle their own client moves. The speed comes from the team's size and the ground under the dogs. Each dog is given a slot ahead of the sled, in pairs, and a goal walks it there at the pace vanilla walking needs to cover that distance. The sled then moves towards a point behind the dogs' average position, never faster than the team's speed. If the dogs are blocked, the sled stops with them.

Hitching takes the lead off each dog and the sled keeps it. Unhitching puts the lead back on the dog, held by its owner, and a dog that dies or leaves the team drops its lead, so leads are never made or lost.

Each dog's sled stamina lives in a dog component and uses the riding stamina rules, with the sprint drain shared across the team. The sled sends the team's total to its rider for the stamina bar.

The client draws the harness lines after all entities have rendered. Each line runs from a `harness_N` locator in the sled's model to the root of the dog's tail, which the dog renderer records while it draws the dog, so the line meets the dog where its rig actually is that frame.

## Consequences

The server decides everything about a run, so two players see the same team, and a modified client can only steer and ask to sprint. Movement over the network is interpolated on clients like a minecart rather than predicted like a boat, so the rider's view trails the server by a tick or two.

The sled does not path. A team that runs into a wall stops, and the rider steers around it. Hitched dogs do not sit or follow their owner, and pick their command up again once unhitched. An owner who teleports leaves them with the sled.

A dog's body length is estimated from its hitbox width to space the team, since the server cannot read the rig. A breed with an unusually long or short body for its width would sit a little close to or far from the dog ahead.
