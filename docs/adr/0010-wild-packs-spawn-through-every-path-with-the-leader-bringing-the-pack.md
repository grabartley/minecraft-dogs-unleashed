# ADR 0010: Wild packs spawn through every path, with the leader bringing the pack

- **Status:** Accepted
- **Date:** 2026-10-18
- **Supersedes:** the spawning parts of [ADR 0007](0007-dire-wolves-grow-by-scaling-profile-slots-and-spawn-only-as-whole-packs.md)

## Context

Wild dogs reach the world through two layers. Vanilla biome spawning places them when chunks generate and while the creature cap has room. The top-up spawner keeps them turning up near players in explored land, where the creature cap stays full.

ADR 0007 kept dire wolves out of vanilla biome spawning so a pack would never land short, and made the top-up spawner their only source. In play that made dire wolves close to impossible to find. They never came from chunk generation, which is where vanilla puts most animals. The top-up spawner, built as a slow top-up, makes one try every one to two minutes. And the spawn rate settings could only lower its rate, so turning a breed above 100% did nothing for a breed it served alone.

## Decision

Every breed uses both layers the same way. A breed that lives in packs is in its biomes' vanilla spawn pools as a single pack leader. When a wild one spawns by any natural path, it spawns the rest of its pack around itself in `initialize`, the way vanilla jockeys bring their riders. Its spawn rule only passes where its smallest pack fits, so a pack still never lands short. Members stay in the leader's chunk, because chunk generation can only add entities there. Vanilla natural spawning places at most one pack leader per chunk per pass.

A breed's spawn rate means the same on both layers: about that many times as many as default. On the vanilla layer it scales the pool weight. On the top-up spawner it scales the number of tries per cycle and how often a try is kept, so each breed's rate is independent of the others.

Every natural group of any breed shares a group id, and the top-up spawner's local limit counts groups rather than single dogs, so a pack of ten counts the same as a pair of huskies. Pack behaviour stays tied to the breed's nature, not to having a group id.

## Consequences

Dire wolves appear in freshly generated cold biomes and keep turning up near players, and the spawn rate settings work for them as they do for every other breed.

The vanilla creature cap counts only the leader when deciding whether to place it, so a pack can push the cap over its limit for a while. The pack room check runs on every vanilla attempt to place a leader, which costs more than an ordinary spawn rule, so it stops as soon as it has found enough spots.

Untamed dogs saved before group ids existed each count as a group of their own, so in an old world a couple of stray untamed dogs nearby can pause the top-up spawner until they wander off or are tamed.

With packs of five to ten coming from chunk generation, there are more dire wolves in the world than before. Their pool weight is what to tune if they stop feeling rare.
