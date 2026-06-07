Fix stale fetch sessions on unloads and let players hand play mode to a different dog cleanly.

**What's included:**
- clean up `ACTIVE_PLAY_SESSIONS` when `UnleashedDogEntity` is removed for non-`KILLED` reasons
- protect `endPlayMode()` so one dog's unload only removes its own `(player, dog)` session entry
- end other nearby fetch sessions for the same owner before starting play mode on a new dog
- add `ActivePlaySessionsTest` coverage for session cleanup and removal behavior

Closes #114
