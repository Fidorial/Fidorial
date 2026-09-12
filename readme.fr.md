<div align="center">
  <h1>Fidorial</h1>

  <p><em>Un serveur Minecraft écrit de zéro en Java — aucun code Mojang, aucun fork.</em></p>

[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-blue.svg)](https://github.com/Euphillya/Fidorial)
[![Java](https://img.shields.io/badge/Java-25+-orange.svg)](https://www.oracle.com/java/)
[![Licence](https://img.shields.io/badge/license-MIT-blue)](LICENCE)
[![Serveurs](https://img.shields.io/endpoint?url=https%3A%2F%2Ffaststats.dev%2Fapi%2Fshields%2Ffidorial%3Fmetric%3Dservers&style=flat)](https://faststats.dev/project/fidorial)
[![Téléchargements](https://img.shields.io/endpoint?url=https%3A%2F%2Ffaststats.dev%2Fapi%2Fshields%2Ffidorial%3Fmetric%3Ddownloads&style=flat)](https://faststats.dev/project/fidorial)

[Documentation](https://fidorial.euphyllia.moe) • [Discord](https://discord.gg/uUJQEB7XNN) • [English](readme.md)

[![Serveurs & Joueurs](https://faststats.dev/embed/default:d01e30ea-8ddc-40f6-b773-24d369336950:servers-and-players.svg?w=960&h=340&theme=dark)](https://faststats.dev/project/fidorial/minecraft-plugin)

</div>

---

Fidorial repose sur des fondations régionalisées et multithread, pensées dès le départ pour celles et ceux qui
veulent modifier le jeu. Ni Forge, ni Fabric, ni Mixin : un plugin s'abonne à des **événements** et remplace des
**services**, ce qui lui permet d'échanger un sous-système entier sans patcher une ligne de code serveur.

> ⚠️ **Projet à ses débuts.** Le gameplay reste partiel et tout peut changer, y compris l'API de plugins.

## Fonctionnalités

**Protocole & réseau**

- Implémentation maison de Minecraft **26.2** (protocole **776**) : handshake, status, login, configuration, play
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

- Scheduler régionalisé inspiré de Folia : le monde est découpé en régions indépendantes de 32×32 chunks, chacune tickée
  à 20 TPS sur son propre thread
- Commandes Brigadier, en jeu et dans une console interactive avec complétion et coloration : `/gamemode` (`/gm`),
  `/weather` (`/w`), `/time`, `/summon`, `/tps`, `/op`, `/deop`, `/bossbar`, `/stop` (`/s`)
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

Au premier démarrage, le serveur écrit un `fidorial.properties` à côté du jar et écoute sur le port **25565**.

| Clé                                               | Défaut                   | Description                                                               |
|---------------------------------------------------|--------------------------|---------------------------------------------------------------------------|
| `port`                                            | `25565`                  | Port d'écoute                                                             |
| `online-mode`                                     | `true`                   | Authentification Mojang                                                   |
| `view-distance` / `send-distance`                 | `8` / `3`                | Distance annoncée au client, puis rayon réel de streaming (`send ≤ view`) |
| `world-path` / `plugins-path`                     | `world` / `plugins`      | Répertoires de données                                                    |
| `region-workers` / `chunk-workers` / `ai-workers` | selon le nombre de cœurs | Threads par sous-système                                                  |
| `default-game-mode`                               | `creative`               | Mode donné aux nouveaux joueurs                                           |
| `motd`                                            | —                        | Description de la liste des serveurs, au format MiniMessage               |
| `proxy-mode` / `velocity-secret`                  | `NONE`                   | Velocity modern forwarding                                                |

Tape `tps` dans la console pour vérifier la santé des régions.

## Écrire un plugin

L'API est publiée sur [repo.euphyllia.moe](https://repo.euphyllia.moe) et fournie par le serveur à l'exécution :

```kotlin
repositories {
    maven("https://repo.euphyllia.moe/repository/maven-public/")
}

dependencies {
    compileOnly("fr.fidorial:fidorial-api:0.1.0-SNAPSHOT")
}
```

Les plugins sont des modules JPMS. Déclare la dépendance dans `module-info.java` et décris le plugin dans un
`fidorial.json` à la racine du jar :

```java
module com.exemple.monplugin {
    requires fr.fidorial;
    requires net.kyori.adventure.api;
}
```

```json
{
  "id": "bedrockguard",
  "name": "Bedrock Guard",
  "version": "1.0.0",
  "main": "com.exemple.BedrockGuard",
  "authors": [
    "toi"
  ],
  "depends": []
}
```

Puis implémente `Plugin` :

```java
public final class BedrockGuard implements Plugin {

    private PluginContext ctx;

    @Override
    public void onLoad(PluginContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onEnable() {
        // Annuler l'événement empêche le bloc de changer du tout :
        // pas de paquet, pas d'écriture disque, pas de mise à jour des fluides.
        ctx.events().subscribe(BlockBreakEvent.class, EventPriority.HIGH, event -> {
            if (event.position().y() < 0) {
                event.setCancelled(true);
                event.player().sendMessage(Component.text("Tu ne peux pas casser ça."));
            }
        });
    }
}
```

Dépose le jar dans `plugins/` et démarre le serveur. Listeners et services sont retirés automatiquement quand un
plugin est désactivé, chaque plugin a son propre classloader, et les listeners tournent sur le thread de la
région propriétaire du bloc ou de l'entité — confie le travail long à `ctx.server().scheduler()`.

**Les plugins comme des mods.** Le serveur n'appelle jamais ses propres implémentations en direct, toujours via
`services.get(X.class)`, et les enregistre en priorité `LOWEST`. Enregistre la tienne plus haut et tous les
points d'appel la prennent à la place :

```java
private void setMyFluid() {
    ctx.services().register(FluidManager.class, new MyFluidPhysics(), this);
}
```

`WorldGenerator`, `FluidManager`, `WeatherManager`, `CombatEngine`, `BlockEditService`,
`PlayerInventoryStorage`, `PlayerDataStorage`, `PlayerEnderChestStorage`, `PermissionRegistry` et d'autres
fonctionnent ainsi — avec des types API purs, sans entrailles du serveur.

Guides complets sur **[fidorial.euphyllia.moe](https://fidorial.euphyllia.moe)**.

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

[MIT](LICENCE) © 2026 Euphyllia Bierque
