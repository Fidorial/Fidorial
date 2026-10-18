package fr.fidorial.entity;

import fr.fidorial.entity.player.Player;

import java.util.Collection;

public interface EntityManager {

    Collection<? extends Entity> entities();

    Collection<? extends Player> players();

    Collection<? extends Entity> entities(EntityPredicate predicate);
}
