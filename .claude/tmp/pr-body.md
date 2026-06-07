Generalize fetch handling so registered fetch items can reuse the same dog play flow.

**What's included:**
- add `FetchItemType`, `FetchTypes`, and `FetchProjectileEntity` so fetch-capable items are registered in one place
- refactor `UnleashedDogEntity`, fetch goals, and `TennisBallProjectileEntity` to use generic fetch state instead of tennis-ball-only checks
- replace `TennisBallTemptGoal` with `FetchTemptGoal` and `DogCarryBallLayer` with `DogCarryFetchItemLayer`
- add focused fetch-system tests covering the new registry and projectile source contracts

Closes #129
