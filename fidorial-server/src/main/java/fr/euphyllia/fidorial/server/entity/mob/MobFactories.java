package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.ambient.Bat;
import fr.euphyllia.fidorial.server.entity.mob.creature.Allay;
import fr.euphyllia.fidorial.server.entity.mob.creature.Armadillo;
import fr.euphyllia.fidorial.server.entity.mob.creature.Bee;
import fr.euphyllia.fidorial.server.entity.mob.creature.Camel;
import fr.euphyllia.fidorial.server.entity.mob.creature.Cat;
import fr.euphyllia.fidorial.server.entity.mob.creature.Chicken;
import fr.euphyllia.fidorial.server.entity.mob.creature.Cow;
import fr.euphyllia.fidorial.server.entity.mob.creature.Donkey;
import fr.euphyllia.fidorial.server.entity.mob.creature.Fox;
import fr.euphyllia.fidorial.server.entity.mob.creature.Frog;
import fr.euphyllia.fidorial.server.entity.mob.creature.Goat;
import fr.euphyllia.fidorial.server.entity.mob.creature.HappyGhast;
import fr.euphyllia.fidorial.server.entity.mob.creature.Horse;
import fr.euphyllia.fidorial.server.entity.mob.creature.Llama;
import fr.euphyllia.fidorial.server.entity.mob.creature.Mooshroom;
import fr.euphyllia.fidorial.server.entity.mob.creature.Mule;
import fr.euphyllia.fidorial.server.entity.mob.creature.Ocelot;
import fr.euphyllia.fidorial.server.entity.mob.creature.Panda;
import fr.euphyllia.fidorial.server.entity.mob.creature.Parrot;
import fr.euphyllia.fidorial.server.entity.mob.creature.Pig;
import fr.euphyllia.fidorial.server.entity.mob.creature.PolarBear;
import fr.euphyllia.fidorial.server.entity.mob.creature.Rabbit;
import fr.euphyllia.fidorial.server.entity.mob.creature.Sheep;
import fr.euphyllia.fidorial.server.entity.mob.creature.SkeletonHorse;
import fr.euphyllia.fidorial.server.entity.mob.creature.Sniffer;
import fr.euphyllia.fidorial.server.entity.mob.creature.Strider;
import fr.euphyllia.fidorial.server.entity.mob.creature.SulfurCube;
import fr.euphyllia.fidorial.server.entity.mob.creature.TraderLlama;
import fr.euphyllia.fidorial.server.entity.mob.creature.Turtle;
import fr.euphyllia.fidorial.server.entity.mob.creature.WanderingTrader;
import fr.euphyllia.fidorial.server.entity.mob.creature.Wolf;
import fr.euphyllia.fidorial.server.entity.mob.creature.ZombieHorse;
import fr.euphyllia.fidorial.server.entity.mob.misc.CopperGolem;
import fr.euphyllia.fidorial.server.entity.mob.misc.IronGolem;
import fr.euphyllia.fidorial.server.entity.mob.misc.SnowGolem;
import fr.euphyllia.fidorial.server.entity.mob.misc.Villager;
import fr.euphyllia.fidorial.server.entity.mob.monster.Blaze;
import fr.euphyllia.fidorial.server.entity.mob.monster.Bogged;
import fr.euphyllia.fidorial.server.entity.mob.monster.Breeze;
import fr.euphyllia.fidorial.server.entity.mob.monster.CamelHusk;
import fr.euphyllia.fidorial.server.entity.mob.monster.CaveSpider;
import fr.euphyllia.fidorial.server.entity.mob.monster.Creaking;
import fr.euphyllia.fidorial.server.entity.mob.monster.Creeper;
import fr.euphyllia.fidorial.server.entity.mob.monster.Drowned;
import fr.euphyllia.fidorial.server.entity.mob.monster.ElderGuardian;
import fr.euphyllia.fidorial.server.entity.mob.monster.EnderDragon;
import fr.euphyllia.fidorial.server.entity.mob.monster.Enderman;
import fr.euphyllia.fidorial.server.entity.mob.monster.Endermite;
import fr.euphyllia.fidorial.server.entity.mob.monster.Evoker;
import fr.euphyllia.fidorial.server.entity.mob.monster.Ghast;
import fr.euphyllia.fidorial.server.entity.mob.monster.Giant;
import fr.euphyllia.fidorial.server.entity.mob.monster.Guardian;
import fr.euphyllia.fidorial.server.entity.mob.monster.Hoglin;
import fr.euphyllia.fidorial.server.entity.mob.monster.Husk;
import fr.euphyllia.fidorial.server.entity.mob.monster.Illusioner;
import fr.euphyllia.fidorial.server.entity.mob.monster.MagmaCube;
import fr.euphyllia.fidorial.server.entity.mob.monster.Parched;
import fr.euphyllia.fidorial.server.entity.mob.monster.Phantom;
import fr.euphyllia.fidorial.server.entity.mob.monster.Piglin;
import fr.euphyllia.fidorial.server.entity.mob.monster.PiglinBrute;
import fr.euphyllia.fidorial.server.entity.mob.monster.Pillager;
import fr.euphyllia.fidorial.server.entity.mob.monster.Ravager;
import fr.euphyllia.fidorial.server.entity.mob.monster.Shulker;
import fr.euphyllia.fidorial.server.entity.mob.monster.Silverfish;
import fr.euphyllia.fidorial.server.entity.mob.monster.Skeleton;
import fr.euphyllia.fidorial.server.entity.mob.monster.Slime;
import fr.euphyllia.fidorial.server.entity.mob.monster.Spider;
import fr.euphyllia.fidorial.server.entity.mob.monster.Stray;
import fr.euphyllia.fidorial.server.entity.mob.monster.Vex;
import fr.euphyllia.fidorial.server.entity.mob.monster.Vindicator;
import fr.euphyllia.fidorial.server.entity.mob.monster.Warden;
import fr.euphyllia.fidorial.server.entity.mob.monster.Witch;
import fr.euphyllia.fidorial.server.entity.mob.monster.Wither;
import fr.euphyllia.fidorial.server.entity.mob.monster.WitherSkeleton;
import fr.euphyllia.fidorial.server.entity.mob.monster.Zoglin;
import fr.euphyllia.fidorial.server.entity.mob.monster.Zombie;
import fr.euphyllia.fidorial.server.entity.mob.monster.ZombieNautilus;
import fr.euphyllia.fidorial.server.entity.mob.monster.ZombieVillager;
import fr.euphyllia.fidorial.server.entity.mob.monster.ZombifiedPiglin;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.Axolotl;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.Cod;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.Dolphin;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.GlowSquid;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.Nautilus;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.Pufferfish;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.Salmon;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.Squid;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.Tadpole;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.TropicalFish;
import fr.fidorial.entity.EntityType;
import fr.fidorial.math.Location;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;

