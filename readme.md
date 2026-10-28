<div align="center">
  <h1>Fidorial</h1>

  <p>
    <em>A Minecraft server written from scratch in Java — no Mojang code, no fork.</em><br/>
    <em>Fidorial is an independent project, not affiliated with, associated with, or endorsed by Mojang Studios or Microsoft. Minecraft is a trademark of Mojang Studios, owned by Microsoft.</em>
  </p>

[![Minecraft](https://img.shields.io/badge/Minecraft-26.4_snapshot_6-purple.svg)](https://github.com/Euphillya/Fidorial)
[![Java](https://img.shields.io/badge/Java-25+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENCE)
[![Servers](https://img.shields.io/endpoint?url=https%3A%2F%2Ffaststats.dev%2Fapi%2Fshields%2Ffidorial%3Fmetric%3Dservers&style=flat)](https://faststats.dev/project/fidorial)
[![Downloads](https://img.shields.io/endpoint?url=https%3A%2F%2Ffaststats.dev%2Fapi%2Fshields%2Ffidorial%3Fmetric%3Ddownloads&style=flat)](https://faststats.dev/project/fidorial)

[Documentation](https://www.fidorial.moe/) • [Discord](https://discord.gg/uUJQEB7XNN) • [Français](readme.fr.md)

[![Servers & Players](https://faststats.dev/embed/default:d01e30ea-8ddc-40f6-b773-24d369336950:servers-and-players.svg?w=960&h=340&theme=dark)](https://faststats.dev/project/fidorial/minecraft-plugin)

</div>

---

Fidorial is built on a regionized, multithreaded foundation designed from day one for people who want to
modify the game. There is no Forge, no Fabric, no Mixin: plugins observe **events** and replace **services**,
so a plugin can swap an entire subsystem without patching a single line of server code.

> ⚠️ **Early stage.** Gameplay coverage is still partial and everything can change, including the plugin API.

## Features

**Protocol & network**

- Hand-written implementation of Minecraft **26.4 Snapshot 6** (protocol **1073742168**): handshake, status, login,
  configuration, play
- Mojang authentication, packet encryption and compression; offline mode for testing
- Velocity modern forwarding (`proxy-mode=VELOCITY`)
- Netty with epoll, kqueue and optional io_uring transports

**World**

- Multiple worlds, Anvil persistence (region files, NBT), asynchronous chunk loading and streaming around players
- Superflat generator by default; plugins provide their own through the `WorldGenerator` service
- Block placement and breaking, block entities, containers and ender chests, explosions
- Water and lava simulation: sources, downward-first flow, per-fluid drop-off, obsidian and cobblestone interaction
- Block and sky light engine
- Day/night cycle and vanilla-style weather, both persisted in `level.dat`

**Entities & gameplay**

- ~90 entity types with their variants and metadata, spawnable with `/summon`
- Goal-based AI (stroll, panic, tempt, follow parent, melee attack, break door…) with A\* pathfinding on a dedicated
  worker pool
- Combat: damage, knockback, death, respawn, PvP switch
- Four game modes, creative inventory, per-player persistence of inventory, ender chest and player data
- Chat, boss bars, sounds, and translations (`en_us`, `fr_fr`)

**Platform**

- Folia-inspired regionized scheduler: the world is split into independent regions (32×32 chunks by default,
  configurable with `threading.region-section-shift`), each ticking at 20 TPS on its own thread
- Brigadier commands, in game and in an interactive console with completion and highlighting: `/gamemode` (`/gm`),
  `/weather` (`/w`), `/time`, `/gamerule`, `/summon`, `/tps`, `/op`, `/deop`, `/bossbar`, `/stop` (`/s`)
- Permission system with operator list and plugin-declared nodes
- Plugin API based on JPMS modules, with [Adventure](https://docs.advntr.dev/) for all text
- Scenario test harness running against a real server
- Pseudonymous metrics via [FastStats](https://faststats.dev/project/fidorial/minecraft-plugin)

## Getting started

Requires **Java 25** or newer.

```bash
git clone https://github.com/Euphillya/Fidorial.git
cd Fidorial
./gradlew :fidorial-server:shadowJar
java -jar fidorial-server/build/libs/Fidorial-*.jar
```

## Writing a plugin

The API is published on [repo.euphyllia.moe](https://repo.euphyllia.moe) and provided by the server at runtime:

```kotlin
repositories {
    maven("https://repo.euphyllia.moe/repository/maven-public/")
}

dependencies {
    compileOnly("fr.fidorial:fidorial-api:26.4-snapshot-6-build.+")
}
```

## Project structure

| Module                        | Purpose                                                                                       |
|-------------------------------|-----------------------------------------------------------------------------------------------|
| `fidorial-api`                | Public API: events, services, plugins, commands, entities, scheduler, registries, world types |
| `fidorial-auth`               | Mojang session service and encryption utilities                                               |
| `fidorial-server`             | The server itself: network, protocol, world, entities, AI, commands                           |
| `fidorial-registry-generator` | Gradle plugin generating registries and packet catalogs from the vanilla data reports         |
| `fidorial-test-plugin`        | Reference plugin exercising the API (custom generator, commands, scenario tests)              |
| `build-logic`                 | Shared Gradle conventions (Spotless, toolchain)                                               |

Plugins should only import from `fidorial-api`. Needing something out of `fidorial-server` is a gap in the
API — please open an issue.

## Development

```bash
./gradlew :fidorial-server:run          # run a dev server (deploys the test plugin)
./gradlew :fidorial-server:testScenarios # scenario tests against a real server
./gradlew spotlessApply                  # format (Palantir Java Format)
./gradlew spotlessCheck                  # what CI enforces
```

CI builds on JDK 25, checks formatting, runs the tests and publishes the fat jar: a prerelease on every push
to `master`, a stable release on `v*` tags.

## Contributing

Contributions are open to everyone — code, testing, documentation or ideas. Pull requests and issues are
welcome on [GitHub](https://github.com/Euphillya/Fidorial), and discussion happens on
[Discord](https://discord.gg/uUJQEB7XNN).

## License

[MIT](LICENCE) © 2026 Euphyllia Bierque
