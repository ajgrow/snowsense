# IN2000 – Android app project

This is the repo for team 29's Android app project.

## Team-members.

- Alex
- Gustav
- Larissa
- Lisa
- Rukman
- Sondre

## Slab Avalanche Risk Assessment

A [slab avalanche](https://avalanche.org/avalanche-encyclopedia/snowpack/slab/) occurs when a cohesive plate of snow fractures away from a [weak layer](https://avalanche.org/avalanche-encyclopedia/snowpack/weak-layer/) beneath it. Three factors must line up: a weak layer, a cohesive slab on top, and enough stress to break the bond between them. Our model estimates this from 30 days of daily temperature and precipitation data from the Frost API.

We score four normalized [0, 1] factors in two groups:

**Structural factors** — does the snowpack contain a dangerous layered structure?

| Factor | Input |
|---|---|
| Weak layer presence | Cold-weather LWE¹ over 30 days |
| Slab formation | Warmer precip deposited after weak layer |

- **Weak layer presence** ([persistent weak layers](https://avalanche.org/avalanche-encyclopedia/snowpack/weak-layer/persistent-weak-layers/)) — Precipitation deposited in very cold conditions (≤ −5 °C) undergoes [faceting](https://avalanche.org/avalanche-encyclopedia/snowpack/snow-metamorphism/faceting/): strong temperature gradients transform snow grains into angular, poorly bonded crystals that can persist as weaknesses for a long time, possibly the entire season.

- **Slab formation** ([slab](https://avalanche.org/avalanche-encyclopedia/snowpack/slab/)) — For a slab avalanche to release, the slab "needs to be more cohesive than the weak layer and have sufficient tensile strength to help drive a fracture across the slope." We track warmer precipitation deposited after a weak layer; once a cohesive backbone forms, all subsequent precipitation adds load.

**Trigger factors** — what conditions could push a dangerous snowpack toward failure?

| Factor | Input |
|---|---|
| Recent loading | Cold-weather precip, last 3 days |
| Rapid warming | Temperature increase, last 3 days |

- **Recent loading** ([loading / loading rate](https://avalanche.org/avalanche-encyclopedia/snowpack/snowpack-observations/signs-of-instability-red-flags/heavy-snowfall-or-rain/loading-loading-rate/)) — Snowpacks "are sensitive to the amount of loading and also the loading rate." About 25 mm LWE¹ in 24 hours is a standard red flag.

- **Rapid warming** ([rapid warming](https://avalanche.org/avalanche-encyclopedia/snowpack/snowpack-observations/signs-of-instability-red-flags/rapid-warming/)) — "A dramatic and sudden rise in temperature to the point where the snow surface becomes wet or damp" weakens bonds in the snowpack and can trigger release.

**Scoring.** Structural factors are combined with a geometric mean — if either is absent the structural score is zero and the overall risk is Low. Trigger factors are combined so that either alone can elevate risk. The final score blends both: structure alone gives a baseline risk (up to Moderate), since a skier can act as a trigger even in calm weather; active weather triggers amplify the score toward High and Very High.

Danger levels follow the [European Avalanche Danger Scale](https://www.avalanches.org/standards/avalanche-danger-scale/) (EAWS):

| Score | Level | Description |
|---|---|---|
| < 0.10 | 1 – Low | Generally stable conditions |
| 0.10 – 0.25 | 2 – Moderate | Triggering possible from high additional loads on steep slopes |
| 0.25 – 0.50 | 3 – Considerable | Triggering possible even from low additional loads on steep slopes |
| 0.50 – 0.75 | 4 – High | Triggering likely even from low additional loads on many steep slopes |
| ≥ 0.75 | 5 – Very High | Numerous large natural avalanches expected |

**Limitations.** The 30-day window cannot detect deep persistent weak layers that formed earlier in the season. The model uses daily averages from a single weather station and does not account for wind loading, solar aspect, or sub-daily temperature swings.

---

¹ LWE (Liquid Water Equivalent) — mm of water if the precipitation were melted. Equivalent to SWE (Snow Water Equivalent) in avalanche literature. 1 mm LWE ≈ 1 cm of cold dry snow.
