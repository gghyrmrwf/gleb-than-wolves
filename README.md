# Gleb Than Wolves

A Minecraft mod for **Minecraft 1.20.1 + Forge 47.x**.

This is a parody / homage of [Better Than Wolves](https://www.sargunster.com/btw/) by FlowerChild,
rebuilt phase-by-phase on a modern modding stack. The goal is a hardcore-survival overhaul that
makes vanilla progression noticeably harder, layered with mechanical-power machinery
(axles, water wheels, mill stones, etc.) in the BTW spirit.

## Status

Phase 1 complete (1.1 → 1.14 + post-1.14 fix). ~30 hardcore-survival mechanics
implemented on top of vanilla. Phase 2 (full progression redesign) is planned
but not started.

## Documentation

Read these in order before extending the mod:

- **[CHANGELOG.md](CHANGELOG.md)** — every phase, every mechanic, every parameter
- **[ARCHITECTURE.md](ARCHITECTURE.md)** — file structure, event flow, patterns, gotchas
- **[ROADMAP.md](ROADMAP.md)** — Phase 2.0+ design, principles, status of all
  brainstormed mechanics
- **[HISTORY.md](HISTORY.md)** — chronology of how the mod was built, including
  user requests verbatim and bugs encountered

## Roadmap (high-level)

- **Phase 0**: Forge MDK scaffold (done).
- **Phase 1.1–1.14**: Hardcore tweaks — bushcraft, hunger, day/night, mob aggro,
  environmental hazards, perception, item rules (done).
- **Phase 2.0+**: Progression redesign — custom item tiers, bloomery, hammer,
  alternative iron path, restricted trading. See `ROADMAP.md`.
- **Phase 3.0+**: Other dimensions — Nether and End rebuilt as proper
  progression layers.

## Requirements

- JDK 17
- ~4 GB RAM free for the dev client

## Build

```bash
./gradlew build
```

The built jar lands in `build/libs/glebthanwolves-<version>.jar` and is what you drop into the
`mods/` folder of a Minecraft 1.20.1 + Forge 47.x install.

## Run dev client

```bash
./gradlew runClient
```

This launches a Minecraft client with the mod loaded. Look for **Gleb Than Wolves** in the
**Mods** screen on the title menu.

## Run dev server

```bash
./gradlew runServer
```

(First run will exit asking you to accept the EULA — set `eula=true` in
`run/eula.txt` and re-run.)

## License

MIT — see [`LICENSE`](LICENSE).

`LICENSE.txt` (LGPL) is the original Forge MDK template license and applies only to the
gradle template files shipped with the MDK.