import java.util.Map;
import java.util.Set;

public final class MobFactories {

    private static final Map<Key, MobFactory> FACTORIES = Map.<Key, MobFactory>ofEntries(
            Map.entry(EntityTypes.ALLAY.key(), Allay::new),
            Map.entry(EntityTypes.ARMADILLO.key(), Armadillo::new),
            Map.entry(EntityTypes.AXOLOTL.key(), Axolotl::new),
            Map.entry(EntityTypes.BAT.key(), Bat::new),
            Map.entry(EntityTypes.BEE.key(), Bee::new),
            Map.entry(EntityTypes.BLAZE.key(), Blaze::new),
            Map.entry(EntityTypes.BOGGED.key(), Bogged::new),
            Map.entry(EntityTypes.BREEZE.key(), Breeze::new),
            Map.entry(EntityTypes.CAMEL.key(), Camel::new),
            Map.entry(EntityTypes.CAMEL_HUSK.key(), CamelHusk::new),
            Map.entry(EntityTypes.CAT.key(), Cat::new),
            Map.entry(EntityTypes.CAVE_SPIDER.key(), CaveSpider::new),
            Map.entry(EntityTypes.CHICKEN.key(), Chicken::new),
            Map.entry(EntityTypes.COD.key(), Cod::new),
            Map.entry(EntityTypes.COPPER_GOLEM.key(), CopperGolem::new),
            Map.entry(EntityTypes.COW.key(), Cow::new),
            Map.entry(EntityTypes.CREAKING.key(), Creaking::new),
            Map.entry(EntityTypes.CREEPER.key(), Creeper::new),
            Map.entry(EntityTypes.DOLPHIN.key(), Dolphin::new),
            Map.entry(EntityTypes.DONKEY.key(), Donkey::new),
            Map.entry(EntityTypes.DROWNED.key(), Drowned::new),
            Map.entry(EntityTypes.ELDER_GUARDIAN.key(), ElderGuardian::new),
            Map.entry(EntityTypes.ENDER_DRAGON.key(), EnderDragon::new),
            Map.entry(EntityTypes.ENDERMAN.key(), Enderman::new),
            Map.entry(EntityTypes.ENDERMITE.key(), Endermite::new),
            Map.entry(EntityTypes.EVOKER.key(), Evoker::new),
            Map.entry(EntityTypes.FOX.key(), Fox::new),
            Map.entry(EntityTypes.FROG.key(), Frog::new),
            Map.entry(EntityTypes.GHAST.key(), Ghast::new),
            Map.entry(EntityTypes.GIANT.key(), Giant::new),
            Map.entry(EntityTypes.GLOW_SQUID.key(), GlowSquid::new),
            Map.entry(EntityTypes.GOAT.key(), Goat::new),
            Map.entry(EntityTypes.GUARDIAN.key(), Guardian::new),
            Map.entry(EntityTypes.HAPPY_GHAST.key(), HappyGhast::new),
            Map.entry(EntityTypes.HOGLIN.key(), Hoglin::new),
            Map.entry(EntityTypes.HORSE.key(), Horse::new),
            Map.entry(EntityTypes.HUSK.key(), Husk::new),
            Map.entry(EntityTypes.ILLUSIONER.key(), Illusioner::new),
            Map.entry(EntityTypes.IRON_GOLEM.key(), IronGolem::new),
            Map.entry(EntityTypes.LLAMA.key(), Llama::new),
            Map.entry(EntityTypes.MAGMA_CUBE.key(), MagmaCube::new),
            Map.entry(EntityTypes.MOOSHROOM.key(), Mooshroom::new),
            Map.entry(EntityTypes.MULE.key(), Mule::new),
            Map.entry(EntityTypes.NAUTILUS.key(), Nautilus::new),
            Map.entry(EntityTypes.OCELOT.key(), Ocelot::new),
            Map.entry(EntityTypes.PANDA.key(), Panda::new),
            Map.entry(EntityTypes.PARCHED.key(), Parched::new),
            Map.entry(EntityTypes.PARROT.key(), Parrot::new),
            Map.entry(EntityTypes.PHANTOM.key(), Phantom::new),
            Map.entry(EntityTypes.PIG.key(), Pig::new),
            Map.entry(EntityTypes.PIGLIN.key(), Piglin::new),
            Map.entry(EntityTypes.PIGLIN_BRUTE.key(), PiglinBrute::new),
            Map.entry(EntityTypes.PILLAGER.key(), Pillager::new),
            Map.entry(EntityTypes.POLAR_BEAR.key(), PolarBear::new),
            Map.entry(EntityTypes.PUFFERFISH.key(), Pufferfish::new),
            Map.entry(EntityTypes.RABBIT.key(), Rabbit::new),
            Map.entry(EntityTypes.RAVAGER.key(), Ravager::new),
            Map.entry(EntityTypes.SALMON.key(), Salmon::new),
            Map.entry(EntityTypes.SHEEP.key(), Sheep::new),
            Map.entry(EntityTypes.SHULKER.key(), Shulker::new),
            Map.entry(EntityTypes.SILVERFISH.key(), Silverfish::new),
            Map.entry(EntityTypes.SKELETON.key(), Skeleton::new),
            Map.entry(EntityTypes.SKELETON_HORSE.key(), SkeletonHorse::new),
            Map.entry(EntityTypes.SLIME.key(), Slime::new),
            Map.entry(EntityTypes.SNIFFER.key(), Sniffer::new),
            Map.entry(EntityTypes.SNOW_GOLEM.key(), SnowGolem::new),
            Map.entry(EntityTypes.SPIDER.key(), Spider::new),
            Map.entry(EntityTypes.SQUID.key(), Squid::new),
            Map.entry(EntityTypes.STRAY.key(), Stray::new),
            Map.entry(EntityTypes.STRIDER.key(), Strider::new),
            Map.entry(EntityTypes.SULFUR_CUBE.key(), SulfurCube::new),
            Map.entry(EntityTypes.TADPOLE.key(), Tadpole::new),
            Map.entry(EntityTypes.TRADER_LLAMA.key(), TraderLlama::new),
            Map.entry(EntityTypes.TROPICAL_FISH.key(), TropicalFish::new),
            Map.entry(EntityTypes.TURTLE.key(), Turtle::new),
            Map.entry(EntityTypes.VEX.key(), Vex::new),
            Map.entry(EntityTypes.VILLAGER.key(), Villager::new),
            Map.entry(EntityTypes.VINDICATOR.key(), Vindicator::new),
            Map.entry(EntityTypes.WANDERING_TRADER.key(), WanderingTrader::new),
            Map.entry(EntityTypes.WARDEN.key(), Warden::new),
            Map.entry(EntityTypes.WITCH.key(), Witch::new),
            Map.entry(EntityTypes.WITHER.key(), Wither::new),
            Map.entry(EntityTypes.WITHER_SKELETON.key(), WitherSkeleton::new),
            Map.entry(EntityTypes.WOLF.key(), Wolf::new),
            Map.entry(EntityTypes.ZOGLIN.key(), Zoglin::new),
            Map.entry(EntityTypes.ZOMBIE.key(), Zombie::new),
            Map.entry(EntityTypes.ZOMBIE_HORSE.key(), ZombieHorse::new),
            Map.entry(EntityTypes.ZOMBIE_NAUTILUS.key(), ZombieNautilus::new),
            Map.entry(EntityTypes.ZOMBIE_VILLAGER.key(), ZombieVillager::new),
            Map.entry(EntityTypes.ZOMBIFIED_PIGLIN.key(), ZombifiedPiglin::new));

    private MobFactories() {
    }

    public static boolean isMob(final EntityType type) {
        final FidorialMobRegistry registry = registry();
        return registry.isMob(type.key());
    }

    public static boolean isBuiltIn(final Key key) {
        return FACTORIES.containsKey(key);
    }

    public static Set<Key> builtInKeys() {
        return FACTORIES.keySet();
    }

    public static Set<Key> keys() {
        final FidorialMobRegistry registry = registry();
        return registry.types();
    }


    public static AbstractMob create(final EntityType type, final int entityId, final Location location) {
        final FidorialMobRegistry registry = registry();

        AbstractMob mob = registry.createDefined(type, entityId, location);
        if (mob == null) {
            final MobFactory factory = FACTORIES.get(type.key());
            if (factory == null) {
                throw new IllegalArgumentException("No mob implemented for " + type.key());
            }
            mob = factory.create(entityId, world, location);
        }

        registry.applyBehaviours(mob);
        return mob;
    }

    private static FidorialMobRegistry registry() {
        return FidorialServer.getInstance().mobs();
    }

    @FunctionalInterface
    public interface MobFactory {
        AbstractMob create(int entityId, Location location);
    }
}
