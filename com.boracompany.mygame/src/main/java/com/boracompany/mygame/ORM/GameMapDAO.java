package com.boracompany.mygame.ORM;

import com.boracompany.mygame.Model.GameMap;
import com.boracompany.mygame.Model.Player;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import java.util.List;

public class GameMapDAO implements IGameMapDAO {

	private EntityManagerFactory entityManagerFactory;

	public GameMapDAO(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void save(GameMap gameMap) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();

		try {
			transaction.begin();
			entityManager.persist(gameMap);
			transaction.commit(); // Commit only if everything goes well
		} catch (PersistenceException e) {
			if (transaction.isActive()) {
				transaction.rollback(); // Rollback if an exception occurs before commit
			}
			throw new PersistenceException("Failed to save GameMap: " + e.getMessage(), e);
		} finally {
			entityManager.close(); // Ensure the EntityManager is closed
		}
	}

	@Override
	public GameMap findById(Long id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return entityManager.find(GameMap.class, id);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public List<GameMap> findAll() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return entityManager.createQuery("FROM GameMap", GameMap.class).getResultList();
		} finally {
			entityManager.close();
		}
	}

	@Override
	public void update(GameMap gameMap) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			entityManager.merge(gameMap);
			entityManager.getTransaction().commit();
		} catch (PersistenceException e) {
			entityManager.getTransaction().rollback();
			throw new RuntimeException("Failed to update GameMap: " + e.getMessage(), e);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public void delete(Long id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();

		try {
			transaction.begin();
			GameMap gameMap = entityManager.find(GameMap.class, id);

			if (gameMap != null) {
				entityManager.remove(gameMap);
				transaction.commit(); // Commit the transaction if everything is fine
			} else {
				throw new RuntimeException("GameMap with id " + id + " not found.");
			}
		} catch (RuntimeException e) {
			if (transaction.isActive()) {
				transaction.rollback(); // Rollback the transaction if an exception occurs
			}
			throw new RuntimeException("Failed to delete GameMap: " + e.getMessage(), e);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public List<Player> findPlayersByMapId(Long mapId) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return entityManager.createQuery("SELECT p FROM Player p WHERE p.map.id = :mapId", Player.class)
					.setParameter("mapId", mapId).getResultList();
		} finally {
			entityManager.close();
		}
	}

	@Override
	public void addPlayerToMap(Long mapId, Player player) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			GameMap gameMap = entityManager.find(GameMap.class, mapId);
			if (gameMap != null) {
				player.setMap(gameMap); // Set the player's map
				entityManager.persist(player); // Persist the player directly
				entityManager.getTransaction().commit();
			} else {
				entityManager.getTransaction().rollback();
				throw new RuntimeException("GameMap with id " + mapId + " not found.");
			}
		} catch (PersistenceException e) {
			entityManager.getTransaction().rollback();
			throw new RuntimeException("Failed to add player to GameMap: " + e.getMessage(), e);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public void removePlayerFromMap(Long mapId, Player player) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			GameMap gameMap = entityManager.find(GameMap.class, mapId);
			Player managedPlayer = entityManager.find(Player.class, player.getId());

			// Check for null entities before proceeding
			if (gameMap == null || managedPlayer == null || !gameMap.getPlayers().contains(managedPlayer)) {
				throw new RuntimeException("Expected GameMap not found or Player not in this GameMap.");
			}

			gameMap.getPlayers().remove(managedPlayer);
			managedPlayer.setMap(null); // Unset the player's map
			entityManager.merge(gameMap);
			entityManager.merge(managedPlayer);
			transaction.commit();
		} catch (RuntimeException e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e; // Re-throw the original exception
		} finally {
			entityManager.close();
		}
	}

}
