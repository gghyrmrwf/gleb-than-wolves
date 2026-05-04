# Gleb Than Wolves

A Minecraft mod for **Minecraft 1.20.1 + Forge 47.x**.

This is a parody / homage of [Better Than Wolves](https://www.sargunster.com/btw/) by FlowerChild,
rebuilt phase-by-phase on a modern modding stack. The goal is a hardcore-survival overhaul that
makes vanilla progression noticeably harder, layered with mechanical-power machinery
(axles, water wheels, mill stones, etc.) in the BTW spirit.

## Status

Phase 0 — empty mod scaffold. The mod registers itself with Forge, logs a startup line
and shows up in the in-game **Mods** menu. No mechanics yet.

## Roadmap

- **Phase 0**: Forge MDK scaffold, mod id `glebthanwolves`. *(this PR)*
- **Phase 1**: Hardcore-survival changes (stumps, hunger, mining/sleeping rules).
- **Phase 2**: Mechanical-power system (axle, hand crank, water wheel, gearbox).
- **Phase 3**: Production blocks (mill stone, mechanical saw, crucible, bloomery).
- **Phase 4**: New resources (hemp, rope, wicker, steel).
- **Phase 5**: Cosmetic / parody layer.

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
