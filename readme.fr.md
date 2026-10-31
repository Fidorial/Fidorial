<div align="center">
  <h1>Fidorial</h1>

  <p>
    <em>Un serveur Minecraft écrit de zéro en Java — aucun code Mojang, aucun fork.</em><br/>
    <em>Fidorial est un projet indépendant, non affilié, associé ou approuvé par Mojang Studios ou Microsoft. Minecraft est une marque de Mojang Studios, propriété de Microsoft.</em>
</p>

[![Minecraft](https://img.shields.io/badge/Minecraft-26.4_snapshot_6-purple.svg)](https://github.com/Euphillya/Fidorial)
[![Java](https://img.shields.io/badge/Java-25+-orange.svg)](https://www.oracle.com/java/)
[![Licence](https://img.shields.io/badge/license-MIT%20%2F%20GPLv3-blue)](LICENCE.md)
[![Serveurs](https://img.shields.io/endpoint?url=https%3A%2F%2Ffaststats.dev%2Fapi%2Fshields%2Ffidorial%3Fmetric%3Dservers&style=flat)](https://faststats.dev/project/fidorial)
[![Téléchargements](https://img.shields.io/endpoint?url=https%3A%2F%2Ffaststats.dev%2Fapi%2Fshields%2Ffidorial%3Fmetric%3Ddownloads&style=flat)](https://faststats.dev/project/fidorial)

[Documentation](https://www.fidorial.moe/) • [Discord](https://discord.gg/uUJQEB7XNN) • [English](readme.md)

[![Serveurs & Joueurs](https://faststats.dev/embed/default:d01e30ea-8ddc-40f6-b773-24d369336950:servers-and-players.svg?w=960&h=340&theme=dark)](https://faststats.dev/project/fidorial/minecraft-plugin)

</div>

---

Fidorial repose sur des fondations régionalisées et multithread, pensées dès le départ pour celles et ceux qui
veulent modifier le jeu. Ni Forge, ni Fabric, ni Mixin : un plugin s'abonne à des **événements** et remplace des
**services**, ce qui lui permet d'échanger un sous-système entier sans patcher une ligne de code serveur.

> ⚠️ **Projet à ses débuts.** Le gameplay reste partiel et tout peut changer, y compris l'API de plugins.

## Fonctionnalités

**Protocole & réseau**

- Implémentation maison de Minecraft **26.4 Snapshot 6** (protocole **1073742168**) : handshake, status, login,
  configuration, play
- Authentification Mojang, chiffrement et compression des paquets ; mode hors-ligne pour les tests
- Velocity modern forwarding (`proxy-mode=VELOCITY`)
- Netty avec transports epoll, kqueue et io_uring optionnel

**Monde**

- Plusieurs mondes, persistance au format Anvil (region files, NBT), chargement asynchrone des chunks et streaming
  autour des joueurs
- Générateur superflat par défaut ; les plugins fournissent le leur via le service `WorldGenerator`
- Pose et casse de blocs, block entities, conteneurs et ender chests, explosions
- Simulation de l'eau et de la lave : sources, écoulement vertical prioritaire, perte de niveau propre à chaque fluide,
  interaction obsidienne/cobblestone
- Moteur de lumière (blocs et ciel)
- Cycle jour/nuit et météo façon vanilla, tous deux persistés dans le `level.dat`

**Entités & gameplay**

- ~90 types d'entités avec leurs variantes et métadonnées, invocables avec `/summon`
- IA à base de goals (balade, panique, appât, suivi du parent, attaque au corps à corps, ouverture de portes…) et
  pathfinding A\* sur un pool de threads dédié
- Combat : dégâts, knockback, mort, respawn, interrupteur PvP
- Quatre modes de jeu, inventaire créatif, persistance par joueur de l'inventaire, de l'ender chest et des données
- Chat, boss bars, sons et traductions (`en_us`, `fr_fr`)

**Plateforme**

- Scheduler régionalisé inspiré de Folia : le monde est découpé en régions indépendantes (32×32 chunks par défaut,
  configurable avec `threading.region-section-shift`), chacune tickée à 20 TPS sur son propre thread
- Commandes Brigadier, en jeu et dans une console interactive avec complétion et coloration : `/gamemode` (`/gm`),
  `/weather` (`/w`), `/time`, `/gamerule`, `/summon`, `/tps`, `/op`, `/deop`, `/bossbar`, `/stop` (`/s`)
- Système de permissions avec liste d'opérateurs et nœuds déclarés par les plugins
- API de plugins basée sur les modules JPMS, avec [Adventure](https://docs.advntr.dev/) pour tout le texte
- Harnais de tests scénario exécutés contre un vrai serveur
- Métriques pseudonyme via [FastStats](https://faststats.dev/project/fidorial/minecraft-plugin)

## Démarrage

Nécessite **Java 25** ou plus récent.

```bash
git clone https://github.com/Euphillya/Fidorial.git
cd Fidorial
./gradlew :fidorial-server:shadowJar
java -jar fidorial-server/build/libs/Fidorial-*.jar
```

## Écrire un plugin

L'API est publiée sur [repo.euphyllia.moe](https://repo.euphyllia.moe) et fournie par le serveur à l'exécution :

```kotlin
repositories {
    maven("https://repo.euphyllia.moe/repository/maven-public/")
}

dependencies {
    compileOnly("fr.fidorial:fidorial-api:26.4-snapshot-6-build.+")
}
```

## Structure du projet

| Module                        | Rôle                                                                                                   |
|-------------------------------|--------------------------------------------------------------------------------------------------------|
| `fidorial-api`                | API publique : événements, services, plugins, commandes, entités, scheduler, registres, types du monde |
| `fidorial-auth`               | Service de session Mojang et utilitaires de chiffrement                                                |
| `fidorial-server`             | Le serveur lui-même : réseau, protocole, monde, entités, IA, commandes                                 |
| `fidorial-registry-generator` | Plugin Gradle générant les registres et catalogues de paquets depuis les data reports vanilla          |
| `fidorial-test-plugin`        | Plugin de référence qui exerce l'API (générateur custom, commandes, tests scénario)                    |
| `build-logic`                 | Conventions Gradle partagées (Spotless, toolchain)                                                     |

Un plugin ne devrait importer que `fidorial-api`. Avoir besoin de quelque chose venant de `fidorial-server`,
c'est un manque dans l'API — ouvre une issue.

## Développement

```bash
./gradlew :fidorial-server:run           # serveur de dev (déploie le plugin de test)
./gradlew :fidorial-server:testScenarios # tests scénario contre un vrai serveur
./gradlew spotlessApply                  # formatage (Palantir Java Format)
./gradlew spotlessCheck                  # ce que la CI vérifie
```

La CI compile sur JDK 25, vérifie le formatage, lance les tests et publie le fat jar : une prerelease à chaque
push sur `master`, une release stable sur les tags `v*`.

## Contribuer

Les contributions sont ouvertes à tous — code, tests, documentation ou idées. Les pull requests et issues sont
les bienvenues sur [GitHub](https://github.com/Euphillya/Fidorial), et les discussions se passent sur
[Discord](https://discord.gg/uUJQEB7XNN).

## Licence

MIT © 2026 Euphyllia Bierque, sauf `fidorial-server`, sous licence GPLv3 : voir [LICENCE.md](LICENCE.md).
